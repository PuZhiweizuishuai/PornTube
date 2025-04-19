<template>
  <v-container fluid class="pa-4">
    <!-- 页面标题 -->
    <div class="d-flex align-center mb-6">
      <v-icon icon="mdi-account-group" color="primary" size="32" class="mr-3"></v-icon>
      <h1 class="text-h5 font-weight-bold mb-0">粉丝管理</h1>
    </div>

    <!-- 搜索和筛选工具栏 -->
    <!-- <v-card class="mb-6" variant="flat">
      <v-card-text>
        <v-row align="center">
          <v-col cols="12" sm="6" md="4">
            <v-text-field
              v-model="searchQuery"
              label="搜索粉丝"
              density="comfortable"
              variant="outlined"
              hide-details
              prepend-inner-icon="mdi-magnify"
              clearable
              @update:model-value="searchFans"
            ></v-text-field>
          </v-col>
          <v-spacer></v-spacer>
          <v-col cols="auto">
            <v-btn
              color="primary"
              variant="text"
              prepend-icon="mdi-refresh"
              :loading="loading"
              @click="loadFans"
            >
              刷新
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card> -->

    <!-- 加载中状态 -->
    <v-sheet v-if="loading" class="d-flex flex-column align-center justify-center py-10">
      <v-progress-circular
        indeterminate
        color="primary"
        :size="50"
        :width="5"
        class="mb-4"
      ></v-progress-circular>
      <span class="text-body-1 text-medium-emphasis">正在加载粉丝数据...</span>
    </v-sheet>

    <!-- 无粉丝状态 -->
    <v-sheet v-else-if="fans.length === 0" class="pa-8 text-center rounded-lg bg-grey-lighten-4">
      <v-icon
        icon="mdi-account-multiple-remove"
        size="72"
        color="grey-darken-1"
        class="mb-4"
      ></v-icon>
      <h3 class="text-h5 mb-2">暂无粉丝</h3>
      <p class="text-body-1 text-medium-emphasis mb-6">继续创作优质内容，会有更多人关注你</p>
      <v-btn color="primary" variant="elevated" to="/studio" prepend-icon="mdi-video">
        创作中心
      </v-btn>
    </v-sheet>

    <!-- 粉丝列表 -->
    <template v-else>
      <v-card variant="flat" class="mb-2">
        <v-card-text class="px-0 py-2">
          <span class="text-subtitle-1 font-weight-medium">{{ totalCount }} 位粉丝</span>
        </v-card-text>
      </v-card>

      <v-card v-for="user in fans" :key="user.id" class="mb-3 fan-card" variant="outlined">
        <v-card-text class="pa-4">
          <v-row align="center">
            <!-- 用户头像和基本信息 -->
            <v-col cols="12" sm="6" md="4" lg="3">
              <div class="d-flex align-center">
                <router-link :to="`/user/${user.id}`">
                  <v-avatar size="60" class="mr-4">
                    <v-img :src="user.avatarUrl" :alt="user.username" cover></v-img>
                  </v-avatar>
                </router-link>
                <div>
                  <router-link :to="`/user/${user.id}`" class="text-decoration-none">
                    <div class="d-flex align-center">
                      <span class="text-subtitle-1 font-weight-medium">{{ user.username }}</span>
                      <v-chip v-if="user.friend" size="x-small" color="primary" class="ml-2"
                        >互相关注</v-chip
                      >
                    </div>
                  </router-link>
                  <div class="text-caption text-medium-emphasis mt-1">
                    {{ user.introduction || '这个人很懒，什么都没写' }}
                  </div>
                </div>
              </div>
            </v-col>

            <!-- 用户统计信息 -->
            <v-col cols="12" sm="6" md="4" lg="4" class="d-flex justify-sm-center py-3 py-sm-0">
              <div class="d-flex">
                <div class="text-center px-3">
                  <div class="text-subtitle-2 font-weight-bold">{{ user.fansCount }}</div>
                  <div class="text-caption text-medium-emphasis">粉丝</div>
                </div>
                <v-divider vertical class="mx-2"></v-divider>
                <div class="text-center px-3">
                  <div class="text-subtitle-2 font-weight-bold">{{ user.followCount }}</div>
                  <div class="text-caption text-medium-emphasis">关注</div>
                </div>
                <v-divider vertical class="mx-2"></v-divider>
                <div class="text-center px-3">
                  <div class="text-subtitle-2 font-weight-bold">{{ user.submitCount }}</div>
                  <div class="text-caption text-medium-emphasis">投稿</div>
                </div>
              </div>
            </v-col>

            <!-- 操作按钮区 -->
            <v-col cols="12" sm="12" md="4" lg="5" class="d-flex justify-end align-center">
              <v-btn
                v-if="!user.friend"
                color="primary"
                variant="tonal"
                class="mr-2"
                prepend-icon="mdi-account-plus"
                @click="followUser(user.id)"
                :loading="actionLoading === user.id + '_follow'"
              >
                回关
              </v-btn>
              <v-btn
                v-else
                color="primary"
                variant="outlined"
                class="mr-2"
                prepend-icon="mdi-account-check"
                @click="unfollowUser(user.id)"
                :loading="actionLoading === user.id + '_unfollow'"
              >
                已关注
              </v-btn>
              <v-btn
                color="error"
                variant="text"
                prepend-icon="mdi-account-remove"
                @click="confirmRemoveFan(user)"
                :loading="actionLoading === user.id + '_remove'"
              >
                移除粉丝
              </v-btn>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>

      <!-- 分页控件 -->
      <div v-if="totalPage > 1" class="d-flex justify-center my-4">
        <v-pagination
          v-model="page"
          :length="totalPage"
          :total-visible="7"
          rounded="circle"
          @update:model-value="loadFans"
        ></v-pagination>
      </div>
    </template>

    <!-- 确认移除粉丝对话框 -->
    <v-dialog v-model="removeDialog" max-width="500">
      <v-card>
        <v-card-title class="text-h5 pa-4"> 确认移除粉丝 </v-card-title>
        <v-card-text class="pa-4">
          <p>
            确定要将
            <strong>{{ selectedUser ? selectedUser.username : '' }}</strong> 从粉丝列表中移除吗？
          </p>
          <p class="text-caption mt-2">
            <v-icon icon="mdi-information-outline" size="16" class="mr-1"></v-icon>
            移除后，此用户将不再是你的粉丝，但可以再次关注你
          </p>
        </v-card-text>
        <v-card-actions class="pa-4 pt-0">
          <v-spacer></v-spacer>
          <v-btn variant="text" @click="removeDialog = false">取消</v-btn>
          <v-btn
            color="error"
            variant="tonal"
            @click="removeFan"
            :loading="actionLoading === 'remove_confirm'"
          >
            确认移除
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 消息提示组件 -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn icon variant="text" @click="snackbar.show = false">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
    
