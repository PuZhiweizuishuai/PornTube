/**
 * 统一的消息提示 composable
 *
 * 收敛各处重复实现的 showSuccessMessage / showErrorMessage / showMessage /
 * showNotification / snackbar 三件套（原本在 15+ 个组件里各写一遍，字段名还各不相同）。
 *
 * 用法（Options API）：
 *   setup() { return { ...useMessage() } }
 *   // 模板：<AppSnackbar :state="toast" />
 *   // 代码：this.showSuccessMessage('保存成功')
 *
 * 用法（Composition API）：
 *   const { toast, showErrorMessage } = useMessage()
 *
 * 兼容保留的旧名：this.showMessage(text, color) —— 与后台视图原来的调用一致，迁移零成本。
 */
import { reactive } from 'vue'

/** vuetify 语义色。旧代码里混用了 'info' 这类 type，这里做一次归一 */
const COLOR_ALIAS = {
  success: 'success',
  error: 'error',
  danger: 'error',
  warn: 'warning',
  warning: 'warning',
  info: 'info',
  default: 'primary',
}

function normalizeColor(color) {
  return COLOR_ALIAS[color] || color || 'primary'
}

export function useMessage(options = {}) {
  const { timeout = 3000, location = 'top' } = options

  /** 组件唯一需要的响应式状态，交给 <AppSnackbar :state="toast" /> 渲染即可 */
  const toast = reactive({
    show: false,
    text: '',
    color: 'primary',
    timeout,
    location,
  })

  /**
   * 通用提示
   * @param {string} text 提示内容
   * @param {string} [color='primary'] success / error / warning / info ...
   */
  function showMessage(text, color = 'primary') {
    toast.text = text
    toast.color = normalizeColor(color)
    toast.show = true
    return toast
  }

  /** showMessage 的别名，兼容后台视图中 showMessage(text, color) 的调用习惯 */
  function showNotification(message, type = 'info') {
    return showMessage(message, type)
  }

  function showSuccessMessage(message) {
    return showMessage(message, 'success')
  }

  function showErrorMessage(message) {
    return showMessage(message, 'error')
  }

  function showWarningMessage(message) {
    return showMessage(message, 'warning')
  }

  function hideMessage() {
    toast.show = false
  }

  /**
   * 把 Promise 风格的请求包装成「自动提示」：
   * 成功走 onSuccess，失败统一提示 error.message，并彻底解决「错误被吞导致 loading 卡死」
   * @param {Promise} promise
   * @param {(payload:any)=>void} onSuccess
   * @param {object} [opts] { errorText, finally }
   */
  async function withMessage(promise, onSuccess, opts = {}) {
    try {
      const payload = await promise
      if (typeof onSuccess === 'function') await onSuccess(payload)
      return payload
    } catch (error) {
      showErrorMessage(`${opts.errorText ? opts.errorText + '：' : ''}${error?.message || '操作失败'}`)
      return null
    } finally {
      if (typeof opts.finally === 'function') opts.finally()
    }
  }

  return {
    toast,
    showMessage,
    showNotification,
    showSuccessMessage,
    showErrorMessage,
    showWarningMessage,
    hideMessage,
    withMessage,
  }
}

export default useMessage
