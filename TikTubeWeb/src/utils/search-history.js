/**
 * 搜索历史：浏览器 localStorage 持久化，最新搜索在前，最多保留 10 条
 */
import { readJSON, writeJSON, removeKey, STORAGE_KEYS } from './storage'

const MAX_HISTORY = 10

export function loadSearchHistory() {
  const list = readJSON(STORAGE_KEYS.SEARCH_HISTORY, [])
  return Array.isArray(list) ? list.filter((item) => typeof item === 'string' && item.trim()) : []
}

export function addSearchHistory(key) {
  const trimmed = (key || '').trim()
  if (!trimmed) return
  const list = loadSearchHistory().filter((item) => item !== trimmed)
  list.unshift(trimmed)
  writeJSON(STORAGE_KEYS.SEARCH_HISTORY, list.slice(0, MAX_HISTORY))
}

export function removeSearchHistory(key) {
  writeJSON(STORAGE_KEYS.SEARCH_HISTORY, loadSearchHistory().filter((item) => item !== key))
}

export function clearSearchHistory() {
  removeKey(STORAGE_KEYS.SEARCH_HISTORY)
}
