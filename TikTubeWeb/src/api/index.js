/**
 * 统一的 HTTP 请求层（Promise 化）
 *
 * 设计目标（对应「下一步开发文档」第三节「前端可抽取的重复」第 1 条）：
 *  1. Promise 化：所有请求返回 Promise，支持 async/await 与链式调用
 *  2. 检查 response.ok：非 2xx 一律抛出 ApiError（携带状态码与响应体）
 *  3. 错误不再被吞：网络错误 / 超时 / HTTP 错误都会 reject，调用方必须处理
 *  4. 统一 401：HTTP 401 或业务体 status===0 且 message==='登录过期或未登录'
 *     （后端 LoginInterceptor 目前返回 401 + 该 JSON）→ 清本地用户态并跳转登录页
 *     —— 不再依赖「中文文案完全相等」来判定（F6），文案只是兜底之一
 *  5. 超时：默认 15s，AbortController 中断，超时抛 ApiError(TimeoutError)
 *  6. 统一请求头 / credentials / baseURL，消除各处裸 fetch
 *
 * 兼容层：见 @/utils/fetch.js，仍以 `this.httpGet(url, cb)` 回调形式暴露，
 * 旧代码无需改动即可享受上述修复。
 */

/**
 * 会话失效的兜底特征文案。
 * 主判据是 HTTP 401（LoginInterceptor 已返回 401），这里只保留后端明确返回的那一句，
 * 避免把业务上的「请先登录」误判成会话失效从而误跳登录页。
 */
const UNAUTHORIZED_MESSAGE = '登录过期或未登录'

/** 默认请求超时时间（毫秒） */
const DEFAULT_TIMEOUT = 15000

let baseURL = '/api'

/** 全局错误钩子（可选，由 UI 层注册用于统一 toast） */
let globalErrorHandler = null

/** 鉴权失效处理器（由路由层注册，默认清 store + 跳 /login） */
let unauthorizedHandler = null

/** 上一次触发跳转的时间，避免并发请求同时 401 造成重复跳转 */
let lastUnauthorizedAt = 0

export class ApiError extends Error {
  /**
   * @param {string} message 可读的错误描述
   * @param {object} options
   * @param {'http'|'network'|'timeout'|'parse'|'abort'|'unauthorized'} [options.type]
   * @param {number} [options.status] HTTP 状态码
   * @param {*} [options.payload] 已解析的响应体（若有）
   * @param {Error} [options.cause]
   */
  constructor(message, options = {}) {
    super(message, options.cause ? { cause: options.cause } : undefined)
    this.name = 'ApiError'
    this.type = options.type || 'http'
    this.status = options.status ?? null
    this.payload = options.payload ?? null
    /** HTTP 状态码可用性判断 */
    this.isHttpError = options.type === 'http'
    this.isNetworkError = options.type === 'network'
    this.isTimeout = options.type === 'timeout'
    this.isUnauthorized = options.type === 'unauthorized'
  }
}

/** 设置统一前缀（默认 /api，与 main.js 中的 SERVER_API_URL 保持一致） */
export function setBaseURL(url) {
  baseURL = url
}

export function getBaseURL() {
  return baseURL
}

/** 注册全局错误钩子：签名 (error: ApiError, ctx: {url, method}) => void */
export function setGlobalErrorHandler(handler) {
  globalErrorHandler = handler
}

/** 注册鉴权失效处理器：签名 (info: {status, message, url}) => void，返回 true 表示已自行处理 */
export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

