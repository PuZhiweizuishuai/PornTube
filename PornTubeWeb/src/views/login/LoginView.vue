<template>
  <v-main>
    <v-container fill-height>
      <v-row justify="center" align="center">
        <v-col cols="12">
          <v-card class="mx-auto" max-width="500">
            <v-col></v-col>
            <v-row style="height: 64px" justify="center">
              <v-col cols="10">
                <v-btn variant="text" @click="backHome">
                  <v-icon> mdi-arrow-left </v-icon>
                  返回
                </v-btn>
              </v-col>
            </v-row>
            <v-row justify="center">
              <img :src="webInfo.logoUrl" height="36px" />
            </v-row>
            <v-col></v-col>
            <v-row justify="center">
              <h1>{{ webInfo.name }} &nbsp; {{ type }}</h1>
            </v-row>
            <v-row style="height: 48px" />
            <LoginFrom v-show="showLogin" @login="userLogin" />
            <RegisterFrom v-show="showLogin == false" @register="register" />
            <v-col></v-col>
            <v-row justify="center">
              <v-col cols="5">
                <v-btn variant="text" color="primary">忘记密码</v-btn>
              </v-col>
              <v-col cols="5" style="text-align: right">
                <v-btn variant="text" @click="moveRegister">{{ moveMessage }}</v-btn>
              </v-col>
            </v-row>
            <v-col></v-col>
          </v-card>
        </v-col>
      </v-row>
      <v-snackbar v-model="showMessage" :timeout="3000">
        {{ message }}
        <template v-slot:actions>
          <v-btn color="pink" variant="text" @click="showMessage = false"> 关闭 </v-btn>
        </template>
      </v-snackbar>
    </v-container>
  </v-main>
</template>

<script>
import LoginFrom from '@/components/form/LoginForm.vue'
import RegisterFrom from '@/components/form/RegisterForm.vue'
import { useWebInfoStore } from '@/stores/webInfoStore'
import { useUserStore } from '@/stores/userStore'
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
    }
  },
  created() {
    this.webInfo = useWebInfoStore().webInfo
  },
  methods: {
    userLogin(value) {
      this.httpPost('/login', value, (json) => {
        if (json.status === 200) {
          const userData = json.data
          // 保存用户
          this.user.setUserData(userData)
          // 检查是否有重定向参数
          const redirect = this.$route.query.redirect
          // 如果有重定向参数，则跳转到指定页面，否则跳转到首页
          this.$router.push(redirect || '/')
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    register(value) {
      this.httpPost('/register', value, (json) => {
        if (json.status === 200) {
          this.message = '注册成功，即将为你跳转到登录页面！'
          this.showMessage = true
          this.moveRegister()
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
  },
}
</script>

<style></style>
