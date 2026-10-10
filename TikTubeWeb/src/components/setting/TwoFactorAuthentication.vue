<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card class="mx-auto" elevation="2" rounded="lg">
        <v-card-title class="d-flex align-center py-4">
          <v-icon icon="mdi-shield-lock" class="mr-2" size="large" color="primary" />
          <h2>两步验证</h2>
          <v-chip color="warning" variant="outlined" class="ml-4" v-if="!isTwoFactorEnabled">
            <v-icon start icon="mdi-alert-circle"></v-icon>
            建议开启两步验证以提高账户安全性
          </v-chip>
          <v-chip color="success" variant="outlined" class="ml-4" v-else>
            <v-icon start icon="mdi-shield-check"></v-icon>
            已开启两步验证
          </v-chip>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text class="pa-4">
          <v-row>
            <v-col cols="12" md="8">
              <v-alert
                :color="isTwoFactorEnabled ? 'success' : 'info'"
                :icon="isTwoFactorEnabled ? 'mdi-shield-check' : 'mdi-shield-outline'"
                variant="tonal"
                class="mb-4"
              >
                <p class="text-body-1">
                  {{ isTwoFactorEnabled ? '您已开启两步验证，账户安全性得到了提升。' : '开启两步验证后，您将需要在登录时输入验证码，这将大大提高账户安全性。' }}
                </p>
                <p class="text-body-2 mt-2" v-if="!isTwoFactorEnabled">
                  您需要使用身份验证器应用（如Google Authenticator、Microsoft Authenticator或Authy）来生成验证码。
                </p>
              </v-alert>
            </v-col>
          </v-row>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-btn 
            color="primary" 
            @click="handleTOTP" 
            class="mr-2"
            prepend-icon="mdi-shield-key"
            variant="elevated"
            rounded="lg"
          >
            {{ isTwoFactorEnabled ? '重新生成密钥' : '开启两步验证' }}
          </v-btn>
          <v-btn 
            v-if="isTwoFactorEnabled" 
            color="warning" 
            @click="showRecoveryDialog = true" 
            class="mr-2"
            prepend-icon="mdi-key-variant"
            variant="elevated"
            rounded="lg"
          >
            使用恢复密钥
          </v-btn>
          <v-btn 
            v-if="isTwoFactorEnabled" 
            color="error" 
            @click="showCloseDialog = true"
            prepend-icon="mdi-shield-off"
            variant="elevated"
            rounded="lg"
          >
            关闭两步验证
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-col>

    <!-- 二维码弹窗 -->
    <v-dialog v-model="showQrDialog" max-width="500" transition="dialog-top-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-qrcode-scan</v-icon>
            扫描二维码
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="showQrDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <div class="text-center">
            <p class="mb-4">请使用身份验证器应用扫描以下二维码</p>
            <div v-if="qrCodeUrl" class="d-flex justify-center mb-4">
              <img :src="qrCodeUrl" alt="二维码" class="elevation-1 rounded" />
            </div>
            <v-alert v-if="secretKey" type="info" class="mb-4" variant="tonal">
              <p>如果无法扫描二维码，请手动输入以下密钥：</p>
              <p class="font-weight-bold text-h6 mt-2 user-select-all">{{ secretKey }}</p>
            </v-alert>
            <v-alert v-if="recoveryCode" type="warning" class="mb-4" variant="tonal">
              <p>请保存好以下恢复码，当您无法使用验证器时可用于恢复账户：</p>
              <p class="font-weight-bold text-h6 mt-2 user-select-all">{{ recoveryCode }}</p>
              <p class="text-caption mt-2">
                <v-icon size="small" class="mr-1">mdi-information</v-icon>
                请将此恢复码保存在安全的地方，关闭窗口后将无法再次查看
              </p>
            </v-alert>
          </div>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn 
            color="primary" 
            @click="confirmSetup" 
            variant="elevated"
            rounded="lg"
            prepend-icon="mdi-check-circle"
          >
            确认已设置
          </v-btn>
          <v-btn 
            color="grey" 
            @click="showQrDialog = false" 
            variant="text"
            class="ml-2"
          >
            关闭
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 验证码输入弹窗 -->
    <v-dialog v-model="showVerifyDialog" max-width="500" transition="dialog-top-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-numeric</v-icon>
            验证设置
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="showVerifyDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <p class="mb-4">请输入验证器应用生成的6位验证码以完成设置</p>
          <v-text-field
            v-model="verificationCode"
            label="验证码"
            maxlength="6"
            counter="6"
            variant="outlined"
            density="comfortable"
            :error-messages="verifyError"
            prepend-inner-icon="mdi-form-textbox-password"
            class="mt-4"
            autofocus
          ></v-text-field>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn 
            color="primary" 
            @click="verifyCode" 
            variant="elevated"
            rounded="lg"
            prepend-icon="mdi-check"
          >
            验证
          </v-btn>
          <v-btn 
            color="grey" 
            @click="showVerifyDialog = false" 
            variant="text"
            class="ml-2"
          >
            取消
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 重新生成验证码输入弹窗 -->
    <v-dialog v-model="showRegenerateDialog" max-width="500" transition="dialog-top-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-shield-refresh</v-icon>
            验证身份
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="showRegenerateDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <p class="mb-4">请输入当前验证器应用生成的6位验证码以重新生成密钥</p>
          <v-text-field
            v-model="regenerateCode"
            label="验证码"
            maxlength="6"
            counter="6"
            variant="outlined"
            density="comfortable"
            :error-messages="regenerateError"
            prepend-inner-icon="mdi-form-textbox-password"
            class="mt-4"
            autofocus
          ></v-text-field>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn 
            color="primary" 
            @click="regenerateKey" 
            variant="elevated"
            rounded="lg"
            prepend-icon="mdi-check"
          >
            验证
          </v-btn>
          <v-btn 
            color="grey" 
            @click="showRegenerateDialog = false" 
            variant="text"
            class="ml-2"
          >
            取消
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 恢复密钥输入弹窗 -->
    <v-dialog v-model="showRecoveryDialog" max-width="500" transition="dialog-top-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="warning" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-key-variant</v-icon>
            使用恢复密钥
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="showRecoveryDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <p class="mb-4">如果您无法访问验证器应用，请输入之前保存的恢复密钥</p>
          <v-text-field
            v-model="recoveryKeyInput"
            label="恢复密钥"
            variant="outlined"
            density="comfortable"
            :error-messages="recoveryError"
            prepend-inner-icon="mdi-key"
            class="mt-4"
            autofocus
          ></v-text-field>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn 
            color="warning" 
            @click="recoverWithKey" 
            variant="elevated"
            rounded="lg"
            prepend-icon="mdi-key-link"
          >
            恢复
          </v-btn>
          <v-btn 
            color="grey" 
            @click="showRecoveryDialog = false" 
            variant="text"
            class="ml-2"
          >
            取消
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 关闭两步验证弹窗 -->
    <v-dialog v-model="showCloseDialog" max-width="500" transition="dialog-top-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="error" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-shield-off</v-icon>
            关闭两步验证
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="showCloseDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <v-alert
            color="warning"
            icon="mdi-alert"
            border="start"
            elevation="1"
            class="mb-4"
          >
            关闭两步验证将降低您账户的安全性，确定要继续吗？
          </v-alert>
          
          <p class="mb-4">请输入验证器应用生成的6位验证码以关闭两步验证</p>
          <v-text-field
            v-model="closeCode"
            label="验证码"
            maxlength="6"
            counter="6"
            variant="outlined"
            density="comfortable"
            :error-messages="closeError"
            prepend-inner-icon="mdi-form-textbox-password"
            class="mt-4"
            autofocus
          ></v-text-field>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn 
            color="error" 
            @click="closeTwoFactor" 
            variant="elevated"
            rounded="lg"
            prepend-icon="mdi-shield-off"
          >
            关闭两步验证
          </v-btn>
          <v-btn 
            color="grey" 
            @click="showCloseDialog = false" 
            variant="text"
            class="ml-2"
          >
            取消
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 消息提示 -->
    <!-- 消息提示统一由 useMessage + AppSnackbar 承载 -->
    <AppSnackbar :state="toast" />
  </v-row>
