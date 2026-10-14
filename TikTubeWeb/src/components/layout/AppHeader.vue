<template>
  <v-app-bar flat color="background" height="64" class="app-header px-2">
    <template #prepend>
      <v-app-bar-nav-icon v-if="!mobileSearchOpen" variant="text" @click="emit('toggle-nav')" />
      <v-btn v-else icon="mdi-arrow-left" variant="text" aria-label="返回" @click="closeMobileSearch" />
    </template>

    <!-- 移动端搜索态：顶栏整条让位给输入框 -->
    <div
      v-if="mobileSearchOpen"
      class="app-header__mobile-search d-flex align-center ga-1 flex-grow-1"
    >
      <SearchHistoryMenu v-model="historyMenuOpen" @select="searchFromHistory">
        <template #activator="{ activatorProps, open }">
          <v-text-field
            ref="mobileSearchField"
            v-model="searchText"
            v-bind="activatorProps"
            class="flex-grow-1"
            density="compact"
            variant="solo-filled"
            flat
            hide-details
            single-line
            rounded="pill"
            bg-color="surface-variant"
            placeholder="搜索"
            prepend-inner-icon="mdi-magnify"
            @focus="open"
            @keydown="onSearchKeydown"
            @keydown.esc="closeMobileSearch"
          />
        </template>
      </SearchHistoryMenu>
      <v-btn icon="mdi-magnify" variant="text" aria-label="搜索" @click="doSearch" />
    </div>

    <template v-else>
      <!-- 品牌区 -->
      <router-link
        :to="homePath"
        class="app-header__brand d-flex align-center ga-2 text-decoration-none flex-shrink-0"
      >
        <v-avatar size="30" rounded="lg" color="primary">
          <v-img v-if="webInfoStore.getLogoUrl" :src="webInfoStore.getLogoUrl" :alt="webInfoStore.getName" />
          <v-icon v-else size="18">mdi-play</v-icon>
        </v-avatar>
        <span class="text-h6 font-weight-bold d-none d-sm-inline">{{ webInfoStore.getName }}</span>
        <span v-if="suffix" class="text-caption text-medium-emphasis d-none d-md-inline">{{ suffix }}</span>
      </router-link>

      <v-spacer />

      <!-- 搜索区：YouTube 式的居中胶囊输入框 -->
      <div class="app-header__search d-none d-sm-flex align-center ga-1 flex-grow-0">
        <SearchHistoryMenu v-model="historyMenuOpen" @select="searchFromHistory">
          <template #activator="{ activatorProps, open }">
            <v-text-field
              v-model="searchText"
              v-bind="activatorProps"
              density="compact"
              variant="solo-filled"
              flat
              hide-details
              single-line
              rounded="pill"
              bg-color="surface-variant"
              placeholder="搜索"
              prepend-inner-icon="mdi-magnify"
              @focus="open"
              @keydown="onSearchKeydown"
            />
          </template>
        </SearchHistoryMenu>
        <v-tooltip location="bottom" text="搜索">
          <template #activator="{ props }">
            <v-btn v-bind="props" icon="mdi-magnify" variant="text" @click="doSearch" />
          </template>
        </v-tooltip>
      </div>

      <v-spacer />

      <!-- 右侧操作区 -->
      <div class="d-flex align-center ga-1">
        <slot name="actions" />

        <!-- 移动端放不下搜索框，用按钮唤起搜索条 -->
        <v-btn
          class="d-sm-none"
          icon="mdi-magnify"
          variant="text"
          aria-label="搜索"
          @click="openMobileSearch"
        />

        <ThemeToggle />

        <!-- 移动端顶栏宽度不够，Github 只在 sm 及以上显示 -->
        <v-tooltip v-if="showGithub" location="bottom" text="Github 开源地址">
          <template #activator="{ props }">
            <v-btn v-bind="props" class="d-none d-sm-inline-flex" icon variant="text" @click="openGithub">
              <v-icon>mdi-github</v-icon>
            </v-btn>
          </template>
        </v-tooltip>

        <v-tooltip location="bottom" text="投稿">
          <template #activator="{ props }">
            <v-btn v-bind="props" icon variant="text" @click="goTo(publishPath)">
              <v-icon>mdi-video-plus</v-icon>
            </v-btn>
          </template>
        </v-tooltip>

        <v-tooltip location="bottom" text="通知">
          <template #activator="{ props }">
            <v-btn v-bind="props" icon variant="text" @click="goTo('/notification')">
              <v-badge
                v-if="notificationStore.unreadCount > 0"
                color="error"
                :content="String(notificationStore.unreadCount)"
                offset-x="4"
                offset-y="4"
              >
                <v-icon>mdi-bell</v-icon>
              </v-badge>
              <v-icon v-else>mdi-bell</v-icon>
            </v-btn>
          </template>
        </v-tooltip>

        <AppBarHead v-if="userStore.isLogin" />

        <!-- 未登录一律用文字按钮，圆形账号图标容易被当成占位头像 -->
        <v-btn
          v-else
          class="app-header__login ml-1"
          variant="tonal"
          color="primary"
          rounded="pill"
          aria-label="登录"
          @click="goTo('/login')"
        >
          登录
        </v-btn>
      </div>
    </template>
  </v-app-bar>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { useUserStore } from '@/stores/userStore'
