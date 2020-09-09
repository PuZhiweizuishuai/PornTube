<template>
  <div>

    <file-pond
      ref="pond"
      name="file[]"
      label-idle="选择视频或者拖动视频到此处"
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
    />
    <!-- :instant-upload="false" 关闭立即上传到服务器 -->
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
    FilePond
  },
  videMessage,
  data() {
    return {
      videMessage: {},
      myFiles: [],
      server: {
        url: '/api/upload/video',
        process: {
          headers: {
            'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
          },
          onload(response) {
            // 返回上传数据
            videMessage = JSON.parse(response)
          }
        }
      }
    }
  },
  created() {
  },
  methods: {
    handleFilePondInit() {
      // console.log('FilePond has initialized')
      // FilePond instance methods are available on `this.$refs.pond`
    },
    success() {
      this.$emit('video', videMessage)
    }
  }
}
</script>
