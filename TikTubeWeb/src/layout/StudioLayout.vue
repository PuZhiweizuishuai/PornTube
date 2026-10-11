<template>
  <div>
    <AppHeader
      home-path="/studio"
      publish-path="/studio/upload"
      suffix="Studio"
      :show-github="false"
      @toggle-nav="toggleNav"
      @search="onSearch"
    />

    <AppSidebar
      :items="items"
      :open="drawerOpen"
      :rail="rail"
      show-user
      show-admin-entry
      @update:open="drawerOpen = $event"
    />

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
  { icon: 'mdi-application', text: '信息中心', to: '/studio' },
  { icon: 'mdi-filmstrip-box-multiple', text: '投稿列表', to: '/studio/list' },
  { icon: 'mdi-upload', text: '投稿', to: '/studio/upload' },
  { icon: 'mdi-database', text: '数据分析', to: '/studio/data' },
  { icon: 'mdi-account-multiple', text: '粉丝管理', to: '/studio/fans' },
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
