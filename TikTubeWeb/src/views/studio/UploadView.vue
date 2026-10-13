<template>
  <v-container fluid class="pa-4 pa-md-6">
    <div>
      <!-- 页头 -->
      <div class="upload-page__head d-flex align-center flex-wrap ga-3 mb-5">
        <v-avatar color="primary" variant="tonal" size="42" rounded="lg">
          <v-icon icon="mdi-upload" size="22" />
        </v-avatar>
        <div class="flex-grow-1">
          <div class="text-h5 font-weight-bold">投稿</div>
          <div class="text-caption text-medium-emphasis mt-1">
            {{ isEdit ? '编辑已提交的稿件，保存后会重新进入审核' : '选择稿件类型，填写信息后发布' }}
          </div>
        </div>
        <v-chip
          v-if="isEdit"
          size="small"
          variant="tonal"
          color="warning"
          prepend-icon="mdi-pencil-outline"
        >
          编辑稿件 #{{ editCode }}
        </v-chip>
        <v-btn variant="text" prepend-icon="mdi-format-list-bulleted" to="/studio/list">
          稿件管理
        </v-btn>
      </div>

      <!-- 稿件类型：编辑已有稿件时锁定，不能切换类型 -->
      <v-tabs
        v-model="show"
        class="upload-page__tabs mb-5"
        color="primary"
        align-tabs="start"
        density="comfortable"
      >
        <v-tab :value="0" :disabled="isEdit" prepend-icon="mdi-movie-open-outline">视频</v-tab>
        <v-tab :value="2" :disabled="isEdit" prepend-icon="mdi-file-document-outline">文章</v-tab>
      </v-tabs>

      <div class="upload-page__body d-flex flex-column flex-lg-row ga-6">
        <!-- 左：表单 -->
        <div class="upload-page__form">
          <UploadVideo v-if="show === 0" @checklist="checklist = $event" />
          <UploadText v-if="show === 2" @checklist="checklist = $event" />
        </div>

        <!-- 右：发布检查清单 + 投稿须知 -->
        <aside class="upload-page__aside">
          <v-card class="upload-checklist rounded-lg" elevation="2">
            <div class="d-flex align-center ga-2 px-4 pt-4 pb-3">
              <v-icon icon="mdi-clipboard-list" color="primary" size="20" />
              <span class="text-subtitle-1 font-weight-bold flex-grow-1">发布检查清单</span>
              <v-chip
                size="x-small"
                variant="tonal"
                :color="allDone ? 'success' : 'warning'"
              >
                {{ doneCount }}/{{ checklist.length }}
              </v-chip>
            </div>
            <v-divider />
            <div class="py-1">
              <div
                v-for="item in checklist"
                :key="item.key"
                class="upload-checklist__item d-flex align-center ga-3 px-4 py-2"
              >
                <v-icon
                  size="18"
                  :icon="item.done ? 'mdi-check-circle' : item.optional ? 'mdi-image-outline' : 'mdi-checkbox-blank-circle-outline'"
                  :color="item.done ? 'success' : ''"
                  :class="item.done ? '' : 'upload-checklist__icon--pending'"
                />
                <span
                  class="upload-checklist__label text-body-2"
                  :class="item.done ? '' : 'text-medium-emphasis'"
                >
                  {{ item.label }}
                </span>
                <span class="upload-checklist__state text-caption" :class="stateClass(item)">
                  {{ stateText(item) }}
                </span>
              </div>
              <div v-if="!checklist.length" class="text-caption text-medium-emphasis px-4 py-3">
                正在读取稿件状态…
              </div>
            </div>
          </v-card>

          <v-card class="upload-tips mt-4 rounded-lg" elevation="2">
            <div class="d-flex align-center ga-2 px-4 pt-4 pb-2">
              <v-icon icon="mdi-bookmark-check-outline" size="20" />
              <span class="text-subtitle-1 font-weight-bold">投稿须知</span>
            </div>
            <v-divider />
            <ul class="upload-tips__list">
              <li>稿件需要审核，通过后才出现在公开列表</li>
              <li>封面建议 16:9、2MB 以内，支持 PNG / JPG / BMP</li>
              <li>标题与标签越准确，越容易被搜索到</li>
              <li v-if="show === 2">加密段落必须设置访问密码，否则无法投稿</li>
            </ul>
          </v-card>
        </aside>
      </div>
    </div>
  </v-container>
</template>

<script>
import UploadVideo from '@/components/upload/UploadVideo.vue'
import UploadText from '@/components/upload/UploadText.vue'

export default {
  name: 'UploadView',
  components: {
    UploadVideo,
    UploadText,
  },
  data() {
    return {
      show: 0,
      editCode: -1,
      // 由当前稿件表单通过 checklist 事件上报，页面只负责展示
      checklist: [],
    }
  },
  computed: {
    isEdit() {
      return this.editCode !== -1
    },
    doneCount() {
      return this.checklist.filter((item) => item.done).length
    },
    allDone() {
      return this.checklist.length > 0 && this.checklist.every((item) => item.done || item.optional)
    },
  },
  created() {
    // show: 0 视频 / 2 文章；edit: 稿件 id
    this.show = parseInt(this.$route.query.show) === 2 ? 2 : 0
    this.editCode = parseInt(this.$route.query.edit) || -1
  },
  methods: {
    /** 清单项右侧状态：占位宽度固定，避免每行右边缘忽长忽短 */
    stateText(item) {
      if (item.done) {
        return '已完成'
      }
      return item.optional ? '可选' : '待完成'
    },
    stateClass(item) {
      return item.done ? 'text-success' : 'text-medium-emphasis'
    },
  },
}
</script>

<style scoped>
/* 表单列吃掉全部剩余宽度：再给 max-width 就会在右侧留出一条空白带 */
.upload-page__form {
  flex: 1 1 auto;
  min-width: 0;
}

.upload-page__aside {
  flex: 1 1 auto;
  width: 100%;
  min-width: 0;
}

/* 宽屏时右侧清单跟随滚动，填表过程中随时能看到缺什么 */
@media (min-width: 1280px) {
  .upload-page__aside {
    flex: 0 0 340px;
    width: auto;
    align-self: flex-start;
    position: sticky;
    top: calc(var(--app-header-height) + 16px);
  }
}

.upload-page__tabs {
  border-bottom: 1px solid rgb(var(--v-theme-outline));
}

.upload-checklist__item + .upload-checklist__item {
  border-top: 1px solid rgba(var(--v-theme-on-surface), 0.06);
}

.upload-checklist__label {
  flex: 1 1 auto;
  min-width: 0;
}

.upload-checklist__state {
  flex: 0 0 auto;
  min-width: 3.5em;
  text-align: right;
  white-space: nowrap;
}

.upload-checklist__icon--pending {
  color: rgba(var(--v-theme-on-surface), var(--v-disabled-opacity));
}

.upload-tips__list {
  margin: 0;
  padding: 16px 24px 20px 32px;
  list-style: disc;
}

.upload-tips__list li {
  margin-bottom: 6px;
  font-size: 0.8125rem;
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
  line-height: 1.5;
}
</style>
