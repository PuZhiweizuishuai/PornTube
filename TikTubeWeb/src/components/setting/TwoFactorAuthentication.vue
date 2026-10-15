<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center flex-wrap ga-2">
      <v-icon start>mdi-shield-lock-outline</v-icon>
      两步验证
      <v-spacer />
      <v-chip v-if="!isTwoFactorEnabled" color="warning" variant="tonal" size="small">
        <v-icon start size="small">mdi-alert-circle-outline</v-icon>
        建议开启两步验证以提高账户安全性
      </v-chip>
      <v-chip v-else color="success" variant="tonal" size="small">
        <v-icon start size="small">mdi-shield-check</v-icon>
        已开启两步验证
      </v-chip>
    </v-card-title>
    <v-divider />

    <v-card-text class="pt-4">
      <v-alert
        :color="isTwoFactorEnabled ? 'success' : 'info'"
        :icon="isTwoFactorEnabled ? 'mdi-shield-check' : 'mdi-shield-outline'"
        variant="tonal"
      >
        <p class="text-body-1">
          {{ isTwoFactorEnabled ? '您已开启两步验证，账户安全性得到了提升。' : '开启两步验证后，您将需要在登录时输入验证码，这将大大提高账户安全性。' }}
        </p>
        <p class="text-body-2 mt-2" v-if="!isTwoFactorEnabled">
          您需要使用身份验证器应用（如Google Authenticator、Microsoft Authenticator或Authy）来生成验证码。
        </p>
      </v-alert>
    </v-card-text>

    <v-divider />

    <v-card-actions class="pa-4 flex-wrap ga-2">
      <v-btn
        color="primary"
        @click="handleTOTP"
        prepend-icon="mdi-shield-key-outline"
        variant="tonal"
      >
        {{ isTwoFactorEnabled ? '重新生成密钥' : '开启两步验证' }}
      </v-btn>
      <v-btn
        v-if="isTwoFactorEnabled"
        color="warning"
        @click="showRecoveryDialog = true"
        prepend-icon="mdi-key-variant"
        variant="tonal"
      >
        使用恢复密钥
      </v-btn>
      <v-btn
        v-if="isTwoFactorEnabled"
        color="error"
        @click="showCloseDialog = true"
        prepend-icon="mdi-shield-off-outline"
        variant="tonal"
      >
        关闭两步验证
      </v-btn>
    </v-card-actions>
  </v-card>

  <!-- 二维码弹窗 -->
  <v-dialog v-model="showQrDialog" max-width="500">
    <v-card rounded="lg">
      <v-card-title class="d-flex align-center">
        <v-icon start>mdi-qrcode-scan</v-icon>
        扫描二维码
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" size="small" @click="closeQrDialog" />
      </v-card-title>
      <v-divider />

      <v-card-text class="pt-4 text-center">
        <p class="mb-4">请使用身份验证器应用扫描以下二维码</p>
        <div v-if="qrCodeUrl" class="d-flex justify-center mb-4">
          <img :src="qrCodeUrl" alt="二维码" class="rounded bg-white pa-2" />
        </div>
        <v-alert v-if="secretKey" type="info" class="mb-4" variant="tonal" align="start">
          <p>如果无法扫描二维码，请手动输入以下密钥：</p>
          <p class="font-weight-bold text-h6 mt-2 user-select-all">{{ secretKey }}</p>
        </v-alert>
        <v-alert v-if="recoveryCode" type="warning" class="mb-4" variant="tonal" align="start">
          <p>请保存好以下恢复码，当您无法使用验证器时可用于恢复账户：</p>
          <p class="font-weight-bold text-h6 mt-2 user-select-all">{{ recoveryCode }}</p>
          <p class="text-caption mt-2">
            <v-icon size="small" class="mr-1">mdi-information-outline</v-icon>
            请将此恢复码保存在安全的地方，关闭窗口后将无法再次查看
          </p>
        </v-alert>
      </v-card-text>

      <v-divider />
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn color="grey-darken-1" variant="text" @click="closeQrDialog"> 关闭 </v-btn>
        <v-btn color="primary" prepend-icon="mdi-check-circle-outline" @click="confirmSetup">
          确认已设置
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- 验证码输入弹窗 -->
  <v-dialog v-model="showVerifyDialog" max-width="500">
    <v-card rounded="lg">
      <v-card-title class="d-flex align-center">
        <v-icon start>mdi-numeric</v-icon>
        验证设置
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" size="small" @click="closeVerifyDialog" />
      </v-card-title>
      <v-divider />

      <v-card-text class="pt-4">
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
          autofocus
        ></v-text-field>
      </v-card-text>

      <v-divider />
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn color="grey-darken-1" variant="text" @click="closeVerifyDialog"> 取消 </v-btn>
        <v-btn color="primary" prepend-icon="mdi-check" @click="verifyCode"> 验证 </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- 重新生成验证码输入弹窗 -->
  <v-dialog v-model="showRegenerateDialog" max-width="500">
    <v-card rounded="lg">
      <v-card-title class="d-flex align-center">
        <v-icon start>mdi-shield-refresh-outline</v-icon>
        验证身份
        <v-spacer />
        <v-btn
          icon="mdi-close"
          variant="text"
          size="small"
          @click="showRegenerateDialog = false"
        />
      </v-card-title>
      <v-divider />

      <v-card-text class="pt-4">
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
          autofocus
        ></v-text-field>
      </v-card-text>

      <v-divider />
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn color="grey-darken-1" variant="text" @click="showRegenerateDialog = false">
          取消
        </v-btn>
        <v-btn color="primary" prepend-icon="mdi-check" @click="regenerateKey"> 验证 </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- 恢复密钥输入弹窗 -->
  <v-dialog v-model="showRecoveryDialog" max-width="500">
    <v-card rounded="lg">
      <v-card-title class="d-flex align-center">
        <v-icon start color="warning">mdi-key-variant</v-icon>
        使用恢复密钥
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" size="small" @click="showRecoveryDialog = false" />
      </v-card-title>
      <v-divider />

      <v-card-text class="pt-4">
        <p class="mb-4">如果您无法访问验证器应用，请输入之前保存的恢复密钥</p>
        <v-text-field
          v-model="recoveryKeyInput"
          label="恢复密钥"
          variant="outlined"
          density="comfortable"
          :error-messages="recoveryError"
          prepend-inner-icon="mdi-key"
          autofocus
        ></v-text-field>
      </v-card-text>

      <v-divider />
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn color="grey-darken-1" variant="text" @click="showRecoveryDialog = false"> 取消 </v-btn>
        <v-btn color="warning" prepend-icon="mdi-key-link" @click="recoverWithKey"> 恢复 </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

  <!-- 关闭两步验证弹窗 -->
  <v-dialog v-model="showCloseDialog" max-width="500">
    <v-card rounded="lg">
      <v-card-title class="d-flex align-center">
        <v-icon start color="error">mdi-shield-off-outline</v-icon>
        关闭两步验证
        <v-spacer />
        <v-btn icon="mdi-close" variant="text" size="small" @click="showCloseDialog = false" />
      </v-card-title>
      <v-divider />

      <v-card-text class="pt-4">
        <v-alert color="warning" icon="mdi-alert-outline" variant="tonal" class="mb-4">
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
          autofocus
        ></v-text-field>
      </v-card-text>

      <v-divider />
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn color="grey-darken-1" variant="text" @click="showCloseDialog = false"> 取消 </v-btn>
        <v-btn color="error" prepend-icon="mdi-shield-off-outline" @click="closeTwoFactor">
          关闭两步验证
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>

    <!-- 消息提示统一由 useMessage + AppSnackbar 承载 -->
    <AppSnackbar :state="toast" />
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

    // 开启TOTP两步验证（服务端只生成待确认密钥，确认后才生效）
    openTOTP() {
      this.httpPost('/2fa/totp/create', {}, (json) => {
        // 创建成功
        if (json.status === 200 && json.data) {
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

    // 作废服务端待确认密钥（两步验证保持原状态）
    cancelPendingSetup() {
      this.httpPost('/2fa/totp/cancel', {}, () => {})
    },

    // 关闭二维码弹窗 = 放弃本次设置
    closeQrDialog() {
      this.showQrDialog = false
      this.cancelPendingSetup()
    },

    // 取消验证码确认 = 放弃本次设置
    closeVerifyDialog() {
      this.showVerifyDialog = false
      this.cancelPendingSetup()
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
            this.showSuccessMessage('新密钥已生成，扫码确认后才会生效')
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
            this.showSuccessMessage('恢复码校验成功，扫码确认新密钥后才会生效')
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

<style scoped>
.user-select-all {
  user-select: all;
}
</style>