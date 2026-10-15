<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center">
      <v-icon start>mdi-account-edit</v-icon>
      基本信息
    </v-card-title>
    <v-divider />

    <v-card-text class="pt-4">
      <v-row align="center">
        <v-col cols="12" sm="9">
          <v-text-field
            v-model="userInfo.username"
            placeholder="用户名"
            label="用户名(25字以内)"
            clearable
            variant="outlined"
            density="comfortable"
            bg-color="surface"
            :rules="[() => userInfo.username != null || '用户名不能为空']"
            :disabled="usernameAlter"
            prepend-inner-icon="mdi-account"
            hide-details="auto"
          />
        </v-col>
        <v-col cols="12" sm="3">
          <v-btn
            color="primary"
            @click="usernameAlter = !usernameAlter"
            :variant="usernameAlter ? 'outlined' : 'elevated'"
            prepend-icon="mdi-pencil"
            block
          >
            {{ usernameAlter ? '修改' : '取消' }}
          </v-btn>
        </v-col>
      </v-row>

      <v-row align="center">
        <v-col cols="12" sm="9">
          <v-textarea
            v-model="userInfo.introduction"
            label="简介"
            clearable
            variant="outlined"
            auto-grow
            placeholder="填写个人简介，让更多人认识你！"
            :disabled="introductionAlter"
            prepend-inner-icon="mdi-information"
            rows="3"
            hide-details="auto"
          />
        </v-col>
        <v-col cols="12" sm="3">
          <v-btn
            color="primary"
            @click="introductionAlter = !introductionAlter"
            :variant="introductionAlter ? 'outlined' : 'elevated'"
            prepend-icon="mdi-pencil"
            block
          >
            {{ introductionAlter ? '修改' : '取消' }}
          </v-btn>
        </v-col>
      </v-row>
    </v-card-text>

    <v-divider />

    <v-card-actions class="pa-4">
      <v-btn color="primary" @click="save" prepend-icon="mdi-content-save"> 保存 </v-btn>
      <v-spacer />
      <span class="text-caption text-medium-emphasis">点击保存后才会最终修改成功</span>
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
  name: 'UserBaseSetting',
  data() {
    return {
      userInfo: {
        username: '',
        introduction: '',
      },
      usernameAlter: true,
      introductionAlter: true,
      message: '',
      showMessage: false,
    }
  },
  created() {
    this.userInfo = useUserStore().userData
  },
  methods: {
    save() {
      if (this.userInfo.username.length > 50) {
        this.message = '用户名字数不能超过50字'
        this.showMessage = true
        return
      }
      if (this.userInfo.introduction.length > 100) {
        this.message = '简介字数不能超过100字'
        this.showMessage = true
        return
      }
      this.httpPost('/user/update/info', this.userInfo, (json) => {
        if (json.status === 200) {
          // 更新 Pinia store 中的数据
          const userStore = useUserStore()
          userStore.setUserData(this.userInfo)
          this.message = '修改成功'
          this.showMessage = true

          // 恢复禁用状态
          this.usernameAlter = true
          this.introductionAlter = true
        } else {
          this.message = '修改失败！' + json.message
          this.showMessage = true
        }
      })
    },
  },
}
</script>
  