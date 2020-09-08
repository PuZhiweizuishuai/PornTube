<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card
        class="mx-auto"
        outlined
      >
        <v-row justify="center">
          <v-col cols="10">
            <h2>首页顶部大图修改</h2>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">

            <v-img :src="userInfo.topImgUrl" :aspect-ratio="5.98" />

          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-file-input
              :rules="rules"
              accept="image/png, image/jpeg, image/bmp"
              placeholder="选择图片"
              prepend-icon="mdi-camera"
              label="首页顶部大图"
              @change="setFile"
            />
            <v-btn color="primary" @click="uploadFile">
              上传
            </v-btn>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-btn color="primary" @click="save">
              保存
            </v-btn>
          </v-col>
        </v-row>
      </v-card>
    </v-col>
    <v-snackbar
      v-model="showMessage"
      :top="true"
      :timeout="3000"
    >
      {{ message }}

      <template v-slot:action="{ attrs }">
        <v-btn
          color="pink"
          text
          v-bind="attrs"
          @click="showMessage = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </v-row>
</template>

<script>

export default {
  name: 'UserTopSetting',
  data() {
    return {
      userInfo: {
        username: ''
      },
      files: [],
      rules: [
        value => !value || value.size < 2000000 || '图片大小必须在2MB以内！'
      ],
      showMessage: false,
      message: ''
    }
  },
  created() {
    this.userInfo = this.$store.state.userInfo
  },
  methods: {
    setFile(value) {
      this.files = []
      this.files.push(value)
    },
    uploadFile() {
      if (this.files.length === 0) {
        this.message = '请先选择图片，然后上传！'
        this.showMessage = true
        return
      }
      const formData = new FormData()
      for (let i = 0; i < this.files.length; i++) {
        formData.append('file[]', this.files[i])
      }
      fetch(`/api/upload/top`, {
        headers: {
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: formData
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            //
            this.userInfo.topImgUrl = json.data[0].fileUrl
            this.message = '上传成功，请点击保存，保存设置！'
            this.showMessage = true
          } else {
            this.message = '上传失败，请重试！' + json.message
            this.showMessage = true
          }
        })
        .catch(e => {
          return null
        })
    },
    save() {
      fetch(`/api/user/update/top`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(this.userInfo)
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            this.$store.commit('setUserInfo', this.userInfo)
            this.message = '保存成功！'
            this.showMessage = true
          } else {
            //
            this.message = '保存失败！' + json.message
            this.showMessage = true
          }
        })
        .catch(e => {
          return null
        })
    }
  }
}
</script>

<style>

</style>
