<template>
  <div class="video-dropzone">
    <file-pond
      ref="pond"
      name="file[]"
      label-idle="点击选择视频文件，或直接拖到这里"
      label-file-processing="视频正在上传，请稍后"
      label-file-processing-aborted="视频上传被取消"
      label-tap-to-retry="尝试重试"
      label-file-processing-complete="视频上传成功！"
      :allow-multiple="false"
      accepted-file-types="video/*, .flv, .mkv"
      :server="server"
      :files="myFiles"
      @init="handleFilePondInit"
      @processfile="success"
      @addfile="handleFileAdded"
      :instant-upload="false"
    />
    <!-- :instant-upload="false" 关闭立即上传到服务器 -->
    <div class="video-dropzone__hint text-caption text-medium-emphasis mt-2">
      支持 MP4 / FLV / MKV，单个文件；上传完成后再填写下方信息
    </div>
  </div>
</template>

  <script>
// Import Vue FilePond
import vueFilePond from 'vue-filepond'

// Import FilePond styles
import 'filepond/dist/filepond.min.css'

// Import FilePond plugins
// Please note that you need to install these plugins separately

// Import image preview plugin styles
import 'filepond-plugin-image-preview/dist/filepond-plugin-image-preview.min.css'

// Import image preview and file type validation plugins
import FilePondPluginFileValidateType from 'filepond-plugin-file-validate-type'
import FilePondPluginImagePreview from 'filepond-plugin-image-preview'

// Create component
const FilePond = vueFilePond(FilePondPluginFileValidateType, FilePondPluginImagePreview)

let videMessage = {}

export default {
  name: 'FilePondUpdate',
  components: {
    FilePond,
  },
  videMessage,
  data() {
    return {
      videMessage: {},
      myFiles: [],
      server: {
        url: '/api/upload/video',
        process: {
          headers: {},
          onload(response) {
            // 返回上传数据
            videMessage = JSON.parse(response)
          },
        },
      },
    }
  },
  created() {},
  methods: {
    handleFilePondInit() {
      this.myFiles = []
      console.log('FilePond has initialized')
      // FilePond instance methods are available on `this.$refs.pond`
    },
    success() {
      this.$emit('video', videMessage)
    },
    handleFileAdded(error, file) {
      if (!error) {
        // 文件被添加时，向父组件发送文件信
        this.$emit('file-selected', file)
      }
    },
  },
}
</script>

<style>
/**
 * FilePond 的 DOM 由库自己创建，scoped 选择器拿不到它的内部元素，
 * 所以这里用 .video-dropzone 做命名空间的全局覆盖，把默认的蓝色面板
 * 换成站内主题的虚线拖放区（浅色 / 深色都走 --v-theme-* 变量）。
 */
.video-dropzone .filepond--root {
  margin-bottom: 0;
  font-family: inherit;
}

.video-dropzone .filepond--panel-root {
  background-color: rgb(var(--v-theme-surface-sunken));
  border: 1px dashed rgb(var(--v-theme-outline));
  border-radius: var(--app-radius-md);
  opacity: 1;
}

.video-dropzone .filepond--root:hover .filepond--panel-root {
  border-color: rgb(var(--v-theme-primary));
}

.video-dropzone .filepond--drop-label {
  min-height: 7.5em;
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
}

.video-dropzone .filepond--drop-label label {
  font-weight: 500;
}

.video-dropzone .filepond--label-action {
  color: rgb(var(--v-theme-primary));
  text-decoration: none;
  font-weight: 600;
}

.video-dropzone .filepond--drip-blob {
  background-color: rgba(var(--v-theme-on-surface), 0.16);
}

.video-dropzone .filepond--file-info,
.video-dropzone .filepond--file-status {
  text-shadow: none;
}

/* 文件项面板：FilePond 默认是深灰底 + 白字 + 白色进度环，
   直接换成站内浅色变量会让进度环「消失」（拖放区 surface-sunken 与 surface-variant
   几乎是同一个灰），所以面板底色、文字色、进度环色都显式接管：
   等待/上传中用中性深一档的灰，成功转绿、失败转红，进度环始终用高饱和色。 */
.video-dropzone .filepond--item-panel {
  background-color: rgba(var(--v-theme-on-surface), 0.1);
  border: 1px solid rgba(var(--v-theme-on-surface), 0.2);
  border-radius: var(--app-radius-sm);
}

.video-dropzone .filepond--file {
  color: rgb(var(--v-theme-on-surface));
}

.video-dropzone .filepond--progress-indicator {
  color: rgb(var(--v-theme-primary));
}

.video-dropzone [data-filepond-item-state='processing-complete'] .filepond--item-panel {
  background-color: rgba(var(--v-theme-success), 0.14);
  border-color: rgba(var(--v-theme-success), 0.45);
}

.video-dropzone [data-filepond-item-state='processing-complete'] .filepond--progress-indicator,
.video-dropzone [data-filepond-item-state='processing-complete'] .filepond--processing-complete-indicator {
  color: rgb(var(--v-theme-success));
}

.video-dropzone [data-filepond-item-state*='error'] .filepond--item-panel,
.video-dropzone [data-filepond-item-state*='invalid'] .filepond--item-panel {
  background-color: rgba(var(--v-theme-error), 0.14);
  border-color: rgba(var(--v-theme-error), 0.45);
}

.video-dropzone [data-filepond-item-state*='error'] .filepond--progress-indicator,
.video-dropzone [data-filepond-item-state*='invalid'] .filepond--progress-indicator {
  color: rgb(var(--v-theme-error));
}

.video-dropzone .filepond--file-action-button {
  color: rgb(var(--v-theme-on-surface));
  background-color: rgb(var(--v-theme-surface));
  border: 1px solid rgb(var(--v-theme-outline));
  box-shadow: none;
}

.video-dropzone .filepond--file-action-button:hover {
  color: rgb(var(--v-theme-primary));
  background-color: rgb(var(--v-theme-surface));
}

.video-dropzone .filepond--credit {
  color: rgba(var(--v-theme-on-surface), var(--v-disabled-opacity)) !important;
}
</style>
