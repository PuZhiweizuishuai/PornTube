<template>
  <v-container class="py-6" style="max-width: 900px">
    <h1 class="text-h5 font-weight-bold mb-1">设置</h1>
    <p class="text-body-2 text-medium-emphasis mb-6">管理你的外观偏好与账户相关设置</p>

    <!-- 外观 -->
    <v-card rounded="lg" class="mb-6">
      <v-card-title class="d-flex align-center">
        <v-icon start>mdi-palette-outline</v-icon>
        外观
      </v-card-title>
      <v-divider />
      <v-card-text class="pt-4">
        <p class="text-body-2 text-medium-emphasis mb-4">
          当前：{{ store.currentModeMeta.title }}
          <span v-if="store.mode === 'system'" class="text-caption">
            （系统{{ store.systemPrefersDark ? '深色' : '浅色' }}）
          </span>
        </p>

        <v-row dense>
          <v-col v-for="item in THEME_MODES" :key="item.value" cols="12" sm="4">
            <v-card
              rounded="lg"
              :variant="store.mode === item.value ? 'tonal' : 'outlined'"
              :color="store.mode === item.value ? 'primary' : undefined"
              class="pa-4 h-100 d-flex flex-column align-center ga-2"
              style="cursor: pointer"
              @click="store.setMode(item.value)"
            >
              <v-icon :icon="item.icon" size="28" />
              <div class="text-body-2 font-weight-medium">{{ item.title }}</div>
              <div class="text-caption text-medium-emphasis">
                {{ MODE_HINT[item.value] }}
              </div>
              <v-icon
                v-if="store.mode === item.value"
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
            :model-value="store.isDark"
            color="primary"
            hide-details
            inset
            @update:model-value="store.toggle()"
          />
        </div>
      </v-card-text>
    </v-card>

    <!-- 账户 -->
    <v-card rounded="lg">
      <v-card-title class="d-flex align-center">
        <v-icon start>mdi-account-outline</v-icon>
        账户
      </v-card-title>
      <v-divider />
      <v-list density="comfortable">
        <v-list-item
          prepend-icon="mdi-account-circle-outline"
          title="个人资料与频道"
          subtitle="头像、签名、主页背景"
          to="/user/setting"
        />
        <v-list-item
          prepend-icon="mdi-application-outline"
          title="创作中心"
          subtitle="投稿、数据、粉丝管理"
          to="/studio"
        />
      </v-list>
    </v-card>
  </v-container>
</template>

<script setup>
import { THEME_MODES, useThemeStore } from '@/stores/themeStore'

const store = useThemeStore()

const MODE_HINT = {
  light: '始终使用浅色主题',
  dark: '始终使用深色主题',
  system: '跟随操作系统的外观设置',
}
</script>
