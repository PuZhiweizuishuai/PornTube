<template>
  <div class="upload-form">
    <!-- 视频文件 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-movie-open-outline"
      title="视频文件"
      hint="上传成功后会自动用文件名填充标题"
      :done="!!article.video.id"
    >
      <template #actions>
        <v-btn
          v-if="!showUploadFlie"
          size="small"
          variant="tonal"
          prepend-icon="mdi-repeat"
          @click="changeVideo"
        >
          更换视频
        </v-btn>
      </template>

      <FilePondUpdate
        v-if="showUploadFlie"
        ref="videoUploadTool"
        @video="videoUploadSuccess"
        @file-selected="fileAdd"
      />

      <div v-else class="video-file d-flex align-center ga-3 pa-3 rounded-lg">
        <v-avatar color="success" variant="tonal" size="40" rounded="lg">
          <v-icon icon="mdi-video-box" size="22" />
        </v-avatar>
        <div class="flex-grow-1" style="min-width: 0">
          <div class="text-body-2 font-weight-medium text-truncate">
            {{ article.video.fileOriginalName || '视频文件' }}
          </div>
          <div class="d-flex align-center flex-wrap ga-2 mt-2">
            <v-chip size="x-small" variant="tonal" prepend-icon="mdi-clock-outline">
              {{ (article.video.duration || 0).toFixed(1) }} 秒
            </v-chip>
            <v-chip size="x-small" variant="tonal" prepend-icon="mdi-resize">
              {{ article.video.width || 0 }}x{{ article.video.height || 0 }}
            </v-chip>
            <v-chip size="x-small" variant="tonal" prepend-icon="mdi-harddisk">
              {{ formatFileSize(article.video.size || 0) }}
            </v-chip>
          </div>
        </div>
      </div>
    </UploadSectionCard>

    <!-- 封面图 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-image-outline"
      title="封面图"
      hint="必填。视频卡片的封面，建议 16:9、2MB 以内"
      :done="!!article.imgUrl"
    >
      <CoverUploadPanel
        ref="coverPanel"
        v-model:img-url="article.imgUrl"
        v-model:image-id="article.imageId"
        empty-message="请先选择视频封面，然后上传！"
        @notify="showMessage"
      >
        <template #extra>
          <v-btn
            class="mb-3"
            block
            variant="tonal"
            color="primary"
            prepend-icon="mdi-image-frame"
            :disabled="!videoFile"
            @click="openThumbnailCapture"
          >
            {{ videoFile ? '从视频截取封面' : '上传视频后可截取封面' }}
          </v-btn>
        </template>
      </CoverUploadPanel>
    </UploadSectionCard>

    <!-- 基本信息 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-information-outline"
      title="基本信息"
      hint="分区决定稿件出现在哪个频道，标题和简介会展示在视频卡片上"
      :done="basicDone"
    >
      <div class="d-flex ga-4 flex-wrap">
        <v-select
          v-model="selectedMainCategory"
          class="flex-grow-1"
          style="min-width: 220px"
          :items="category"
          label="主分区"
          placeholder="请选择主分区"
          prepend-inner-icon="mdi-shape-outline"
          @update:model-value="getMainCategory"
        ></v-select>
        <v-select
          v-model="selectedSubCategory"
          class="flex-grow-1"
          style="min-width: 220px"
          :items="categoryChildren"
          label="子分区"
          placeholder="请选择子分区"
          prepend-inner-icon="mdi-tag-outline"
          :disabled="!categoryChildren.length"
          @update:model-value="getVideoCategory"
        ></v-select>
      </div>

      <v-text-field
        v-model="article.title"
        class="mt-4"
        placeholder="为你的视频添加一个吸引人的标题"
        label="标题"
        counter="100"
        :rules="[(v) => !!v || '标题不能为空', (v) => (v && v.length <= 100) || '标题不能超过100个字符']"
        prepend-inner-icon="mdi-format-title"
      ></v-text-field>

      <v-textarea
        v-model="article.describe"
        label="简介"
        auto-grow
        rows="5"
        counter="1000"
        placeholder="填写更全面的视频信息，让更多人找到你的视频"
        prepend-inner-icon="mdi-text-box-outline"
        :rules="[(v) => !v || v.length <= 1000 || '简介不能超过1000个字符']"
      ></v-textarea>
    </UploadSectionCard>

    <!-- 标签 -->
    <UploadSectionCard
      class="mb-4"
      icon="mdi-tag-multiple-outline"
      title="标签"
      hint="最多 6 个，标签越准确越容易被搜到"
      :done="tagDone"
    >
      <v-combobox
        v-model="article.tag"
        label="标签"
        placeholder="添加标签让更多人找到你的视频（最多6个）"
        multiple
        chips
        closable-chips
        prepend-inner-icon="mdi-tag-multiple-outline"
        :rules="[
          (v) => (v && v.length > 0) || '至少添加一个标签',
          (v) => !v || v.length <= 6 || '最多只能添加6个标签',
        ]"
      ></v-combobox>
    </UploadSectionCard>

    <UploadSubmitBar
      :missing="missingLabels"
      :ready="isFormValid"
      :submitting="isSubmitting"
      :submit-text="editCode !== -1 ? '保存修改' : '立即投稿'"
      @submit="publis"
    />

    <!-- 视频缩略图截取弹窗 -->
    <VideoThumbnailCapture
      :file="videoFile"
      :show="showThumbnailCapture"
      @close="showThumbnailCapture = false"
      @thumbnail-selected="thumbnailSelected"
    />

    <!-- 消息提示：统一由 useMessage + AppSnackbar 承载 -->
    <AppSnackbar :state="toast" />
  </div>
