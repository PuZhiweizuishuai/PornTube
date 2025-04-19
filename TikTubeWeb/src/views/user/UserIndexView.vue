<!-- 用户首页 -->
<template>
  <v-container fluid>
    <!-- 首页顶部大图 -->
    <v-row v-if="userInfo != null">
      <v-col style="padding: 0px">
        <v-img :src="userInfo.topImgUrl" :aspect-ratio="5.98" />
      </v-col>
    </v-row>
    <v-container>
      <!-- 用户ID -->
      <v-row v-if="userInfo != null" justify="center" class="py-4">
        <v-col cols="12" md="8">
          <v-card flat class="pa-4">
            <div class="d-flex flex-column flex-sm-row align-start align-sm-center">
              <v-avatar size="80" class="mr-sm-4 mb-4 mb-sm-0">
                <v-img :src="userInfo.avatarUrl" alt="用户头像" />
              </v-avatar>
              <div class="flex-grow-1">
                <div class="d-flex align-center">
                  <h2 class="text-h5 font-weight-bold">{{ userInfo.username }}</h2>
                  <v-chip
                    v-if="Power.checkPower(userInfo) === 'vip'"
                    color="yellow"
                    class="ml-2"
                    @click="goTOVIP()"
                  >
                    <v-icon left size="small">mdi-crown</v-icon>
                    VIP
                  </v-chip>
                </div>
                <div class="text-subtitle-2 text-grey mt-1">
                  个性签名：{{ userInfo.introduction }}
                </div>

                <!-- 粉丝信息移动端显示 -->
                <div
                  v-if="!(loginUser && loginUser.id == id)"
                  class="d-flex d-md-none align-center mt-3"
                >
                  <div class="text-subtitle-1 mr-3">
                    粉丝: <span class="font-weight-bold">{{ userInfo.fansCount }}</span>
                  </div>
                  <v-btn
                    :color="isFollowed ? 'grey' : 'primary'"
                    size="small"
                    :prepend-icon="isFollowed ? 'mdi-account-check' : 'mdi-account-plus'"
                    @click="toggleFollow"
                  >
                    {{ isFollowed ? '已关注' : '关注' }}
                  </v-btn>
                </div>
              </div>
            </div>
          </v-card>
        </v-col>

        <!-- 登录用户自己的操作按钮 -->
        <v-col
          v-if="loginUser && loginUser.id == id"
          cols="12"
          md="4"
          class="d-flex align-center justify-center justify-md-end mt-2 mt-md-0"
        >
          <v-btn
            color="primary"
            variant="elevated"
            class="mx-1"
            prepend-icon="mdi-cog"
            @click="goToSetting"
          >
            自定义频道
          </v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            class="mx-1"
            prepend-icon="mdi-video"
            @click="goToStudio"
          >
            创作中心
          </v-btn>
        </v-col>

        <!-- 他人用户的粉丝关注情况，仅在平板和桌面显示 -->
        <v-col v-else cols="12" md="4" class="d-none d-md-flex align-center justify-end mt-md-0">
          <v-card flat class="pa-2">
            <div class="d-flex align-center">
              <div class="text-subtitle-1 mr-4">
                粉丝数： <span class="font-weight-bold">{{ userInfo.fansCount }}</span>
              </div>
              <v-btn
                :color="isFollowed ? 'grey' : 'primary'"
                variant="elevated"
                :prepend-icon="isFollowed ? 'mdi-account-check' : 'mdi-account-plus'"
                @click="toggleFollow"
              >
                {{ isFollowed ? '已关注' : '关注他' }}
              </v-btn>
            </div>
          </v-card>
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
    <v-container>
      <div id="top" />
      <div v-if="type == 4">
        <v-row>
          <v-col> 用户名: {{ userInfo.username }} </v-col>
        </v-row>
        <v-row>
          <v-col> 简介： {{ userInfo.introduction }} </v-col>
        </v-row>
        <v-row>
          <v-col> 视频总数： {{ userInfo.submitCount }} </v-col>
        </v-row>
        <v-row>
          <v-col> 粉丝数： {{ userInfo.fansCount }} </v-col>
        </v-row>
        <v-row>
          <v-col> 关注数： {{ userInfo.followCount }} </v-col>
        </v-row>
        <v-row>
          <v-col> 加入时间： {{ TimeUtil.renderTime(userInfo.createTime) }} </v-col>
        </v-row>
      </div>
      <v-row v-if="type != 4">
        <v-col v-for="item in videoList" :key="item.id" cols="12">
          <VideoListView :video="item" />
        </v-col>
      </v-row>
      <v-row justify="center" style="padding-top: 12px; padding-bottom: 12px">
        <v-pagination
          :total-visible="7"
          v-model="page"
          :length="length"
          rounded="circle"
          @update:model-value="pageChange"
        />
      </v-row>
    </v-container>

    <!-- 消息提示组件 -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar.show = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import Power from '@/utils/check-power.vue'
