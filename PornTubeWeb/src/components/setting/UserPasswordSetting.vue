<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card class="mx-auto" elevation="2" rounded="lg">
        <v-card-title class="d-flex align-center py-4">
          <v-icon icon="mdi-lock-reset" class="mr-2" size="large" color="primary" />
          <h2>密码修改</h2>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text>
          <v-form ref="passwordForm" lazy-validation>
            <v-row justify="center">
              <v-col cols="12" md="8" class="mx-auto">
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
              </v-col>
            </v-row>

            <v-row justify="center">
              <v-col cols="12" md="8" class="mx-auto">
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
              </v-col>
            </v-row>

            <v-row justify="center">
              <v-col cols="12" md="8" class="mx-auto">
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
              </v-col>
            </v-row>

            <v-row justify="center" align="center">
              <v-col cols="12" md="4" class="mx-auto">
                <v-img
                  :src="verifyImageUrl"
                  alt="验证码"
                  title="点击刷新"
                  style="cursor: pointer"
                  max-width="200"
                  @click="getVerifyImage"
                  class="mx-auto rounded elevation-1"
                >
                  <template #placeholder>
                    <div class="d-flex align-center justify-center fill-height">
                      <v-progress-circular indeterminate color="primary"></v-progress-circular>
                    </div>
                  </template>
                </v-img>
              </v-col>
              <v-col cols="12" md="4" class="mx-auto">
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

        <v-divider></v-divider>

        <v-card-actions class="pa-4">
          <v-btn
            color="primary"
            @click="save"
            prepend-icon="mdi-content-save"
            variant="elevated"
            size="large"
          >
            保存
          </v-btn>
          <v-spacer></v-spacer>
          <v-chip color="warning" variant="outlined">
            <v-icon start icon="mdi-information"></v-icon>
            修改密码后需要重新登录
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
  name: 'UserSetting',
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
  
<style>
.v-card-title {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}
</style>
  