<template>
  <div>
    <v-text-field
      v-model="username"
      label="邮箱或手机号"
      placeholder="请输入你的邮箱或手机号"
      :rules="[(v) => !!v || '请输入邮箱或手机号']"
      density="comfortable"
      class="mb-4"
      prepend-inner-icon="mdi-email-outline"
      clearable
    />

    <v-text-field
      v-model="password"
      label="密码"
      placeholder="密码"
      :rules="[(v) => !!v || '密码不能为空']"
      density="comfortable"
      class="mb-4"
      prepend-inner-icon="mdi-lock-outline"
      type="password"
      clearable
    />

    <div class="d-flex align-center mb-4">
      <v-img
        :src="verifyImageUrl"
        alt="验证码"
        title="点击刷新"
        style="cursor: pointer; max-width: 150px; height: 40px; border-radius: 4px"
        @click="getVerifyImage"
        class="me-3"
      />
      <v-text-field
        v-model="verifyCode"
        label="验证码"
        placeholder="验证码"
        :rules="[(v) => !!v || '验证码不能为空']"
        density="comfortable"
        prepend-inner-icon="mdi-text-box-check-outline"
        clearable
      />
    </div>

    <div class="d-flex mb-6">
      <v-switch
        v-model="rememberMe"
        label="记住我"
        color="primary"
        density="comfortable"
        hide-details
      />
    </div>

    <div class="text-center">
      <v-btn
        color="primary"
        size="large"
        variant="elevated"
        rounded="lg"
        :loading="loading"
        @click="submitLog"
        block
      >
        登录
      </v-btn>
    </div>
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
      loading: false,
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
      this.loading = true
      this.$emit('login', user)
      setTimeout(() => {
        this.loading = false
      }, 2000)
    },
    getVerifyImage() {
      this.verifyImageUrl = '/api/verifyImage?t=' + new Date().getTime()
    },
  },
}
</script>

<style></style>
