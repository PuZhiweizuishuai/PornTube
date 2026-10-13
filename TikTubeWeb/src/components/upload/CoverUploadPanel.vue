<template>
  <div class="cover-panel d-flex flex-column flex-sm-row ga-4">
    <div class="cover-panel__preview rounded-lg overflow-hidden">
      <v-img
        v-if="preview"
        :src="preview"
        :aspect-ratio="16 / 9"
        cover
        :alt="alt"
        class="cover-panel__img"
      />
      <div v-else class="cover-panel__empty">
        <v-icon icon="mdi-image-outline" size="40" class="mb-2" />
        <span class="text-body-2">{{ emptyText }}</span>
      </div>

      <!-- 换封面前得先能把当前封面摘掉，否则清空只能靠刷新页面重来 -->
      <v-btn
        v-if="clearable && preview"
        class="cover-panel__remove"
        icon="mdi-close"
        size="small"
        variant="flat"
        color="error"
        :disabled="uploading"
        @click="clearCover"
      >
        <v-tooltip activator="parent" location="top">{{ removeText }}</v-tooltip>
      </v-btn>
    </div>

    <div class="cover-panel__controls flex-grow-1">
      <slot name="extra" />

      <v-file-input
        v-model="file"
        :rules="rules"
        accept="image/png, image/jpeg, image/bmp"
        placeholder="选择图片文件"
        prepend-icon="mdi-camera"
        :label="label"
        density="comfortable"
        variant="outlined"
        color="primary"
        chips
        show-size
        clearable
        @update:model-value="onFileChange"
      />

      <v-btn
        block
        color="primary"
        class="mt-2"
        :loading="uploading"
        :disabled="!file || uploading"
        @click="upload(file)"
      >
        <v-icon icon="mdi-cloud-upload" class="mr-1" />
        {{ uploadText }}
      </v-btn>

      <div class="text-caption text-medium-emphasis mt-2">{{ hint }}</div>
    </div>
  </div>
</template>

<script>
/**
 * 投稿封面组件：16:9 预览 + 选文件 + 上传，视频稿与文章稿共用。
 *
 * 上传结果通过 v-model 回写给父组件的 article.imgUrl / article.imageId，
 * 提示交给父组件的 useMessage（这里只 emit notify，避免出现第二个 snackbar）。
 */
import { uploadCover } from '@/utils/upload'

/** 封面大小上限，与后端 /api/upload/photo、/api/upload/article/cover 的校验保持一致 */
const MAX_COVER_SIZE = 2 * 1024 * 1024

export default {
  name: 'CoverUploadPanel',
  props: {
    imgUrl: { type: String, default: '' },
    endpoint: { type: String, default: '/upload/photo' },
    label: { type: String, default: '自定义封面' },
    uploadText: { type: String, default: '上传封面' },
    emptyText: { type: String, default: '暂无封面' },
    alt: { type: String, default: '封面图预览' },
    hint: { type: String, default: '建议使用 16:9 的图片，大小 2MB 以内，支持 PNG / JPG / BMP' },
    emptyMessage: { type: String, default: '请先选择封面，然后上传！' },
    /** 允许移除封面（视频稿件必须有封面，故默认关闭） */
    clearable: { type: Boolean, default: false },
    removeText: { type: String, default: '移除封面' },
  },
  emits: ['update:imgUrl', 'update:imageId', 'notify'],
  data() {
    return {
      file: null,
      uploading: false,
      // 选中但还没上传的图片预览（blob），让用户看得到自己刚选的文件
      blobUrl: '',
      rules: [
        (value) => !value || value.size < MAX_COVER_SIZE || '封面图大小不能超过 2MB',
      ],
    }
  },
  computed: {
    preview() {
      return this.blobUrl || this.imgUrl
    },
  },
  beforeUnmount() {
    this.revoke()
  },
  methods: {
    onFileChange(value) {
      this.revoke()
      if (!value) return
      if (value.size >= MAX_COVER_SIZE) {
        this.file = null
        this.$emit('notify', '封面图大小不能超过 2MB', 'error')
        return
      }
      this.blobUrl = URL.createObjectURL(value)
    },
    revoke() {
      if (!this.blobUrl) return
      URL.revokeObjectURL(this.blobUrl)
      this.blobUrl = ''
    },
    /** 清掉已选文件与已上传封面，父组件保存后后端会把旧封面文件退回临时区 */
    clearCover() {
      this.file = null
      this.revoke()
      this.$emit('update:imageId', null)
      this.$emit('update:imgUrl', '')
    },
    /**
     * 上传封面。parentFile 供「从视频截取封面」这类外部来源直接调用，
     * 此时文件不经过选框，预览仍然走 blob。
     */
    async upload(parentFile) {
      const target = parentFile || this.file
      if (!target) {
        this.$emit('notify', this.emptyMessage, 'error')
        return
      }
      if (target.size >= MAX_COVER_SIZE) {
        this.$emit('notify', '封面图大小不能超过 2MB', 'error')
        return
      }
      this.uploading = true
      try {
        const { imageId, imgUrl } = await uploadCover([target], {
          endpoint: this.endpoint,
          emptyMessage: this.emptyMessage,
        })
        this.$emit('update:imageId', imageId)
        this.$emit('update:imgUrl', imgUrl)
        this.file = null
        this.revoke()
        this.$emit('notify', '封面上传成功', 'success')
      } catch (error) {
        this.$emit('notify', error?.message || '封面上传失败', 'error')
      } finally {
        this.uploading = false
      }
    },
  },
}
</script>

<style scoped>
.cover-panel__preview {
  position: relative;
  flex: 0 0 auto;
  width: 100%;
  max-width: 320px;
  border: 1px solid rgb(var(--v-theme-outline));
  background-color: rgb(var(--v-theme-surface-variant));
}

.cover-panel__remove {
  position: absolute;
  top: 6px;
  right: 6px;
  opacity: 0.92;
}

.cover-panel__remove:hover {
  opacity: 1;
}

.cover-panel__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  aspect-ratio: 16 / 9;
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
}

.cover-panel__controls {
  min-width: 0;
}

@media (max-width: 600px) {
  .cover-panel__preview {
    max-width: none;
  }
}
</style>
