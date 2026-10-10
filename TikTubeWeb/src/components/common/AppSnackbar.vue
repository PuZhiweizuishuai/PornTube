<!--
  统一的消息条
  配合 @/composables/useMessage 使用，消除全站 15+ 处重复的 v-snackbar 模板：
    <AppSnackbar :state="toast" />
-->
<script setup>
const props = defineProps({
  /** useMessage() 返回的 toast 响应式对象 */
  state: {
    type: Object,
    required: true,
  },
  timeout: {
    type: Number,
    default: 3000,
  },
  location: {
    type: String,
    default: 'top',
  },
  /** 关闭按钮文案，传 null 则不显示 */
  closeText: {
    type: String,
    default: '关闭',
  },
})

function close() {
  props.state.show = false
}
</script>

<template>
  <v-snackbar
    v-model="state.show"
    :timeout="state.timeout || timeout"
    :location="state.location || location"
    :color="state.color || 'primary'"
  >
    {{ state.text }}
    <template v-if="closeText" #actions>
      <v-btn variant="text" @click="close">{{ closeText }}</v-btn>
    </template>
  </v-snackbar>
</template>
