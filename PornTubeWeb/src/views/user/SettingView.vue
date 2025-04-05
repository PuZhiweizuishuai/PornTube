<template>
  <v-container>
    <v-tabs color="blue">
      <v-tab @click="setType(0)">基本</v-tab>
      <v-tab @click="setType(1)">头像设置</v-tab>
      <v-tab @click="setType(2)">首页顶部图</v-tab>
      <v-tab @click="setType(3)">密码邮箱手机设置</v-tab>
      <v-tab @click="setType(4)">登录历史</v-tab>
    </v-tabs>
    <UserBaseSetting v-if="type == 0" />
    <UserHeadSetting v-if="type == 1" />
    <UserTopImage v-if="type == 2" />
    <UserPasswordSetting v-if="type === 3" />
    <UserLoginLog v-if="type === 4" />
  </v-container>
</template>

<script>
import UserBaseSetting from '@/components/setting/UserBaseSetting.vue'
import UserHeadSetting from '@/components/setting/UserHeadSetting.vue'
import UserLoginLog from '@/components/setting/UserLoginLog.vue'
import UserPasswordSetting from '@/components/setting/UserPasswordSetting.vue'
import UserTopImage from '@/components/setting/UserTopImage.vue'

export default {
  name: 'UserSetting',
  components: {
    UserBaseSetting,
    UserLoginLog,
    UserPasswordSetting,
    UserHeadSetting,
    UserTopImage,
  },
  data() {
    return {
      type: 0,
    }
  },
  created() {},
  methods: {
    setType(value) {
      this.type = value
    },
  },
}
</script>

<style>
</style>