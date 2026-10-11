import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { STORAGE_KEYS, readString, writeString } from '@/utils/storage'

/** 可选的外观模式，UI 直接渲染这个列表，避免各处重复写死 */
export const THEME_MODES = [
  { value: 'light', title: '浅色', icon: 'mdi-white-balance-sunny' },
  { value: 'dark', title: '深色', icon: 'mdi-weather-night' },
  { value: 'system', title: '跟随系统', icon: 'mdi-laptop' },
]

const VALID_MODES = THEME_MODES.map((item) => item.value)

function readMode() {
  const cached = readString(STORAGE_KEYS.THEME_MODE, 'system')
  return VALID_MODES.includes(cached) ? cached : 'system'
}

/**
 * 外观（夜间模式）唯一数据源。
 *
 * - mode: 用户的选择（浅色 / 深色 / 跟随系统），持久化到 localStorage
 * - isDark: 实际是否处于深色，跟随系统时由 matchMedia 推导
 * - themeName: 直接喂给 Vuetify 的 `theme.global.name`
 */
export const useThemeStore = defineStore('theme', () => {
  const mode = ref(readMode())
  const systemPrefersDark = ref(false)

  let media = null
  let mediaHandler = null

  const isDark = computed(
    () => mode.value === 'dark' || (mode.value === 'system' && systemPrefersDark.value)
  )
  const themeName = computed(() => (isDark.value ? 'dark' : 'light'))
  const currentModeMeta = computed(
    () => THEME_MODES.find((item) => item.value === mode.value) || THEME_MODES[2]
  )

  function setMode(next) {
    const value = VALID_MODES.includes(next) ? next : 'system'
    mode.value = value
    writeString(STORAGE_KEYS.THEME_MODE, value)
  }

  /** 在浅色 / 深色之间快速切换（顶栏按钮用） */
  function toggle() {
    setMode(isDark.value ? 'light' : 'dark')
  }

  /**
   * 订阅系统外观变化。可重复调用，内部做了幂等处理。
   */
  function hydrate() {
    if (media || typeof window === 'undefined' || !window.matchMedia) return
    media = window.matchMedia('(prefers-color-scheme: dark)')
    systemPrefersDark.value = media.matches
    mediaHandler = (event) => {
      systemPrefersDark.value = event.matches
    }
    if (media.addEventListener) media.addEventListener('change', mediaHandler)
    else if (media.addListener) media.addListener(mediaHandler)
  }

  function destroy() {
    if (!media || !mediaHandler) return
    if (media.removeEventListener) media.removeEventListener('change', mediaHandler)
    else if (media.removeListener) media.removeListener(mediaHandler)
    media = null
    mediaHandler = null
  }

  return {
    mode,
    systemPrefersDark,
    isDark,
    themeName,
    currentModeMeta,
    setMode,
    toggle,
    hydrate,
    destroy,
  }
})
