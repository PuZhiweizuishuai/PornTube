<template>
  <v-card class="article-segment rounded-lg" :class="index === segments.length - 1 ? '' : 'mb-4'" variant="outlined">
    <div class="d-flex align-center ga-2 px-3 py-2">
      <v-chip size="x-small" variant="tonal" color="primary">段落 {{ index + 1 }}</v-chip>
      <v-icon v-if="segment.type === 1" icon="mdi-comment-alert-outline" size="18" color="warning" />
      <v-icon v-if="segment.type === 2" icon="mdi-lock-outline" size="18" color="warning" />
      <span v-if="segment.type !== 0" class="text-caption text-medium-emphasis">
        {{ segment.type === 1 ? '回复可见' : '加密段落' }}
      </span>
      <v-spacer />
      <v-btn
        v-if="segments.length > 1"
        icon="mdi-delete-outline"
        size="small"
        variant="text"
        color="error"
        @click="$emit('remove-segment', index)"
      ></v-btn>
    </div>

    <v-divider />

    <v-card-text>
      <!-- 文章类型选择 -->
      <div class="d-flex ga-4 flex-wrap mb-4">
        <v-select
          v-model="segment.type"
          class="flex-grow-1"
          style="min-width: 220px"
          :items="articleTypeOptions"
          label="段落类型"
          item-title="title"
          item-value="value"
          prepend-inner-icon="mdi-format-list-text"
          @update:model-value="handleTypeChange"
        ></v-select>

        <!-- 加密文章密码输入框 -->
        <v-text-field
          v-if="segment.type === 2"
          v-model="segment.password"
          class="flex-grow-1"
          style="min-width: 220px"
          label="访问密码"
          placeholder="请设置段落访问密码"
          prepend-inner-icon="mdi-lock"
          :rules="[(v) => segment.type !== 2 || !!v || '密码不能为空']"
          type="password"
        ></v-text-field>
      </div>

      <!-- Markdown编辑器 -->
      <ArticleTextEdit
        :ref="`markdownEditor${index}`"
        :idname="`articleEditor${index}`"
        :height="400"
        :uploadurl="`/api/upload/article`"
        :markdown="segment.content"
        :placeholder="`在这里编写段落${index + 1}的内容...`"
        @vditor-input="updateContent"
      />
    </v-card-text>
  </v-card>
</template>

<script>
import ArticleTextEdit from '@/components/vditor/ArticleTextEdit.vue'

export default {
  name: 'ArticleSegment',
  components: {
    ArticleTextEdit
  },
  props: {
    segment: {
      type: Object,
      required: true
    },
    index: {
      type: Number,
      required: true
    },
    segments: {
      type: Array,
      required: true
    },
    articleTypeOptions: {
      type: Array,
      required: true
    }
  },
  emits: ['remove-segment', 'update-content', 'update-type'],
  created() {
  },
  methods: {
    updateContent(content) {
      this.segment.content = content
      this.$emit('update-content', this.index, content)
    },
    handleTypeChange(value) {
      // 如果不是加密文章，清空密码
      if (value !== 2) {
        this.segment.password = ''
      }
      this.$emit('update-type', this.index, value)
    },
    setTextValue(value) {
      this.$refs[`markdownEditor${this.index}`].setTextValue(value)
    }
  }
}
</script>

<style scoped>
.article-segment :deep(.v-card-text) {
  padding-top: 16px;
}
</style>
