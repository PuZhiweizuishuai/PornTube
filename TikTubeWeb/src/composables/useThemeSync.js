import { onScopeDispose, watch } from 'vue'
import { useTheme } from 'vuetify'
import { useThemeStore } from '@/stores/themeStore'

const THEME_COLOR = { light: '#FFFFFF', dark: '#0F0F0F' }

/**
 * 把 themeStore（唯一数据源）同步到 Vuetify 与 <html>。
 *
 * - Vuetify：`theme.global.name` 决定全部组件配色
 * - <html data-theme>：给自定义 CSS / 滚动条 / 浏览器 UI 用
 * - <meta name="theme-color">：移动端地址栏跟随
 *
 * 在 App.vue 的 setup 中调用一次即可，组件卸载时自动解绑。
 */
export function useThemeSync() {
  const store = useThemeStore()
  const theme = useTheme()

  function apply() {
    const name = store.themeName
    if (theme.global.name.value !== name) {
      theme.global.name.value = name
    }
    const root = document.documentElement
    root.dataset.theme = name
    root.style.colorScheme = name

    let meta = document.querySelector('meta[name="theme-color"]')
    if (!meta) {
      meta = document.createElement('meta')
      meta.setAttribute('name', 'theme-color')
      document.head.appendChild(meta)
    }
    meta.setAttribute('content', THEME_COLOR[name] || THEME_COLOR.light)
  }

  store.hydrate()
  apply()

  const stop = watch(() => store.themeName, apply)

  onScopeDispose(() => {
    stop()
    store.destroy()
  })

  return { store, apply }
}

export default useThemeSync
