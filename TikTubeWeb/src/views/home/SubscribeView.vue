<template>
  <v-container fluid class="pa-0">
    <!-- 未登录提示 -->
    <v-container v-if="userInfo.userData == null">
      <NotLoginCard></NotLoginCard>
    </v-container>

    <!-- 已登录内容 -->
    <div v-else>
      <!-- 页面标题区域 -->
      <v-container class="py-2">
        <v-row align="center" class="mb-2">
          <v-col cols="12">
            <div class="d-flex align-center">
              <v-icon icon="mdi-youtube-subscription" color="red" size="32" class="mr-3"></v-icon>
              <h2 class="text-h5 font-weight-bold mb-0">订阅</h2>
            </div>
            <p class="text-body-2 text-medium-emphasis mt-1 ml-8">你关注的那些博主的最新动态</p>
          </v-col>
        </v-row>
      </v-container>

      <v-divider></v-divider>

      <!-- 关注用户区域 -->
      <v-container>
        <v-row>
          <v-col cols="12">
            <div class="d-flex align-center mb-3">
              <h3 class="text-subtitle-1 font-weight-medium mb-0">关注的创作者</h3>
              <v-spacer></v-spacer>
              <v-btn
                v-if="followUsersTotalCount > followUsersSize"
                variant="text"
                color="primary"
                density="comfortable"
                @click="showAllFollowUsers"
              >
                查看全部
                <v-icon end>mdi-chevron-right</v-icon>
              </v-btn>
            </div>

            <!-- 关注用户加载状态 -->
            <v-card v-if="loadingUsers" variant="flat" class="mb-4 py-6 bg-grey-lighten-4">
              <div class="d-flex justify-center align-center">
                <v-progress-circular
                  indeterminate
                  color="primary"
                  :size="36"
                  :width="3"
                ></v-progress-circular>
                <span class="ml-4 text-body-2">正在加载关注的创作者...</span>
              </div>
            </v-card>

            <!-- 无关注用户状态 -->
            <v-card
              v-else-if="followUsers.length === 0"
              variant="flat"
              class="mb-4 pa-6 bg-grey-lighten-4"
            >
              <div class="text-center">
                <v-avatar class="mb-4 bg-grey-lighten-2" size="80">
                  <v-icon size="40" color="grey-darken-1">mdi-account-multiple</v-icon>
                </v-avatar>
                <h3 class="text-subtitle-1 font-weight-medium mb-2">还没有关注任何创作者</h3>
                <p class="text-body-2 text-medium-emphasis mb-4">
                  关注你喜欢的创作者，获取他们的最新视频和动态
                </p>
                <v-btn color="primary" to="/" variant="tonal" prepend-icon="mdi-compass">
                  探索更多创作者
                </v-btn>
              </div>
            </v-card>

            <!-- 关注用户列表 -->
            <v-card v-else variant="flat" class="mb-4 pa-0 overflow-hidden">
              <v-slide-group show-arrows class="py-3">
                <v-slide-group-item v-for="user in followUsers" :key="user.id">
                  <v-hover v-slot="{ isHovering, props }">
                    <v-card
                      v-bind="props"
                      :elevation="isHovering ? 3 : 0"
                      class="ma-2 rounded-lg"
                      width="120"
                      height="160"
                      :class="isHovering ? 'on-hover' : ''"
                      @click="goToUserPage(user.id)"
                    >
                      <div class="d-flex flex-column align-center py-3 px-2 h-100 mb-2">
                        <v-avatar size="70" class="mb-2">
                          <v-img :src="user.avatarUrl" :alt="user.username" cover></v-img>
                        </v-avatar>
                        <div class="text-center mb-2">
                          <div
                            class="text-subtitle-2 font-weight-medium text-truncate"
                            style="max-width: 110px"
                          >
                            {{ user.username }}
                          </div>
                          <div class="text-caption text-medium-emphasis mt-1">
                            {{ user.fansCount }} 粉丝
                          </div>
                          <v-chip
                            v-if="user.friend"
                            size="x-small"
                            color="primary"
                            variant="flat"
                            class="mt-2 mb-2"
                            density="comfortable"
                          >
                            <v-icon start size="x-small">mdi-account-multiple-check</v-icon>
                            互相关注
                          </v-chip>
                        </div>
                      </div>
                    </v-card>
                  </v-hover>
                </v-slide-group-item>
              </v-slide-group>
            </v-card>
          </v-col>
        </v-row>
      </v-container>

      <v-divider></v-divider>

      <!-- 订阅视频区域 -->
      <v-container>
        <div class="d-flex align-center mb-4">
          <h3 class="text-subtitle-1 font-weight-medium">最新视频</h3>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            variant="text"
            density="comfortable"
            prepend-icon="mdi-refresh"
            :disabled="loading || refreshing"
            :loading="refreshing"
            @click="refreshSubscriptions"
          >
            刷新
          </v-btn>
        </div>

        <!-- 视频加载状态 -->
        <v-sheet v-if="loading" class="d-flex flex-column align-center justify-center py-8">
          <v-progress-circular
            indeterminate
            color="primary"
            :size="50"
            :width="5"
            class="mb-4"
          ></v-progress-circular>
          <span class="text-body-1 text-medium-emphasis">正在加载订阅内容...</span>
        </v-sheet>

        <!-- 无订阅内容状态 -->
        <v-sheet
          v-else-if="videoList.length === 0"
          class="pa-8 text-center rounded-lg bg-grey-lighten-4"
        >
          <v-icon icon="mdi-playlist-remove" size="72" color="grey-darken-1" class="mb-4"></v-icon>
          <h3 class="text-h5 mb-2">暂无订阅内容</h3>
          <p class="text-body-1 text-medium-emphasis mb-6">
            关注更多创作者，这里将会显示他们的最新视频
          </p>
          <v-btn color="primary" size="large" to="/" prepend-icon="mdi-home" variant="elevated">
            浏览首页
          </v-btn>
        </v-sheet>

        <!--             cols="12"
            sm="6"
            md="4"
            xl="3"
            class="d-flex" -->
        <!-- 订阅视频网格布局 -->
        <v-row v-else>
          <v-col v-for="video in videoList" :key="video.id">
            <VideoCard :video="video" />
          </v-col>
        </v-row>

        <!-- 分页控件 -->
        <div v-if="videoList.length > 0 && totalPage > 1" class="d-flex justify-center mt-6 mb-4">
          <v-pagination
            v-model="page"
            :length="totalPage"
            :total-visible="7"
            rounded="circle"
            @update:model-value="loadSubscribedVideos"
          ></v-pagination>
        </div>
      </v-container>
    </div>

    <!-- 消息提示组件 -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn icon variant="text" @click="snackbar.show = false">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </template>
    </v-snackbar>

    <!-- 全部关注用户对话框 -->
    <v-dialog v-model="followUsersDialog" max-width="800" transition="dialog-bottom-transition">
      <v-card>
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-white">全部关注的创作者</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="followUsersDialog = false">
            <v-icon color="white">mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="pa-4">
          <v-row v-if="allFollowUsers.length === 0" justify="center" class="my-8">
            <v-progress-circular
              indeterminate
              color="primary"
              :size="40"
              :width="3"
            ></v-progress-circular>
          </v-row>

          <v-row v-else>
            <v-col
              v-for="user in allFollowUsers"
              :key="user.id"
              cols="6"
              sm="4"
              md="3"
              class="d-flex"
            >
              <v-hover v-slot="{ isHovering, props }">
                <v-card
                  v-bind="props"
                  :elevation="isHovering ? 3 : 1"
                  class="ma-2 rounded-lg flex-grow-1"
                  :class="isHovering ? 'on-hover' : ''"
                  @click="goToUserPage(user.id)"
                >
                  <div class="d-flex flex-column align-center pa-3">
                    <v-avatar size="70" class="mb-3">
                      <v-img :src="user.avatarUrl" :alt="user.username" cover></v-img>
                    </v-avatar>
                    <div class="text-center">
                      <div class="d-flex align-center justify-center">
                        <div
                          class="text-subtitle-2 font-weight-medium text-truncate"
                          style="max-width: 110px"
                        >
                          {{ user.username }}
                        </div>
                        <v-tooltip v-if="user.friend" location="top">
                          <template v-slot:activator="{ props }">
                            <v-icon
                              v-bind="props"
                              color="primary"
                              size="small"
                              class="ml-1"
                              icon="mdi-account-multiple-check"
                            ></v-icon>
                          </template>
                          <span>互相关注</span>
                        </v-tooltip>
                      </div>
                      <div class="text-caption text-medium-emphasis mt-1">
                        {{ user.fansCount }} 粉丝，{{ user.submitCount }} 投稿
                      </div>
                      <div class="d-flex align-center justify-center mt-1">
                        <v-chip
                          v-if="user.friend"
                          size="x-small"
                          color="primary"
                          variant="flat"
                          class="mb-2"
                        >
                          互相关注
                        </v-chip>
                      </div>
                      <v-btn
                        size="small"
                        class="mt-1"
                        color="primary"
                        variant="tonal"
                        block
                        :to="`/user/${user.id}`"
                      >
                        查看
                      </v-btn>
                    </div>
                  </div>
                </v-card>
              </v-hover>
            </v-col>
          </v-row>

          <!-- 分页控件 - 全部用户弹窗 -->
          <div v-if="followUsersTotalPage > 1" class="d-flex justify-center mt-4">
            <v-pagination
              v-model="followUsersPage"
              :length="followUsersTotalPage"
              :total-visible="7"
              rounded="circle"
              @update:model-value="loadAllFollowUsers"
            ></v-pagination>
          </div>
        </v-card-text>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
