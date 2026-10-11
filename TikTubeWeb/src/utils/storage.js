/**
 * 本地持久化统一入口
 *
 * 背景：之前 router / store / 组件各自裸调 localStorage，同一份数据出现多个数据源，
 * 且 JSON.parse 没有兜底（脏数据会直接抛异常打断路由守卫）。
 *
 * 约定：**localStorage 只是 Pinia 的持久化镜像，任何业务代码都不再直接读写它**。
 * 读取统一走 readJSON / readString，写入统一走 writeJSON / writeString。
 */

/** 全站使用的 storage key，集中登记避免各处硬编码字符串 */
export const STORAGE_KEYS = {
  USER: 'user',
  WEB_INFO: 'webInfo',
  THEME_MODE: 'themeMode',
}

function getStore() {
  try {
    if (typeof window === 'undefined' || !window.localStorage) return null
    // 隐私模式下访问 localStorage 会抛异常，这里做一次探测
    window.localStorage.getItem('__probe__')
    return window.localStorage
  } catch {
    return null
  }
}

export function readString(key, fallback = null) {
  const store = getStore()
  if (!store) return fallback
  try {
    const value = store.getItem(key)
    return value === null || value === undefined ? fallback : value
  } catch {
    return fallback
  }
}

export function writeString(key, value) {
  const store = getStore()
  if (!store) return
  try {
    if (value === null || value === undefined) store.removeItem(key)
    else store.setItem(key, String(value))
  } catch {
    /* 配额溢出 / 隐私模式：静默失败，不阻塞业务 */
  }
}

/**
 * 读取并反序列化 JSON。
 * 关键点：历史上 localStorage 里可能残留字符串 'undefined' / '' / 非对象，
 * 这里全部识别为「无数据」并返回 fallback，避免上层再写一堆防御代码。
 */
export function readJSON(key, fallback = null) {
  const raw = readString(key, null)
  if (!raw || raw === 'undefined' || raw === 'null') return fallback
  try {
    const parsed = JSON.parse(raw)
    return parsed === null || parsed === undefined ? fallback : parsed
  } catch {
    // 脏数据直接清理，防止下次启动继续踩坑
    removeKey(key)
    return fallback
  }
}

export function writeJSON(key, value) {
  const store = getStore()
  if (!store) return
  try {
    if (value === null || value === undefined) store.removeItem(key)
    else store.setItem(key, JSON.stringify(value))
  } catch {
    /* 忽略写入失败 */
  }
}

export function removeKey(key) {
  const store = getStore()
  if (!store) return
  try {
    store.removeItem(key)
  } catch {
    /* ignore */
  }
}
