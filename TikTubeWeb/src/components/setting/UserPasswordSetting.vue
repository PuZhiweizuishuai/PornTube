<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center">
      <v-icon start>mdi-lock-outline</v-icon>
      修改密码
    </v-card-title>
    <v-divider />

    <v-card-text class="pt-4">
      <v-form ref="passwordForm" lazy-validation>
        <v-text-field
          v-model="password.oldPassword"
          placeholder="原始密码"
          label="原始密码"
          clearable
          variant="outlined"
          density="comfortable"
          type="password"
          :rules="[(v) => !!v || '原始密码不能为空']"
          prepend-inner-icon="mdi-lock"
          autocomplete="current-password"
        />

        <v-text-field
          v-model="password.newPassword"
          placeholder="新密码"
          label="新密码"
          clearable
          variant="outlined"
          density="comfortable"
          type="password"
          :rules="[(v) => !!v || '新密码不能为空']"
          prepend-inner-icon="mdi-lock-plus"
          autocomplete="new-password"
        />

        <v-text-field
          v-model="temp"
          placeholder="请再输入一遍"
          label="请再输入一遍"
          clearable
          variant="outlined"
          density="comfortable"
          type="password"
          :rules="[
            (v) => !!v || '请再次输入密码',
            (v) => v === password.newPassword || '两次密码不相同',
          ]"
          prepend-inner-icon="mdi-lock-check"
          autocomplete="new-password"
        />

        <v-row align="center" class="mt-1">
          <v-col cols="12" sm="5">
            <v-img
              :src="verifyImageUrl"
              alt="验证码"
              title="点击刷新"
              style="cursor: pointer"
              max-width="200"
              height="60"
              contain
              @click="getVerifyImage"
              class="mx-auto mx-sm-0 rounded bg-surface"
            >
              <template #placeholder>
                <div class="d-flex align-center justify-center fill-height">
                  <v-progress-circular indeterminate color="primary" size="28"></v-progress-circular>
                </div>
              </template>
            </v-img>
          </v-col>
          <v-col cols="12" sm="7">
            <v-text-field
              v-model="password.verifyCode"
              placeholder="验证码"
              label="验证码"
              variant="outlined"
              density="comfortable"
              :rules="[(v) => !!v || '验证码不能为空']"
              clearable
              prepend-inner-icon="mdi-form-textbox-password"
            />
          </v-col>
        </v-row>
      </v-form>
    </v-card-text>

    <v-divider />

    <v-card-actions class="pa-4">
      <v-btn color="primary" @click="save" prepend-icon="mdi-content-save"> 保存 </v-btn>
      <v-spacer />
      <span class="text-caption text-medium-emphasis">修改密码后需要重新登录</span>
    </v-card-actions>

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
export default {
  name: 'UserPasswordSetting',
  data() {
    return {
      password: {
        oldPassword: '',
        newPassword: '',
        verifyCode: '',
      },
      temp: '',
      verifyImageUrl: '/api/verifyImage',
      showMessage: false,
      message: '',
      userInfo: null,
    }
  },
  created() {
    this.userInfo = useUserStore()
    this.getVerifyImage()
  },
  methods: {
    getVerifyImage() {
      this.verifyImageUrl = '/api/verifyImage?t=' + new Date().getTime()
    },
    async save() {
      // 使用表单验证
      const { valid } = await this.$refs.passwordForm.validate()
      if (!valid) return

      if (this.password.newPassword.length > 50) {
        this.message = '密码长度不能超过50个字符！'
        this.showMessage = true
        return
      }
      this.httpPost('/user/update/password', this.password, (json) => {
        if (json.status === 200) {
          this.message = '修改成功，即将跳转，请重新登录！'
          this.showMessage = true
          // 延迟执行登出和跳转
          setTimeout(() => {
            this.userInfo.deleteUserData()
            this.$router.push('/login')
          }, 2000)
        } else {
          this.message = '修改失败!' + json.message
          this.showMessage = true
          // 刷新验证码
          this.getVerifyImage()
        }
      })
    },
  },
}
</script>
  