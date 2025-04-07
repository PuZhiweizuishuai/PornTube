<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="red">
        <v-toolbar-title class="text-h5 font-weight-medium">我的稿件</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn
          prepend-icon="mdi-refresh"
          color="white"
          variant="tonal"
          @click="getList"
          class="mr-2"
        >
          刷新数据
        </v-btn>
      </v-toolbar>

      <v-tabs v-model="activeTab" color="primary" align-tabs="center" class="mb-4">
        <v-tab value="all">所有稿件</v-tab>
        <!-- <v-tab value="passed">已通过</v-tab>
        <v-tab value="pending">待审核</v-tab>
        <v-tab value="rejected">未通过</v-tab> -->
      </v-tabs>

      <v-table hover>
        <thead>
          <tr>
            <th class="text-center" width="220">封面</th>
            <th class="text-left">视频信息</th>
            <th class="text-center">数据统计</th>
            <th class="text-center" width="100">时长</th>
            <th class="text-center" width="120">分区</th>
            <th class="text-center" width="120">审核状态</th>
            <th class="text-center" width="120">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredArticles" :key="item.id">
            <td class="text-center">
              <v-img
                :src="item.imgUrl"
                height="120px"
                width="213px"
                class="rounded-lg elevation-1 my-2 mx-auto"
                :aspect-ratio="16 / 9"
              />
            </td>
            <td>
              <div class="d-flex flex-column">
                <div class="text-subtitle-1 font-weight-medium text-truncate">
                  {{ item.title }}
                </div>
                <div class="text-caption text-grey">
                  {{ formatDate(item.createTime) }}
                </div>
                <div class="mt-1 text-caption d-flex align-center">
                  <v-icon size="small" class="mr-1">mdi-tag-multiple</v-icon>
                  <span v-for="(tag, index) in parseTags(item.tag)" :key="index" class="mr-1">
                    <v-chip size="x-small" color="blue" class="mr-1">
                      {{ tag }}
                    </v-chip>
                  </span>
                </div>
              </div>
            </td>
            <td class="text-center">
              <div class="d-flex flex-column align-center">
                <div class="d-flex align-center mb-1">
                  <v-icon size="small" class="mr-1">mdi-eye</v-icon>
                  <span class="text-caption">{{ formatNumber(item.viewCount) }} 次观看</span>
                </div>
                <div class="d-flex align-center mb-1">
                  <v-icon size="small" class="mr-1">mdi-thumb-up</v-icon>
                  <span class="text-caption">{{ formatNumber(item.likeCount) }} 点赞</span>
                </div>
                <div class="d-flex align-center mb-1">
                  <v-icon size="small" class="mr-1">mdi-message</v-icon>
                  <span class="text-caption">{{ formatNumber(item.commentCount) }} 评论</span>
                </div>
                <div class="d-flex align-center">
                  <v-icon size="small" class="mr-1">mdi-message-text</v-icon>
                  <span class="text-caption">{{ formatNumber(item.danmakuCount) }} 弹幕</span>
                </div>
              </div>
            </td>
            <td class="text-center">
              {{ formatDuration(item.duration) }}
            </td>
            <td class="text-center">
              <div v-if="getCategoryInfo(item.category)" class="d-flex flex-column align-center">
                <v-chip size="small" color="primary-lighten-1" class="mb-1">
                  {{ getCategoryInfo(item.category).parent }}
                </v-chip>
                <v-chip size="small" variant="outlined" color="primary">
                  {{ getCategoryInfo(item.category).children }}
                </v-chip>
              </div>
              <div v-else class="text-caption text-grey">未分类</div>
            </td>
            <td class="text-center">
              <v-chip :color="getStatusColor(item.examineStatus)" size="small" class="text-white">
                {{ getStatusText(item.examineStatus) }}
              </v-chip>
              <div
                v-if="item.examineStatus !== 1 && item.examineMessage"
                class="mt-2 text-caption text-red"
              >
                {{ item.examineMessage }}
              </div>
            </td>
            <td class="text-center">
              <div class="d-flex justify-center">
                <v-tooltip location="top" text="预览">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      color="primary"
                      class="mr-1"
                      :href="`/video/${item.id}`"
                      target="_blank"
                    >
                      <v-icon>mdi-eye</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>

                <v-tooltip location="top" text="编辑">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      color="warning"
                      class="mr-1"
                      @click="editItem(item)"
                    >
                      <v-icon>mdi-pencil</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>

                <v-tooltip location="top" text="删除">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      color="error"
                      @click="confirmDelete(item)"
                    >
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
              </div>
            </td>
          </tr>
          <tr v-if="!articles.length">
            <td colspan="7">
              <div class="d-flex flex-column align-center pa-4">
                <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
                <span class="text-body-1 text-grey">暂无稿件数据</span>
                <v-btn color="primary" class="mt-4" @click="getList">刷新数据</v-btn>
              </div>
            </td>
          </tr>
        </tbody>
      </v-table>

      <div class="text-center pa-4">
        <v-pagination
          v-model="page"
          :length="totalPages"
          :total-visible="7"
          @update:model-value="handlePageChange"
          rounded
        ></v-pagination>
      </div>
    </v-card>

    <!-- 删除确认对话框 -->
    <v-dialog v-model="deleteDialog" max-width="400">
      <v-card>
        <v-card-title class="text-h5">确认删除</v-card-title>
        <v-card-text>
          您确定要删除视频 <strong>{{ selectedItem?.title }}</strong> 吗？此操作不可撤销。
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" variant="elevated" @click="deleteItem">删除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 消息提示 -->
    <v-snackbar v-model="showMessage" :timeout="3000" location="top" :color="messageType">
      {{ message }}
      <template #actions>
        <v-btn color="white" variant="text" @click="showMessage = false"> 关闭 </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  name: 'StudioListView',
  data() {
    return {
      activeTab: 'all',
      articles: [],
      loading: false,
      page: 1,
      pageSize: 10,
      totalPages: 1,
      totalCount: 0,
      showMessage: false,
      message: '',
      messageType: 'info',
      deleteDialog: false,
      selectedItem: null,
      categoryList: {},
      categoryMap: {},
    }
  },
  computed: {
    filteredArticles() {
      if (this.activeTab === 'all') {
        return this.articles
      } else if (this.activeTab === 'passed') {
        return this.articles.filter((item) => item.examineStatus === 1)
      } else if (this.activeTab === 'pending') {
        return this.articles.filter((item) => item.examineStatus === 0)
      } else {
        return this.articles.filter((item) => item.examineStatus === 2)
      }
    },
  },
  mounted() {
    this.getList()
    this.getCategoryList()
  },
  methods: {
    getList() {
      this.loading = true
      this.httpGet(`/studio/article/list?limit=${this.pageSize}&page=${this.page}`, (json) => {
        this.loading = false
        if (json && json.data) {
          this.articles = json.data.list || []
          this.totalCount = json.data.totalCount
          this.totalPages = json.data.totalPage
          this.page = json.data.currPage
        } else {
          this.showNotification('获取数据失败', 'error')
        }
      })
    },
    getCategoryList() {
      this.httpGet(`/category/tree`, (json) => {
        if (json && json.data) {
          this.categoryList = json.data
          this.buildCategoryMap()
        }
      })
    },
    buildCategoryMap() {
      this.categoryMap = {}

      for (const parentId in this.categoryList) {
        const parent = this.categoryList[parentId]
        if (parent.children && parent.children.length > 0) {
          parent.children.forEach((child) => {
            this.categoryMap[child.id] = {
              parentId: parent.id,
              parentName: parent.name,
              childName: child.name,
            }
          })
        }
      }
    },
    getCategoryInfo(categoryId) {
      if (!categoryId || !this.categoryMap[categoryId]) return null

      const info = this.categoryMap[categoryId]
      return {
        parent: info.parentName,
        children: info.childName,
      }
    },
    handlePageChange(page) {
      this.page = page
      this.getList()
    },
    formatNumber(num) {
      if (num === null || num === undefined) return '0'

      if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万'
      }
      return num.toString()
    },
    formatDate(timestamp) {
      if (!timestamp) return ''
      const date = new Date(timestamp)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
      })
    },
    formatDuration(seconds) {
      if (!seconds) return '00:00'

      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = Math.floor(seconds % 60)

      if (minutes >= 60) {
        const hours = Math.floor(minutes / 60)
        const remainingMinutes = minutes % 60
        return `${hours.toString().padStart(2, '0')}:${remainingMinutes
          .toString()
          .padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`
      }

      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds
        .toString()
        .padStart(2, '0')}`
    },
    getStatusText(status) {
      if (status === 1) return '已通过'
      if (status === 0) return '审核中'
      return '未通过'
    },
    getStatusColor(status) {
      if (status === 1) return 'success'
      if (status === 0) return 'blue'
      return 'error'
    },
    parseTags(tagString) {
      if (!tagString) return []
      try {
        return JSON.parse(tagString)
      } catch {
        return []
      }
    },
    editItem(item) {
      // 跳转到编辑页面
      this.$router.push(`/studio/upload?edit=${item.id}`)
    },
    confirmDelete(item) {
      this.selectedItem = item
      this.deleteDialog = true
    },
    deleteItem() {
      if (!this.selectedItem) return

      this.httpPost('/studio/article/delete', this.selectedItem, (json) => {
        if (json.data == 0) {
          this.showNotification('删除成功', 'success')
          this.articles = this.articles.filter((item) => item.id !== this.selectedItem.id)
          this.deleteDialog = false
          this.selectedItem = null
        } else if (json.data == 1) {
          this.showNotification('没有权限', 'success')
          this.deleteDialog = false
          this.selectedItem = null
        } else {
          this.showNotification('已经删除', 'success')
          this.deleteDialog = false
          this.selectedItem = null
        }
      })
    },
    showNotification(message, type = 'info') {
      this.message = message
      this.messageType = type
      this.showMessage = true
    },
  },
}
</script>

<style scoped>
.v-img {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.2s ease;
}

.v-img:hover {
  transform: scale(1.02);
}
</style>