/** 清掉本地用户态的默认实现（延迟 import 避免循环依赖） */
function defaultUnauthorizedHandler() {
  // 800ms 内的重复 401 只处理一次，避免并发请求重复跳转
  const now = Date.now()
  if (now - lastUnauthorizedAt < 800) return true
  lastUnauthorizedAt = now

  import('@/stores/userStore')
    .then(({ useUserStore }) => {
      try {
        useUserStore().deleteUserData()
      } catch (e) {
        /* Pinia 尚未就绪时忽略 */
      }
    })
    .catch(() => {})

  if (typeof window !== 'undefined') {
    const target = '/login'
    const current = window.location.hash
      ? window.location.hash.replace(/^#/, '')
      : window.location.pathname
    if (current !== target) {
      window.location.href = target
    }
  }
  return true
}

/** webInfo store 取值器（由 @/utils/fetch.js 插件安装时注入，避免模块顶层强依赖 Pinia） */
let webInfoProvider = null

/** 默认请求头。FormData 场景会在 request() 中删掉 Content-Type */
function buildDefaultHeaders() {
  return { 'Content-Type': 'application/json; charset=UTF-8', ...appNameHeader() }
}

/** 运行期动态取 App-Name（站点名），Pinia 未就绪时降级为空 */
function appNameHeader() {
  try {
    const webInfo = webInfoProvider && webInfoProvider()
    if (webInfo && webInfo.name) return { 'App-Name': webInfo.name }
  } catch (e) {
    /* Pinia 尚未激活时忽略 */
  }
  return {}
}

/**
 * 注入 webInfo 取值器：@param {() => object} provider 形如 () => useWebInfoStore().webInfo
 * 之所以用注入而不是直接 import，是因为本模块可能被 Pinia 上下文之外的代码引入。
 */
export function registerWebInfoProvider(provider) {
  webInfoProvider = provider
}

function isUnauthorizedPayload(payload) {
  return !!payload && typeof payload === 'object' && payload.status === 0 && payload.message === UNAUTHORIZED_MESSAGE
}

async function parseBody(response) {
  const text = await response.text()
  if (!text) return null
  try {
    return JSON.parse(text)
  } catch (e) {
    return null
  }
}

/**
 * 核心请求方法
 *
 * @param {string} url 以 / 开头的接口路径
 * @param {object} options
 * @param {string} [options.method='GET']
 * @param {object|FormData|string} [options.body]
 * @param {object} [options.params] query 参数对象，自动拼到 url 上
 * @param {object} [options.headers] 额外请求头
 * @param {number} [options.timeout=15000]
 * @param {boolean} [options.withCredentials=true]
 * @param {boolean} [options.raw=false] true 时返回 Response 而不解析 JSON（如下载场景）
 * @param {AbortSignal} [options.signal] 外部中断信号
 * @param {boolean} [options.skipUnauthorized=false] 为 true 时不走全局未登录处理（调用方自行兜底）
 * @returns {Promise<*>} 解析后的 JSON（后端业务响应统一为 200 + 信封，业务码由调用方判断）
 * @throws {ApiError}
 */
export async function request(url, options = {}) {
  const {
    method = 'GET',
    params,
    headers = {},
    timeout = DEFAULT_TIMEOUT,
    withCredentials = true,
    raw = false,
    signal: externalSignal,
    skipUnauthorized = false,
  } = options
  const { body } = options

  const controller = new AbortController()
  let timedOut = false
  const timer = setTimeout(() => {
    timedOut = true
    controller.abort(new DOMException('timeout', 'TimeoutError'))
  }, timeout)
  const onExternalAbort = () => controller.abort()
  if (externalSignal) {
    if (externalSignal.aborted) controller.abort()
    else externalSignal.addEventListener('abort', onExternalAbort)
  }

  const fullUrl = buildUrl(url, params)

  let isFormData = false
  if (typeof FormData !== 'undefined' && body instanceof FormData) {
    isFormData = true
  }

  let payloadBody
  if (body !== undefined && body !== null) {
    if (isFormData || typeof body === 'string' || body instanceof Blob || body instanceof ArrayBuffer) {
      payloadBody = body
    } else {
      payloadBody = JSON.stringify(body)
    }
  }

  const finalHeaders = { ...buildDefaultHeaders(), ...headers }
  // FormData 交给浏览器自动生成 multipart boundary
  if (isFormData) delete finalHeaders['Content-Type']

  const ctx = { url: fullUrl, method }
  try {
    const response = await fetch(fullUrl, {
      method,
      headers: finalHeaders,
      credentials: withCredentials ? 'include' : 'same-origin',
      body: payloadBody,
      signal: controller.signal,
    })

    if (raw) return response

    const payload = await parseBody(response)

    // 统一 401 / 会话失效处理
    if (response.status === 401 || (!skipUnauthorized && isUnauthorizedPayload(payload))) {
      const error = new ApiError(payload?.message || '登录过期或未登录', {
        type: 'unauthorized',
        status: response.status,
        payload,
      })
      const handledByExternal = unauthorizedHandler
        ? unauthorizedHandler({ status: response.status, message: payload?.message, url: fullUrl })
        : false
      if (!handledByExternal) defaultUnauthorizedHandler()
      throw error
    }

    // 检查 HTTP 状态码：非 2xx 抛出（原始实现直接 response.json()，错误被静默吞掉）
    if (!response.ok) {
      throw new ApiError(`请求失败：HTTP ${response.status}`, {
        type: 'http',
        status: response.status,
        payload,
      })
    }

    if (payload === null) {
      throw new ApiError('响应不是合法的 JSON', { type: 'parse', status: response.status })
    }
    return payload
  } catch (error) {
    if (error instanceof ApiError) {
      notifyGlobalError(error, ctx)
      throw error
    }
    let apiError
    if (timedOut) {
      apiError = new ApiError(`请求超时（${timeout}ms）`, { type: 'timeout', cause: error })
    } else if (error?.name === 'AbortError' || controller.signal.aborted) {
      apiError = new ApiError('请求已取消', { type: 'abort', cause: error })
    } else {
      apiError = new ApiError(error?.message || '网络异常，请稍后重试', {
        type: 'network',
        cause: error,
      })
    }
    notifyGlobalError(apiError, ctx)
    throw apiError
  } finally {
    clearTimeout(timer)
    if (externalSignal) externalSignal.removeEventListener('abort', onExternalAbort)
  }
}

function notifyGlobalError(error, ctx) {
  if (!globalErrorHandler) return
  try {
    globalErrorHandler(error, ctx)
  } catch (e) {
    /* 钩子自身异常不应影响主流程 */
  }
}

function buildUrl(url, params) {
  let full = url.startsWith('http') ? url : `${baseURL}${url.startsWith('/') ? url : `/${url}`}`
  if (!params) return full
  const search = new URLSearchParams()
  Object.keys(params).forEach((key) => {
    const value = params[key]
    if (value === undefined || value === null || value === '') return
    search.append(key, value)
  })
  const query = search.toString()
  if (query) full += (full.includes('?') ? '&' : '?') + query
  return full
}

const http = {
  request,
  get(url, params, options = {}) {
    return request(url, { ...options, method: 'GET', params })
  },
  post(url, body, options = {}) {
    return request(url, { ...options, method: 'POST', body })
  },
  put(url, body, options = {}) {
    return request(url, { ...options, method: 'PUT', body })
  },
  delete(url, body, options = {}) {
    return request(url, { ...options, method: 'DELETE', body })
  },
  /** 表单上传（FormData） */
  upload(url, form, options = {}) {
    return request(url, { ...options, method: 'POST', body: form })
  },
}

export default http
