<template>
  <router-view v-slot="{ Component, route }">
    <transition :name="transitionName" mode="out-in">
      <component :is="Component" :key="route.fullPath" ref="pageRef" />
    </transition>
  </router-view>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const pageRef = ref(null)

/**
 * 只在「真正换页」时播放过渡动画。
 *
 * 列表分页、筛选等只改 query 的跳转同样会触发组件重建（key 用的是 fullPath），
 * 如果无差别播动画，翻页就会闪一下。这里对比 path：
 * path 变了才 animate，否则用零时长的 page-instant。
 */
const animate = ref(true)
let lastPath = route.path

watch(
  () => route.fullPath,
  () => {
    animate.value = route.path !== lastPath
    lastPath = route.path
  }
)

const transitionName = computed(() => (animate.value ? 'page' : 'page-instant'))

/** 供布局调用页面实例方法（如搜索页的 setSearchKey） */
function getPageInstance() {
  return pageRef.value
}

defineExpose({ getPageInstance })
</script>
