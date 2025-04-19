<template>
  <v-container>
    <v-row>
      <v-col cols="12">
        <v-card class="notification-card">
          <v-card-title class="d-flex align-center px-4 py-3 bg-grey-lighten-4">
            <div class="d-flex align-center">
              <v-icon icon="mdi-bell" color="primary" class="mr-2" size="large"></v-icon>
              <h2 class="text-h5 font-weight-medium">消息通知</h2>
              <v-chip class="ml-2" color="primary" size="small" variant="flat">
                {{ totalCount }}
              </v-chip>
            </div>
            <v-spacer></v-spacer>

            <!-- 全部已读按钮 -->
            <v-btn
              v-if="hasUnreadNotifications"
              color="primary"
              prepend-icon="mdi-check-all"
              variant="text"
              density="comfortable"
              class="mr-2"
              @click="markAllAsRead"
              :loading="markingAllRead"
            >
              全部已读
            </v-btn>

            <v-menu location="bottom end" offset="8">
              <template v-slot:activator="{ props }">
                <v-btn icon="mdi-filter-variant" v-bind="props" variant="text"></v-btn>
              </template>
              <v-card min-width="200" elevation="3">
                <v-list density="compact">
                  <v-list-subheader>筛选消息类型</v-list-subheader>
                  <v-list-item
                    v-for="(item, type) in typeMap"
                    :key="type"
                    :value="type"
                    @click="selectFilterType(type)"
                  >
                    <template v-slot:prepend>
                      <v-icon :color="item.color" :icon="item.icon"></v-icon>
                    </template>
                    <v-list-item-title>{{ item.text }}</v-list-item-title>
                    <template v-slot:append>
                      <v-icon v-if="selectedType === type" color="success">mdi-check</v-icon>
                    </template>
                  </v-list-item>
                  <v-divider></v-divider>
                  <v-list-item value="all" @click="selectFilterType('all')">
                    <template v-slot:prepend>
                      <v-icon color="grey">mdi-filter-off</v-icon>
                    </template>
                    <v-list-item-title>显示全部</v-list-item-title>
                    <template v-slot:append>
                      <v-icon v-if="selectedType === 'all'" color="success">mdi-check</v-icon>
                    </template>
                  </v-list-item>
                </v-list>
              </v-card>
            </v-menu>
          </v-card-title>

          <v-tabs
            v-model="statusTab"
            @update:model-value="filterByStatus"
            color="primary"
            density="comfortable"
            align-tabs="center"
            class="px-4"
          >
            <v-tab value="all" class="text-body-1">全部通知</v-tab>
            <v-tab value="0" class="text-body-1">
              <v-badge
                v-if="unreadCount > 0"
                :content="unreadCount"
                color="error"
                inline
                class="mr-2"
              ></v-badge>
              未读
            </v-tab>
            <v-tab value="1" class="text-body-1">已读</v-tab>
          </v-tabs>

          <v-divider></v-divider>

          <v-card-text class="pa-4">
            <v-row v-if="loading" justify="center" class="my-6">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
              <span class="text-body-1 ml-3 text-medium-emphasis">加载中...</span>
            </v-row>

            <v-row v-else-if="notificationList.length === 0" justify="center" class="my-6">
              <div class="text-center">
                <v-icon
                  icon="mdi-bell-off-outline"
                  size="64"
                  color="grey-lighten-1"
                  class="mb-3"
                ></v-icon>
                <h3 class="text-grey-lighten-1 text-h6">暂无消息通知</h3>
                <p class="text-body-2 text-medium-emphasis mt-2">
                  当有新的评论、回复或点赞时，会在这里显示通知
                </p>
              </div>
            </v-row>

            <template v-else>
              <div
                v-for="(item, index) in notificationList"
                :key="item.notification.id"
                class="mb-4"
              >
                <v-hover v-slot="{ isHovering, props }">
                  <v-card
                    v-bind="props"
                    variant="outlined"
                    class="notification-item-card transition-swing pa-3"
                    :class="{ 'elevation-2': isHovering }"
                    :color="item.notification.status === 0 ? 'blue-lighten-5' : undefined"
                    @click="markAsRead(item.notification.id, index)"
                  >
                    <v-row>
                      <v-col cols="12" sm="12" md="12" class="pb-0">
                        <v-row no-gutters>
                          <v-col cols="auto">
                            <router-link :to="`/user/${item.user.id}`">
                              <v-avatar size="48" class="mr-3">
                                <v-img :src="item.user.avatarUrl" alt="用户头像" />
                              </v-avatar>
                            </router-link>
                          </v-col>
                          <v-col>
                            <div class="d-flex align-center flex-wrap">
                              <router-link
                                :to="`/user/${item.user.id}`"
                                class="text-decoration-none mr-2"
                              >
                                <span class="text-subtitle-1 font-weight-medium">{{
                                  item.user.username
                                }}</span>
                              </router-link>
                              <v-chip
                                v-if="item.notification.status === 0"
                                class="ml-2 mr-1"
                                color="error"
                                size="x-small"
                                text-color="white"
                                variant="flat"
                              >
                                未读
                              </v-chip>
                              <v-chip
                                class="mr-2"
                                :color="getTypeColor(item.notification.type)"
                                size="x-small"
                                text-color="white"
                                variant="flat"
                              >
                                {{ getTypeText(item.notification.type) }}
                              </v-chip>
                              <v-spacer></v-spacer>
                              <span class="text-caption text-medium-emphasis">
                                {{ formatTime(item.notification.createTime) }}
                              </span>
                            </div>
                          </v-col>
                        </v-row>
                      </v-col>
                    </v-row>

                    <v-row>
                      <v-col class="pl-15 pt-2 pb-1">
                        <div class="notification-title">{{ item.notification.title }}</div>
                        <ShowMarkdown :markdown="item.notification.content" :anchor="0" />
                      </v-col>
                    </v-row>

                    <!-- 操作区域 -->
                    <v-row>
                      <v-col>
                        <div class="d-flex justify-end align-center">
                          <template v-if="canReply(item.notification.type)">
                            <v-btn
                              variant="text"
                              color="primary"
                              @click.stop="openReplyDialog(item)"
                              prepend-icon="mdi-reply"
                              size="small"
                              class="mr-2"
                              rounded
                            >
                              回复
                            </v-btn>
                          </template>

                          <template v-if="canViewPost(item.notification.type)">
                            <v-btn
                              variant="text"
                              color="success"
                              :to="`/video/${item.notification.articleId}`"
                              prepend-icon="mdi-eye"
                              size="small"
                              rounded
                            >
                              查看原帖
                            </v-btn>
                          </template>
                        </div>
                      </v-col>
                    </v-row>
                  </v-card>
                </v-hover>
              </div>

              <!-- 无限加载提示 -->
              <div v-if="loading && currPage > 1" class="text-center py-3">
                <v-progress-circular
                  indeterminate
                  color="primary"
                  size="24"
                  class="mr-2"
                ></v-progress-circular>
                <span class="text-body-2">加载更多消息...</span>
              </div>
            </template>
          </v-card-text>

          <v-divider v-if="totalPage > 1"></v-divider>

          <div v-if="totalPage > 1" class="d-flex justify-center pa-4">
            <v-pagination
              v-model="currPage"
              :length="totalPage"
              :total-visible="7"
              @update:model-value="handlePageChange"
              rounded
              density="comfortable"
              active-color="primary"
            ></v-pagination>
          </div>
        </v-card>
      </v-col>
    </v-row>

    <!-- 回复对话框 -->
    <v-dialog v-model="replyDialog" max-width="600px" transition="dialog-bottom-transition">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white d-flex align-center">
          <v-icon class="mr-2">mdi-reply</v-icon>
          回复消息
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" color="white" @click="replyDialog = false"></v-btn>
        </v-card-title>
        <v-card-text class="pt-4">
          <div class="mb-4">
            <div class="text-subtitle-1 font-weight-medium mb-2 d-flex align-center">
              <v-icon icon="mdi-pencil" size="small" class="mr-1"></v-icon>
              回复内容
            </div>
            <CommentVditor
              ref="commentVditor"
              :idname="`reply-comment`"
              :placeholder="replyPlaceholder"
              @vditor-input="getCommentText"
            />
          </div>
        </v-card-text>
        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="replyDialog = false">取消</v-btn>
          <v-btn
            color="primary"
            @click="submitReply"
            :loading="submittingReply"
            :disabled="!replyComment || replyComment.trim() === ''"
          >
            提交回复
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar
      v-model="showMessage"
      :timeout="3000"
      location="top"
      :color="messageColor"
      rounded="pill"
    >
      <div class="d-flex align-center">
        <v-icon
          :icon="messageColor === 'success' ? 'mdi-check-circle' : 'mdi-alert-circle'"
          start
          class="mr-2"
        ></v-icon>
        {{ message }}
      </div>
      <template v-slot:actions>
        <v-btn icon="mdi-close" variant="text" @click="showMessage = false"></v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import CommentVditor from '@/components/vditor/VditorComment.vue'
