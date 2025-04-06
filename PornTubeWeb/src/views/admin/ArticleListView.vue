<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="indigo">
        <v-toolbar-title class="text-h5 font-weight-medium">稿件管理</v-toolbar-title>
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

      <v-card-text>
        <v-data-table-server
          :headers="headers"
          :itemsLength="totalCount"
          :items="filteredArticles"
          :items-per-page="pageSize"
          v-model:page="page"
          :loading="loading"
          hover
          @update:options="pageChange"
          class="elevation-1"
          hide-default-footer
        >
          <template #[`item.imgUrl`]="{ item }">
            <v-img
              :src="item.imgUrl"
              max-height="120px"
              max-width="213px"
              class="rounded-lg elevation-1 my-2"
            />
          </template>

          <template #[`item.info`]="{ item }">
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

          <template #[`item.category`]="{ item }">
            <div v-if="getCategoryInfo(item.category)" class="d-flex flex-column">
              <v-chip size="small" color="primary-lighten-1" class="mb-1">
                {{ getCategoryInfo(item.category).parent }}
              </v-chip>
              <v-chip size="small" variant="outlined" color="blue">
                {{ getCategoryInfo(item.category).children }}
              </v-chip>
            </div>
            <div v-else class="text-caption text-grey">未分类</div>
          </template>

          <template #[`item.type`]="{ item }">
            <v-chip size="small" :color="getTypeColor(item.type)" class="text-white">
              {{ getType(item.type) }}
            </v-chip>
          </template>

          <template #[`item.stats`]="{ item }">
            <div class="d-flex flex-column">
              <div class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-1">mdi-eye</v-icon>
                <span class="text-caption">{{ formatNumber(item.viewCount) }}</span>
              </div>
              <div class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-1">mdi-thumb-up</v-icon>
                <span class="text-caption">{{ formatNumber(item.likeCount) }}</span>
              </div>
              <div class="d-flex align-center">
                <v-icon size="small" class="mr-1">mdi-message</v-icon>
                <span class="text-caption">{{ formatNumber(item.commentCount) }}</span>
              </div>
            </div>
          </template>

          <template #[`item.examineStatus`]="{ item }">
            <v-chip :color="getStatusColor(item.examineStatus)" size="small" class="text-white">
              {{ getStatusText(item.examineStatus) }}
            </v-chip>
            <div
              v-if="item.examineStatus !== 1 && item.examineMessage"
              class="mt-2 text-caption text-red"
            >
              {{ item.examineMessage }}
            </div>
          </template>

          <template #[`item.actions`]="{ item }">
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

              <v-tooltip location="top" text="审核">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    color="warning"
                    class="mr-1"
                    @click="examine(item)"
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
          </template>

          <template #no-data>
            <div class="d-flex flex-column align-center pa-4">
              <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
              <span class="text-body-1 text-grey">暂无稿件数据</span>
              <v-btn color="primary" class="mt-4" @click="getList">刷新数据</v-btn>
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

    <!-- 审核对话框 -->
    <v-dialog v-model="examineDialog" persistent max-width="600px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-clipboard-check</v-icon>
          审核 - {{ selectedItem?.title }}
        </v-card-title>
        <v-card-text class="pt-4">
          <v-container>
            <v-row>
              <v-col>
                <v-select
                  v-model="selectedStatus"
                  :items="examineStatus"
                  label="审核结果"
                  variant="outlined"
                  @update:model-value="handleExamineStatusChange"
                ></v-select>
              </v-col>
            </v-row>
            <v-row v-if="showRejectionOptions">
              <v-col>
                <v-select
                  v-model="rejectionType"
                  :items="examineItems"
                  label="不通过原因"
                  variant="outlined"
                ></v-select>
              </v-col>
            </v-row>
            <v-row v-if="showRejectionOptions">
              <v-col>
                <v-textarea
                  v-model="examineMessage"
                  label="处理意见"
                  variant="outlined"
                  rows="3"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="grey" variant="text" @click="examineDialog = false">取消</v-btn>
          <v-btn color="primary" variant="elevated" @click="saveExamine">确认</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

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
        <v-btn color="white" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  name: 'ArticleListView',
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
      examineDialog: false,
      deleteDialog: false,
      selectedItem: null,
      selectedStatus: null,
      showRejectionOptions: false,
      rejectionType: '',
      examineMessage: '',
      examineStatus: ['通过', '不通过'],
      examineItems: [],
      categoryList: {},
      categoryMap: {},
      headers: [
        { title: '封面', key: 'imgUrl', align: 'center', sortable: false, width: '220px' },
        { title: '稿件信息', key: 'info', align: 'start', sortable: false },
        { title: '用户', key: 'userId', align: 'center', sortable: false },
        { title: '分区', key: 'category', align: 'center', sortable: false },
        { title: '类型', key: 'type', align: 'center', sortable: false },
        { title: '数据统计', key: 'stats', align: 'center', sortable: false },
        { title: '审核状态', key: 'examineStatus', align: 'center', sortable: false },
        { title: '操作', key: 'actions', align: 'center', sortable: false, width: '140px' },
      ],
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
    this.getExamineItems()
  },
  methods: {
    getList() {
      this.loading = true
      this.httpGet(
        `/studio/article/list?limit=${this.pageSize}&page=${this.page}&type=admin`,
        (json) => {
          this.loading = false
          if (json && json.data) {
            this.articles = json.data.list || []
            this.totalCount = json.data.totalCount
            this.totalPages = json.data.totalPage
            this.page = json.data.currPage
          } else {
            this.showNotification('获取数据失败', 'error')
          }
        }
      )
    },
    getCategoryList() {
      this.httpGet(`/category/tree`, (json) => {
        if (json && json.data) {
          this.categoryList = json.data
          this.buildCategoryMap()
        }
      })
    },
    getExamineItems() {
      this.httpGet('/examine/item', (json) => {
        if (json && json.data) {
          this.examineItems = json.data
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
    examine(item) {
      this.selectedItem = item
      this.examineDialog = true
      this.resetExamineForm()
    },
    confirmDelete(item) {
      this.selectedItem = item
      this.deleteDialog = true
    },
    deleteItem() {
      if (!this.selectedItem) return

      // 这里应添加实际的删除API调用
      // this.httpPost('/admin/article/delete', { id: this.selectedItem.id }, (json) => {
      //   if (json.status === 200) {
      //     this.showNotification('删除成功', 'success')
      //     this.getList()
      //   } else {
      //     this.showNotification('删除失败: ' + json.message, 'error')
      //   }
      // })

      // 模拟删除成功
      this.showNotification('删除成功', 'success')
      this.articles = this.articles.filter((item) => item.id !== this.selectedItem.id)
      this.deleteDialog = false
      this.selectedItem = null
    },
    handleExamineStatusChange(value) {
      this.showRejectionOptions = value === '不通过'
    },
    resetExamineForm() {
      this.selectedStatus = null
      this.rejectionType = ''
      this.examineMessage = ''
      this.showRejectionOptions = false
    },
    saveExamine() {
      if (!this.selectedItem) return

      const result = this.selectedStatus === '通过'

      const data = {
        videoId: this.selectedItem.id,
        result,
        type: this.rejectionType,
        message: this.examineMessage,
      }

      this.httpPost(`/admin/examine`, data, (json) => {
        if (json.status === 200) {
          this.showNotification('审核完成', 'success')
          this.getList()
          this.examineDialog = false
        } else {
          this.showNotification('审核失败：' + json.message, 'error')
        }
      })
    },
    handlePageChange(page) {
      this.page = page
      this.getList()
    },
    pageChange({ page }) {
      this.page = page
      this.getList()
      window.scrollTo({ top: 0, behavior: 'smooth' })
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
    getType(code) {
      if (code === 0) return '视频'
      if (code === 1) return '图片'
      if (code === 2) return '文章'
      return '音乐'
    },
    getTypeColor(code) {
      if (code === 0) return 'red'
      if (code === 1) return 'blue'
      if (code === 2) return 'green'
      return 'purple'
    },
    parseTags(tagString) {
      if (!tagString) return []
      try {
        return JSON.parse(tagString)
      } catch {
        return []
      }
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