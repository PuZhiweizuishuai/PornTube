<template>
  <v-container class="py-6" style="max-width: 1080px">
    <h1 class="text-h5 font-weight-bold mb-1">设置</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">
      外观、频道信息与账户安全，都在这里管理
    </p>

    <!-- 移动端：横向分区导航 -->
    <v-slide-group v-if="!mdAndUp" show-arrows class="mb-4">
      <v-slide-group-item v-for="section in SECTIONS" :key="section.key">
        <v-btn
          :prepend-icon="section.icon"
          :variant="active === section.key ? 'tonal' : 'text'"
          :color="active === section.key ? 'primary' : undefined"
          class="mr-2"
          rounded="pill"
          @click="select(section.key)"
        >
          {{ section.title }}
        </v-btn>
      </v-slide-group-item>
    </v-slide-group>

    <v-row no-gutters>
      <!-- 桌面端：左侧分区导航 -->
      <v-col v-if="mdAndUp" cols="12" md="3" class="pe-4">
        <div class="settings-nav">
          <v-card rounded="lg">
            <v-list density="compact" nav>
              <v-list-item
                v-for="section in SECTIONS"
                :key="section.key"
                :prepend-icon="section.icon"
                :title="section.title"
                :subtitle="section.hint"
                rounded="lg"
                :active="active === section.key"
                color="primary"
                @click="select(section.key)"
              />
            </v-list>
          </v-card>
        </div>
      </v-col>

      <v-col cols="12" :md="mdAndUp ? 9 : 12">
        <!-- 外观 -->
        <v-card v-if="active === 'appearance'" rounded="lg">
          <v-card-title class="d-flex align-center">
            <v-icon start>mdi-palette-outline</v-icon>
            外观
          </v-card-title>
          <v-divider />
          <v-card-text class="pt-4">
            <p class="text-body-2 text-medium-emphasis mb-4">
              当前：{{ themeStore.currentModeMeta.title }}
              <span v-if="themeStore.mode === 'system'" class="text-caption">
                （系统{{ themeStore.systemPrefersDark ? '深色' : '浅色' }}）
              </span>
            </p>

            <v-row dense>
              <v-col v-for="item in THEME_MODES" :key="item.value" cols="12" sm="4">
                <v-card
                  rounded="lg"
                  :variant="themeStore.mode === item.value ? 'tonal' : 'outlined'"
                  :color="themeStore.mode === item.value ? 'primary' : undefined"
                  class="pa-4 h-100 d-flex flex-column align-center ga-2"
                  style="cursor: pointer"
                  @click="themeStore.setMode(item.value)"
                >
                  <v-icon :icon="item.icon" size="28" />
                  <div class="text-body-2 font-weight-medium">{{ item.title }}</div>
                  <div class="text-caption text-medium-emphasis">
                    {{ MODE_HINT[item.value] }}
                  </div>
                  <v-icon
                    v-if="themeStore.mode === item.value"
                    color="primary"
                    size="small"
                    class="mt-auto"
                  >
                    mdi-check-circle
                  </v-icon>
                </v-card>
              </v-col>
            </v-row>

            <v-divider class="my-4" />

            <div class="d-flex align-center">
              <div>
                <div class="text-body-2 font-weight-medium">夜间模式</div>
                <div class="text-caption text-medium-emphasis">立即在浅色与深色之间切换</div>
              </div>
              <v-spacer />
              <v-switch
                :model-value="themeStore.isDark"
                color="primary"
                hide-details
                inset
                @update:model-value="themeStore.toggle()"
              />
            </div>
          </v-card-text>
        </v-card>

        <!-- 频道与账户 -->
        <UserBaseSetting v-else-if="active === 'profile'" />
        <UserHeadSetting v-else-if="active === 'avatar'" />
        <UserTopImage v-else-if="active === 'top-image'" />
        <UserPasswordSetting v-else-if="active === 'password'" />
        <TwoFactorAuthentication v-else-if="active === 'two-factor'" />
        <UserLoginLog v-else-if="active === 'login-log'" />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useDisplay } from 'vuetify'
import { THEME_MODES, useThemeStore } from '@/stores/themeStore'
import UserBaseSetting from '@/components/setting/UserBaseSetting.vue'
import UserHeadSetting from '@/components/setting/UserHeadSetting.vue'
import UserTopImage from '@/components/setting/UserTopImage.vue'
import UserPasswordSetting from '@/components/setting/UserPasswordSetting.vue'
import TwoFactorAuthentication from '@/components/setting/TwoFactorAuthentication.vue'
import UserLoginLog from '@/components/setting/UserLoginLog.vue'

const SECTIONS = [
  { key: 'appearance', title: '外观', hint: '浅色 / 深色主题', icon: 'mdi-palette-outline' },
  { key: 'profile', title: '基本信息', hint: '用户名、简介', icon: 'mdi-account-edit' },
  { key: 'avatar', title: '头像', hint: '上传与剪裁头像', icon: 'mdi-account-circle-outline' },
  { key: 'top-image', title: '首页顶部图', hint: '频道主页横幅', icon: 'mdi-image-outline' },
  { key: 'password', title: '修改密码', hint: '更新登录密码', icon: 'mdi-lock-outline' },
  { key: 'two-factor', title: '两步验证', hint: '登录二次验证码', icon: 'mdi-shield-lock-outline' },
  { key: 'login-log', title: '登录历史', hint: '设备与 IP 记录', icon: 'mdi-history' },
]

const MODE_HINT = {
  light: '始终使用浅色主题',
  dark: '始终使用深色主题',
  system: '跟随操作系统的外观设置',
}

const route = useRoute()
const router = useRouter()
const { mdAndUp } = useDisplay()
const themeStore = useThemeStore()

const active = computed(() => {
  const section = route.query.section
  return SECTIONS.some((item) => item.key === section) ? section : 'appearance'
})

function select(key) {
  if (key === active.value) return
  router.push({ path: '/settings', query: { ...route.query, section: key } })
}
</script>

<style scoped>
.settings-nav {
  position: sticky;
  top: 84px;
}
</style>
