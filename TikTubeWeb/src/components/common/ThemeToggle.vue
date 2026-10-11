<template>
  <v-menu location="bottom end" offset="8">
    <template #activator="{ props: menuProps }">
      <v-tooltip location="bottom" :text="`外观：${store.currentModeMeta.title}`">
        <template #activator="{ props: tooltipProps }">
          <v-btn
            v-bind="mergeProps(menuProps, tooltipProps)"
            :icon="store.isDark ? 'mdi-weather-night' : 'mdi-white-balance-sunny'"
            variant="text"
            :aria-label="`外观：${store.currentModeMeta.title}`"
          />
        </template>
      </v-tooltip>
    </template>

    <v-list density="compact" nav min-width="180" class="py-1">
      <v-list-item
        v-for="item in THEME_MODES"
        :key="item.value"
        :title="item.title"
        :prepend-icon="item.icon"
        :active="store.mode === item.value"
        active-color="primary"
        rounded="lg"
        @click="store.setMode(item.value)"
      >
        <template #append>
          <v-icon v-if="store.mode === item.value" size="small" color="primary">mdi-check</v-icon>
        </template>
      </v-list-item>
    </v-list>
  </v-menu>
</template>

<script setup>
import { mergeProps } from 'vue'
import { THEME_MODES, useThemeStore } from '@/stores/themeStore'

const store = useThemeStore()
</script>
