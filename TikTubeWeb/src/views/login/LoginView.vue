<template>
  <v-container fill-height>
    <v-row justify="center" align="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="mx-auto elevation-6 rounded-lg" color="surface">
          <v-card-item>
            <v-row class="mt-2 mb-2" justify="start">
              <v-btn color="primary" variant="text" prepend-icon="mdi-arrow-left" @click="backHome">
                返回
              </v-btn>
            </v-row>
            <v-row justify="center" class="mb-4">
              <v-img :src="webInfo.logoUrl" height="40" class="mx-auto" contain></v-img>
            </v-row>
            <v-row justify="center" class="mb-6">
              <h1 class="text-h4 font-weight-bold primary--text text-center">
                {{ webInfo.name }} {{ type }}
              </h1>
            </v-row>
          </v-card-item>

          <v-card-text>
            <LoginFrom v-if="showLogin" @login="userLogin" />
            <RegisterFrom v-else @register="register" />
          </v-card-text>

          <v-card-actions class="px-6 mb-3">
            <v-btn variant="text" color="primary" @click="openForgotPasswordDialog">
              忘记密码
            </v-btn>
            <v-spacer></v-spacer>
            <v-btn variant="text" color="primary" @click="moveRegister">
              {{ moveMessage }}
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
    <v-snackbar v-model="showMessage" :timeout="3000" color="error" location="top" rounded="pill">
      {{ message }}
      <template v-slot:actions>
        <v-btn color="white" variant="text" @click="showMessage = false"> 关闭 </v-btn>
      </template>
    </v-snackbar>

    <!-- 两步认证弹框 -->
    <v-dialog v-model="showTotpDialog" persistent max-width="400">
      <v-card>
        <v-card-title class="text-h5 font-weight-bold primary--text"> 两步验证 </v-card-title>
        <v-card-text>
          <p class="mb-4">请输入您的两步验证码以完成登录</p>
          <v-otp-input
            v-model="totpCode"
            length="6"
            label="验证码"
            type="text"
            variant="outlined"
            :rules="[(v) => !!v || '验证码不能为空']"
            maxlength="6"
            class="mb-2"
          ></v-otp-input>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="cancelTotp"> 取消 </v-btn>
          <v-btn color="primary" @click="loginTOTP" :disabled="!totpCode"> 确认 </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 忘记密码弹框 -->
    <v-dialog v-model="showForgotPasswordDialog" max-width="500" transition="dialog-top-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-lock-reset</v-icon>
            忘记密码
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="showForgotPasswordDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <!-- 管理员未配置邮箱 -->
          <div v-if="webInfo.openEmail == 0" class="text-center pa-4">
            <v-icon size="64" color="warning" class="mb-4">mdi-alert-circle</v-icon>
            <h3 class="text-h5 mb-2">无法找回密码</h3>
            <p class="text-body-1">管理员未配置找回密码规则，请联系管理员处理。</p>
          </div>

          <!-- 正常找回密码表单 -->
          <v-form v-else ref="forgotPasswordForm" @submit.prevent="submitForgotPassword">
            <v-text-field
              v-model="forgotPassword.mail"
              label="邮箱"
              placeholder="请输入你的邮箱"
              :rules="[(v) => !!v || '邮箱不能为空']"
              type="email"
              density="comfortable"
              class="mb-4"
              prepend-inner-icon="mdi-email-outline"
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
                v-model="forgotPassword.verifyCode"
                label="验证码"
                placeholder="验证码"
                :rules="[(v) => !!v || '验证码不能为空']"
                density="comfortable"
                prepend-inner-icon="mdi-text-box-check-outline"
                clearable
              />
            </div>

            <!-- 邮箱验证码 -->
            <v-row class="mb-4">
              <v-col cols="8">
                <v-text-field
                  v-model="forgotPassword.emailCode"
                  label="邮箱验证码"
                  placeholder="请输入邮箱验证码"
                  :rules="[(v) => !!v || '邮箱验证码不能为空']"
                  density="comfortable"
                  prepend-inner-icon="mdi-email-check-outline"
                  clearable
                />
              </v-col>
              <v-col cols="4">
                <v-btn
                  color="primary"
                  :disabled="countDown > 0"
                  @click="sendForgotPasswordEmailVerify"
                  class="mt-2"
                  block
                >
                  {{ countDown > 0 ? `${countDown}秒` : '发送验证码' }}
                </v-btn>
              </v-col>
            </v-row>

            <v-text-field
              v-model="forgotPassword.newPassword"
              label="新密码"
              placeholder="请输入新密码"
              :rules="[
                (v) => !!v || '新密码不能为空',
                (v) => (v && v.length >= 6) || '密码长度不能小于6个字符',
              ]"
              density="comfortable"
              class="mb-4"
              prepend-inner-icon="mdi-lock-outline"
              type="password"
              clearable
            />

            <v-text-field
              v-model="forgotPassword.confirmPassword"
              label="确认密码"
              placeholder="请再次输入新密码"
              :rules="[
                (v) => !!v || '确认密码不能为空',
                (v) => v === forgotPassword.newPassword || '两次密码不一致',
              ]"
              density="comfortable"
              class="mb-4"
              prepend-inner-icon="mdi-lock-check-outline"
              type="password"
              clearable
            />


          </v-form>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            @click="submitForgotPassword"
            variant="elevated"
            rounded="lg"
            prepend-icon="mdi-check"
          >
            提交
          </v-btn>
          <v-btn color="grey" @click="showForgotPasswordDialog = false" variant="text" class="ml-2">
            取消
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import LoginFrom from '@/components/form/LoginForm.vue'
import RegisterFrom from '@/components/form/RegisterForm.vue'
import { useWebInfoStore } from '@/stores/webInfoStore'
import { useUserStore } from '@/stores/userStore'
import StringUtils from '@/utils/string-utils'
export default {
  name: 'LoginView',
  components: {
    LoginFrom,
    RegisterFrom,
  },
  data() {
    return {
      verifyImageUrl: '/api/verifyImage',
      user: useUserStore(),
      type: '登录',
      moveMessage: '没有账号，创建账号',
      showLogin: true,
      message: '',
      showMessage: false,
      webInfo: {},
      totpCode: '',
      totpUserInfo: {},
      showTotpDialog: false,
      // 忘记密码相关数据
      showForgotPasswordDialog: false,
      forgotPasswordLoading: false,
      countDown: 0,
      timer: null,
      forgotPassword: {
        mail: '',
        emailCode: '',
        newPassword: '',
        confirmPassword: '',
        verifyCode: '',
      },
    }
  },
  created() {
    this.webInfo = useWebInfoStore().webInfo
  },
  methods: {
    getVerifyImage() {
      this.verifyImageUrl = '/api/verifyImage?t=' + new Date().getTime()
    },
    userLogin(value) {
      this.httpPost('/login', value, (json) => {
        if (json.status === 200) {
          const userData = json.data
          // 检查用户是否开启两步认证
          if (!userData.loginStatus && userData.otp === 1) {
            // 弹出两步认证验证码输入弹窗
            this.totpUserInfo = userData
            this.showTotpDialog = true
            this.totpCode = ''
          } else {
            // 保存用户
            this.user.setUserData(userData)
            // 检查是否有重定向参数
            const redirect = this.$route.query.redirect
            // 如果有重定向参数，则跳转到指定页面，否则跳转到首页
            this.$router.push(redirect || '/')
          }
        } else if (json.status === 4002) {
          this.message = StringUtils.dataErrorMessage(json.data)
          this.showMessage = true
        } else if (json.status === 1001) {
          this.message = json.data
          this.showMessage = true
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    loginTOTP() {
      const data = {
        code: this.totpCode,
        key: this.totpUserInfo.key,
      }
      this.httpPost('/login/totp', data, (json) => {
        if (json.status === 200) {
          const userData = json.data
          this.user.setUserData(userData)
          // 关闭弹窗
          this.showTotpDialog = false
          // 检查是否有重定向参数
          const redirect = this.$route.query.redirect
          // 如果有重定向参数，则跳转到指定页面，否则跳转到首页
          this.$router.push(redirect || '/')
        } else if (json.status === 4002) {
          this.message = StringUtils.dataErrorMessage(json.data)
          this.showMessage = true
        } else if (json.status === 1001) {
          this.message = json.data
          this.showMessage = true
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    cancelTotp() {
      // 取消两步验证，关闭弹窗
      this.showTotpDialog = false
      this.totpCode = ''
      this.totpUserInfo = {}
    },
    register(value) {
      this.httpPost('/register', value, (json) => {
        if (json.status === 200) {
          this.message = '注册成功，即将为你跳转到登录页面！'
          this.showMessage = true
          this.moveRegister()
        } else if (json.status === 4002) {
          this.message = StringUtils.dataErrorMessage(json.data)
          this.showMessage = true
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    moveRegister() {
      if (this.type === '登录') {
        this.type = '注册'
      } else {
        this.type = '登录'
      }
      if (this.moveMessage === '没有账号，创建账号') {
        this.moveMessage = '已有账号，我要登录'
      } else {
        this.moveMessage = '没有账号，创建账号'
      }
      this.showLogin = !this.showLogin
    },
    backHome() {
      this.$router.push('/')
    },

    // 打开忘记密码弹窗
    openForgotPasswordDialog() {
      this.showForgotPasswordDialog = true
      this.forgotPassword = {
        mail: '',
        emailCode: '',
        newPassword: '',
        confirmPassword: '',
        verifyCode: '',
      }
      this.getVerifyImage()
    },

    // 发送忘记密码邮箱验证码
    sendForgotPasswordEmailVerify() {
      // 验证邮箱格式
      var re =
        /^(([^()[\]\\.,;:\s@]+(\.[^()[\]\\.,;:\s@]+)*)|(.+))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
      if (!re.test(this.forgotPassword.mail)) {
        this.message = '邮箱格式错误'
        this.showMessage = true
        return
      }

      if (this.forgotPassword.verifyCode == '' || this.forgotPassword.verifyCode.length < 4) {
        this.message = '验证码不能为空且不能小于4个字符'
        this.color = 'error'
        this.snackbar = true
        return
      }

      // 发送邮箱验证码
      this.httpPost('/verify/send', { mail: this.forgotPassword.mail, verifyCode: this.forgotPassword.verifyCode }, (json) => {
        if (json.status === 200) {
          this.message = '邮箱验证码发送成功，请查收'
          this.showMessage = true
          this.startCountDown()
        } else {
          this.message = json.message || '邮箱验证码发送失败'
          this.showMessage = true
        }
      })
    },

    // 倒计时功能
    startCountDown() {
      // 设置倒计时60秒
      this.countDown = 60
      // 清除可能存在的定时器
      if (this.timer) {
        clearInterval(this.timer)
      }
      // 创建新的定时器
      this.timer = setInterval(() => {
        if (this.countDown > 0) {
          this.countDown--
        } else {
          clearInterval(this.timer)
        }
      }, 1000)
    },

    // 提交忘记密码表单
    submitForgotPassword() {
      // 表单验证
      if (
        !this.forgotPassword.mail ||
        !this.forgotPassword.emailCode ||
        !this.forgotPassword.newPassword ||
        !this.forgotPassword.confirmPassword ||
        !this.forgotPassword.verifyCode
      ) {
        this.message = '请填写所有必填项'
        this.showMessage = true
        return
      }

      if (this.forgotPassword.newPassword !== this.forgotPassword.confirmPassword) {
        this.message = '两次输入的密码不一致'
        this.showMessage = true
        return
      }

      if (this.forgotPassword.newPassword.length < 6) {
        this.message = '密码长度不能小于6个字符'
        this.showMessage = true
        return
      }

      if (this.forgotPassword.newPassword.length > 64) {
        this.message = '密码长度不能超过64个字符'
        this.showMessage = true
        return
      }

      // 提交数据
      this.forgotPasswordLoading = true
      const data = {
        mail: this.forgotPassword.mail,
        emailCode: this.forgotPassword.emailCode,
        password: this.forgotPassword.newPassword,
        verifyCode: this.forgotPassword.verifyCode,
      }

      this.httpPost('/user/forgot/password', data, (json) => {
        this.forgotPasswordLoading = false
        if (json.status === 200) {
          this.message = '密码重置成功，请使用新密码登录'
          this.showMessage = true
          this.showForgotPasswordDialog = false
        } else {
          this.message = json.message || '密码重置失败'
          this.showMessage = true
          this.getVerifyImage()
        }
      })
    },
  },
  beforeUnmount() {
    // 组件销毁前清除定时器
    if (this.timer) {
      clearInterval(this.timer)
      this.timer = null
    }
  },
}
</script>

<style>
</style>
