<template>
  <v-card class="upload-section-card rounded-lg" elevation="2">
    <div class="upload-section-card__head d-flex align-center ga-3 px-4 pt-4 pb-3">
      <v-avatar :color="done ? 'success' : 'primary'" variant="tonal" size="34" rounded="lg">
        <v-icon :icon="done ? 'mdi-check' : icon" size="19" />
      </v-avatar>
      <div class="flex-grow-1">
        <div class="text-subtitle-1 font-weight-bold">{{ title }}</div>
        <div v-if="hint" class="text-caption text-medium-emphasis mt-1">{{ hint }}</div>
      </div>
      <slot name="actions" />
    </div>
    <v-divider />
    <div class="upload-section-card__body pa-4">
      <slot />
    </div>
  </v-card>
</template>

<script>
/**
 * 投稿表单的分区卡片：图标 + 标题 + 说明，右上可放操作按钮。
 * 与创作中心首页的指标卡用同一套「tonal 头像 + 标题 + caption」语言。
 */
export default {
  name: 'UploadSectionCard',
  props: {
    icon: { type: String, required: true },
    title: { type: String, required: true },
    hint: { type: String, default: '' },
    /** 填完了图标变绿勾，让长表单有进度感 */
    done: { type: Boolean, default: false },
  },
}
</script>

<style scoped>
.upload-section-card__head .text-subtitle-1 {
  line-height: 1.35;
}
</style>
