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
          <v-btn color="primary" @click="goToSetting">自定义频道</v-btn> <v-btn color="primary" @click="goToStudio">创作中心</v-btn>
        </v-col>
        <v-col
          v-if="this.$store.state.userInfo == null"
          cols="6"
          md="4"
        >
          粉丝数：  {{ userInfo.fansCount }} <v-btn color="primary">关注他</v-btn>
        </v-col>
      </v-row>

      <v-tabs>
        <v-tab @click="setType(0)">首页</v-tab>
        <v-tab @click="setType(0)">视频</v-tab>
        <v-tab @click="setType(2)">讨论</v-tab>
        <v-tab @click="setType(4)">简介</v-tab>
      </v-tabs>
    </v-container>
    <v-divider />
    <v-container fill-height>
      <div id="top" />
      <div v-if="type == 4">
        <v-row>
          <v-col>
            用户名: {{ userInfo.username }}
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            简介： {{ userInfo.introduction }}
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            视频总数： {{ userInfo.submitCount }}
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            粉丝数： {{ userInfo.fansCount }}
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            关注数： {{ userInfo.followCount }}
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            加入时间： {{ TimeUtil.renderTime(userInfo.createTime) }}
          </v-col>
        </v-row>
      </div>
      <v-row v-if="type != 4">
        <v-col
          v-for="item in videoList"
          :key="item.id"
          cols="12"
        >

          <VideoList :video="item" />
        </v-col>
      </v-row>
      <v-pagination
        v-if="type != 4"
        v-model="page"
        :length="length"
        @input="pageChange"
      />
    </v-container>
  </div>
</template>

<script>
import Power from '@/utils/check-power.vue'
import VideoList from '@/components/player/video-list.vue'
import TimeUtil from '@/utils/time-util.vue'
export default {
  name: 'UserHome',
  components: {
    VideoList
  },
  data() {
    return {
      TimeUtil,
      Power,
      id: 0,
      userInfo: {},
      videoList: [],
      page: 1,
      size: 20,
      length: 1,
      totalCount: 0,
      type: 0
    }
  },
  created() {
    this.id = this.$route.params.id
    this.id = parseInt(this.$route.params.id)
    this.getUserInfo()
    this.geVideoList()
  },
  methods: {
    getUserInfo() {
      // console.log(this.$store.state.userInfo)
      if (this.$store.state.userInfo !== null && this.$store.state.userInfo.id === this.id) {
        this.userInfo = this.$store.state.userInfo
        document.title = this.userInfo.username
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
          document.title = json.data.username
          if (json.data.id === -1) {
            this.$router.push('/404')
          }
        })
        .catch(e => {
          return null
        })
    },
    geVideoList() {
      fetch(`/api/article/user/list/${this.id}?page=${this.page}&limit=${this.size}&type=${this.type}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.totalCount = json.data.totalCount
        })
        .catch(e => {
          return null
        })
    },
    pageChange(page) {
      this.page = page
      this.geVideoList(this.type)
    },
    setType(type) {
      this.type = type
      this.page = 1
      if (type === 4) {
        return
      }
      this.geVideoList()
      this.$vuetify.goTo(0)
    },
    goTOVIP() {
      this.$router.push('/vip')
    },
    goToStudio() {
      this.$router.push('/studio')
    },
    goToSetting() {
      this.$router.push('/user/setting')
    }
  }
}
</script>

<style>

</style>
