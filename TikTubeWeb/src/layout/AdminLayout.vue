<template>
  <div>
    <AppHeader
      home-path="/admin"
      publish-path="/studio/upload"
      suffix="Admin"
      :show-github="false"
      @toggle-nav="toggleNav"
      @search="onSearch"
    />

    <AppSidebar :items="items" :open="drawerOpen" :rail="rail" show-user @update:open="drawerOpen = $event" />

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
  { icon: 'mdi-view-dashboard', text: '概览', to: '/admin' },
  { icon: 'mdi-server', text: '运行状态', to: '/admin/runinfo' },
  { icon: 'mdi-ticket-account', text: '邀请码', to: '/admin/invitation' },
  { icon: 'mdi-video-check', text: '待审核', to: '/admin/examine' },
  { icon: 'mdi-flag', text: '举报 / 意见 / 申诉', to: '/admin/opinion' },
  { icon: 'mdi-filmstrip-box-multiple', text: '投稿列表', to: '/admin/article/list' },
  { icon: 'mdi-file-outline', text: '文件列表', to: '/admin/file/list' },
  { icon: 'mdi-account-multiple', text: '用户列表', to: '/admin/userlist' },
  { icon: 'mdi-comment-text-outline', text: '评论管理', to: '/admin/comment' },
  { icon: 'mdi-airplane', text: '弹幕管理', to: '/admin/danmuku' },
  { icon: 'mdi-advertisements', text: '广告公告管理', to: '/admin/ads' },
  { icon: 'mdi-playlist-edit', text: '分类管理', to: '/admin/category' },
  { icon: 'mdi-database', text: '存储管理', to: '/admin/oss' },
  { icon: 'mdi-robot', text: 'AI 模型管理', to: '/admin/ai' },
  { icon: 'mdi-square-edit-outline', text: '网页设置', to: '/admin/websetting' },
]

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
  router.push({ path: '/search', query: { key } })
}
</script>
