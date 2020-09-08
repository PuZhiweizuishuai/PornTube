<template>
  <div>

    <file-pond
      ref="pond"
      name="file[]"
      label-idle="选择图片或拖动图片到此处"
      label-file-processing="图片正在上传，请稍后"
      label-file-processing-aborted="图片上传被取消"
      label-tap-to-retry="尝试重试"
      label-file-processing-complete="图片上传成功！"
      :allow-multiple="false"
      accepted-file-types="image/png, image/jpeg, image/jpg, image/gif"
      :server="server"
      :files="myFiles"
      :instant-upload="false"
      :image-edit-editor="editor"
      :image-edit-instant-edit="true"
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
import 'filepond-plugin-image-edit/dist/filepond-plugin-image-edit.css'
// Import image preview and file type validation plugins
import FilePondPluginFileValidateType from 'filepond-plugin-file-validate-type'
import FilePondPluginImageEdit from 'filepond-plugin-image-edit'
import FilePondPluginImagePreview from 'filepond-plugin-image-preview'
import FilePondPluginImageExifOrientation from 'filepond-plugin-image-exif-orientation'
import FilePondPluginFileValidateSize from 'filepond-plugin-file-validate-size'
// Create component
const FilePond = vueFilePond(
  FilePondPluginFileValidateType,
  FilePondPluginImagePreview,
  FilePondPluginImageExifOrientation,
  FilePondPluginFileValidateSize,
  FilePondPluginImageEdit
)

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
      },
      editor: {

        // Called by FilePond to edit the image
        // - should open your image editor
        // - receives file object and image edit instructions
        open: (file, instructions) => {
        // open editor here
        },

        // Callback set by FilePond
        // - should be called by the editor when user confirms editing
        // - should receive output object, resulting edit information
        onconfirm: (output) => {},

        // Callback set by FilePond
        // - should be called by the editor when user cancels editing
        oncancel: () => {},

        // Callback set by FilePond
        // - should be called by the editor when user closes the editor
        onclose: () => {}
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
      this.$emit('photo', videMessage)
    }
  }
}
</script>
