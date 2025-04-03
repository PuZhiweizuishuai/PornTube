<template>
  <div>
    <v-row justify="center">
      <v-col cols="10">
        <v-text-field
          v-model="username"
          placeholder="请输入你的邮箱或手机号"
          label="邮箱或手机号"
          :rules="[() => username != null || '请输入邮箱或手机号']"
          clearable
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="10">
        <v-text-field
          v-model="password"
          placeholder="密码"
          label="密码"
          clearable
          :rules="[() => password != null || '密码不能为空']"
          type="password"
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="5">
        <img
          :src="verifyImageUrl"
          alt="验证码"
          title="点击刷新"
          style="cursor: pointer"
          @click="getVerifyImage"
        />
      </v-col>
      <v-col cols="5">
        <v-text-field
          v-model="verifyCode"
          placeholder="验证码"
          label="验证码"
          :rules="[() => verifyCode != null || '验证码不能为空']"
          clearable
        />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="3">
        <v-switch v-model="rememberMe" label="记住我" />
      </v-col>
      <v-col cols="7" />
    </v-row>
    <v-row justify="center">
      <v-btn color="primary" @click="submitLog">登录</v-btn>
    </v-row>
  </div>
</template>

<script>
export default {
  name: 'LoginForm',
  data() {
    return {
      verifyImageUrl: '/api/verifyImage',
      username: '',
      password: '',
      verifyCode: '',
      rememberMe: false,
    }
  },
  methods: {
    submitLog() {
      const username = this.username
      const password = this.password
      const verifyCode = this.verifyCode
      const rememberMe = this.rememberMe
      const user = {
        username,
        password,
        verifyCode,
        rememberMe,
      }
      if (username === '' || password === '' || verifyCode === '') {
        return
      }
      this.$emit('login', user)
    },
    getVerifyImage() {
      this.verifyImageUrl = '/api/verifyImage?t=' + new Date().getTime()
    },
  },
}
</script>

<style></style>
