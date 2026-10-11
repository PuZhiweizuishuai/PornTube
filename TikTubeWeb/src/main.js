import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import HttpFetch from './utils/fetch'
import { createAppVuetify } from './plugins/vuetify'
import { useThemeStore } from '@/stores/themeStore'
import { useUserStore } from '@/stores/userStore'
import { useWebInfoStore } from '@/stores/webInfoStore'
import App from './App.vue'

import './assets/css/Main.css'

const app = createApp(App)

app.config.globalProperties.SERVER_API_URL = '/api'

// Pinia 必须最先安装：后面所有 store 的读取都依赖它
const pinia = createPinia()
app.use(pinia)

// store 在创建时就已经从持久化镜像中 rehydrate，这里只做一次显式同步，
// 保证「内存状态」与「本地缓存」从启动时就是一致的，不再出现双数据源。
useUserStore(pinia).hydrate()
useWebInfoStore(pinia).hydrate()

// 主题先算出来再创建 Vuetify，避免首屏闪一下浅色
const themeStore = useThemeStore(pinia)
themeStore.hydrate()

app.use(createAppVuetify(themeStore.themeName))
app.use(router)
app.use(HttpFetch)

console.log('%c不挂高数出品 https://www.buguagaoshu.com', 'color: #1976d2;font-size:2em')
console.log('%cPowered by buguagaosh', 'color: #1976d2;font-size:1em')

app.mount('#app')
