<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card class="mx-auto" elevation="2" rounded="lg">
        <v-card-title class="d-flex align-center py-4">
          <v-icon icon="mdi-account-edit" class="mr-2" size="large" color="primary" />
          <h2>基本信息修改</h2>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text>
          <v-row justify="center">
            <v-col cols="12" md="8">
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
              />
            </v-col>
            <v-col cols="12" md="2" class="d-flex align-center">
              <v-btn
                color="primary"
                @click="usernameAlter = !usernameAlter"
                :variant="usernameAlter ? 'outlined' : 'elevated'"
                prepend-icon="mdi-pencil"
              >
                {{ usernameAlter ? '修改' : '取消' }}
              </v-btn>
            </v-col>
          </v-row>

          <v-row justify="center">
            <v-col cols="12" md="8">
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
              />
            </v-col>
            <v-col cols="12" md="2" class="d-flex align-center">
              <v-btn
                color="primary"
                @click="introductionAlter = !introductionAlter"
                :variant="introductionAlter ? 'outlined' : 'elevated'"
                prepend-icon="mdi-pencil"
              >
                {{ introductionAlter ? '修改' : '取消' }}
              </v-btn>
            </v-col>
          </v-row>
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
          <v-chip color="info" variant="outlined">
            <v-icon start icon="mdi-information"></v-icon>
            点击保存后才会最终修改成功
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
  
<style>
.v-card-title {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}
</style>
  