import { computed, onScopeDispose, watch } from 'vue'
import { useThemeStore } from '@/stores/themeStore'

/** Vditor 只认 'classic' | 'dark'，站内是 'light' | 'dark' */
function toVditorTheme(themeName) {
  return themeName === 'dark' ? 'dark' : 'classic'
}

/**
 * 让 Vditor 编辑器跟随站内主题（夜间模式）。
 *
 * Vditor 的深色外观靠根元素上的 `.vditor--dark` 变量集，构造时的 `theme`
 * 选项与实例方法 `setTheme()` 都会设置它；切主题时不重新初始化编辑器。
 *
 * 用法：
 *   setup() { return { vditorTheme: useVditorTheme() } }
 *   new Vditor(id, { theme: this.vditorTheme.themeName.value, ..., after: () => this.vditorTheme.bind(el) })
 */
export function useVditorTheme() {
  const store = useThemeStore()
  const themeName = computed(() => toVditorTheme(store.themeName))

  let editor = null

  function apply() {
    if (editor && typeof editor.setTheme === 'function') {
      editor.setTheme(themeName.value)
    }
  }

  /** 编辑器实例创建好后登记，之后的主题切换会自动同步 */
  function bind(instance) {
    editor = instance
    apply()
  }

  const stop = watch(themeName, apply)
  onScopeDispose(() => {
    stop()
    editor = null
  })

  return { themeName, bind, apply }
}

export default useVditorTheme
