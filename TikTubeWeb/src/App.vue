<template>
  <v-app>
    <router-view />
  </v-app>
</template>

<script setup>
import { onMounted } from 'vue'
import http from '@/api/index'
import { useThemeSync } from '@/composables/useThemeSync'
import { useWebInfoStore } from '@/stores/webInfoStore'

// 把 themeStore 同步到 Vuetify / <html>，夜间模式在这里真正生效
useThemeSync()

const webInfoStore = useWebInfoStore()

onMounted(async () => {
  try {
    const json = await http.get('/web/info')
    if (json?.data) {
      // 站点信息以 Pinia 为唯一数据源，持久化由 store 自己负责
      webInfoStore.updateWebInfo(json.data)
      const metaDesc = document.querySelector('meta[name="description"]')
      if (metaDesc) metaDesc.setAttribute('content', json.data.webDescribe || '')
    }
  } catch (e) {
    // 接口挂了也不影响启动：store 里已有上次缓存的站点信息
    console.warn('[app] 站点信息获取失败，沿用本地缓存', e)
  }
})
</script>

<style>
a {
  text-decoration: none;
}
</style>
