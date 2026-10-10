<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="indigo">
        <v-toolbar-title class="text-h5 font-weight-medium">弹幕管理</v-toolbar-title>
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

      <!-- 搜索 userId 参数和 videoId 参数 -->
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
              placeholder="输入用户ID搜索该用户的弹幕"
              prepend-inner-icon="mdi-account-search"
              @keyup.enter="handleSearch"
              @click:clear="clearUserIdSearch"
            ></v-text-field>
          </v-col>
          <v-col cols="12" sm="5">
            <v-text-field
              v-model="searchVideoId"
              label="搜索视频ID"
              variant="outlined"
              density="comfortable"
              hide-details
              clearable
              placeholder="输入视频ID搜索该视频的弹幕"
              prepend-inner-icon="mdi-movie-search"
              @keyup.enter="handleSearch"
              @click:clear="clearVideoIdSearch"
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
        <v-tab value="pending">待审核弹幕</v-tab>
        <v-tab value="all">所有弹幕</v-tab>
        <v-tab value="normal">正常弹幕</v-tab>
        <v-tab value="deleted">已删除弹幕</v-tab>
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
          empty-text="暂无弹幕数据"
          show-refresh-on-empty
          @update:page="table.load"
          @refresh="table.refresh"
        >
          <template #[`item.text`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-body-1 title-wrap">{{ item.text }}</div>
              <div class="text-caption mt-1">
                <v-chip size="x-small" color="grey-lighten-2" class="mr-1">
                  {{ formatTime(item.time) }}
                </v-chip>
              </div>
            </div>
          </template>

          <template #[`item.author`]="{ item }">
            <v-btn
              :to="`/user/${item.author}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
              size="small"
            >
              {{ item.author }}
            </v-btn>
          </template>

          <template #[`item.colorView`]="{ item }">
            <div class="d-flex align-center justify-center">
              <v-avatar size="24" :style="{ backgroundColor: item.color }" class="mr-2"></v-avatar>
              <span class="text-caption">{{ item.color }}</span>
            </div>
          </template>

          <template #[`item.type`]="{ item }">
            <v-chip :color="getDanmakuTypeColor(item.type)" size="small" class="text-white">
              {{ getDanmakuTypeName(item.type) }}
            </v-chip>
          </template>

          <template #[`item.ipInfo`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-body-2">{{ item.ip || '未知IP' }}</div>
              <div class="text-caption text-grey mt-1">{{ item.city || '未知地区' }}</div>
              <div class="text-caption text-grey-darken-1 mt-1">
                {{ item.ua ? getUaInfo(item.ua) : '未知设备' }}
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

          <template #no-data>
            <div class="d-flex flex-column align-center pa-4">
              <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
              <span class="text-body-1 text-grey">暂无弹幕数据</span>
              <v-btn color="primary" class="mt-4" @click="getDanmukuList">刷新数据</v-btn>
            </div>
          </template>
        </AdminDataTable>
      </v-card-text>
    </v-card>

    <!-- 弹幕详情对话框 -->
    <v-dialog v-model="detailDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-message-text</v-icon>
          弹幕详情
        </v-card-title>
        <v-card-text class="pt-4">
          <v-list lines="two">
            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-account</v-icon>
              </template>
              <v-list-item-title>用户ID</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.author }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-video</v-icon>
              </template>
              <v-list-item-title>视频ID</v-list-item-title>
              <v-list-item-subtitle>{{ selectedItem?.videoId }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-message-text</v-icon>
              </template>
              <div>
                {{ selectedItem?.text }}
              </div>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-palette</v-icon>
              </template>
              <v-list-item-title>颜色</v-list-item-title>
              <v-list-item-subtitle>
                <div class="d-flex align-center">
                  <v-avatar
                    size="24"
                    :style="{ backgroundColor: selectedItem?.color }"
                    class="mr-2"
                  ></v-avatar>
                  {{ selectedItem?.color }}
                </div>
              </v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-timer</v-icon>
              </template>
              <v-list-item-title>出现时间</v-list-item-title>
              <v-list-item-subtitle>{{
                selectedItem ? formatTime(selectedItem.time) : ''
              }}</v-list-item-subtitle>
            </v-list-item>

            <v-list-item>
              <template #prepend>
                <v-icon color="indigo">mdi-format-float-none</v-icon>
              </template>
              <v-list-item-title>弹幕类型</v-list-item-title>
              <v-list-item-subtitle>
                <v-chip
                  :color="selectedItem ? getDanmakuTypeColor(selectedItem.type) : 'primary'"
                  size="small"
                  class="text-white"
                >
                  {{ selectedItem ? getDanmakuTypeName(selectedItem.type) : '' }}
                </v-chip>
              </v-list-item-subtitle>
            </v-list-item>

            <v-list-item v-if="selectedItem?.ip">
              <template #prepend>
                <v-icon color="indigo">mdi-earth</v-icon>
              </template>
              <v-list-item-title>IP地址</v-list-item-title>
              <v-list-item-subtitle
                >{{ selectedItem?.ip }}
                {{ selectedItem?.city || '(未知地区)' }}</v-list-item-subtitle
              >
            </v-list-item>

            <v-list-item v-if="selectedItem?.ua">
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
              {{ selectedItem?.status === 1 ? '恢复弹幕' : '删除弹幕' }}
            </v-btn>
          </template>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除 / 恢复 / 审核确认：统一由 ConfirmDialog 承载 -->
    <ConfirmDialog
      v-model="deleteDialog"
      title="确认删除"
      text="您确定要删除这条弹幕吗？此操作可以撤销。"
      confirm-text="删除"
      @confirm="deleteItem"
    />
    <ConfirmDialog
      v-model="restoreDialog"
      title="确认恢复"
      text="您确定要恢复这条弹幕吗？"
      confirm-text="恢复"
      color="success"
      @confirm="restoreItem"
    />
    <ConfirmDialog
      v-model="approveDialog"
      title="确认通过"
      text="您确定要通过这条弹幕的审核吗？"
      confirm-text="通过"
      color="success"
      @confirm="approveItem"
    />

    <AppSnackbar :state="toast" />
  </v-container>
</template>

<script>
import { ref } from 'vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useTable } from '@/composables/useTable'
import { useMessage } from '@/composables/useMessage'
import { formatDate, getUaInfo } from '@/utils/format'
import http from '@/api/index'

/** 状态标签 -> 后端 status 参数 */
const STATUS_MAP = { pending: -1, normal: 0, deleted: 1 }

export default {
  name: 'DanmukuControlView',
  components: {
    AdminDataTable,
    ConfirmDialog,
    AppSnackbar,
  },
  setup() {
    const { toast, showMessage } = useMessage()
    const activeTab = ref('pending')
    const searchUserId = ref('')
    const searchVideoId = ref('')

    // 分页、加载态、筛选联动全部由 useTable 承担
    const table = useTable({
      url: '/admin/danmaku/list',
      size: 20,
      params: () => ({
        status: STATUS_MAP[activeTab.value],
        userId: searchUserId.value,
        videoId: searchVideoId.value,
      }),
    })

    return {
      toast,
      showMessage,
      activeTab,
      searchUserId,
      searchVideoId,
      table,
      // 模板里直接使用这几个格式化函数
      formatDate,
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
        { title: '弹幕内容', key: 'text', align: 'start', sortable: false },
        { title: '用户ID', key: 'author', align: 'center', sortable: false, width: '100px' },
        { title: '文件ID', key: 'videoId', align: 'center', sortable: false, width: '100px' },
        { title: '颜色', key: 'colorView', align: 'center', sortable: false, width: '120px' },
        { title: '类型', key: 'type', align: 'center', sortable: false, width: '100px' },
        { title: '出现时间', key: 'time', align: 'center', sortable: false, width: '120px' },
        { title: 'IP信息', key: 'ipInfo', align: 'center', sortable: false },
        {
          title: 'AI 审核意见',
          key: 'aiExamineMessage',
          align: 'center',
          sortable: false,
          width: '100px',
        },
        { title: '状态', key: 'status', align: 'center', sortable: false, width: '100px' },
        { title: '创建时间', key: 'createTime', align: 'center', sortable: false, width: '180px' },
        { title: '操作', key: 'actions', align: 'center', sortable: false, width: '120px' },
      ],
    }
  },
  methods: {
    /**
     * 统一的写操作：成功后自动刷新列表并提示
     * @param {Promise} promise
     * @param {string} successText
     * @param {object} [opts] { isSuccess, errorText }
     *   isSuccess 用于判定成功（审核类接口只回 status/message，不带 data）
     */
    async doAction(promise, successText, opts = {}) {
      const { isSuccess, errorText = '操作失败' } = opts
      try {
        const json = await promise
        if (isSuccess ? isSuccess(json) : !!json?.data) {
          this.showMessage(successText, 'success')
          this.closeAllDialogs()
          await this.table.refresh()
        } else {
          this.showMessage(json?.message || errorText, 'error')
        }
      } catch (error) {
        this.showMessage(error?.message || errorText, 'error')
      }
    },
    closeAllDialogs() {
      this.deleteDialog = false
      this.restoreDialog = false
      this.approveDialog = false
      this.detailDialog = false
    },
    getDanmakuList() {
      return this.table.refresh()
    },
    handleTabChange() {
      return this.table.reload()
    },
    viewDetails(item) {
      this.selectedItem = item
      this.detailDialog = true
    },
    confirmApprove(item) {
      this.selectedItem = item
      this.approveDialog = true
    },
    confirmDelete(item) {
      this.selectedItem = item
      this.deleteDialog = true
    },
    confirmRestore(item) {
      this.selectedItem = item
      this.restoreDialog = true
    },
    approveItem() {
      if (!this.selectedItem) return
      const entity = { ...this.selectedItem, status: 0 }
      return this.doAction(
        http.post('/admin/examine/danmaku', entity),
        '弹幕已通过审核',
        // 审核接口返回 { status, message }，不带 data 信封
        { isSuccess: (json) => json?.status === 200 }
      )
    },
    deleteItem() {
      if (!this.selectedItem) return
      return this.doAction(
        http.post('/admin/danmaku/toggle', { id: this.selectedItem.id }),
        '弹幕已删除'
      )
    },
    restoreItem() {
      if (!this.selectedItem) return
      return this.doAction(
        http.post('/admin/danmaku/toggle', { id: this.selectedItem.id }),
        '弹幕已恢复'
      )
    },
    /** 弹幕出现时间：秒 -> mm:ss */
    formatTime(seconds) {
      if (seconds === undefined || seconds === null) return '00:00'

      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = Math.floor(seconds % 60)

      return `${minutes.toString().padStart(2, '0')}:${remainingSeconds
        .toString()
        .padStart(2, '0')}`
    },
    getDanmakuTypeName(type) {
      switch (type) {
        case 0:
          return '滚动'
        case 1:
          return '顶部'
        case 2:
          return '底部'
        default:
          return '未知'
      }
    },
    getDanmakuTypeColor(type) {
      switch (type) {
        case 0:
          return 'primary'
        case 1:
          return 'orange'
        case 2:
          return 'green'
        default:
          return 'grey'
      }
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
    handleSearch() {
      return this.table.reload()
    },
    clearUserIdSearch() {
      this.searchUserId = ''
      return this.table.reload()
    },
    clearVideoIdSearch() {
      this.searchVideoId = ''
      return this.table.reload()
    },
    clearAllFilters() {
      this.searchUserId = ''
      this.searchVideoId = ''
      return this.table.reload()
    },
    removeFilter(type) {
      if (type === 'userId') this.searchUserId = ''
      else if (type === 'videoId') this.searchVideoId = ''
      return this.table.reload()
    },
  },
  computed: {
    activeSearchFilters() {
      const filters = []
      if (this.searchUserId) filters.push({ type: 'userId', label: `用户: ${this.searchUserId}` })
      if (this.searchVideoId) filters.push({ type: 'videoId', label: `视频: ${this.searchVideoId}` })
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
