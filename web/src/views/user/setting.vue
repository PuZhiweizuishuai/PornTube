<template>
  <v-container fill-height>
    <v-tabs>
      <v-tab @click="setType(0)">基本</v-tab>
      <v-tab @click="setType(1)">头像设置</v-tab>
      <v-tab @click="setType(2)">首页顶部图</v-tab>
      <v-tab @click="setType(3)">密码邮箱手机设置</v-tab>
      <v-tab @click="setType(4)">登录历史</v-tab>
    </v-tabs>
    <BaseSetting v-if="type == 0" />
    <HeadSetting v-if="type == 1" />
    <TopSetting v-if="type == 2" />
    <PasswordSetting v-if="type === 3" />
    <LoginLog v-if="type === 4" />
  </v-container>
</template>

<script>
import BaseSetting from '@/components/setting/user-base-setting.vue'
import HeadSetting from '@/components/setting/user-head-setting.vue'
import TopSetting from '@/components/setting/user-top-image.vue'
import PasswordSetting from '@/components/setting/user-password-setting.vue'
import LoginLog from '@/components/setting/user-login-log.vue'
export default {
  name: 'UserSetting',
  components: {
    BaseSetting,
    HeadSetting,
    TopSetting,
    PasswordSetting,
    LoginLog
  },
  data() {
    return {
      type: 0
    }
  },
  created() {

  },
  methods: {
    setType(value) {
      this.type = value
    }
  }
}
</script>

<style>

</style>
