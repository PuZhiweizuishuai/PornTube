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
          :loading="table.loading"
          @click="table.refresh"
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
        <v-tab value="pending">待审核评论</v-tab>
        <v-tab value="all">所有评论</v-tab>

        <v-tab value="normal">正常评论</v-tab>
        <v-tab value="deleted">已删除评论</v-tab>
      </v-tabs>

      <v-card-text>
        <!-- 表格 + 分页由通用组件承载，业务差异通过插槽透传 -->
        <AdminDataTable
          :headers="headers"
          :items="table.items"
          :loading="table.loading"
          :page="table.page"
          :total-pages="table.totalPages"
          :total-count="table.totalCount"
          :page-size="table.pageSize"
          empty-text="暂无评论数据"
          show-refresh-on-empty
          @update:page="table.load"
          @refresh="table.refresh"
        >
          <template #[`item.comment`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-body-1 title-wrap">{{ item.comment }}</div>
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
            <v-chip :color="getStatusColor(item.status)" size="small" class="text-white">
              {{ getStatusText(item.status) }}
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

              <template v-if="item.status === -1">
                <v-tooltip location="top" text="通过审核">
                  <template #activator="{ props }">
                    <v-btn
                      v-bind="props"
                      icon
                      size="small"
                      color="success"
                      class="mr-1"
                      @click="confirmApprove(item)"
                    >
                      <v-icon>mdi-check</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
                <v-tooltip location="top" text="删除">
                  <template #activator="{ props }">
                    <v-btn v-bind="props" icon size="small" color="error" @click="confirmDelete(item)">
                      <v-icon>mdi-delete</v-icon>
                    </v-btn>
                  </template>
                </v-tooltip>
              </template>

              <template v-else>
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
              </template>
            </div>
          </template>
        </AdminDataTable>
      </v-card-text>
    </v-card>

    <!-- 评论详情对话框 -->
    <v-dialog v-model="detailDialog" width="90vh">
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
              <ShowMarkdown :markdown="selectedItem.comment" :anchor="0"></ShowMarkdown>
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

            <!-- AI 审核信息 -->
            <v-list-item v-if="selectedItem?.aiExamineMessage">
              <template #prepend>
                <v-icon color="indigo">mdi-robot</v-icon>
              </template>
              <v-list-item-title>AI审核状态</v-list-item-title>
              <div>
                AI审核意见：{{ selectedItem.aiExamineMessage }} | 消耗 TOKEN:
                {{ selectedItem.aiExamineToken }}
              </div>
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

          <template v-if="selectedItem?.status === -1">
            <v-btn color="success" variant="elevated" @click="approveItem()"> 通过审核 </v-btn>
          </template>

          <template v-else>
            <v-btn
              :color="selectedItem?.status === 1 ? 'success' : 'error'"
              variant="elevated"
              @click="selectedItem?.status === 1 ? restoreItem() : deleteItem()"
            >
              {{ selectedItem?.status === 1 ? '恢复评论' : '删除评论' }}
            </v-btn>
          </template>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除 / 恢复 / 审核确认：统一由 ConfirmDialog 承载 -->
    <ConfirmDialog
      v-model="deleteDialog"
      title="确认删除"
      text="您确定要删除这条评论吗？此操作可以撤销。"
      confirm-text="删除"
      @confirm="deleteItem"
    />
    <ConfirmDialog
      v-model="restoreDialog"
      title="确认恢复"
      text="您确定要恢复这条评论吗？"
      confirm-text="恢复"
      color="success"
      @confirm="restoreItem"
    />
    <ConfirmDialog
      v-model="approveDialog"
      title="确认通过"
      text="您确定要通过这条评论的审核吗？"
      confirm-text="通过"
      color="success"
      @confirm="approveItem"
    />

    <AppSnackbar :state="toast" />
  </v-container>
</template>

<script>
import { ref } from 'vue'
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useTable } from '@/composables/useTable'
import { useMessage } from '@/composables/useMessage'
import { formatDate, formatNumber, getUaInfo } from '@/utils/format'
import http from '@/api/index'

/** 状态标签 -> 后端 status 参数 */
const STATUS_MAP = { pending: -1, normal: 0, deleted: 1 }

