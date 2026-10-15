<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center">
      <v-icon start>mdi-image-outline</v-icon>
      首页顶部图
    </v-card-title>
    <v-divider />

    <v-card-text class="pt-4">
      <v-sheet class="mb-4 rounded overflow-hidden" elevation="1">
        <v-img
          :src="userInfo.userData.topImgUrl"
          :aspect-ratio="5.98"
          cover
          class="bg-grey-lighten-2"
        >
          <template #placeholder>
            <div class="d-flex align-center justify-center fill-height">
              <v-progress-circular indeterminate color="primary" size="28"></v-progress-circular>
            </div>
          </template>
        </v-img>
      </v-sheet>

      <v-row align="center">
        <v-col cols="12" sm="9">
          <v-file-input
            accept="image/png, image/jpeg, image/bmp"
            placeholder="选择图片"
            prepend-icon="mdi-camera"
            label="顶部大图"
            variant="outlined"
            density="comfortable"
            @update:model-value="onFileSelected"
            show-size
            counter
            bg-color="surface"
            hide-details="auto"
          />
        </v-col>
        <v-col cols="12" sm="3">
          <v-btn
            color="primary"
            @click="uploadFile"
            prepend-icon="mdi-cloud-upload"
            :loading="uploading"
            :disabled="files.length === 0 || uploading"
            block
          >
            上传
          </v-btn>
        </v-col>
      </v-row>
    </v-card-text>

    <v-divider />

    <v-card-actions class="pa-4">
      <v-btn
        color="primary"
        @click="save"
        prepend-icon="mdi-content-save"
        :disabled="userInfo.userData.fileId === undefined || uploading"
      >
        保存
      </v-btn>
      <v-spacer />
      <span class="text-caption text-medium-emphasis">上传后需点击保存完成设置</span>
    </v-card-actions>

    <!-- 图片剪裁对话框 -->
    <v-dialog v-model="showCropper" persistent max-width="800px">
      <v-card rounded="lg">
        <v-card-title class="d-flex align-center">
          <v-icon start>mdi-crop</v-icon>
          剪裁顶部大图
        </v-card-title>
        <v-divider />
        <v-card-text>
          <div class="cropper-container">
            <vue-cropper
              ref="cropper"
              :img="cropImg"
              :outputSize="0.9"
              :outputType="'jpg'"
              :info="true"
              :full="true"
              :canMove="true"
              :canMoveBox="true"
              :original="false"
              :autoCrop="true"
              :autoCropWidth="598"
              :autoCropHeight="100"
              :fixedBox="true"
              :fixedNumber="[5.98, 1]"
              :centerBox="true"
              :high="true"
              :infoTrue="true"
              @realTime="realTime"
            />
          </div>
        </v-card-text>
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn color="error" variant="text" @click="cancelCrop"> 取消 </v-btn>
          <v-btn color="primary" @click="confirmCrop"> 确认剪裁 </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="showMessage" location="top" :timeout="3000" color="info">
      {{ message }}

      <template #actions>
        <v-btn color="white" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>
  
<script>
import { useUserStore } from '@/stores/userStore'
import { VueCropper } from 'vue-cropper'
import 'vue-cropper/dist/index.css'

export default {
  name: 'UserTopSetting',
  components: {
    VueCropper,
  },
  data() {
    return {
      userInfo: null,
      files: [],
      //rules: [(value) => !value || value.size < 2000000 || '图片大小必须在2MB以内！'],
      showMessage: false,
      message: '',
      uploading: false,
      // 剪裁相关数据
      showCropper: false,
      cropImg: '',
      cropBlob: null,
      cropInfo: {},
      originalFile: null,
    }
  },
  created() {
    this.userInfo = useUserStore()
  },
  methods: {
    // 文件选择处理
    onFileSelected(value) {
      if (!value) {
        this.files = []
        return
      }

      this.originalFile = value

      // 读取文件并显示剪裁对话框
      const reader = new FileReader()
      reader.onload = (e) => {
        this.cropImg = e.target.result
        this.showCropper = true
      }
      reader.readAsDataURL(value)
    },

    // 剪裁实时数据
    realTime(data) {
      this.cropInfo = data
    },

    // 取消剪裁
    cancelCrop() {
      this.showCropper = false
      this.cropImg = ''
      this.files = []
    },

    // 确认剪裁
    confirmCrop() {
      const cropper = this.$refs.cropper
      const options = {
        maxWidth: 2000,
        maxHeight: 334, // 保持5.98:1的比例
        fillColor: '#fff',
      }

      cropper.getCropBlob((blob) => {
        this.cropBlob = blob

        // 创建一个新的File对象
        const fileName = this.originalFile.name
        const fileType = this.originalFile.type
        const croppedFile = new File([blob], fileName, { type: fileType })

        this.files = [croppedFile]
        this.showCropper = false

        // 预览剪裁后的图片
        const reader = new FileReader()
        reader.onload = (e) => {
          this.userInfo.userData.topImgUrl = e.target.result
        }
        reader.readAsDataURL(blob)
      }, options)
    },

    uploadFile() {
      if (this.files.length === 0) {
        this.message = '请先选择图片，然后上传！'
        this.showMessage = true
        return
      }
      if (this.files[0].size > 2000000) {
        this.message = '图片大小不能超过2MB！'
        this.showMessage = true
        return
      }

      this.uploading = true
      const formData = new FormData()
      for (let i = 0; i < this.files.length; i++) {
        formData.append('file[]', this.files[i])
      }

      this.uploadFiles('/upload/top', formData, (json) => {
        this.uploading = false
        if (json.status === 200) {
          this.userInfo.userData.topImgUrl = json.data[0].fileUrl
          this.userInfo.userData.fileId = json.data[0].id
          this.message = '上传成功，请点击保存，保存设置！'
          this.showMessage = true
        } else {
          this.message = '上传失败，请重试！' + json.message
          this.showMessage = true
        }
      })
    },
    save() {
      if (this.userInfo.userData.topImgUrl.startsWith('data:image/png;base64')) {
        this.message = '请先上传图片，然后保存！'
        this.showMessage = true
        return
      }
      this.httpPost('/user/update/top', this.userInfo.userData, (json) => {
        if (json.status === 200) {
          this.userInfo.setUserData(this.userInfo.userData)
          this.message = '保存成功！'
          this.showMessage = true
        } else {
          this.message = '保存失败！' + json.message
          this.showMessage = true
        }
      })
    },
  },
}
</script>
  
<style scoped>
.cropper-container {
  height: 60vh;
  width: 100%;
  background-color: rgb(var(--v-theme-surface-variant));
  overflow: hidden;
}
</style>
  