import NotLoginCard from '@/components/card/NotLoginCard.vue'
import VideoCard from '@/components/card/VideoCard.vue'
import { useUserStore } from '@/stores/userStore'
export default {
  components: {
    NotLoginCard,
    VideoCard,
  },
  data() {
    return {
      userInfo: useUserStore(),
      videoList: [],
      loading: true,
      refreshing: false,
      page: 1,
      size: 20,
      totalPage: 1,
      totalCount: 0,
      // 关注用户数据
      followUsers: [],
      allFollowUsers: [],
      loadingUsers: true,
      followUsersPage: 1,
      followUsersSize: 10,
      followUsersTotalPage: 1,
      followUsersTotalCount: 0,
      followUsersDialog: false,
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
    }
  },
  created() {
    document.title = '订阅 - TikTube'
    if (this.userInfo.userData) {
      this.loadSubscribedVideos()
      this.loadFollowUsers()
    }
  },
  methods: {
    loadSubscribedVideos() {
      this.loading = true

      // 构建请求参数
      const params = `?page=${this.page}&limit=${this.size}`

      this.httpGet(`/follow/follow/article${params}`, (json) => {
        if (json.status === 200) {
          this.videoList = json.data.list || []
          this.page = json.data.currPage
          this.totalPage = json.data.totalPage
          this.totalCount = json.data.totalCount

          // 如果没有数据但不是第一页，回到第一页
          if (this.videoList.length === 0 && this.page > 1) {
            this.page = 1
            this.loadSubscribedVideos()
            return
          }

          this.loading = false
        } else {
          this.showMessage('获取订阅内容失败，请稍后重试', 'error')
          this.loading = false
        }
      })
    },

    // 加载关注的用户
    loadFollowUsers() {
      this.loadingUsers = true

      // 构建请求参数 - 只加载前10个关注用户
      const params = `?page=1&limit=${this.followUsersSize}`

      this.httpGet(`/follow/follow${params}`, (json) => {
        if (json.status === 200) {
          this.followUsers = json.data.list || []
          this.followUsersTotalCount = json.data.totalCount
          this.followUsersTotalPage = json.data.totalPage
          this.loadingUsers = false
        } else {
          this.showMessage('获取关注创作者失败', 'error')
          this.loadingUsers = false
        }
      })
    },

    // 加载所有关注用户（分页）- 用于弹窗
    loadAllFollowUsers() {
      const params = `?page=${this.followUsersPage}&limit=${this.followUsersSize}`

      this.httpGet(`/follow/follow${params}`, (json) => {
        if (json.status === 200) {
          this.allFollowUsers = json.data.list || []
          this.followUsersTotalCount = json.data.totalCount
          this.followUsersTotalPage = json.data.totalPage
        } else {
          this.showMessage('获取关注创作者失败', 'error')
        }
      })
    },

    // 显示所有关注用户弹窗
    showAllFollowUsers() {
      this.followUsersPage = 1
      this.loadAllFollowUsers()
      this.followUsersDialog = true
    },

    // 跳转到用户主页
    goToUserPage(userId) {
      this.$router.push(`/user/${userId}`)
    },

    refreshSubscriptions() {
      this.refreshing = true
      this.page = 1

      this.httpGet(`/follow/follow/article?page=1&limit=${this.size}`, (json) => {
        if (json.status === 200) {
          this.videoList = json.data.list || []
          this.page = json.data.currPage
          this.totalPage = json.data.totalPage
          this.totalCount = json.data.totalCount
          this.showMessage('订阅内容已更新', 'success')
        } else {
          this.showMessage('刷新失败，请稍后重试', 'error')
        }
        this.refreshing = false
      })
    },

    showMessage(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
  },
}
</script>

<style scoped>
.on-hover {
  transform: translateY(-4px);
  transition: transform 0.3s ease;
}

.v-card {
  transition: all 0.3s ease;
}

@media (max-width: 600px) {
  .v-slide-group__content {
    padding-left: 16px;
  }
}

/* 确保VideoCard组件铺满整个列
.v-col {
  display: flex;
}
.v-col > * {
  flex-grow: 1;
} */
</style>