export default {
  name: 'CommentControlView',
  components: {
    ShowMarkdown,
    AdminDataTable,
    ConfirmDialog,
    AppSnackbar,
  },
  setup() {
    const { toast, showMessage } = useMessage()
    const activeTab = ref('pending')
    const searchUserId = ref('')
    const searchArticleId = ref('')

    // 分页、加载态、筛选联动全部由 useTable 承担
    const table = useTable({
      url: '/admin/comment/list',
      size: 20,
      params: () => ({
        status: STATUS_MAP[activeTab.value],
        userId: searchUserId.value,
        articleId: searchArticleId.value,
      }),
    })

    return {
      toast,
      showMessage,
      activeTab,
      searchUserId,
      searchArticleId,
      table,
      // 模板里直接使用这几个格式化函数
      formatDate,
      formatNumber,
      getUaInfo,
    }
  },
  data() {
    return {
      detailDialog: false,
      deleteDialog: false,
      approveDialog: false,
      restoreDialog: false,
      selectedItem: null,
      headers: [
        { title: '评论内容', key: 'comment', align: 'start', sortable: false },
        { title: '用户ID', key: 'userId', align: 'center', sortable: false, width: '100px' },
        { title: '稿件ID', key: 'articleId', align: 'center', sortable: false, width: '100px' },
        { title: '互动', key: 'interactions', align: 'center', sortable: false, width: '100px' },
        { title: '类型', key: 'type', align: 'center', sortable: false, width: '100px' },
        { title: 'AI 审核意见', key: 'aiExamineMessage', align: 'center', sortable: false, width: '100px' },
        { title: 'IP信息', key: 'ipInfo', align: 'center', sortable: false },
        { title: '状态', key: 'status', align: 'center', sortable: false, width: '100px' },
        { title: '创建时间', key: 'createTime', align: 'center', sortable: false, width: '180px' },
        { title: '操作', key: 'actions', align: 'center', sortable: false, width: '120px' },
      ],
    }
  },
  methods: {
    /** 统一的写操作：成功后自动刷新列表并提示 */
    async doAction(promise, successText) {
      try {
        const json = await promise
        if (json?.data) {
          this.showMessage(successText, 'success')
          this.closeAllDialogs()
          await this.table.refresh()
        } else {
          this.showMessage(json?.message || '操作失败', 'error')
        }
      } catch (error) {
        this.showMessage(error?.message || '操作失败', 'error')
      }
    },
    closeAllDialogs() {
      this.deleteDialog = false
      this.restoreDialog = false
      this.approveDialog = false
      this.detailDialog = false
    },
    getStatusColor(status) {
      if (status === 0) return 'success'
      if (status === 1) return 'error'
      if (status === -1) return 'warning'
      return 'grey'
    },
    getStatusText(status) {
      if (status === 0) return '正常'
      if (status === 1) return '已删除'
      if (status === -1) return '待审核'
      return '未知'
    },
    confirmApprove(item) {
      this.selectedItem = item
      this.approveDialog = true
    },
    approveItem() {
      if (!this.selectedItem) return
      const entity = { ...this.selectedItem, status: 0 }
      return this.doAction(http.post('/admin/examine/comment', entity), '评论已通过审核')
    },
    handleTabChange() {
      return this.table.reload()
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
      return this.doAction(http.post('/admin/comment/toggle', { id: this.selectedItem.id }), '评论已删除')
    },
    restoreItem() {
      if (!this.selectedItem) return
      return this.doAction(http.post('/admin/comment/toggle', { id: this.selectedItem.id }), '评论已恢复')
    },
    handleSearch() {
      return this.table.reload()
    },
    clearUserIdSearch() {
      this.searchUserId = ''
      return this.table.reload()
    },
    clearArticleIdSearch() {
      this.searchArticleId = ''
      return this.table.reload()
    },
    clearAllFilters() {
      this.searchUserId = ''
      this.searchArticleId = ''
      return this.table.reload()
    },
    removeFilter(type) {
      if (type === 'userId') this.searchUserId = ''
      else if (type === 'articleId') this.searchArticleId = ''
      return this.table.reload()
    },
  },
  computed: {
    activeSearchFilters() {
      const filters = []
      if (this.searchUserId) filters.push({ type: 'userId', label: `用户: ${this.searchUserId}` })
      if (this.searchArticleId) {
        filters.push({ type: 'articleId', label: `稿件: ${this.searchArticleId}` })
      }
      return filters
    },
  },
}
</script>

<style scoped>
:deep(.v-data-table-server th) {
  font-weight: 600 !important;
  background-color: #f5f5f5;
}
</style>
