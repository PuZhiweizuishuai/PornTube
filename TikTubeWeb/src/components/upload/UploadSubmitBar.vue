<template>
  <v-card class="upload-submit-bar rounded-lg" elevation="8" rounded="lg">
    <div class="d-flex align-center flex-wrap ga-3 pa-3 px-4">
      <v-icon
        :icon="ready ? 'mdi-check-circle' : 'mdi-alert-circle-outline'"
        :color="ready ? 'success' : 'warning'"
        size="20"
      />
      <div class="text-caption flex-grow-1" :class="ready ? 'text-success' : 'text-medium-emphasis'">
        {{ ready ? '信息已填写完整，可以发布了' : `还差：${missing.join('、')}` }}
      </div>
      <v-btn variant="text" prepend-icon="mdi-close" :to="cancelTo">取消</v-btn>
      <v-btn
        color="primary"
        size="large"
        rounded
        min-width="132"
        prepend-icon="mdi-send"
        :loading="submitting"
        :disabled="submitting || !ready"
        @click="$emit('submit')"
      >
        {{ submitText }}
      </v-btn>
    </div>
  </v-card>
</template>

<script>
/**
 * 投稿页底部的固定操作条：左边提示还缺什么，右边是发布按钮。
 * 两个投稿表单共用，缺项文案由父组件的检查清单算好后传进来。
 */
export default {
  name: 'UploadSubmitBar',
  props: {
    missing: { type: Array, default: () => [] },
    ready: { type: Boolean, default: false },
    submitting: { type: Boolean, default: false },
    submitText: { type: String, default: '立即投稿' },
    cancelTo: { type: String, default: '/studio' },
  },
  emits: ['submit'],
}
</script>

<style scoped>
.upload-submit-bar {
  position: sticky;
  bottom: 16px;
  z-index: 3;
}
</style>
