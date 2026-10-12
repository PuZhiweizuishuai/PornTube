<template>
  <div>
    <AppHeader home-path="/" publish-path="/studio/upload" @toggle-nav="toggleNav" @search="onSearch" />

    <AppSidebar :items="items" :open="drawerOpen" :rail="rail" @update:open="drawerOpen = $event" />

    <v-main class="app-main">
      <AppRouterView />
    </v-main>
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
import AppRouterView from '@/components/layout/AppRouterView.vue'

const router = useRouter()
const { mdAndUp } = useDisplay()

const items = [
  { icon: 'mdi-home', text: '首页', to: '/' },
  { icon: 'mdi-trending-up', text: '时下流行', to: '/hot' },
  { icon: 'mdi-youtube-subscription', text: '订阅', to: '/subscribe' },
  { icon: 'mdi-history', text: '历史记录', to: '/history' },
  { icon: 'mdi-heart', text: '我的收藏', to: '/playlist' },
  { icon: 'mdi-text-box-outline', text: '文章', to: '/articles' },
  // { icon: 'mdi-cog-outline', text: '设置', to: '/settings' },
  { icon: 'mdi-help-circle', text: '关于', to: '/about' },
]

// 桌面端：永久抽屉，汉堡按钮在「展开 / mini」之间切换
// 移动端：临时抽屉，汉堡按钮控制显示隐藏
const drawerOpen = ref(false)
const rail = ref(false)

watch(mdAndUp, (isDesktop) => {
  if (isDesktop) drawerOpen.value = false
})

function toggleNav() {
  if (mdAndUp.value) rail.value = !rail.value
  else drawerOpen.value = !drawerOpen.value
}

function onSearch(key) {
  // 走路由即可：AppRouterView 以 fullPath 作为 key，query 变化会自动重建搜索页
  router.push({ path: '/search', query: { key } })
}
</script>
