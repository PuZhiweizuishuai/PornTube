/**
 * HTTP 全局插件（兼容层 + 新能力）
 *
 * 底层实现已下沉到 @/api/index.js（Promise 化、超时、response.ok 校验、统一 401）。
 * 本插件只负责把它挂到 Vue 实例上，并且**保持旧调用签名完全可用**，
 * 这样全站 70+ 个 SFC 无需一次性改造即可享受修复：
 *
 *   this.httpGet(url, cb)          // 旧：回调
 *   await this.httpGet(url)        // 新：Promise（不传 cb 时返回 Promise）
 *   this.httpGet(url, cb, errCb)   // 旧：回调 + 可选失败回调（推荐，避免 loading 卡死）
 *   this.$api.get(url, params)     // 新：结构化 API
 *   this.$api.post(url, body)
 *   this.$api.upload(url, form)
 *
 * 迁移方式：新代码请一律使用 `this.$api.xxx` 或 `import http from '@/api'`，
 * 不要再写回调，也不要再直接裸调用 fetch()。
 */
import http, {
  ApiError,
  registerWebInfoProvider,
  setBaseURL,
  setUnauthorizedHandler,
} from '@/api/index'
import { useWebInfoStore } from '@/stores/webInfoStore'
import { useUserStore } from '@/stores/userStore'

/**
 * 把 Promise 适配为「可选回调」形式：
 * - 传了 cb → 走旧的回调语义，成功 cb(payload)，失败走 errCb（没有就 console.error）
 * - 没传 cb → 直接把 Promise 交给调用方（async/await / .then）
 *
 * 注意：即使走回调语义也返回 Promise，方便调用方随时升级。
 */
function adapt(promise, cb, errCb) {
  if (typeof cb !== 'function') return promise
  return promise.then((payload) => {
    cb(payload)
    return payload
  }).catch((error) => {
    if (typeof errCb === 'function') {
      errCb(error)
    } else if (error && !error.isUnauthorized) {
      // 未登录已由统一处理器负责跳转，这里不再刷日志
      console.error('[api] 请求失败:', error?.message || error, error)
    }
    throw error
  }).catch(() => undefined)
}

export default {
  install: (app) => {
    setBaseURL(app.config.globalProperties.SERVER_API_URL || '/api')
    registerWebInfoProvider(() => useWebInfoStore().webInfo)

    // 401：优先用 router 做 SPA 跳转（原实现直接用 window.location 会整页刷新）
    setUnauthorizedHandler(() => {
      const router = app.config.globalProperties.$router
      try {
        useUserStore().deleteUserData()
      } catch (e) {
        /* Pinia 尚未就绪 */
      }
      if (router) {
        if (router.currentRoute?.value?.path !== '/login') {
          router.push('/login')
        }
        return true
      }
      return false
    })

    // ---- 新版 API ----
    app.config.globalProperties.$api = http
    app.config.globalProperties.$ApiError = ApiError
    app.provide('api', http)

    // ---- 旧版兼容 API（回调风格）----
    app.config.globalProperties.httpGet = function (url, cb, errCb) {
      return adapt(http.get(url), cb, errCb)
    }

    app.config.globalProperties.httpPost = function (url, data, cb, errCb) {
      return adapt(http.post(url, data), cb, errCb)
    }

    app.config.globalProperties.uploadFiles = function (url, form, cb, errCb) {
      return adapt(http.upload(url, form), cb, errCb)
    }
  },
}
