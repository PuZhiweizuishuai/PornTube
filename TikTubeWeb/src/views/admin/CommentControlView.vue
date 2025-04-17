<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="indigo">
        <v-toolbar-title class="text-h5 font-weight-medium">评论管理</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn
          prepend-icon="mdi-refresh"
          color="white"
          variant="tonal"
          @click="getCommentList"
          class="mr-2"
        >
          刷新数据
        </v-btn>
      </v-toolbar>

      <!-- 搜索 userId 参数，查找该用户所有评论，articleId 参数，查找该稿件所有评论 -->
      <div class="search-options pa-4 bg-grey-lighten-5">
        <v-row>
          <v-col cols="12" sm="5">
            <v-text-field
              v-model="searchUserId"
              label="搜索用户ID"
              variant="outlined"
              density="comfortable"
              hide-details
              clearable
              placeholder="输入用户ID搜索该用户的评论"
              prepend-inner-icon="mdi-account-search"
              @keyup.enter="handleSearch"
              @click:clear="clearUserIdSearch"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="5">
            <v-text-field
              v-model="searchArticleId"
              label="搜索稿件ID"
              variant="outlined"
              density="comfortable"
              hide-details
              clearable
              placeholder="输入稿件ID搜索该稿件的评论"
              prepend-inner-icon="mdi-movie-search"
              @keyup.enter="handleSearch"
              @click:clear="clearArticleIdSearch"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="2" class="d-flex align-center">
            <v-btn
              color="primary"
              block
              height="48"
              prepend-icon="mdi-magnify"
              @click="handleSearch"
            >
              搜索
            </v-btn>
          </v-col>
        </v-row>
        <v-chip-group v-if="activeSearchFilters.length > 0" class="mt-2">
          <v-chip
            v-for="(filter, index) in activeSearchFilters"
            :key="index"
            closable
            @click:close="removeFilter(filter.type)"
          >
            {{ filter.label }}
          </v-chip>
          <v-btn size="small" variant="text" color="error" @click="clearAllFilters">
            清除全部筛选
          </v-btn>
        </v-chip-group>
      </div>

      <v-tabs
        v-model="activeTab"
        color="primary"
        align-tabs="center"
        class="mb-4"
        @update:model-value="handleTabChange"
      >
        <v-tab value="all">所有评论</v-tab>
        <v-tab value="normal">正常评论</v-tab>
        <v-tab value="deleted">已删除评论</v-tab>
      </v-tabs>

      <v-card-text>
        <v-data-table-server
          :headers="headers"
          :itemsLength="totalCount"
          :items-per-page="pageSize"
          :items="comments"
          v-model:page="page"
          :loading="loading"
          hover
          @update:options="pageChange"
          class="elevation-1"
          hide-default-footer
        >
          <template #[`item.comment`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-body-1">{{ item.comment }}</div>
              <div v-if="item.parentCommentId !== 0" class="text-caption text-grey mt-1">
                <v-chip size="x-small" color="primary" class="mr-1">回复评论</v-chip>
                父评论ID: {{ item.parentCommentId }}
              </div>
            </div>
          </template>

          <template #[`item.userId`]="{ item }">
            <v-btn
              :to="`/user/${item.userId}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
              size="small"
            >
              {{ item.userId }}
            </v-btn>
          </template>

          <template #[`item.articleId`]="{ item }">
            <v-btn
              :to="`/video/${item.articleId}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
              size="small"
            >
              {{ item.articleId }}
            </v-btn>
          </template>

          <template #[`item.interactions`]="{ item }">
            <div class="d-flex flex-column">
              <div class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-1">mdi-thumb-up</v-icon>
                <span class="text-caption">{{ formatNumber(item.likeCount) }}</span>
              </div>
              <div class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-1">mdi-thumb-down</v-icon>
                <span class="text-caption">{{ formatNumber(item.dislikeCount) }}</span>
              </div>
              <div class="d-flex align-center">
                <v-icon size="small" class="mr-1">mdi-reply</v-icon>
                <span class="text-caption">{{ formatNumber(item.commentCount) }}</span>
              </div>
            </div>
          </template>

          <template #[`item.type`]="{ item }">
            <v-chip :color="item.type === 1 ? 'primary' : 'info'" size="small" class="text-white">
              {{ item.type === 1 ? '评论' : '回复' }}
            </v-chip>
          </template>

          <template #[`item.ipInfo`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-body-2">{{ item.ip }}</div>
              <div class="text-caption text-grey mt-1">{{ item.city || '未知地区' }}</div>
              <div class="text-caption text-grey-darken-1 mt-1">
                {{ getUaInfo(item.ua) }}
              </div>
            </div>
          </template>

          <template #[`item.status`]="{ item }">
            <v-chip
              :color="item.status === 0 ? 'success' : 'error'"
              size="small"
              class="text-white"
            >
              {{ item.status === 0 ? '正常' : '已删除' }}
            </v-chip>
          </template>

          <template #[`item.createTime`]="{ item }">
            {{ formatDate(item.createTime) }}
          </template>

          <template #[`item.actions`]="{ item }">
            <div class="d-flex justify-center">
              <v-tooltip location="top" text="查看详情">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    color="primary"
                    class="mr-1"
                    @click="viewDetails(item)"
                  >
                    <v-icon>mdi-eye</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip location="top" :text="item.status === 1 ? '恢复' : '删除'">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    :color="item.status === 1 ? 'success' : 'error'"
                    @click="item.status === 1 ? confirmRestore(item) : confirmDelete(item)"
                  >
                    <v-icon>{{ item.status === 1 ? 'mdi-restore' : 'mdi-delete' }}</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>
            </div>
          </template>

          <template #no-data>
            <div class="d-flex flex-column align-center pa-4">
              <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
              <span class="text-body-1 text-grey">暂无评论数据</span>
              <v-btn color="primary" class="mt-4" @click="getCommentList">刷新数据</v-btn>
            </div>
          </template>
        </v-data-table-server>
      </v-card-text>

      <div class="d-flex justify-center pa-4">
        <v-pagination
          v-model="page"
          :length="totalPages"
          :total-visible="7"
          @update:model-value="handlePageChange"
          rounded
        ></v-pagination>
      </div>
    </v-card>

    <!-- 评论详情对话框 -->
    <v-dialog v-model="detailDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-comment-text</v-icon>
          评论详情
        </v-card-title>
        <v-card-text class="pt-4">
          <v-list lines="two">
            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-account</v-icon>
              </template>
              <v-list-item-title>用户ID</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.userId }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-video</v-icon>
              </template>
              <v-list-item-title>稿件ID</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.articleId }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-comment-text</v-icon>
              </template>
              <v-list-item-title>评论内容</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.comment }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item v-if="selectedItem?.parentCommentId !== 0">
              <template #prepend>
                <v-icon color="indigo">mdi-comment-multiple</v-icon>
              </template>
              <v-list-item-title>父评论ID</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.parentCommentId }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-earth</v-icon>
              </template>
              <v-list-item-title>IP地址</v-list-item-title>
              <v-list-item-subtitle
                >{{ selectedItem?.ip }}
                {{ selectedItem?.city || '(未知地区)' }}</v-list-item-subtitle
              >
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-laptop</v-icon>
              </template>
              <v-list-item-title>设备信息</v-list-item-title>
              <v-list-item-subtitle>{{
                selectedItem ? getUaInfo(selectedItem.ua) : ''
              }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-clock</v-icon>
              </template>
              <v-list-item-title>创建时间</v-list-item-title>
              <v-list-item-subtitle>{{
                selectedItem ? formatDate(selectedItem.createTime) : ''
              }}</v-list-item-subtitle>
            </v-list-item>
          </v-list>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="detailDialog = false">关闭</v-btn>
          <v-btn
            :color="selectedItem?.status === 1 ? 'success' : 'error'"
            variant="elevated"
            @click="selectedItem?.status === 1 ? restoreItem() : deleteItem()"
          >
            {{ selectedItem?.status === 1 ? '恢复评论' : '删除评论' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除确认对话框 -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">确认删除</v-card-title>
        <v-card-text> 您确定要删除这条评论吗？此操作可以撤销。 </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" variant="elevated" @click="deleteItem">删除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 恢复确认对话框 -->
    <v-dialog v-model="restoreDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">确认恢复</v-card-title>
        <v-card-text> 您确定要恢复这条评论吗？ </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="restoreDialog = false">取消</v-btn>
          <v-btn color="success" variant="elevated" @click="restoreItem">恢复</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 消息提示 -->
    <v-snackbar v-model="showMessage" :timeout="3000" location="top" :color="messageType">
      {{ message }}
      <template #actions>
        <v-btn color="white" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import { UAParser } from 'ua-parser-js'

export default {
  name: 'CommentControlView',
  data() {
    return {
      activeTab: 'all',
      comments: [],
      loading: false,
      page: 1,
      pageSize: 20,
      totalPages: 1,
      totalCount: 0,
      showMessage: false,
      message: '',
      messageType: 'info',
      detailDialog: false,
      deleteDialog: false,
      restoreDialog: false,
      selectedItem: null,
      searchUserId: '',
      searchArticleId: '',
      headers: [
        { title: '评论内容', key: 'comment', align: 'start', sortable: false },
        { title: '用户ID', key: 'userId', align: 'center', sortable: false, width: '100px' },
        { title: '稿件ID', key: 'articleId', align: 'center', sortable: false, width: '100px' },
        { title: '互动', key: 'interactions', align: 'center', sortable: false, width: '100px' },
        { title: '类型', key: 'type', align: 'center', sortable: false, width: '100px' },
        { title: 'IP信息', key: 'ipInfo', align: 'center', sortable: false },
        { title: '状态', key: 'status', align: 'center', sortable: false, width: '100px' },
        { title: '创建时间', key: 'createTime', align: 'center', sortable: false, width: '180px' },
        { title: '操作', key: 'actions', align: 'center', sortable: false, width: '120px' },
      ],
    }
  },
  mounted() {
    this.getCommentList()
  },
  methods: {
    getCommentList() {
      this.loading = true

      let url = `/admin/comment/list?page=${this.page}&limit=${this.pageSize}`

      if (this.activeTab === 'normal') {
        url += '&status=0'
      } else if (this.activeTab === 'deleted') {
        url += '&status=1'
      }

      if (this.searchUserId) {
        url += `&userId=${this.searchUserId}`
      }

      if (this.searchArticleId) {
        url += `&articleId=${this.searchArticleId}`
      }

      this.httpGet(url, (json) => {
        this.loading = false
        if (json && json.data) {
          this.comments = json.data.list || []
          this.totalCount = json.data.totalCount
          this.totalPages = json.data.totalPage
          this.page = json.data.currPage
        } else {
          this.showNotification('获取数据失败', 'error')
        }
      })
    },

    pageChange(options) {
      this.page = options.page
      this.getCommentList()
    },

    handlePageChange() {
      this.getCommentList()
    },

    handleTabChange() {
      this.page = 1
      this.getCommentList()
    },

    viewDetails(item) {
      this.selectedItem = item
      this.detailDialog = true
    },

    confirmDelete(item) {
      this.selectedItem = item
      this.deleteDialog = true
    },

    confirmRestore(item) {
      this.selectedItem = item
      this.restoreDialog = true
    },

    deleteItem() {
      if (!this.selectedItem) return

      this.httpPost(`/admin/comment/toggle`, { id: this.selectedItem.id }, (json) => {
        if (json.data) {
          this.showNotification('评论已删除', 'success')
          this.deleteDialog = false
          this.detailDialog = false
          this.getCommentList()
        } else {
          this.showNotification(json?.message || '操作失败', 'error')
        }
      })
    },

    restoreItem() {
      if (!this.selectedItem) return

      this.httpPost(`/admin/comment/toggle`, { id: this.selectedItem.id }, (json) => {
        if (json.data) {
          this.showNotification('评论已恢复', 'success')
          this.restoreDialog = false
          this.detailDialog = false
          this.getCommentList()
        } else {
          this.showNotification(json?.message || '操作失败', 'error')
        }
      })
    },

    formatDate(timestamp) {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
        hour12: false,
      }).format(date)
    },

    formatNumber(num) {
      if (!num) return '0'
      return num.toString()
    },

    getUaInfo(uastr) {
      if (!uastr) return '未知设备'
      const ua = new UAParser(uastr)
      return `操作系统：${ua.getOS().name || '未知'} ${ua.getOS().version || ''} 浏览器：${
        ua.getBrowser().name || '未知'
      } ${ua.getBrowser().major || ''}`
    },

    showNotification(message, type = 'info') {
      this.message = message
      this.messageType = type
      this.showMessage = true
    },

    handleSearch() {
      this.page = 1
      this.getCommentList()
    },

    clearUserIdSearch() {
      this.searchUserId = ''
      this.page = 1
      this.getCommentList()
    },

    clearArticleIdSearch() {
      this.searchArticleId = ''
      this.page = 1
      this.getCommentList()
    },

    clearAllFilters() {
      this.searchUserId = ''
      this.searchArticleId = ''
      this.page = 1
      this.getCommentList()
    },

    removeFilter(type) {
      if (type === 'userId') {
        this.searchUserId = ''
      } else if (type === 'articleId') {
        this.searchArticleId = ''
      }
      this.page = 1
      this.getCommentList()
    },
  },
  computed: {
    activeSearchFilters() {
      const filters = []

      if (this.searchUserId) {
        filters.push({
          type: 'userId',
          label: `用户: ${this.searchUserId}`,
        })
      }

      if (this.searchArticleId) {
        filters.push({
          type: 'articleId',
          label: `稿件: ${this.searchArticleId}`,
        })
      }

      return filters
    },
  },
}
</script>

<style>
.v-data-table-server th {
  font-weight: 600 !important;
  background-color: #f5f5f5;
}
</style>