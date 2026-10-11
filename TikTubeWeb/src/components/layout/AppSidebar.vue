<template>
  <v-navigation-drawer
    :model-value="mdAndUp ? true : open"
    :permanent="mdAndUp"
    :temporary="!mdAndUp"
    :rail="isRail"
    width="240"
    class="app-sidebar"
    @update:model-value="onUpdateOpen"
  >
    <!-- 用户信息（创作中心 / 管理中心展示） -->
    <template v-if="showUser && userStore.isLogin">
      <div class="pa-4 d-flex align-center" :class="isRail ? 'justify-center' : 'ga-3'">
        <v-avatar size="40">
          <v-img :src="userStore.userData.avatarUrl" :alt="userStore.userData.username" cover />
        </v-avatar>
        <div v-if="!isRail" class="overflow-hidden">
          <div class="text-body-2 font-weight-medium text-truncate">
            {{ userStore.userData.username }}
          </div>
          <router-link
            :to="`/user/${userStore.userData.id}`"
            class="text-caption text-medium-emphasis text-decoration-none"
          >
            查看我的频道
          </router-link>
        </div>
      </div>
      <v-divider />
    </template>

    <v-list nav density="comfortable" color="primary" class="app-sidebar__list py-2">
      <!-- 收起（仅图标）时用 tooltip 补足文字；展开时禁用 tooltip，避免与标题重复 -->
      <v-tooltip
        v-for="item in items"
        :key="item.to"
        :text="item.text"
        location="end"
        :disabled="!isRail"
      >
        <template #activator="{ props: tooltipProps }">
          <v-list-item
            v-bind="tooltipProps"
            :to="item.to"
            :prepend-icon="item.icon"
            :title="isRail ? '' : item.text"
            :active="item.to === activePath"
            rounded="lg"
            :class="isRail ? 'app-sidebar__rail-item mb-1' : 'mx-2 mb-1'"
          />
        </template>
      </v-tooltip>

      <v-tooltip
        v-if="showAdminEntry && userStore.isAdmin"
        text="管理中心"
        location="end"
        :disabled="!isRail"
      >
        <template #activator="{ props: tooltipProps }">
          <v-list-item
            v-bind="tooltipProps"
            to="/admin"
            prepend-icon="mdi-shield-crown"
            :title="isRail ? '' : '管理中心'"
            :active="activePath === '/admin'"
            rounded="lg"
            :class="isRail ? 'app-sidebar__rail-item mb-1' : 'mx-2 mb-1'"
          />
        </template>
      </v-tooltip>
    </v-list>

    <!-- 底部版权 / 反馈：收起后空间不足会挤成一团，直接隐藏 -->
    <template v-if="!isRail" #append>
      <v-divider />
      <div class="pa-4 text-caption text-medium-emphasis">
        <div class="d-flex flex-wrap ga-1 align-center">
          <a href="https://www.buguagaoshu.com" target="_blank" rel="noopener" class="app-sidebar__link">不挂高数</a>
          <span>·</span>
          <a href="https://vuetifyjs.com/" target="_blank" rel="noopener" class="app-sidebar__link">Vuetify</a>
          <span>·</span>
          <button type="button" class="app-sidebar__link" @click="showOpinionDialog = true">意见反馈</button>
        </div>
        <div class="mt-1">© {{ currentYear }}</div>
      </div>
    </template>
  </v-navigation-drawer>

  <v-dialog v-model="showOpinionDialog" width="50vh">
    <OpinionCard
      :target-id="-1"
      :type-num="10"
      target-title=""
      :is-report="false"
      @close="showOpinionDialog = false"
    />
  </v-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useUserStore } from '@/stores/userStore'
import OpinionCard from '@/components/card/OpinionCard.vue'

const props = defineProps({
  /** 导航项：{ icon, text, to } —— 用 to 而不是 href，避免整页刷新 */
  items: { type: Array, default: () => [] },
  /** 移动端抽屉是否展开 */
  open: { type: Boolean, default: false },
  /** 桌面端是否收窄成 mini 侧栏 */
  rail: { type: Boolean, default: false },
  /** 顶部是否展示当前用户 */
  showUser: { type: Boolean, default: false },
  /** 是否为管理员追加「管理中心」入口 */
  showAdminEntry: { type: Boolean, default: false },
})

const emit = defineEmits(['update:open'])

const { mdAndUp } = useDisplay()
const userStore = useUserStore()
const route = useRoute()

/** 只有桌面端才有「收起成图标栏」这回事，移动端是整块抽屉的显示/隐藏 */
const isRail = computed(() => mdAndUp.value && props.rail)

/**
 * 当前应该高亮的导航项。
 *
 * 不能直接用 Vuetify / vue-router 的默认激活判定：它对 `to="/"` 走前缀匹配，
 * 会命中所有路径，导致「首页」永远高亮。这里改成显式计算，并且只选**最长匹配**
 * 的那一项，保证任何时候最多只有一个条目处于选中态。
 */
const activePath = computed(() => {
  const path = route.path
  let matched = ''
  const consider = (to) => {
    if (!to) return
    const hit = to === '/' ? path === '/' : path === to || path.startsWith(`${to}/`)
    if (hit && to.length > matched.length) matched = to
  }
  props.items.forEach((item) => consider(item.to))
  if (props.showAdminEntry && userStore.isAdmin) consider('/admin')
  return matched
})

const showOpinionDialog = ref(false)
const currentYear = computed(() => new Date().getFullYear())

function onUpdateOpen(value) {
  emit('update:open', !!value)
}
</script>

<style scoped>
.app-sidebar__list {
  flex: 1 1 auto;
  overflow-y: auto;
}

.app-sidebar__link {
  color: inherit;
  text-decoration: none;
  background: none;
  border: 0;
  padding: 0;
  cursor: pointer;
  font: inherit;
}

.app-sidebar__link:hover {
  color: rgb(var(--v-theme-primary));
}
</style>
