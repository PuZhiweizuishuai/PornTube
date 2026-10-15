<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center">
      <v-icon start>mdi-account-circle-outline</v-icon>
      头像
    </v-card-title>
    <v-divider />

    <v-card-text class="pt-4">
      <div class="d-flex justify-center my-4">
        <v-avatar size="128" class="elevation-4">
          <v-img :src="userInfo.avatarUrl" cover :aspect-ratio="1">
            <template #placeholder>
              <div class="d-flex align-center justify-center fill-height">
                <v-progress-circular indeterminate color="primary" size="28"></v-progress-circular>
              </div>
            </template>
          </v-img>
        </v-avatar>
      </div>

      <v-row align="center">
        <v-col cols="12" sm="9">
          <v-file-input
            accept="image/png, image/jpeg, image/bmp"
            placeholder="选择头像"
            prepend-icon="mdi-camera"
            label="头像图片"
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
        :disabled="userInfo.fileId === undefined || uploading"
      >
        保存
      </v-btn>
      <v-spacer />
      <span class="text-caption text-medium-emphasis">上传后需点击保存完成设置</span>
    </v-card-actions>

    <!-- 图片剪裁对话框 -->
    <v-dialog v-model="showCropper" persistent max-width="600px">
      <v-card rounded="lg">
        <v-card-title class="d-flex align-center">
          <v-icon start>mdi-crop</v-icon>
          剪裁头像
        </v-card-title>
        <v-divider />
        <v-card-text>
          <div class="cropper-container">
            <vue-cropper
              ref="cropper"
              :img="cropImg"
              :outputSize="1"
              :outputType="'png'"
              :info="true"
              :full="false"
              :canMove="true"
              :canMoveBox="true"
              :original="false"
              :autoCrop="true"
              :autoCropWidth="300"
              :autoCropHeight="300"
              :fixedBox="true"
              :fixedNumber="[1, 1]"
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
  name: 'UserHeadSetting',
  components: {
    VueCropper,
  },
  data() {
    return {
      userInfo: {
        username: '',
        avatarUrl: '',
      },
      files: [],
      //rules: [(value) => !value || value.size < 2000000 || '头像大小必须在2MB以内！'],
      showMessage: false,
      message: '',
      user: null,
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
    this.user = useUserStore()
    this.userInfo = this.user.userData
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
          this.userInfo.avatarUrl = e.target.result
        }
        reader.readAsDataURL(blob)
      })
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

      this.uploadFiles('/upload/avatar', formData, (json) => {
        this.uploading = false
        if (json.status === 200) {
          this.userInfo.avatarUrl = json.data[0].fileUrl
          this.userInfo.fileId = json.data[0].id
          this.message = '上传成功，请点击保存，保存头像设置！'
          this.showMessage = true
        } else {
          this.message = '上传失败，请重试！' + json.message
          this.showMessage = true
        }
      })
    },
    save() {
      if (this.userInfo.avatarUrl.startsWith('data:image/png;base64')) {
        this.message = '请先上传图片，然后保存！'
        this.showMessage = true
        return
      }
      this.httpPost('/user/update/avatar', this.userInfo, (json) => {
        if (json.status === 200) {
          
          this.user.setUserData(this.userInfo)
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
  