</template>

<script>
import { useUserStore } from '@/stores/userStore'
import QRCode from 'qrcode'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useMessage } from '@/composables/useMessage'
export default {
  components: { AppSnackbar },
  setup() {
    const { toast, showSuccessMessage, showErrorMessage } = useMessage()
    return { toast, showSuccessMessage, showErrorMessage }
  },
  data() {
    return {
      userStore: useUserStore(),
      showQrDialog: false,
      showVerifyDialog: false,
      showRegenerateDialog: false,
      showRecoveryDialog: false,
      showCloseDialog: false,
      qrCodeUrl: '',
      secretKey: '',
      recoveryCode: '',
      twoFactorData: null,
      verificationCode: '',
      verifyError: '',
      regenerateCode: '',
      regenerateError: '',
      recoveryKeyInput: '',
      recoveryError: '',
      closeCode: '',
      closeError: '',
    }
  },
  computed: {
    // 根据userStore.userData.otp判断是否开启两步认证
    isTwoFactorEnabled() {
      return this.userStore.userData && this.userStore.userData.otp === 1
    },
  },
  methods: {
    // 处理TOTP按钮点击
    handleTOTP() {
      if (this.isTwoFactorEnabled) {
        // 如果已开启，则显示重新生成验证对话框
        this.showRegenerateDialog = true
      } else {
        // 如果未开启，则创建新的TOTP
        this.openTOTP()
      }
    },

    // 开启TOTP两步验证
    openTOTP() {
      this.httpPost('/2fa/totp/create', {}, (json) => {
        // 创建成功
        if (json.status === 200) {
          this.twoFactorData = json.data
          this.secretKey = json.data.secret
          this.recoveryCode = json.data.recoveryCode

          // 生成二维码
          this.generateQrCode(json.data.qrData)

          // 显示弹窗
          this.showQrDialog = true
        } else {
          this.showErrorMessage(json.message || '创建两步验证失败')
        }
      })
    },

    // 重新生成密钥
    regenerateKey() {
      if (!this.regenerateCode || this.regenerateCode.length !== 6) {
        this.regenerateError = '请输入6位验证码'
        return
      }

      this.httpPost(
        '/2fa/totp/new',
        {
          code: this.regenerateCode,
        },
        (json) => {
          if (json.status === 200 && json.data) {
            // 重新生成成功
            this.showRegenerateDialog = false
            this.twoFactorData = json.data
            this.secretKey = json.data.secret
            this.recoveryCode = json.data.recoveryCode

            // 生成二维码
            this.generateQrCode(json.data.qrData)

            // 显示弹窗
            this.showQrDialog = true
            this.showSuccessMessage('密钥重新生成成功，请更新您的验证器应用')
          } else {
            // 验证码错误或其他错误
            this.regenerateError = '验证码错误'
          }
        }
      )
    },

    // 使用恢复密钥恢复
    recoverWithKey() {
      if (!this.recoveryKeyInput) {
        this.recoveryError = '请输入恢复密钥'
        return
      }

      this.httpPost(
        '/2fa/totp/recover',
        {
          recoveryCode: this.recoveryKeyInput,
        },
        (json) => {
          if (json.status === 200 && json.data) {
            // 恢复成功
            this.showRecoveryDialog = false
            this.twoFactorData = json.data
            this.secretKey = json.data.secret
            this.recoveryCode = json.data.recoveryCode

            // 生成二维码
            this.generateQrCode(json.data.qrData)

            // 显示弹窗
            this.showQrDialog = true
            this.showSuccessMessage('恢复成功，请使用新的密钥更新您的验证器应用')
          } else {
            // 恢复密钥错误或其他错误
            this.recoveryError = '恢复密钥无效或已过期'
          }
        }
      )
    },

    // 关闭两步验证
    closeTwoFactor() {
      if (!this.closeCode || this.closeCode.length !== 6) {
        this.closeError = '请输入6位验证码'
        return
      }

      this.httpPost(
        '/2fa/totp/close',
        {
          code: this.closeCode,
        },
        (json) => {
          if (json.status === 200 && json.data === true) {
            // 关闭成功
            this.showCloseDialog = false
            
            // 更新用户数据中的otp状态为0（已关闭）
            const userData = { ...this.userStore.userData, otp: 0 }
            this.userStore.setUserData(userData)
            
            this.showSuccessMessage('两步验证已成功关闭')
          } else {
            // 验证码错误或其他错误
            this.closeError = '验证码错误或关闭失败'
          }
        }
      )
    },

    // 生成二维码
    async generateQrCode(qrData) {
      try {
        // 构建otpauth URL
        const otpauthUrl = `otpauth://${qrData.type}/${encodeURIComponent(qrData.label)}?secret=${
          qrData.secret
        }&issuer=${encodeURIComponent(qrData.issuer)}&algorithm=${qrData.algorithm}&digits=${
          qrData.digits
        }&period=${qrData.period}`

        // 使用QRCode库生成二维码
        this.qrCodeUrl = await QRCode.toDataURL(otpauthUrl, {
          width: 200,
          margin: 2,
          color: {
            dark: '#000000',
            light: '#ffffff',
          },
        })
      } catch (error) {
        console.error('生成二维码失败:', error)
        this.showErrorMessage('生成二维码失败')
      }
    },

    // 确认设置
    confirmSetup() {
      this.showQrDialog = false
      this.showVerifyDialog = true
    },

    // 验证验证码
    verifyCode() {
      if (!this.verificationCode || this.verificationCode.length !== 6) {
        this.verifyError = '请输入6位验证码'
        return
      }

      this.httpPost(
        '/2fa/totp/check',
        {
          userId: this.twoFactorData.userId,
          code: this.verificationCode,
        },
        (json) => {
          if (json.data) {
            this.showVerifyDialog = false

            // 更新用户数据中的otp状态为1（已开启）
            const userData = { ...this.userStore.userData, otp: 1 }
            this.userStore.setUserData(userData)

            this.showSuccessMessage('两步验证已成功开启')
          } else {
            this.verifyError = json.message || '验证码无效'
          }
        }
      )
    },

    // 显示成功 / 失败消息的方法由 useMessage 提供（见 setup），此处不再重复实现
  },
}
</script>

<style>
.v-card {
  border-radius: 8px;
}

.v-card-title {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.user-select-all {
  user-select: all;
}
</style>