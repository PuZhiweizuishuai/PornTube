<template>
  <v-menu
    v-model="open"
    :open-on-click="false"
    :open-on-focus="false"
    :close-on-content-click="false"
    location="bottom start"
    offset="4"
    min-width="280"
    max-width="420"
  >
    <template #activator="{ props: activatorProps }">
      <slot name="activator" :activator-props="activatorProps" :open="requestOpen" />
    </template>

    <v-card rounded="lg" class="pa-1">
      <div class="d-flex align-center pl-3 pr-2 py-1">
        <span class="text-caption text-medium-emphasis">搜索历史</span>
        <v-spacer></v-spacer>
        <v-btn size="x-small" variant="text" color="primary" @click="onClear">清空</v-btn>
      </div>

      <v-list nav density="compact" class="py-0">
        <v-list-item
          v-for="item in history"
          :key="item"
          :title="item"
          prepend-icon="mdi-history"
          @click="select(item)"
        >
          <template #append>
            <v-btn
              icon="mdi-close"
              size="x-small"
              variant="text"
              aria-label="删除该条搜索历史"
              @click.stop="remove(item)"
            />
          </template>
        </v-list-item>
      </v-list>
    </v-card>
  </v-menu>
</template>

<script setup>
import { ref } from 'vue'
import {
  loadSearchHistory,
  removeSearchHistory,
  clearSearchHistory,
} from '@/utils/search-history'

const open = defineModel({ type: Boolean, default: false })
const emit = defineEmits(['select'])

const history = ref([])

/** 供使用方在搜索框 focus 时调用：无历史记录则不弹出空面板 */
function requestOpen() {
  history.value = loadSearchHistory()
  if (history.value.length > 0) open.value = true
}

function select(item) {
  open.value = false
  emit('select', item)
}

function remove(item) {
  removeSearchHistory(item)
  history.value = loadSearchHistory()
  if (history.value.length === 0) open.value = false
}

function onClear() {
  clearSearchHistory()
  history.value = []
  open.value = false
}
</script>
