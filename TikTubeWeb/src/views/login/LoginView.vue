<template>
  <div class="login-page">
    <!-- 返回主页：固定在页面左上角 -->
    <v-btn
      class="login-page__back"
      variant="text"
      color="primary"
      prepend-icon="mdi-arrow-left"
      @click="backHome"
    >
      返回主页
    </v-btn>

    <v-container maxWidth="1140" class="w-100 py-8">
      <v-row align="center" justify="center">
        <!-- 品牌区：桌面端左侧，登录与注册共用 -->
        <v-col cols="12" md="6" class="d-none d-md-flex flex-column align-start ps-4 ps-lg-8">
          <v-img :src="webInfo.logoUrl" height="64" max-width="64" contain class="mb-8 rounded-lg" />
          <h1 class="text-h3 font-weight-bold mb-4">{{ webInfo.name }} {{ type }}</h1>
          <p class="text-body-1 text-medium-emphasis mb-6 login-page__slogan">
            {{ showLogin ? '登录后即可观看视频、阅读文章、参与弹幕与评论。' : '注册后即可发布视频与文章，管理自己的订阅与收藏。' }}
          </p>
          <div class="d-flex flex-wrap ga-3">
            <v-chip prepend-icon="mdi-play-box-outline" variant="tonal" color="primary" size="small">
              视频与文章
            </v-chip>
            <v-chip prepend-icon="mdi-bell-outline" variant="tonal" color="primary" size="small">
              消息通知
            </v-chip>
            <v-chip prepend-icon="mdi-shield-lock-outline" variant="tonal" color="primary" size="small">
              两步验证
            </v-chip>
          </div>
        </v-col>

        <v-col cols="12" sm="9" md="6" lg="5" class="py-8">
          <v-card class="mx-auto elevation-8 border" color="surface" rounded="lg">
            <div class="text-center pa-6 pb-2">
              <v-img
                :src="webInfo.logoUrl"
                height="44"
                max-width="44"
                contain
                class="rounded-circle mb-4 mx-auto"
              />
              <h2 class="text-h5 font-weight-bold text-primary">{{ formTitle }}</h2>
              <p class="text-body-2 text-medium-emphasis mt-1">{{ formHint }}</p>
            </div>

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
    </v-container>
    <v-snackbar v-model="showMessage" :timeout="3000" :color="messageColor" location="top" rounded="pill">
      {{ message }}
      <template v-slot:actions>
        <v-btn color="white" variant="text" @click="showMessage = false"> 关闭 </v-btn>
      </template>
    </v-snackbar>

    <!-- 两步认证弹框 -->
    <v-dialog v-model="showTotpDialog" persistent max-width="400" @after:show="focusTotpInput">
      <v-card>
        <v-card-title class="text-h5 font-weight-bold primary--text"> 两步验证 </v-card-title>
        <v-card-text>
          <p class="mb-4">请输入您的两步验证码以完成登录</p>
          <v-otp-input
            ref="totpInput"
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
    <v-dialog v-model="showForgotPasswordDialog" max-width="500" scrollable>
      <v-card rounded="lg">
        <v-card-title class="d-flex align-center">
          <v-icon start>mdi-lock-reset</v-icon>
          忘记密码
          <v-spacer />
          <v-btn icon="mdi-close" variant="text" size="small" @click="showForgotPasswordDialog = false" />
        </v-card-title>
        <v-divider />

        <v-card-text class="pt-4">
          <!-- 管理员未配置邮箱 -->
          <div v-if="webInfo.openEmail == 0" class="text-center py-6">
            <v-icon size="56" color="warning" class="mb-3">mdi-alert-circle-outline</v-icon>
            <p class="text-body-1 mb-0">管理员未配置找回密码规则，请联系管理员处理。</p>
          </div>

          <v-form v-else ref="forgotPasswordForm" lazy-validation @submit.prevent="submitForgotPassword">
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
              autocomplete="email"
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
                v-model="forgotPassword.verifyCode"
                label="图形验证码"
                placeholder="图形验证码"
                :rules="[(v) => !!v || '验证码不能为空']"
                density="comfortable"
                prepend-inner-icon="mdi-text-box-check-outline"
                clearable
              />
            </div>

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
                  variant="tonal"
                  :disabled="countDown > 0"
                  class="mt-2"
                  block
                  @click="sendForgotPasswordEmailVerify"
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
                (v) => (v && v.length >= 6 && v.length <= 64) || '密码长度需在 6 到 64 个字符之间',
              ]"
              :type="showNewPassword ? 'text' : 'password'"
              :append-inner-icon="showNewPassword ? 'mdi-eye-off' : 'mdi-eye'"
              density="comfortable"
              class="mb-4"
              prepend-inner-icon="mdi-lock-outline"
              clearable
              autocomplete="new-password"
              @click:append-inner="showNewPassword = !showNewPassword"
            />

            <v-text-field
              v-model="forgotPassword.confirmPassword"
              label="确认密码"
              placeholder="请再次输入新密码"
              :rules="[
                (v) => !!v || '确认密码不能为空',
                (v) => v === forgotPassword.newPassword || '两次密码不一致',
              ]"
              :type="showConfirmPassword ? 'text' : 'password'"
              :append-inner-icon="showConfirmPassword ? 'mdi-eye-off' : 'mdi-eye'"
              density="comfortable"
              prepend-inner-icon="mdi-lock-check-outline"
              clearable
              autocomplete="new-password"
              @click:append-inner="showConfirmPassword = !showConfirmPassword"
            />
          </v-form>
        </v-card-text>

        <v-divider />
        <v-card-actions class="pa-4">
          <v-spacer />
          <v-btn color="grey-darken-1" variant="text" @click="showForgotPasswordDialog = false"> 取消 </v-btn>
          <v-btn
            v-if="webInfo.openEmail != 0"
            color="primary"
            prepend-icon="mdi-check"
            :loading="forgotPasswordLoading"
            @click="submitForgotPassword"
          >
            重置密码
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script>
import LoginFrom from '@/components/form/LoginForm.vue'
import RegisterFrom from '@/components/form/RegisterForm.vue'
import { useWebInfoStore } from '@/stores/webInfoStore'
import { useUserStore } from '@/stores/userStore'
import StringUtils from '@/utils/string-utils'