<script>
import { useUserStore } from '@/stores/userStore'

export default {
  name: 'FansView',
  data() {
    return {
      fans: [],
      loading: true,
      page: 1,
      size: 20,
      totalPage: 1,
      totalCount: 0,
      searchQuery: '',
      searchTimeout: null,
      actionLoading: null, // 操作中的用户ID
      removeDialog: false,
      selectedUser: null,
      userInfo: useUserStore().userData,
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
    }
  },
  created() {
    document.title = '粉丝管理 - 创作中心'
    this.loadFans()
  },
  methods: {
    // 加载粉丝列表
    loadFans() {
      this.loading = true

      const params = `?page=${this.page}&limit=${this.size}${
        this.searchQuery ? '&keyword=' + this.searchQuery : ''
      }`

      this.httpGet(`/follow/fans${params}`, (json) => {
        if (json.status === 200) {
          this.fans = json.data.list || []
          this.page = json.data.currPage
          this.totalPage = json.data.totalPage
          this.totalCount = json.data.totalCount
          this.loading = false
        } else {
          this.showMessage('获取粉丝数据失败，请稍后重试', 'error')
          this.loading = false
        }
      })
    },

    // 搜索粉丝
    searchFans() {
      // 防抖处理
      clearTimeout(this.searchTimeout)
      this.searchTimeout = setTimeout(() => {
        this.page = 1
        this.loadFans()
      }, 500)
    },

    // 关注用户
    followUser(userId) {
      if (!this.userInfo) {
        this.showMessage('请先登录', 'error')
        return
      }

      this.actionLoading = userId + '_follow'

      const data = {
        followUser: userId,
        createUser: this.userInfo.id,
      }

      this.httpPost('/follow/add', data, (json) => {
        if (json.data) {
          // 更新互关状态
          const userIndex = this.fans.findIndex((user) => user.id === userId)
          if (userIndex !== -1) {
            this.fans[userIndex].friend = true
          }
          this.showMessage('关注成功', 'success')
        } else {
          this.showMessage('关注失败，请稍后重试', 'error')
        }
        this.actionLoading = null
      })
    },

    // 取消关注
    unfollowUser(userId) {
      if (!this.userInfo) {
        this.showMessage('请先登录', 'error')
        return
      }

      this.actionLoading = userId + '_unfollow'

      const data = {
        followUser: userId,
        createUser: this.userInfo.id,
      }

      this.httpPost('/follow/delete', data, (json) => {
        if (json.data) {
          // 更新互关状态
          const userIndex = this.fans.findIndex((user) => user.id === userId)
          if (userIndex !== -1) {
            this.fans[userIndex].friend = false
          }
          this.showMessage('已取消关注', 'info')
        } else {
          this.showMessage('取消关注失败，请稍后重试', 'error')
        }
        this.actionLoading = null
      })
    },

    // 打开移除粉丝确认对话框
    confirmRemoveFan(user) {
      this.selectedUser = user
      this.removeDialog = true
    },

    // 移除粉丝
    removeFan() {
      if (!this.selectedUser) return

      this.actionLoading = 'remove_confirm'

      const data = {
        followUser: this.userInfo.id,
        createUser: this.selectedUser.id,
      }

      // 移除粉丝实际上是让粉丝取消关注你
      this.httpPost('/follow/remove', data, (json) => {
        if (json.data) {
          // 从列表中移除该用户
          this.fans = this.fans.filter((fan) => fan.id !== this.selectedUser.id)
          this.totalCount--

          // 如果移除后页面为空且不是第一页，则返回上一页
          if (this.fans.length === 0 && this.page > 1) {
            this.page--
            this.loadFans()
          }

          this.showMessage(`已将 ${this.selectedUser.username} 从粉丝列表移除`, 'success')
          this.removeDialog = false
          this.selectedUser = null
        } else {
          this.showMessage('移除粉丝失败，请稍后重试', 'error')
        }
        this.actionLoading = null
      })
    },

    // 显示消息提示
    showMessage(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
  },
}
</script>
    
<style scoped>
.fan-card {
  transition: all 0.3s ease;
}

.fan-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  transform: translateY(-2px);
}

@media (max-width: 600px) {
  .v-col {
    padding-top: 8px;
    padding-bottom: 8px;
  }
}
</style>