import TimeUtil from '@/utils/time-util.vue'
import { useUserStore } from '@/stores/userStore'
import { useWebInfoStore } from '@/stores/webInfoStore'
import VideoListView from '@/components/player/VideoList.vue'
export default {
  name: 'UserHome',
  components: {
    VideoListView,
  },
  data() {
    return {
      TimeUtil,
      Power,
      id: 0,
      userInfo: null,
      videoList: [],
      page: 1,
      size: 20,
      length: 1,
      totalCount: 0,
      type: 0,
      loginUser: useUserStore().userData,
      isFollowed: false, // 是否已关注
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
    }
  },
  created() {
    this.id = this.$route.params.id
    this.id = parseInt(this.$route.params.id)
    this.getUserInfo()
    this.geVideoList()
    this.checkFollow()
  },
  methods: {
    getUserInfo() {
      this.httpGet(`/user/info/${this.id}`, (json) => {
        if (json.data.id === -1) {
          this.$router.push('/')
          return
        }
        this.userInfo = json.data
        document.title = useWebInfoStore().webInfo.name + ' - ' + json.data.username
      })
    },
    geVideoList() {
      this.httpGet(
        `/article/user/list/${this.id}?page=${this.page}&limit=${this.size}&type=${this.type}`,
        (json) => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.totalCount = json.data.totalCount
        }
      )
    },
    followUser() {
      if (!this.loginUser) {
        this.showMessage('请先登录后再关注', 'error')
        return
      }
      const data = {
        followUser: this.id,
        createUser: this.loginUser.id,
      }
      this.httpPost('/follow/add', data, (json) => {
        if (json.data) {
          this.isFollowed = true
          this.userInfo.fansCount++
          this.showMessage('关注成功', 'success')
        } else {
          this.showMessage('关注失败，请稍后重试', 'error')
        }
      })
    },
    delelteFollow() {
      if (!this.loginUser) {
        this.showMessage('请先登录', 'error')
        return
      }
      const data = {
        followUser: this.id,
        createUser: this.loginUser.id,
      }
      this.httpPost('/follow/delete', data, (json) => {
        if (json.data) {
          this.isFollowed = false
          this.userInfo.fansCount--
          this.showMessage('已取消关注', 'info')
        } else {
          this.showMessage('取消关注失败，请稍后重试', 'error')
        }
      })
    },
    checkFollow() {
      if (!this.loginUser) {
        this.isFollowed = false
        return
      }

      this.httpGet(`/follow/check?followId=${this.id}`, (json) => {
        this.isFollowed = json.data
      })
    },
    // 切换关注状态
    toggleFollow() {
      if (!this.loginUser) {
        this.showMessage('请先登录后再关注', 'error')
        return
      }

      if (this.isFollowed) {
        this.delelteFollow()
      } else {
        this.followUser()
      }
    },
    // 显示消息提示
    showMessage(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
    pageChange(page) {
      this.page = page
      this.geVideoList(this.type)
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
    setType(type) {
      this.type = type
      this.page = 1
      if (type === 4) {
        return
      }
      this.geVideoList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
    goTOVIP() {
      this.$router.push('/vip')
    },
    goToStudio() {
      this.$router.push('/studio')
    },
    goToSetting() {
      this.$router.push('/user/setting')
    },
  },
}
</script>

<style>
</style>