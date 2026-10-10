/**
 * 评论列表 + 发表评论 的通用逻辑
 *
 * 背景：SecondComment.vue（二级评论弹窗，236 行）与 views/comment/VideoComment.vue
 * （一级评论，220 行）把「编辑器 + 排序 tab + 列表 + 空态 + 分页 + 提交 + 本地插入」
 * 整套逻辑几乎原样写了两遍。这里抽成 composable，两个视图只保留各自的排版差异。
 *
 * 用法（Options API）：
 *   setup() { return { comments: useCommentList({ articleId: 1, type: 1 }) } }
 *   模板：v-for="item in comments.items" / <Pagination v-model="comments.page" ... />
 */
import { computed, reactive, ref, unref, watch } from 'vue'
import http from '@/api/index'
import { useUserStore } from '@/stores/userStore'
import { dataErrorMessage } from '@/utils/string-utils'
import { extractPageResult } from './usePagination'

/**
 * @param {object} options
 * @param {number|Function} options.articleId 稿件 ID
 * @param {number} [options.type=1] 1 一级评论 2 二级评论（沿用后端约定）
 * @param {number|Function} [options.fatherId=0] 二级评论时的父评论 ID
 * @param {number} [options.sort=1] 排序：1 时间倒序 2 最多点赞 3 最多评论
 * @param {number} [options.size=10]
 * @param {boolean} [options.immediate=true] 是否在创建时立即加载第一页
 * @param {Function} [options.buildPayload] 自定义提交体（默认拼标准的 comment 结构）
 */
export function useCommentList(options = {}) {
  const {
    type = 1,
    sort: initialSort = 1,
    size: defaultSize = 10,
    immediate = true,
    buildPayload = null,
  } = options

  const userStore = useUserStore()

  const page = ref(1)
  const pageSize = ref(defaultSize)
  const sort = ref(initialSort)
  const total = ref(0)
  const totalPages = ref(1)
  const loading = ref(false)
  const submitting = ref(false)
  const items = ref([])
  /** 编辑器草稿内容 */
  const draft = ref('')
  /** 回复目标（二级评论回复某人时使用） */
  const replyTo = ref(null)

  const length = computed(() => Math.max(1, totalPages.value || 1))
  const isEmpty = computed(() => !loading.value && items.value.length === 0)
  const parentUserId = computed(() => (replyTo.value ? replyTo.value.userId : 0))
  const parentCommentId = computed(() => (replyTo.value ? replyTo.value.id : 0))

  function resolveArticleId(extra) {
    const raw = options.articleId
    const fromOptions = typeof raw === 'function' ? raw() : unref(raw)
    return fromOptions ?? extra ?? 0
  }

  function resolveFatherId() {
    const raw = options.fatherId
    return (typeof raw === 'function' ? raw() : unref(raw)) ?? 0
  }

  async function load(targetPage) {
    if (targetPage !== undefined && targetPage !== null) page.value = Number(targetPage) || 1
    loading.value = true
    try {
      const params = {
        article: resolveArticleId(),
        type,
        sort: sort.value,
        page: page.value,
        limit: pageSize.value,
      }
      const fatherId = resolveFatherId()
      if (fatherId) params.fatherId = fatherId

      const json = await http.get('/comment/list', params)
      const result = extractPageResult(json)
      items.value = result?.list || []
      if (result?.currPage) page.value = result.currPage
      totalPages.value = Math.max(1, result?.totalPage ?? 1)
      total.value = result?.totalCount ?? items.value.length
      return items.value
    } catch (e) {
      console.error('[useCommentList] 加载评论失败:', e?.message || e)
      items.value = []
      return []
    } finally {
      loading.value = false
    }
  }

  function handlePageChange(value) {
    return load(value)
  }

  function setSort(value) {
    sort.value = Number(value)
    return load(1)
  }

  /**
   * 发表评论
   * @param {string} [text] 不传则使用 draft
   * @returns {Promise<{ok:boolean, code:number, message:string, data:any}>}
   *   code: 200 成功 / 2001 内容为空 / 2002 审核中（后端返回 data.status === -1）/ 其它为业务失败
   */
  async function submit(text) {
    const content = text === undefined || text === null ? draft.value : text
    if (!content || content.trim() === '' || content === '\n') {
      return { ok: false, code: 2001, message: '评论内容不能为空！', data: null }
    }
    submitting.value = true
    try {
      const payload =
        typeof buildPayload === 'function'
          ? buildPayload(content)
          : {
              articleId: resolveArticleId(),
              comment: content,
              type,
              parentCommentId: parentCommentId.value,
              ...(parentUserId.value ? { parentUserId: parentUserId.value } : {}),
            }

      const json = await http.post('/comment/save', payload)
      if (json && json.status === 200) {
        const created = json.data || {}
        // 后端不返回用户信息，这里补上，让新评论能立刻渲染出头像与昵称
        if (userStore.userData) {
          created.avatarUrl = created.avatarUrl || userStore.userData.avatarUrl
          created.username = created.username || userStore.userData.username
          created.userId = created.userId || userStore.userData.id
        }
        items.value.unshift(created)
        total.value += 1
        draft.value = ''
        if (created.status === -1) {
          return {
            ok: true,
            code: 2002,
            message: '评论成功，待管理员审核通过后，其他观众即可看见你的评论！',
            data: created,
          }
        }
        return { ok: true, code: 200, message: '评论成功！', data: created }
      }
      if (json && json.status === 4002) {
        return { ok: false, code: 4002, message: dataErrorMessage(json.data), data: json.data }
      }
      return { ok: false, code: json?.status ?? -1, message: json?.message || '评论失败', data: json?.data }
    } catch (e) {
      return { ok: false, code: -1, message: e?.message || '评论失败，请稍后重试', data: null }
    } finally {
      submitting.value = false
    }
  }

  /** 设置回复目标并清空状态（页面通常还会滚动到编辑器） */
  function reply(comment) {
    replyTo.value = comment || null
    return replyTo.value
  }

  function clearReply() {
    replyTo.value = null
  }

  /** 一级 -> 二级的总数变化时同步（例如在评论区新增了一级评论） */
  function setTotal(value) {
    total.value = Number(value) || 0
  }

  // 首次渲染即加载第一页（替代原先各组件 created() 里的手动调用）
  // 用 watch + immediate 而非直接调用：稿件 id 可能是异步就绪的（路由参数/父组件数据），
  // 拿到有效 id 后才发请求，避免用 article=0 打一次无效请求。
  if (immediate) {
    watch(
      () => resolveArticleId(),
      (articleId) => {
        if (articleId) load(1)
      },
      { immediate: true }
    )
  }

  // 用 reactive 包装后，模板里可以直接 comments.items / v-model="comments.page"，无需 .value
  return reactive({
    page,
    pageSize,
    sort,
    total,
    totalPages,
    length,
    loading,
    submitting,
    items,
    draft,
    replyTo,
    isEmpty,
    load,
    reload: () => load(1),
    refresh: () => load(page.value),
    handlePageChange,
    setSort,
    submit,
    reply,
    clearReply,
    setTotal,
  })
}

export default useCommentList
