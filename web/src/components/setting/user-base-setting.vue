<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card
        class="mx-auto"
        outlined
      >
        <v-row justify="center">
          <v-col cols="10">
            <h2>基本信息修改</h2>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="8">
            <v-text-field
              v-model="userInfo.username"
              placeholder="用户名"
              label="用户名(25字以内)"
              clearable
              :rules="[() => userInfo.username != null || '标题不能为空']"
              :disabled="usernameAlter"
            />
          </v-col>
          <v-col cols="2">
            <v-btn color="primary" @click="usernameAlter = !usernameAlter">修改</v-btn>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="8">
            <v-textarea
              v-model="userInfo.introduction"
              label="简介"
              clearable
              placeholder="填写个人简介，让更多人认识你！"
              :disabled="introductionAlter"
            />
          </v-col>
          <v-col cols="2">
            <v-btn color="primary" @click="introductionAlter = !introductionAlter">修改</v-btn>
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="2">
            <v-btn color="primary" @click="save">保存</v-btn>
          </v-col>
          <v-col cols="8">
            说明： 点击保存后才会最终修改成功
          </v-col>
        </v-row>
      </v-card>
    </v-col>
    <v-snackbar
      v-model="showMessage"
      :top="true"
      :timeout="3000"
    >
      {{ message }}

      <template v-slot:action="{ attrs }">
        <v-btn
          color="pink"
          text
          v-bind="attrs"
          @click="showMessage = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </v-row>
</template>

<script>
export default {
  name: 'UserSetting',
  data() {
    return {
      userInfo: {
        username: ''
      },
      usernameAlter: true,
      introductionAlter: true,
      message: '',
      showMessage: false
    }
  },
  created() {
    this.userInfo = this.$store.state.userInfo
  },
  methods: {
    save() {
      fetch(`/api/user/update/info`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(this.userInfo)
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            this.$store.commit('setUserInfo', this.userInfo)
            this.message = '修改成功'
            this.showMessage = true
          } else {
            this.message = '修改失败！' + json.message
            this.showMessage = true
          }
        })
        .catch(e => {
          return null
        })
    }
  }
}
</script>

<style>

</style>
