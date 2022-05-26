<template>
  <div>
    <v-row justify="center">
      <v-col cols="10">
        <v-text-field
          v-model="registerUser.mail"
          placeholder="请输入你的邮箱"
          label="邮箱"
          :rules="[() => registerUser.mail != null || '邮箱不能为空']"
          type="email"
          clearable
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="10">
        <v-text-field
          v-model="registerUser.phone"
          placeholder="请输入你的手机号（系统原因，此项选填）"
          label="手机号"

          clearable
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="10">
        <v-text-field
          v-model="registerUser.username"
          placeholder="昵称"
          label="昵称"
          :rules="[() => registerUser.username != null || '昵称不能为空']"
          clearable
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="10">
        <v-text-field
          v-model="registerUser.password"
          placeholder="密码"
          label="密码"
          :rules="[() => registerUser.password != null || '密码不能为空']"
          clearable
          type="password"
        />
      </v-col>
    </v-row>
    <v-row v-if="this.$store.state.webInfo.openInvitationRegister == 1" justify="center">
      <v-col cols="10">
        <v-text-field
          v-model="registerUser.invitationCode"
          placeholder="邀请码"
          label="邀请码"
          clearable
          :rules="[() => registerUser.invitationCode != null || '邀请码不能为空']"
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="5">
        <img :src="verifyImageUrl" alt="验证码" title="点击刷新" style="cursor:pointer;" @click="getVerifyImage">
      </v-col>
      <v-col cols="5">
        <v-text-field
          v-model="registerUser.verifyCode"
          placeholder="验证码"
          label="验证码"
          :rules="[() => registerUser.verifyCode != null || '验证码不能为空']"
          clearable
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-btn color="primary" @click="submitRegister">注册</v-btn>
    </v-row>
    <v-snackbar
      v-model="snackbar"
      :color="color"
      :timeout="3000"
      :top="true"
    >
      {{ message }}
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'Register',
  data() {
    return {
      verifyImageUrl: '/api/verifyImage',
      registerUser: {
        mail: '',
        phone: '',
        password: '',
        invitationCode: '',
        verifyCode: '',
        username: ''
      },
      snackbar: false,
      color: 'success',
      message: '分享成功'
    }
  },
  methods: {
    submitRegister() {
      var re = /^(([^()[\]\\.,;:\s@\"]+(\.[^()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
      if (!re.test(this.registerUser.mail)) {
        this.message = '邮箱格式错误'
        this.color = 'error'
        this.snackbar = true
        return
      }

      if (this.registerUser.password === '' || this.registerUser.password.length < 6 || this.registerUser.verifyCode === '' || this.registerUser.username === '') {
        this.message = '密码不能为空且不能小于6个字符'
        this.color = 'error'
        this.snackbar = true
        return
      }
      if (this.$store.state.webInfo.openInvitationRegister === 1 && this.registerUser.invitationCode === '' && this.registerUser.username !== 'admin') {
        this.message = '邀请码不能为空'
        this.color = 'error'
        this.snackbar = true
        return
      }

      if (this.registerUser.phone !== '') {
        var myreg = /^[1][3,4,5,7,8][0-9]{9}$/
        if (!myreg.test((this.registerUser.phone))) {
          this.message = '手机号码格式错误'
          this.color = 'error'
          this.snackbar = true
          return
        }
      }
      // sconsole.log(this.registerUser)
      this.$emit('register', this.registerUser)
    },
    getVerifyImage() {
      this.verifyImageUrl = '/api/verifyImage?t=' + new Date().getTime()
    }
  }
}
</script>

<style>

</style>
