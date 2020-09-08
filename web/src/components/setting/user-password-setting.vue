<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card
        class="mx-auto"
        outlined
      >
        <v-row justify="center">
          <v-col cols="10">
            <h2>密码修改</h2>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-text-field
              v-model="passoword.oldPassword"
              placeholder="原始密码"
              label="原始密码"
              clearable
              type="password"
              :rules="[() => passoword.oldPassword != null || '原始密码不能为空']"
            />
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-text-field
              v-model="passoword.newPassword"
              placeholder="新密码"
              label="新密码"
              clearable
              type="password"
              :rules="[() => passoword.newPassword != null || '新密码不能为空']"
            />
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10">
            <v-text-field
              v-model="temp"
              placeholder="请再输入一遍"
              label="请再输入一遍"
              clearable
              type="password"
              :rules="[() => temp == passoword.newPassword || '两次密码不相同']"
            />
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="5">
            <v-img :src="verifyImageUrl" alt="验证码" title="点击刷新" style="cursor:pointer;" max-width="200" @click="getVerifyImage" />
          </v-col>
          <v-col cols="5">
            <v-text-field
              v-model="passoword.verifyCode"
              placeholder="验证码"
              label="验证码"
              :rules="[() => passoword.verifyCode != null || '验证码不能为空']"
              clearable
            />
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="2">
            <v-btn color="primary" @click="save">保存</v-btn>
          </v-col>
          <v-col cols="8">
            说明： 修改密码后需要重新登录
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
  name: 'UserSetting',
  data() {
    return {
      passoword: {
        oldPassword: '',
        newPassword: '',
        verifyCode: ''
      },
      temp: '',
      verifyImageUrl: '/api/verifyImage',
      showMessage: false,
      message: ''
    }
  },
  created() {
  },
  methods: {
    getVerifyImage() {
      this.verifyImageUrl = '/api/verifyImage?t=' + new Date().getTime()
    },
    save() {
      if (this.passoword.oldPassword === '') {
        return
      }
      if (this.passoword.newPassword === '') {
        return
      }
      if (this.passoword.newPassword !== this.temp) {
        return
      }
      if (this.passoword.verifyCode === '') {
        return
      }
      fetch(`/api/user/update/password`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(this.passoword)
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            this.message = '修改成功，即将跳转，请重新登录！'
            this.showMessage = true
            this.$store.commit('setUserInfo', null)
            this.$router.push('/login')
          } else {
            this.message = '修改失败!' + json.message
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