const createForgotPasswordForm = () => ({
  mail: '',
  emailCode: '',
  newPassword: '',
  confirmPassword: '',
  verifyCode: '',
})

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
      messageColor: 'error',
      showMessage: false,
      webInfo: {},
      totpCode: '',
      totpUserInfo: {},
      showTotpDialog: false,
      // 忘记密码相关数据
      showForgotPasswordDialog: false,
      forgotPasswordLoading: false,
      showNewPassword: false,
      showConfirmPassword: false,
      countDown: 0,
      timer: null,
      forgotPassword: createForgotPasswordForm(),
    }
  },
  created() {
    this.webInfo = useWebInfoStore().webInfo
  },
  computed: {
    formTitle() {
      return this.showLogin ? '登录你的账号' : '创建新账号'
    },
    formHint() {
      const name = this.webInfo.name || 'TikTube'
      return this.showLogin
        ? `使用邮箱或手机号继续访问 ${name}`
        : `注册 ${name}，开始发布视频与文章`
    },
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
          this.notify(StringUtils.dataErrorMessage(json.data))
        } else if (json.status === 1001) {
          this.notify(json.data)
        } else {
          this.notify(json.message)
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
          this.notify(StringUtils.dataErrorMessage(json.data))
        } else if (json.status === 1001) {
          this.notify(json.data)
        } else {
          this.notify(json.message)
        }
      })
    },
    cancelTotp() {
      // 取消两步验证，关闭弹窗
      this.showTotpDialog = false
      this.totpCode = ''
      this.totpUserInfo = {}
    },
    focusTotpInput() {
      // Chrome 不会对动态插入的元素应用原生 autofocus，弹窗过渡结束后手动聚焦第一个输入格
      const el = this.$refs.totpInput?.$el?.querySelector('input')
      el?.focus()
    },
    register(value) {
      this.httpPost('/register', value, (json) => {
        if (json.status === 200) {
          this.notify('注册成功，即将为你跳转到登录页面！', 'success')
          this.moveRegister()
        } else if (json.status === 4002) {
          this.notify(StringUtils.dataErrorMessage(json.data))
        } else {
          this.notify(json.message)
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

    // 消息提示：默认按失败展示，成功路径显式传 success
    notify(message, color = 'error') {
      this.message = message
      this.messageColor = color
      this.showMessage = true
    },

    // 打开忘记密码弹窗
    openForgotPasswordDialog() {
      this.forgotPassword = createForgotPasswordForm()
      this.showNewPassword = false
      this.showConfirmPassword = false
      this.showForgotPasswordDialog = true
      this.getVerifyImage()
    },

    // 发送忘记密码邮箱验证码
    sendForgotPasswordEmailVerify() {
      // 验证邮箱格式
      var re =
        /^(([^()[\]\\.,;:\s@]+(\.[^()[\]\\.,;:\s@]+)*)|(.+))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
      if (!re.test(this.forgotPassword.mail)) {
        this.notify('邮箱格式错误')
        return
      }

      if (this.forgotPassword.verifyCode == '' || this.forgotPassword.verifyCode.length < 4) {
        this.notify('验证码不能为空且不能小于4个字符')
        return
      }

      // 发送邮箱验证码
      this.httpPost('/verify/send', { mail: this.forgotPassword.mail, verifyCode: this.forgotPassword.verifyCode }, (json) => {
        if (json.status === 200) {
          this.notify('邮箱验证码发送成功，请查收', 'success')
          this.startCountDown()
        } else {
          this.notify(json.message || '邮箱验证码发送失败')
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
    async submitForgotPassword() {
      const { valid } = await this.$refs.forgotPasswordForm.validate()
      if (!valid) return

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
          this.notify('密码重置成功，请使用新密码登录', 'success')
          this.showForgotPasswordDialog = false
        } else {
          this.notify(json.message || '密码重置失败')
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

<style scoped>
/* 登录 / 注册页底色：主题色光晕 + 纵向渐变，浅深两套主题都走 CSS 变量 */
.login-page {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 100vh;
  background-color: rgb(var(--v-theme-background));
  background-image:
    radial-gradient(60% 55% at 8% 0%, rgba(var(--v-theme-primary), 0.22), transparent 62%),
    radial-gradient(55% 50% at 95% 100%, rgba(var(--v-theme-primary), 0.14), transparent 65%),
    linear-gradient(180deg, rgb(var(--v-theme-surface-variant)) 0%, rgb(var(--v-theme-background)) 55%);
}

.login-page__slogan {
  max-width: 460px;
}

.login-page__back {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 1;
}
</style>