</template>

<script>
import FilePondUpdate from '@/components/upload/FilepondUpload.vue'
import VideoThumbnailCapture from '@/components/upload/VideoThumbnailCapture.vue'
import UploadSectionCard from '@/components/upload/UploadSectionCard.vue'
import CoverUploadPanel from '@/components/upload/CoverUploadPanel.vue'
import UploadSubmitBar from '@/components/upload/UploadSubmitBar.vue'
import StringUtils from '@/utils/string-utils'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useMessage } from '@/composables/useMessage'
export default {
  components: {
    FilePondUpdate,
    VideoThumbnailCapture,
    UploadSectionCard,
    CoverUploadPanel,
    UploadSubmitBar,
    AppSnackbar,
  },
  emits: ['checklist'],
  setup() {
    const { toast, showMessage, showSuccessMessage, showErrorMessage } = useMessage()
    return { toast, showMessage, showSuccessMessage, showErrorMessage }
  },
  data() {
    return {
      article: {
        title: '',
        describe: '',
        imgUrl: '',
        category: -1,
        tag: [],
        video: {},
        imageId: '',
      },
      categoryMap: {
        Set: function (key, value) {
          this[key] = value
        },
        Get: function (key) {
          return this[key]
        },
        Contains: function (key) {
          return this.Get(key) != null
        },
        Remove: function (key) {
          delete this[key]
        },
      },
      category: [],
      categoryChildren: [],
      selectedMainCategory: null,
      selectedSubCategory: null,
      nowCategory: {},
      isSubmitting: false,
      videoFile: null,
      showThumbnailCapture: false,
      editCode: -1,
      showUploadFlie: true,
    }
  },
  computed: {
    basicDone() {
      return this.article.category !== -1 && !!(this.article.title || '').trim()
    },
    tagDone() {
      return this.article.tag.length > 0 && this.article.tag.length <= 6
    },
    isFormValid() {
      return (
        !!this.article.video.id &&
        this.basicDone &&
        this.tagDone &&
        !!this.article.imgUrl
      )
    },
    /** 右侧「发布检查清单」的数据源 */
    checklist() {
      return [
        { key: 'video', label: '上传视频', done: !!this.article.video.id },
        { key: 'cover', label: '上传封面', done: !!this.article.imgUrl },
        { key: 'category', label: '选择分区', done: this.article.category !== -1 },
        { key: 'title', label: '填写标题', done: !!(this.article.title || '').trim() },
        { key: 'tag', label: '添加标签', done: this.tagDone },
      ]
    },
    missingLabels() {
      return this.checklist.filter((item) => !item.done && !item.optional).map((item) => item.label)
    },
  },
  watch: {
    checklist: {
      immediate: true,
      deep: true,
      handler(value) {
        this.$emit('checklist', value)
      },
    },
  },
  created() {
    this.getCategory()
    // 判断编辑状态
    this.editCode = parseInt(this.$route.query.edit) || -1

    if (this.editCode && this.editCode !== -1) {
      this.editVideo()
    }
  },
  methods: {
    editVideo() {
      this.httpGet(`/article/edit/${this.editCode}`, (json) => {
        if (json.data != null) {
          // 回显数据
          this.showUploadFlie = false
          const editData = json.data

          // 基础信息回显
          this.article.id = editData.id
          this.article.title = editData.title
          this.article.describe = editData.describes
          this.article.imgUrl = editData.imgUrl
          this.article.imageId = editData.imageId
          this.article.tag = [...editData.tag]

          // 处理视频数据
          if (editData.video && editData.video.length > 0) {
            this.article.video = editData.video[0]
          }

          // 处理分类数据
          if (editData.fatherCategory) {
            // 设置主分区
            this.selectedMainCategory = editData.fatherCategory.name
            this.nowCategory = editData.fatherCategory

            // 加载子分区列表
            this.getMainCategory(editData.fatherCategory.name)

            // 设置子分区（延迟执行确保子分区列表已加载）
            if (editData.childrenCategory) {
              this.selectedSubCategory = editData.childrenCategory.name
              this.article.category = editData.childrenCategory.id
            } else {
              this.article.category = editData.fatherCategory.id
            }
          }

          this.showSuccessMessage('稿件数据加载成功，可以进行编辑')
        } else {
          this.showErrorMessage('无法加载稿件数据')
          this.$router.push('/studio/upload')
        }
      })
    },
    fileAdd(file) {
      // 保存视频文件供缩略图截取使用
      try {
        // 检查是否是FilePond的File Item对象
        if (file && file.file && file.file instanceof File) {
          // FilePond返回的是一个Item，真正的文件在file属性中
          this.videoFile = file.file
        } else if (file instanceof File) {
          // 直接是File对象
          this.videoFile = file
        }
      } catch (error) {
        console.error('处理文件时出错:', error)
      }
    },

    // 打开视频截图组件
    openThumbnailCapture() {
      if (!this.videoFile) {
        this.showErrorMessage('请先上传视频文件')
        return
      }

      // 确保视频文件类型正确
      if (this.videoFile instanceof File) {
        const fileType = this.videoFile.type
        if (!fileType.startsWith('video/')) {
          this.showErrorMessage('选择的文件不是视频格式')
          return
        }
      }

      this.showThumbnailCapture = true
    },
    thumbnailSelected(file) {
      if (file) {
        // 截取到的画面直接交给封面组件上传
        this.$refs.coverPanel.upload(file)
      }
    },

    // 重新选择视频文件
    changeVideo() {
      this.showUploadFlie = true
      this.article.video = {}
      this.videoFile = null
    },

    publis() {
      if (!this.article.video.id) {
        this.showErrorMessage('你还没有上传视频')
        return
      }
      if (
        !this.article.title ||
        !this.article.imgUrl ||
        this.article.tag.length === 0 ||
        this.article.category === -1
      ) {
        this.showErrorMessage('标题，封面，标签，分区不能为空')
        return
      }
      if (this.article.tag.length > 6) {
        this.showErrorMessage('标签不能超过6个')
        return
      }

      this.isSubmitting = true

      // 根据是否为编辑模式选择不同的API端点
      const apiEndpoint =
        this.editCode && this.editCode !== -1 ? `/article/video/update` : '/article/video'

      this.httpPost(apiEndpoint, this.article, (json) => {
        this.isSubmitting = false

        if (json.status === 200) {
          this.showSuccessMessage(
            this.editCode !== -1 ? '更新成功' : '投稿成功，等待审核通过后你就可以看到你的视频了'
          )
          this.$router.push('/studio')
        } else if (json.status === 4002) {
          this.showErrorMessage(StringUtils.dataErrorMessage(json.data))
        } else {
          this.showErrorMessage(json.message || '提交失败')
        }
      })
    },

    formatFileSize(bytes) {
      if (bytes === 0) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    videoUploadSuccess(value) {
      if (value.status === 200) {
        // 设置封面图和视频
        for (let i = 0; i < value.data.length; i++) {
          if (value.data[i].type === 5) {
            continue
          }
          if (value.data[i].type === 0) {
            this.article.video = value.data[i]
            this.setTitle(value.data[i].fileOriginalName)

            // 尝试从FilePond获取原始视频文件
            try {
              const filepondInstance = this.$refs.videoUploadTool.$refs.pond
              if (filepondInstance && filepondInstance.getFiles().length > 0) {
                const fileItem = filepondInstance.getFiles()[0]
                if (fileItem && fileItem.file instanceof File) {
                  this.videoFile = fileItem.file
                }
              }
            } catch (error) {
              console.warn('无法获取原始视频文件:', error)
            }
          }
        }
        this.showUploadFlie = false
        this.showSuccessMessage('上传视频成功')
      } else {
        this.showErrorMessage(
          '上传出现异常，请重试！' + (value.message || '') + ' ' + (value.data || '')
        )
        this.$refs.videoUploadTool.handleFilePondInit()
      }
    },

    setTitle(title) {
      if (!this.article.title) {
        if (title.length > 50) {
          this.article.title = title.substring(0, 50)
        } else {
          this.article.title = title
        }
      }
    },

    getCategory() {
      this.httpGet('/category/list', (json) => {
        for (let i = 0; i < json.data.length; i++) {
          const name = json.data[i].name
          this.category.push(name)
          this.categoryMap.Set(name, json.data[i])
        }
      })
    },

    getMainCategory(value) {
      this.categoryChildren = []
      this.selectedSubCategory = null
      this.nowCategory = this.categoryMap.Get(value)
      this.article.category = this.nowCategory.id

      const c = this.nowCategory.children
      if (c) {
        for (let i = 0; i < c.length; i++) {
          this.categoryChildren.push(c[i].name)
        }
      }
    },

    getVideoCategory(value) {
      const c = this.nowCategory.children
      for (let i = 0; i < c.length; i++) {
        if (c[i].name === value) {
          this.article.category = c[i].id
        }
      }
    },
  },
}
</script>

<style scoped>
.video-file {
  border: 1px solid rgb(var(--v-theme-outline));
  background-color: rgb(var(--v-theme-surface-sunken));
}
</style>
