/**
 * 分页 composable
 *
 * 收敛全站 23 处各具特色的「page / size / length / totalPage + pageChange」重复实现，
 * 统一出一套字段与行为：
 *   const { page, pageSize, total, totalPages, loading, items, load, refresh, reset, next, prev, applyResult } = usePagination(fetchFn)
 *
 * 约定（同时兼容本项目后端返回）：
 *   - fetchFn({page,size}) 可以是 async 函数返回已解析的响应体，
 *     也可以是普通的 this.httpGet(...) 式回调（内部会自动转成 Promise）
 *   - 结果可以是 { data: { list, currPage, totalPage, totalCount } }
 *     或直接是 { list, totalPage, totalCount, currPage }
 *     也可以是数组
 *
 * 取名统一为 items / totalPages / total，避免原代码里 length、totalPage、totalPages、
 * pageCount 混用造成的心智负担。
 */
import { computed, ref, unref } from 'vue'
import http from '@/api/index'

/** 从后端信封里提取分页信息 */
export function extractPageResult(payload, { root = 'data' } = {}) {
  if (!payload) return null
  const source = root && payload[root] !== undefined ? payload[root] : payload
  if (Array.isArray(source)) {
    return { list: source, currPage: null, totalPage: null, totalCount: source.length }
  }
  if (!source || typeof source !== 'object') return null
  return {
    list: source.list || source.records || [],
    currPage: source.currPage ?? source.page ?? null,
    totalPage: source.totalPage ?? source.totalPages ?? null,
    totalCount: source.totalCount ?? source.total ?? null,
  }
}

/**
 * @param {Function} fetchFn 形如 ({page,size}) => payload | Promise<payload>，或直接是 url 字符串
 * @param {object} [options]
 * @param {number} [options.size=10]
 * @param {boolean} [options.immediate=true] 创建时是否立即加载
 * @param {object} [options.params] 附加请求参数（会自动传给 http.get）
 * @param {number} [options.minTotalPages=1] totalPages 的下限
 */
export function usePagination(fetchFn, options = {}) {
  const {
    size: defaultSize = 10,
    immediate = true,
    params = null,
    root = 'data',
    minTotalPages = 1,
    onChange = null,
  } = options

  const page = ref(1)
  const pageSize = ref(defaultSize)
  const total = ref(0)
  const totalPages = ref(minTotalPages)
  const loading = ref(false)
  const items = ref([])
  /** 最近一次请求的错误，方便调用方展示 */
  const error = ref(null)

  const hasPrev = computed(() => page.value > 1)
  const hasNext = computed(() => page.value < totalPages.value)
  const isEmpty = computed(() => !loading.value && items.value.length === 0)
  /** 页码列表项，配合 <Pagination> 使用 */
  const length = computed(() => Math.max(minTotalPages, totalPages.value || minTotalPages))

  async function load(targetPage = page.value) {
    if (targetPage !== undefined && targetPage !== null) page.value = Number(targetPage) || 1
    loading.value = true
    error.value = null
    try {
      const requestParams = { page: page.value, size: pageSize.value }
      let payload
      if (typeof fetchFn === 'string') {
        payload = await http.get(fetchFn, { ...requestParams, ...(params ? unref(params) : {}) })
      } else {
        payload = await fetchFn(requestParams)
      }
      applyResult(payload)
      if (typeof onChange === 'function') onChange({ page: page.value, items: items.value })
      return items.value
    } catch (e) {
      error.value = e
      // 错误向上抛不符合兼容层语义（旧代码不 catch），这里统一记录后返回空列表
      console.error('[usePagination] 加载失败:', e?.message || e)
      return []
    } finally {
      loading.value = false
    }
  }

  /** 把后端信封套用到分页状态上 */
  function applyResult(payload) {
    const result = extractPageResult(payload, { root })
    if (!result) {
      items.value = []
      return false
    }
    items.value = result.list || []
    if (result.currPage) page.value = result.currPage
    if (result.totalPage !== null && result.totalPage !== undefined) {
      totalPages.value = Math.max(minTotalPages, result.totalPage)
    }
    if (result.totalCount !== null && result.totalCount !== undefined) {
      total.value = result.totalCount
    }
    return true
  }

  /** v-pagination 的 @update:model-value 直接绑它 */
  function handleChange(value) {
    return load(value)
  }

  /** 保持当前页重新拉取 */
  function refresh() {
    return load(page.value)
  }

  /** 回到第一页重新拉取（筛选条件变化时使用） */
  function reset() {
    return load(1)
  }

  function next() {
    if (!hasNext.value) return Promise.resolve(items.value)
    return load(page.value + 1)
  }

  function prev() {
    if (!hasPrev.value) return Promise.resolve(items.value)
    return load(page.value - 1)
  }

  function setPageSize(size) {
    pageSize.value = size
    return load(1)
  }

  /** 本地追加一条（发布评论/弹幕后免刷新） */
  function unshift(record) {
    if (record) items.value.unshift(record)
    return items.value
  }

  if (immediate) {
    // 与组件 created 生命周期等价：微任务中执行，组件实例已就绪
    Promise.resolve().then(() => load(1))
  }

  return {
    page,
    pageSize,
    total,
    totalPages,
    length,
    loading,
    items,
    error,
    hasPrev,
    hasNext,
    isEmpty,
    load,
    refresh,
    reset,
    next,
    prev,
    handleChange,
    setPageSize,
    applyResult,
    unshift,
  }
}

export default usePagination
