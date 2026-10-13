/**
 * 点赞 / 点踩 复用逻辑
 *
 * 背景：CommentCard.vue 与 SecondCard.vue 各自实现了一遍 like()/checkLike()/dislike()
 * （返回体判断、计数自增自减、未登录提示、snackbar 三件套全部重复），且 ~85% 模板雷同。
 * 这里抽成 composable + BaseCommentCard 组件统一承载。
 *
 * 用法：
 *   const like = useLike(() => props.comment.id, { type: 1, count: () => props.comment.likeCount,
 *                                                   liked: () => props.comment.liked })
 *   like.liked        // ref<boolean>
 *   like.count        // ref<number>
 *   await like.toggle()  // -> { ok, needLogin, liked, message }
 */
import { computed, ref, watch } from 'vue'
import http from '@/api/index'
import { useUserStore } from '@/stores/userStore'

/**
 * @param {number|Function|import('vue').Ref} target 目标对象 id，支持 ref / getter / 常量
 * @param {object} [options]
 * @param {number} [options.type=1] 业务类型：1 评论  2 稿件 ...（沿用后端约定）
 * @param {number|Function} [options.count] 初始点赞数
 * @param {boolean|Function} [options.liked] 列表接口一并返回的点赞状态；有效时不再单独发状态查询请求
 * @param {boolean} [options.auto=true] 目标变化时自动同步点赞状态
 * @param {string} [options.action='点赞'] 文案主体，用于生成 '点赞成功'/'取消点赞成功'
 */
export function useLike(target, options = {}) {
  const { type = 1, auto = true, action = '点赞' } = options

  // Pinia 必须在组件上下文中才激活，这里延迟取用，避免在 setup 之外被误调用而直接崩溃
  let userStore = null
  function getUserStore() {
    if (!userStore) userStore = useUserStore()
    return userStore
  }

  const liked = ref(false)
  const likeCount = ref(resolve(options.count))
  const loading = ref(false)

  function resolve(value) {
    return typeof value === 'function' ? value() : (value ?? 0)
  }

  function resolveTargetId() {
    if (typeof target === 'function') return target()
    if (target && typeof target === 'object' && 'value' in target) return target.value
    return target
  }

  /** 列表接口一并返回的点赞状态，null / undefined 表示服务端没给，需要回退单条查询 */
  function serverLiked() {
    return typeof options.liked === 'function' ? options.liked() : options.liked
  }

  function syncLiked() {
    const fromServer = serverLiked()
    if (fromServer === null || fromServer === undefined) {
      liked.value = false
      check()
      return
    }
    liked.value = !!fromServer
  }

  const isLogin = computed(() => {
    try {
      return !!getUserStore().userData
    } catch (e) {
      return false
    }
  })

  /** 查询当前用户的点赞状态（未登录直接置 false，不发请求） */
  async function check() {
    const id = resolveTargetId()
    if (!isLogin.value || id === null || id === undefined) {
      liked.value = false
      return false
    }
    try {
      const json = await http.get('/like/status', { likeObjId: id, type })
      liked.value = !!(json && json.data)
      return liked.value
    } catch (e) {
      // 状态查询失败不该影响展示，静默降级为未点赞
      console.error('[useLike] 查询点赞状态失败:', e?.message || e)
      return false
    }
  }

  /**
   * 切换点赞
   * @returns {Promise<{ok:boolean, needLogin:boolean, liked:boolean, count:number, message:string}>}
   */
  async function toggle() {
    const id = resolveTargetId()
    if (!isLogin.value) {
      return { ok: false, needLogin: true, liked: false, count: likeCount.value, message: `请先登录后再${action}` }
    }
    loading.value = true
    try {
      const json = await http.post(`/like/toggle?likeObjId=${id}&type=${type}`, {})
      const data = json && json.data
      if (!data || !data.like) {
        return {
          ok: false,
          needLogin: false,
          liked: liked.value,
          count: likeCount.value,
          message: data?.info || '操作失败',
        }
      }
      liked.value = !liked.value
      likeCount.value = Math.max(0, likeCount.value + (liked.value ? 1 : -1))
      return {
        ok: true,
        needLogin: false,
        liked: liked.value,
        count: likeCount.value,
        message: liked.value ? `${action}成功` : `取消${action}成功`,
      }
    } catch (e) {
      return {
        ok: false,
        needLogin: false,
        liked: liked.value,
        count: likeCount.value,
        message: e?.message || '操作失败，请稍后重试',
      }
    } finally {
      loading.value = false
    }
  }

  /** 点赞 -> 必然为已赞（互斥场景下用），返回值含义同 toggle */
  async function dislike() {
    const id = resolveTargetId()
    if (!isLogin.value) {
      return { ok: false, needLogin: true, liked: false, message: '请先登录后再操作' }
    }
    loading.value = true
    try {
      const json = await http.post(`/dislike/toggle?dislikeObjId=${id}&type=${type}`, {})
      const data = json && json.data
      if (!data || !data.dislike) {
        return { ok: false, needLogin: false, message: data?.info || '操作失败' }
      }
      // 点踩成功时若原本已点赞，需同步回退点赞数（原 CommentCard 实现漏了这一步）
      if (liked.value) {
        liked.value = false
        likeCount.value = Math.max(0, likeCount.value - 1)
      }
      return { ok: true, needLogin: false, message: data.info || '操作成功' }
    } catch (e) {
      return { ok: false, needLogin: false, message: e?.message || '操作失败' }
    } finally {
      loading.value = false
    }
  }

  /** 由外部同步计数（例如列表刷新后） */
  function syncCount(count) {
    likeCount.value = Number(count) || 0
  }

  if (auto) {
    // 服务端随列表给出点赞状态时直接采用，评论量再大也不会产生逐条的状态请求
    watch([resolveTargetId, serverLiked], syncLiked, { immediate: true })
  }

  return { liked, likeCount, count: likeCount, loading, isLogin, check, toggle, dislike, syncCount }
}

export default useLike
