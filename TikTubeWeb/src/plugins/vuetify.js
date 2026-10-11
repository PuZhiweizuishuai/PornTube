import 'vuetify/styles'
// 图标字体：icons.defaultSet 设为 'mdi' 后必须引入，否则所有图标只会渲染成空白圆圈
import '@mdi/font/css/materialdesignicons.css'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

/**
 * Vuetify 主题与全局默认值的唯一配置处。
 *
 * 配色参考 YouTube：浅色为纯白底 + 浅灰层次；深色为 #0F0F0F 底 + #181818/#272727 层次。
 * 品牌色沿用站点红色，深色下提亮以保住对比度。
 * 额外声明的自定义色（surface-hover / outline 等）同样会被 Vuetify 导出成
 * `--v-theme-xxx` CSS 变量，业务 CSS 可直接引用，无需再写死十六进制色值。
 */
const BRAND = { light: '#F44336', dark: '#FF5252' }

export const lightTheme = {
  dark: false,
  colors: {
    primary: BRAND.light,
    'on-primary': '#FFFFFF',
    secondary: '#5F6368',
    'on-secondary': '#FFFFFF',
    background: '#FFFFFF',
    'on-background': '#0F0F0F',
    surface: '#FFFFFF',
    'on-surface': '#0F0F0F',
    'surface-variant': '#F2F2F2',
    'on-surface-variant': '#606060',
    'surface-hover': '#F2F2F2',
    'surface-sunken': '#F9F9F9',
    outline: '#E0E0E0',
    success: '#1E8E3E',
    warning: '#B06000',
    error: '#D93025',
    info: '#1A73E8',
  },
  variables: {
    'border-color': '#000000',
    'border-opacity': 0.12,
    'high-emphasis-opacity': 0.87,
    'medium-emphasis-opacity': 0.6,
    'disabled-opacity': 0.38,
    'hover-opacity': 0.04,
    'focus-opacity': 0.08,
    'selected-opacity': 0.08,
    'activated-opacity': 0.12,
    'pressed-opacity': 0.12,
    'dragged-opacity': 0.08,
    'theme-scroll-bar': '#C6C6C6',
  },
}

export const darkTheme = {
  dark: true,
  colors: {
    primary: BRAND.dark,
    'on-primary': '#FFFFFF',
    secondary: '#9AA0A6',
    'on-secondary': '#0F0F0F',
    background: '#0F0F0F',
    'on-background': '#F1F1F1',
    surface: '#181818',
    'on-surface': '#F1F1F1',
    'surface-variant': '#272727',
    'on-surface-variant': '#AAAAAA',
    'surface-hover': '#272727',
    'surface-sunken': '#0F0F0F',
    outline: '#303030',
    success: '#66BB6A',
    warning: '#FFB74D',
    error: '#FF6B66',
    info: '#64B5F6',
  },
  variables: {
    'border-color': '#FFFFFF',
    'border-opacity': 0.16,
    'high-emphasis-opacity': 0.9,
    'medium-emphasis-opacity': 0.7,
    'disabled-opacity': 0.38,
    'hover-opacity': 0.08,
    'focus-opacity': 0.12,
    'selected-opacity': 0.12,
    'activated-opacity': 0.16,
    'pressed-opacity': 0.16,
    'dragged-opacity': 0.12,
    'theme-scroll-bar': '#5A5A5A',
  },
}

/**
 * 全局组件默认值：把「风格」固化在配置里，而不是靠每个页面各写一遍属性。
 * 单个组件显式传的 props 依然优先，所以不会影响已有的特殊用法。
 */
const defaults = {
  VCard: { rounded: 'lg', elevation: 0 },
  VChip: { border: false },
  VTextField: { variant: 'outlined', density: 'comfortable', color: 'primary' },
  VTextarea: { variant: 'outlined', density: 'comfortable', color: 'primary' },
  VSelect: { variant: 'outlined', density: 'comfortable', color: 'primary' },
  VAutocomplete: { variant: 'outlined', density: 'comfortable', color: 'primary' },
  VCombobox: { variant: 'outlined', density: 'comfortable', color: 'primary' },
  VPagination: { color: 'primary', rounded: 'circle', density: 'comfortable' },
  VDataTable: { density: 'comfortable' },
  VDataTableServer: { density: 'comfortable' },
  VTooltip: { location: 'bottom' },
  VNavigationDrawer: { color: 'background', border: 'none' },
  VSnackbar: { rounded: 'pill', location: 'top' },
}

/**
 * @param {string} defaultTheme 初始主题名（由 themeStore 计算后传入，避免首屏闪白）
 */
export function createAppVuetify(defaultTheme = 'light') {
  return createVuetify({
    components,
    directives,
    icons: { defaultSet: 'mdi' },
    display: { mobileBreakpoint: 'md' },
    theme: {
      defaultTheme,
      themes: { light: lightTheme, dark: darkTheme },
    },
    defaults,
  })
}
