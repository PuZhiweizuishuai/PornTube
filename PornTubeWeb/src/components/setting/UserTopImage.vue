<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card class="mx-auto" elevation="2" rounded="lg">
        <v-card-title class="d-flex align-center py-4">
          <v-icon icon="mdi-image" class="mr-2" size="large" color="primary" />
          <h2>首页顶部大图修改</h2>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text>
          <v-row justify="center">
            <v-col cols="12">
              <v-sheet class="mb-4 rounded overflow-hidden" elevation="1">
                <v-img
                  :src="userInfo.userData.topImgUrl"
                  :aspect-ratio="5.98"
                  cover
                  class="bg-grey-lighten-2"
                >
                  <template #placeholder>
                    <div class="d-flex align-center justify-center fill-height">
                      <v-progress-circular indeterminate color="primary"></v-progress-circular>
                    </div>
                  </template>
                </v-img>
              </v-sheet>
            </v-col>
          </v-row>

          <v-row justify="center">
            <v-col cols="12" md="9">
              <v-file-input
                :rules="rules"
                accept="image/png, image/jpeg, image/bmp"
                placeholder="选择图片"
                prepend-icon="mdi-camera"
                label="首页顶部大图"
                variant="outlined"
                density="comfortable"
                @update:model-value="setFile"
                show-size
                counter
                bg-color="surface"
              />
            </v-col>
            <v-col cols="12" md="3" class="d-flex align-center">
              <v-btn
                color="primary"
                @click="uploadFile"
                prepend-icon="mdi-cloud-upload"
                variant="elevated"
                :loading="uploading"
                :disabled="files.length === 0 || uploading"
                block
                class="ml-md-n4"
              >
                上传
              </v-btn>
            </v-col>
          </v-row>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4">
          <v-btn
            color="primary"
            @click="save"
            prepend-icon="mdi-content-save"
            variant="elevated"
            size="large"
            :disabled="userInfo.userData.fileId === undefined || uploading"
          >
            保存
          </v-btn>
          <v-spacer></v-spacer>
          <v-chip color="info" variant="outlined">
            <v-icon start icon="mdi-information"></v-icon>
            上传后需点击保存完成设置
          </v-chip>
        </v-card-actions>
      </v-card>
    </v-col>

    <v-snackbar v-model="showMessage" location="top" :timeout="3000" color="info">
      {{ message }}

      <template #actions>
        <v-btn color="white" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-row>
</template>
  
<script>
import { useUserStore } from '@/stores/userStore'
export default {
  name: 'UserTopSetting',
  data() {
    return {
      userInfo: null,
      files: [],
      rules: [(value) => !value || value.size < 2000000 || '图片大小必须在2MB以内！'],
      showMessage: false,
      message: '',
      uploading: false,
    }
  },
  created() {
    this.userInfo = useUserStore()
  },
  methods: {
    setFile(value) {
      this.files = []
      if (value) {
        this.files.push(value)
      }
    },
    uploadFile() {
      if (this.files.length === 0) {
        this.message = '请先选择图片，然后上传！'
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
  
<style>
.v-card-title {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}
</style>
  