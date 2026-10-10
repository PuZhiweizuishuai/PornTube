/**
 * 后台列表页通用数据拉取 composable
 *
 * 背景：CommentControlView / DanmukuControlView / FileTableView / AdvertisementView /
 * OpinionView / ArticleListView / UserList 七个视图各自实现了一遍：
 *   loading + page + pageSize + totalPages + 拼 url + httpGet 回调 + this.loading=false
 * 这里把它们收敛成一个：给定接口地址与查询参数，自动管理分页、加载态、错误态。
 *
 * 用法：
 *   const table = useTable({ url: '/admin/comment/list', params: () => ({ status: tab.value }) })
 *   模板里的 v-pagination 换成 <Pagination v-model="table.page.value" ... /> 或直接用 table.handlePageChange
 *
 * 兼容 Options API：在 setup() 里 return { table }，模板即用 table.items / table.totalPages。
 */
import { computed, isRef, reactive, ref, toRaw, unref } from 'vue'
import http from '@/api/index'
import { extractPageResult } from './usePagination'

/**
 * @param {object} options
 * @param {string} options.url 列表接口（不含 baseURL）
 * @param {object|Function} [options.params] 额外筛选参数，支持 reactive / ref / getter
 * @param {number} [options.size=20]
 * @param {boolean} [options.immediate=true]
 * @param {string} [options.root='data'] 分页数据所在的信封层级
 * @param {Function} [options.transform] 对 list 做二次处理
 * @param {Function} [options.onError] 错误回调，默认 console.error
 */
export function useTable(options = {}) {
  const {
    url,
    params = null,
    size: defaultSize = 20,
    immediate = true,
    root = 'data',
    transform = null,
    onError = null,
  } = options

  const page = ref(1)
  const pageSize = ref(defaultSize)
  const items = ref([])
  const totalCount = ref(0)
  const totalPages = ref(1)
  const loading = ref(false)
  const error = ref(null)
  const keyword = ref('')

  const length = computed(() => Math.max(1, totalPages.value || 1))
  const isEmpty = computed(() => !loading.value && items.value.length === 0)

  /** 解析出最终的查询对象 */
  function resolveParams() {
    let extra = {}
    if (typeof params === 'function') extra = params() || {}
    else if (params) extra = unref(params) || {}
    // 忽略空串/null，避免拼出 &status=
    const query = {}
    Object.entries(toRaw(extra)).forEach(([key, value]) => {
      if (value === null || value === undefined || value === '' || Number.isNaN(value)) return
      query[key] = value
    })
    if (keyword.value) query.keyword = keyword.value
    return query
  }

  async function load(targetPage) {
    if (targetPage !== undefined && targetPage !== null) page.value = Number(targetPage) || 1
    loading.value = true
    error.value = null
    try {
      const payload = await http.get(url, { ...resolveParams(), page: page.value, limit: pageSize.value })
      const result = extractPageResult(payload, { root })
      items.value = transform && result?.list ? transform(result.list) : result?.list || []
      if (result?.currPage) page.value = result.currPage
      totalPages.value = Math.max(1, result?.totalPage ?? 1)
      totalCount.value = result?.totalCount ?? items.value.length
      return items.value
    } catch (e) {
      error.value = e
      if (typeof onError === 'function') onError(e)
      else console.error('[useTable] 加载列表失败:', e?.message || e)
      items.value = []
      return []
    } finally {
      loading.value = false
    }
  }

  /** v-pagination 的 @update:model-value 绑定它（新值会先写回 v-model，这里只需重拉） */
  function handlePageChange() {
    return load(page.value)
  }

  /** 筛选条件变化：回到第一页 */
  function reload() {
    return load(1)
  }

  /** 当前页刷新 */
  function refresh() {
    return load(page.value)
  }

  /**
   * 行操作（删除/恢复/审核等）后的统一处理：成功后刷新当前列表
   * @param {Promise} promise
   * @param {object} [opts] { resetPage }
   */
  async function submit(promise, opts = {}) {
    try {
      const payload = await promise
      await load(opts.resetPage ? 1 : page.value)
      return payload
    } catch (e) {
      error.value = e
      throw e
    }
  }

  const state = reactive({
    page,
    pageSize,
    items,
    totalCount,
    totalPages,
    length,
    loading,
    error,
    isEmpty,
    keyword,
    load,
    reload,
    refresh,
    submit,
    handlePageChange,
  })

  if (immediate) {
    Promise.resolve().then(() => load(1))
  }

  return state
}

/** 供模板里判断某个值是否为 ref（一般不用导出，保留以防扩展） */
export { isRef }

export default useTable