import { useWebInfoStore } from '@/stores/webInfoStore'
import { useNotificationStore } from '@/stores/notificationStore'
import ThemeToggle from '@/components/common/ThemeToggle.vue'
import AppBarHead from '@/components/user/AppBarHead.vue'
import SearchHistoryMenu from '@/components/layout/SearchHistoryMenu.vue'
import { addSearchHistory } from '@/utils/search-history'

defineProps({
  /** 点击品牌 LOGO 跳转的地址 */
  homePath: { type: String, default: '/' },
  /** 「投稿」按钮跳转的地址 */
  publishPath: { type: String, default: '/studio/upload' },
  /** 品牌名后的小后缀，如 Studio / Admin */
  suffix: { type: String, default: '' },
  showGithub: { type: Boolean, default: true },
})

const emit = defineEmits(['toggle-nav', 'search'])

const router = useRouter()
const userStore = useUserStore()
const webInfoStore = useWebInfoStore()
const notificationStore = useNotificationStore()
const { smAndUp } = useDisplay()

const searchText = ref('')
const mobileSearchOpen = ref(false)
const mobileSearchField = ref(null)
const historyMenuOpen = ref(false)

// 切回桌面档位后搜索条不再有意义
watch(smAndUp, (isDesktop) => {
  if (isDesktop) mobileSearchOpen.value = false
})

// 未读数放在 store 里：消息页标记已读后会直接刷新角标
watch(() => userStore.isLogin, () => notificationStore.refreshUnreadCount(), { immediate: true })

function goTo(path) {
  router.push(path)
}

async function openMobileSearch() {
  mobileSearchOpen.value = true
  await nextTick()
  mobileSearchField.value?.focus()
}

function closeMobileSearch() {
  mobileSearchOpen.value = false
  historyMenuOpen.value = false
}

function doSearch() {
  const key = searchText.value.trim()
  if (!key) return
  addSearchHistory(key)
  historyMenuOpen.value = false
  emit('search', key)
  searchText.value = ''
  closeMobileSearch()
}

function searchFromHistory(key) {
  searchText.value = key
  doSearch()
}

function onSearchKeydown(event) {
  if (event.key === 'Enter') doSearch()
}

function openGithub() {
  window.open('https://github.com/PuZhiweizuishuai/TikTube', '_blank', 'noopener')
}
</script>

<style scoped>
.app-header {
  border-bottom: 1px solid rgba(var(--v-border-color), var(--v-border-opacity));
}

.app-header__brand {
  color: rgb(var(--v-theme-on-surface));
}

.app-header__search {
  width: min(46%, 520px);
  min-width: 180px;
}

.app-header__mobile-search {
  min-width: 0;
}

/* 顶栏不换行、也不该横向滚动：窄屏把登录按钮的默认最小宽度收掉，
   让搜索 / 主题 / 投稿 / 通知 / 登录在 360px 宽的机型上也放得下 */
@media (max-width: 400px) {
  .app-header__login {
    min-width: 0;
    padding-inline: 10px;
  }
}
</style>
