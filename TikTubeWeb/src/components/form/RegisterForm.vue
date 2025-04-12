<template>
  <div>
    <v-text-field
      v-model="registerUser.mail"
      label="邮箱"
      placeholder="请输入你的邮箱"
      :rules="[(v) => !!v || '邮箱不能为空']"
      type="email"
      density="comfortable"
      class="mb-4"
      prepend-inner-icon="mdi-email-outline"
      clearable
    />

    <v-text-field
      v-model="registerUser.phone"
      label="手机号"
      placeholder="请输入你的手机号（系统原因，此项选填）"
      density="comfortable"
      class="mb-4"
      prepend-inner-icon="mdi-phone-outline"
      clearable
    />

    <v-text-field
      v-model="registerUser.username"
      label="昵称"
      placeholder="昵称"
      :rules="[(v) => !!v || '昵称不能为空']"
      density="comfortable"
      class="mb-4"
      prepend-inner-icon="mdi-account-outline"
      clearable
    />

    <v-text-field
      v-model="registerUser.password"
      label="密码"
      placeholder="密码"
      :rules="[
        (v) => !!v || '密码不能为空',
        (v) => (v && v.length >= 6) || '密码长度不能小于6个字符',
      ]"
      density="comfortable"
      class="mb-4"
      prepend-inner-icon="mdi-lock-outline"
      type="password"
      clearable
    />

    <v-text-field
      v-if="webInfo.openInvitationRegister == 1"
      v-model="registerUser.invitationCode"
      label="邀请码"
      placeholder="邀请码"
      :rules="[(v) => !!v || '邀请码不能为空']"
      density="comfortable"
      class="mb-4"
      prepend-inner-icon="mdi-ticket-outline"
      clearable
    />

    <div class="d-flex align-center mb-6">
      <v-img
        :src="verifyImageUrl"
        alt="验证码"
        title="点击刷新"
        style="cursor: pointer; max-width: 150px; height: 40px; border-radius: 4px"
        @click="getVerifyImage"
        class="me-3"
      />
      <v-text-field
        v-model="registerUser.verifyCode"
        label="验证码"
        placeholder="验证码"
        :rules="[(v) => !!v || '验证码不能为空']"
        density="comfortable"
        prepend-inner-icon="mdi-text-box-check-outline"
        clearable
      />
    </div>

    <div class="text-center">
      <v-btn
        color="primary"
        size="large"
        variant="elevated"
        rounded="lg"
        :loading="loading"
        @click="submitRegister"
        block
      >
        注册
      </v-btn>
    </div>

    <v-snackbar v-model="snackbar" :color="color" :timeout="3000" location="top" rounded="pill">
      {{ message }}
    </v-snackbar>
  </div>
</template>

<script>
import { useWebInfoStore } from '@/stores/webInfoStore'
export default {
  name: 'RegisterFrom',
  data() {
    return {
      verifyImageUrl: '/api/verifyImage',
      registerUser: {
        mail: '',
        phone: '',
        password: '',
        invitationCode: '',
        verifyCode: '',
        username: '',
      },
      snackbar: false,
      color: 'success',
      message: '分享成功',
      webInfo: {},
      loading: false,
    }
  },
  created() {
    this.webInfo = useWebInfoStore().webInfo
  },
  methods: {
    submitRegister() {
      var re =
        /^(([^()[\]\\.,;:\s@]+(\.[^()[\]\\.,;:\s@]+)*)|(.+))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
      if (!re.test(this.registerUser.mail)) {
        this.message = '邮箱格式错误'
        this.color = 'error'
        this.snackbar = true
        return
      }

      if (
        this.registerUser.password === '' ||
        this.registerUser.password.length < 6 ||
        this.registerUser.verifyCode === '' ||
        this.registerUser.username === ''
      ) {
        this.message = '密码不能为空且不能小于6个字符'
        this.color = 'error'
        this.snackbar = true
        return
      }
      if (
        this.webInfo.openInvitationRegister === 1 &&
        this.registerUser.invitationCode === '' &&
        this.registerUser.username !== 'admin'
      ) {
        this.message = '邀请码不能为空'
        this.color = 'error'
        this.snackbar = true
        return
      }

      if (this.registerUser.phone !== '') {
        var myreg = /^[1][3,4,5,7,8][0-9]{9}$/
        if (!myreg.test(this.registerUser.phone)) {
          this.message = '手机号码格式错误'
          this.color = 'error'
          this.snackbar = true
          return
        }
      }

      this.loading = true
      this.$emit('register', this.registerUser)
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