import { useUserStore } from '@/stores/userStore'

export default {
  name: 'NotificationView',
  components: {
    ShowMarkdown,
    CommentVditor,
  },
  data() {
    return {
      notificationList: [],
      totalCount: 0,
      pageSize: 10,
      totalPage: 0,
      currPage: 1,
      loading: false,
      selectedType: 'all',
      statusTab: 'all',
      showMessage: false,
      message: '',
      messageColor: 'primary',
      typeMap: {
        0: { text: '帖子评论', icon: 'mdi-message-reply-text', color: 'blue' },
        1: { text: '评论回复', icon: 'mdi-comment-text', color: 'indigo' },
        2: { text: '帖子点赞', icon: 'mdi-thumb-up', color: 'pink' },
        3: { text: '评论点赞', icon: 'mdi-thumb-up-outline', color: 'pink-lighten-2' },
        10: { text: '系统通知', icon: 'mdi-bell', color: 'green' },
        11: { text: '举报通知', icon: 'mdi-flag', color: 'red' },
      },
      replyDialog: false,
      currentNotification: null,
      replyPlaceholder: '',
      replyComment: '',
      userInfo: useUserStore(),
      unreadCount: 0,
      markingAllRead: false,
      submittingReply: false,
    }
  },
  computed: {
    hasUnreadNotifications() {
      return (
        this.unreadCount > 0 ||
        (this.statusTab === '0' && this.notificationList.length > 0) ||
        this.notificationList.some((item) => item.notification.status === 0)
      )
    },
  },
  watch: {
    notificationList: {
      handler(newVal) {
        this.updateUnreadCount(newVal)
      },
      deep: true,
    },
  },
  created() {
    // 从URL获取页码和状态
    if (this.$route.query.page) {
      this.currPage = parseInt(this.$route.query.page)
    }
    if (this.$route.query.status) {
      this.statusTab = this.$route.query.status
    }
    if (this.$route.query.type) {
      this.selectedType = this.$route.query.type
    }

    this.fetchNotifications()
  },
  methods: {
    fetchNotifications() {
      this.loading = true

      let url = `/notification/get?page=${this.currPage}&limit=${this.pageSize}`

      // 添加状态过滤
      if (this.statusTab !== 'all') {
        url += `&status=${this.statusTab}`
      }

      // 添加类型过滤
      if (this.selectedType !== 'all') {
        url += `&type=${this.selectedType}`
      }

      this.httpGet(url, (json) => {
        if (json.status === 200) {
          this.notificationList = json.data.list
          this.totalCount = json.data.totalCount
          this.pageSize = json.data.pageSize
          this.totalPage = json.data.totalPage
          this.currPage = json.data.currPage
          this.updateUnreadCount(this.notificationList)
        } else {
          this.showErrorMessage(json.message || '获取消息失败')
        }
        this.loading = false
      })
    },
    updateUnreadCount(notifications) {
      this.unreadCount = notifications.filter((item) => item.notification.status === 0).length
    },
    handlePageChange(page) {
      this.currPage = page
      // 更新URL以反映当前页码
      this.updateQueryParams()
      this.fetchNotifications()
      // 滚动到顶部
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
    filterByType() {
      this.currPage = 1
      this.updateQueryParams()
      this.fetchNotifications()
    },
    filterByStatus() {
      this.currPage = 1
      this.updateQueryParams()
      this.fetchNotifications()
    },
    selectFilterType(type) {
      this.selectedType = type
      this.filterByType()
    },
    updateQueryParams() {
      const query = {
        page: this.currPage,
      }

      if (this.statusTab !== 'all') {
        query.status = this.statusTab
      }

      if (this.selectedType !== 'all') {
        query.type = this.selectedType
      }

      this.$router.push({ query })
    },
    markAsRead(notificationId, index) {
      // 如果已经是已读状态，不执行操作
      if (this.notificationList[index].notification.status === 1) {
        return
      }

      this.httpPost(`/notification/read`, { id: notificationId }, (json) => {
        if (json.data) {
          // 更新本地状态
          this.notificationList[index].notification.status = 1
          this.updateUnreadCount(this.notificationList)
          this.showSuccessMessage('已标记为已读')
        } else {
          this.showErrorMessage(json.message || '操作失败')
        }
      })
    },
    markAllAsRead() {
      if (!this.hasUnreadNotifications) {
        return
      }

      this.markingAllRead = true

      this.httpPost('/notification/readAll', {}, (json) => {
        if (json.data) {
          // 更新所有本地通知状态为已读
          this.notificationList.forEach((item) => {
            if (item.notification.status === 0) {
              item.notification.status = 1
            }
          })
          this.updateUnreadCount(this.notificationList)
          this.showSuccessMessage('已将所有消息标记为已读')
        } else {
          this.showErrorMessage(json.message || '操作失败')
        }
        this.markingAllRead = false
      })
    },
    formatTime(timestamp) {
      if (!timestamp) return ''

      const date = new Date(timestamp)
      const now = new Date()
      const diffMs = now - date

      // 1分钟内
      if (diffMs < 60000) {
        return '刚刚'
      }

      // 1小时内
      if (diffMs < 3600000) {
        return Math.floor(diffMs / 60000) + '分钟前'
      }

      // 24小时内
      if (diffMs < 86400000) {
        return Math.floor(diffMs / 3600000) + '小时前'
      }

      // 7天内
      if (diffMs < 604800000) {
        return Math.floor(diffMs / 86400000) + '天前'
      }

      // 超过7天
      const year = date.getFullYear()
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${year}-${month}-${day}`
    },
    getTypeIcon(type) {
      return this.typeMap[type]?.icon || 'mdi-bell'
    },
    getTypeColor(type) {
      return this.typeMap[type]?.color || 'grey'
    },
    getTypeText(type) {
      return this.typeMap[type]?.text || '未知类型'
    },
    showSuccessMessage(msg) {
      this.message = msg
      this.messageColor = 'success'
      this.showMessage = true
    },
    showErrorMessage(msg) {
      this.message = msg
      this.messageColor = 'error'
      this.showMessage = true
    },
    canReply(notificationType) {
      // 只有帖子评论和评论回复可以回复
      return notificationType === 0 || notificationType === 1
    },
    canViewPost(notificationType) {
      // 帖子评论、评论回复、帖子点赞可以查看原帖
      return notificationType === 0 || notificationType === 1 || notificationType === 2
    },
    openReplyDialog(item) {
      if (!this.userInfo.userData) {
        this.showErrorMessage('请先登录后再回复')
        return
      }

      this.currentNotification = item.notification

      if (item.notification.type === 0) {
        // 帖子评论
        this.replyPlaceholder = `回复 @${item.user.username} 的评论`
      } else if (item.notification.type === 1) {
        // 评论回复
        this.replyPlaceholder = `回复 @${item.user.username} 的回复`
      }

      this.replyDialog = true

      // 清空之前的评论内容
      this.replyComment = ''
      // 组件挂载后需要重置编辑器内容
      this.$nextTick(() => {
        if (this.$refs.commentVditor) {
          this.$refs.commentVditor.setTextValue('')
        }
      })
    },
    getCommentText(value) {
      this.replyComment = value
    },
    submitReply() {
      if (!this.replyComment || this.replyComment.trim() === '') {
        this.showErrorMessage('回复内容不能为空')
        return
      }

      this.submittingReply = true
      const notification = this.currentNotification

      // 构建评论数据
      const commentData = {
        comment: this.replyComment,
        type: 2, // 帖子评论回复type=1，评论回复回复type=2
        articleId: notification.articleId,
        commentId: notification.outerId,
        parentCommentId: notification.commentId,
        parentUserId: notification.notifier,
      }

      this.httpPost('/comment/save', commentData, (json) => {
        this.submittingReply = false
        if (json.status === 200) {
          this.showSuccessMessage('回复成功')
          this.replyDialog = false

          // 可选：刷新消息列表
          this.fetchNotifications()
        } else {
          this.showErrorMessage(json.message || '回复失败')
        }
      })
    },
  },
}
</script>

<style>
.notification-card {
  border-radius: 8px;
  overflow: hidden;
}

.notification-item-card {
  border-radius: 8px;
  transition: transform 0.2s, box-shadow 0.2s;
}

.notification-item-card:hover {
  transform: translateY(-2px);
}

.notification-title {
  font-weight: 500;
  margin-bottom: 8px;
  color: rgba(0, 0, 0, 0.87);
}

.notification-content {
  white-space: pre-line;
  color: rgba(0, 0, 0, 0.6);
}

.v-btn--size-small {
  text-transform: none;
  letter-spacing: normal;
}

.v-pagination {
  margin-top: 8px;
}
</style>