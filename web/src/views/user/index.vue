<template>
  <div>
    <v-container fill-height fluid style="padding: 0px;">
      <v-row>
        <v-col style="padding: 0px;">
          <v-img :src="userInfo.topImgUrl" :aspect-ratio="5.98" />
        </v-col>
      </v-row>

    </v-container>
    <v-container fill-height>
      <v-row align="center">
        <v-col
          cols="12"
          md="8"
        >
          <v-avatar size="80" style="float: left;">
            <v-img :src="userInfo.avatarUrl" />
          </v-avatar>
          <h2 style="margin-top: 20px;margin-left: 100px;">
            {{ userInfo.username }}
            <v-chip v-if="Power.checkPower(userInfo) === 'vip'" color="yellow" @click="goTOVIP()">
              VIP
            </v-chip>
          </h2>
        </v-col>
        <v-col
          v-if="this.$store.state.userInfo"
          cols="6"
          md="4"
          class="hidden-sm-and-down ml-0 pl-4"
        >
          <v-btn color="primary">自定义频道</v-btn> <v-btn color="primary">创作中心</v-btn>
        </v-col>
      </v-row>

      <v-tabs>
        <v-tab>首页</v-tab>
        <v-tab>视频</v-tab>
        <v-tab>讨论</v-tab>
        <v-tab>简介</v-tab>
      </v-tabs>
    </v-container>
    <v-divider />
    <v-container fill-height>
      视频列表
    </v-container>
  </div>
</template>

<script>
import Power from '@/utils/check-power.vue'
export default {
  name: 'UserHome',
  data() {
    return {
      Power,
      id: 0,
      userInfo: {}
    }
  },
  created() {
    this.id = this.$route.params.id
    this.getUserInfo()
  },
  methods: {
    getUserInfo() {
      // console.log(this.$store.state.userInfo)
      if (this.$store.state.userInfo !== null && this.$store.state.userInfo.id === parseInt(this.id)) {
        this.userInfo = this.$store.state.userInfo
        return
      }
      fetch(`/api/user/info/${this.id}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          this.userInfo = json.data
          if (json.data.id === -1) {
            this.$router.push('/404')
          }
        })
        .catch(e => {
          return null
        })
    },
    goTOVIP() {
      this.$router.push('/vip')
    }
  }
}
</script>

<style>

</style>
