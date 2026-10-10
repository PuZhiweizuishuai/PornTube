<!--
  通用二次确认弹窗
  后台 7 个 CRUD 视图里各写了 1~3 个「确认删除/恢复/通过」对话框，结构完全一致。
  用法：
    <ConfirmDialog v-model="deleteDialog" title="确认删除" confirm-text="删除"
                   :text="`确定删除 ${name} 吗？`" @confirm="doDelete" />
-->
<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    default: false,
  },
  title: {
    type: String,
    default: '确认操作',
  },
  /** 支持 HTML（内容来自受控文案时使用） */
  text: {
    type: String,
    default: '',
  },
  confirmText: {
    type: String,
    default: '确定',
  },
  cancelText: {
    type: String,
    default: '取消',
  },
  color: {
    type: String,
    default: 'error',
  },
  maxWidth: {
    type: [String, Number],
    default: 400,
  },
  loading: {
    type: Boolean,
    default: false,
  },
  persistent: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

function close() {
  emit('update:modelValue', false)
  emit('cancel')
}

function confirm() {
  emit('confirm')
}
</script>

<template>
  <v-dialog
    :model-value="modelValue"
    :max-width="maxWidth"
    :persistent="persistent || loading"
    @update:model-value="(v) => emit('update:modelValue', v)"
  >
    <v-card>
      <v-card-title class="text-h5">{{ title }}</v-card-title>
      <v-card-text>
        <!-- 默认走纯文本渲染，避免把后台数据当成 HTML 造成 DOM XSS；需要富文本用默认插槽 -->
        <template v-if="!$slots.default">{{ text }}</template>
        <slot />
      </v-card-text>
      <v-card-actions>
        <v-spacer />
        <v-btn color="grey" variant="text" :disabled="loading" @click="close">{{ cancelText }}</v-btn>
        <v-btn :color="color" variant="elevated" :loading="loading" @click="confirm">
          {{ confirmText }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>
