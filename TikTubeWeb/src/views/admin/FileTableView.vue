<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="blue">
        <v-toolbar-title class="text-h5 font-weight-medium">
          <v-icon icon="mdi-file-multiple" class="mr-2"></v-icon>
          文件管理
        </v-toolbar-title>
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

      <v-alert type="info" variant="tonal" border="start" density="comfortable" class="ma-4">
        文件管理系统支持视频、图片、附件等多种类型文件的管理。当前共有：{{ table.totalCount }}
        个文件。 临时文件将在一段时间后自动清理，请确保重要文件已标记为"已发布"状态。
      </v-alert>

      <!-- 筛选区域 -->
      <div class="search-options pa-4 bg-grey-lighten-5">
        <v-row>
          <v-col cols="12" sm="5">
            <v-select
              v-model="fileTypeFilter"
              :items="fileTypeOptions"
              label="文件类型"
              variant="outlined"
              density="comfortable"
              hide-details
              prepend-inner-icon="mdi-filter-variant"
              @update:model-value="loadItems"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="5">
            <v-select
              v-model="statusFilter"
              :items="statusOptions"
              label="状态"
              variant="outlined"
              density="comfortable"
              hide-details
              prepend-inner-icon="mdi-check-circle-outline"
              @update:model-value="loadItems"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="2" class="d-flex align-center">
            <v-btn
              color="primary"
              variant="text"
              prepend-icon="mdi-refresh"
              @click="resetFilters"
              block
            >
              重置筛选
            </v-btn>
          </v-col>
        </v-row>
      </div>

      <!-- 数据表格 -->
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
          empty-text="暂无文件数据"
          show-refresh-on-empty
          @update:page="table.load"
          @refresh="table.refresh"
        >
          <!-- 文件类型列 -->
          <template #[`item.type`]="{ item }">
            <v-chip :color="getTypeColor(item.type)" text-color="white" size="small">
              {{ getTypeText(item.type) }}
            </v-chip>
          </template>

          <!-- 文件状态列 -->
          <template #[`item.status`]="{ item }">
            <v-chip
              :color="item.status === 0 ? 'warning' : item.status === 1 ? 'success' : 'error'"
              text-color="white"
              size="small"
            >
              {{ item.status === 0 ? '临时文件' : item.status === 1 ? '已发布' : '删除失败' }}
            </v-chip>
          </template>

          <!-- 文件URL列 -->
          <template #[`item.fileUrl`]="{ item }">
            <div class="url-cell">{{ item.fileUrl }}</div>
          </template>

          <!-- 文件大小列 -->
          <template #[`item.size`]="{ item }">
            {{ formatFileSize(item.size) }}
          </template>

          <!-- 上传时间列 -->
          <template #[`item.uploadTime`]="{ item }">
            {{ formatDate(item.uploadTime) }}
          </template>

          <!-- 操作列 -->
          <template #[`item.actions`]="{ item }">
            <div class="d-flex justify-center">
              <v-tooltip location="top" text="预览">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    variant="text"
                    size="small"
                    class="mr-1"
                    color="info"
                    @click="previewFile(item)"
                  >
                    <v-icon>mdi-eye</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip v-if="item.articleId" location="top" text="查看稿件">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    variant="text"
                    size="small"
                    color="primary"
                    :href="`/video/${item.articleId}`"
                    target="_blank"
                    class="mr-1"
                  >
                    <v-icon>mdi-open-in-new</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip location="top" text="修改 URL">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    variant="text"
                    size="small"
                    color="warning"
                    @click="editFileUrl(item)"
                    class="mr-1"
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
                    variant="text"
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
              <span class="text-body-1 text-grey">暂无文件数据</span>
              <v-btn color="primary" class="mt-4" @click="loadItems">刷新数据</v-btn>
            </div>
          </template>
        </AdminDataTable>
      </v-card-text>
    </v-card>

    <!-- 预览对话框 -->
    <v-dialog v-model="previewDialog" max-width="800px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-row class="mt-1 mb-1">
            <v-icon class="mr-2">mdi-eye</v-icon>
            文件预览
            <v-spacer></v-spacer>
            <v-btn
              icon="mdi-close"
              variant="text"
              color="white"
              @click="previewDialog = false"
            ></v-btn>
          </v-row>
        </v-card-title>
        <v-card-text class="pa-4">
          <div v-if="selectedFile" class="text-center">
            <!-- 视频预览 -->
            <video
              v-if="selectedFile.type === 0"
              controls
              class="preview-media"
              :src="selectedFile.fileUrl"
            ></video>

            <!-- 图片预览 -->
            <img
              v-else-if="[1, 3, 4].includes(selectedFile.type)"
              :src="selectedFile.fileUrl"
              class="preview-media"
              alt="文件预览"
            />

            <!-- 其他文件类型 -->
            <div v-else class="pa-5">
              <v-icon size="large" color="primary">mdi-file-document-outline</v-icon>
              <div class="mt-2">{{ selectedFile.fileOriginalName }}</div>
              <div class="text-caption">不支持预览此类型文件</div>
            </div>

            <div class="mt-3 text-left">
              <v-card variant="outlined" class="pa-3">
                <div class="d-flex flex-column gap-2">
                  <div class="d-flex align-center">
                    <v-icon size="small" color="primary" class="mr-2">mdi-file-document</v-icon>
                    <strong>文件名：</strong>
                    <span class="ml-2">{{ selectedFile.fileOriginalName }}</span>
                  </div>
                  <div class="d-flex align-center">
                    <v-icon size="small" color="primary" class="mr-2">mdi-link</v-icon>
                    <strong>URL：</strong>
                    <span class="ml-2 text-truncate">{{ selectedFile.fileUrl }}</span>
                  </div>
                  <div class="d-flex align-center">
                    <v-icon size="small" color="primary" class="mr-2">mdi-harddisk</v-icon>
                    <strong>大小：</strong>
                    <span class="ml-2">{{ formatFileSize(selectedFile.size) }}</span>
                  </div>
                  <div class="d-flex align-center">
                    <v-icon size="small" color="primary" class="mr-2">mdi-calendar</v-icon>
                    <strong>上传时间：</strong>
                    <span class="ml-2">{{ formatDate(selectedFile.uploadTime) }}</span>
                  </div>
                  <div class="d-flex align-center">
                    <v-icon size="small" color="primary" class="mr-2">mdi-file-cog</v-icon>
                    <strong>文件类型：</strong>
                    <v-chip
                      size="x-small"
                      :color="getTypeColor(selectedFile.type)"
                      class="ml-2"
                      text-color="white"
                    >
                      {{ getTypeText(selectedFile.type) }}
                    </v-chip>
                  </div>
                  <div class="d-flex align-center">
                    <v-icon size="small" color="primary" class="mr-2">mdi-information</v-icon>
                    <strong>状态：</strong>
                    <v-chip
                      size="x-small"
                      :color="
                        selectedFile.status === 0
                          ? 'warning'
                          : selectedFile.status === 1
                            ? 'success'
                            : 'error'
                      "
                      class="ml-2"
                      text-color="white"
                    >
                      {{
                        selectedFile.status === 0
                          ? '临时文件'
                          : selectedFile.status === 1
                            ? '已发布'
                            : '删除失败'
                      }}
                    </v-chip>
                  </div>
                </div>
              </v-card>
            </div>
          </div>
        </v-card-text>
        <v-card-actions class="pa-4 bg-grey-lighten-5">
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            variant="text"
            @click="previewDialog = false"
            prepend-icon="mdi-close"
          >
            关闭
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 修改URL对话框 -->
    <v-dialog v-model="editDialog" max-width="600px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-row class="mt-1 mb-1">
            <v-icon class="mr-2">mdi-pencil</v-icon>
            修改文件信息
            <v-spacer></v-spacer>
            <v-btn
              icon="mdi-close"
              variant="text"
              color="white"
              @click="editDialog = false"
            ></v-btn>
          </v-row>
        </v-card-title>
        <v-card-text class="pt-4">
          <v-row>
            <v-col> 谨慎使用，误修改可能造成系统数据异常，部分稿件无法正常访问等问题 </v-col>
          </v-row>
          <v-form ref="form">
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="editedFile.fileOriginalName"
                  label="文件名"
                  required
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-file-document"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-text-field
                  v-model="editedFile.fileUrl"
                  label="文件URL"
                  required
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-link"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="editedFile.status"
                  :items="statusOptions.filter((item) => item.value !== null)"
                  label="文件状态"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-check-circle-outline"
                  item-title="title"
                  item-value="value"
                ></v-select>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="editedFile.articleId"
                  label="关联稿件ID"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-file-link"
                  hint="可选，关联到特定稿件"
                  persistent-hint
                  type="number"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 bg-grey-lighten-5">
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="editDialog = false" prepend-icon="mdi-close">
            取消
          </v-btn>
          <v-btn
            color="primary"
            variant="text"
            @click="updateFileInfo"
            prepend-icon="mdi-content-save"
          >
            保存
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除确认：统一由 ConfirmDialog 承载 -->
    <ConfirmDialog
      v-model="deleteDialog"
      title="确认删除"
      confirm-text="删除"
      max-width="500"
      @confirm="deleteFile"
    >
      <v-alert type="warning" variant="tonal" border="start" density="comfortable" class="mb-3">
        您确定要删除这个文件吗？此操作不可撤销。
      </v-alert>
      <div v-if="selectedFile" class="d-flex align-center">
        <v-icon size="small" color="primary" class="mr-2">mdi-file-document</v-icon>
        <strong>文件名：</strong>
        <span class="ml-2">{{ selectedFile.fileOriginalName }}</span>
      </div>
    </ConfirmDialog>

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
import { formatDate } from '@/utils/format'
import http from '@/api/index'

export default {
  name: 'FileTableView',
  components: {
    AdminDataTable,
    ConfirmDialog,
    AppSnackbar,
  },
  setup() {
    const { toast, showMessage } = useMessage()
    const fileTypeFilter = ref(null)
    const statusFilter = ref(null)

    // 分页、加载态、筛选联动全部由 useTable 承担
    const table = useTable({
      url: '/admin/files/list',
      size: 25,
      params: () => ({
        type: fileTypeFilter.value,
        status: statusFilter.value,
      }),
      onError: () => showMessage('加载文件列表失败', 'error'),
    })

    return {
      toast,
      showMessage,
      fileTypeFilter,
      statusFilter,
      table,
      // 模板里直接使用的格式化函数
      formatDate,
    }
  },
  data() {
    return {
      // 表格列配置
      headers: [
        { title: 'ID', key: 'id', sortable: false },
        { title: '文件名', key: 'fileOriginalName', sortable: false },
        { title: '文件URL', key: 'fileUrl', sortable: false },
        { title: '文件大小', key: 'size', sortable: false },
        { title: '上传时间', key: 'uploadTime', sortable: false },
        { title: '上传用户ID', key: 'uploadUserId', sortable: false },
        { title: '文件类型', key: 'type', sortable: false },
        { title: '状态', key: 'status', sortable: false },
        { title: '操作', key: 'actions', sortable: false },
      ],

      // 筛选选项
      fileTypeOptions: [
        { title: '全部类型', value: null },
        { title: '视频', value: 0 },
        { title: '图片', value: 1 },
        { title: '附件', value: 2 },
        { title: '头像', value: 3 },
        { title: '顶部大图', value: 4 },
        { title: '其他', value: 5 },
      ],
      statusOptions: [
        { title: '全部状态', value: null },
        { title: '临时文件', value: 0 },
        { title: '已发布', value: 1 },
        { title: '删除失败，需手动处理', value: -1 },
      ],

      // 对话框控制
      previewDialog: false,
      editDialog: false,
      deleteDialog: false,

      // 选中的文件和编辑数据
      selectedFile: null,
      editedFileUrl: '',
      editedFile: {
        id: null,
        fileOriginalName: '',
        fileUrl: '',
        status: 0,
        articleId: null,
      },
    }
  },
  methods: {
    /**
     * 统一的写操作：成功后自动刷新列表并提示
     * @param {Promise} promise
     * @param {string} successText
     * @param {object} [opts] { failureText } 失败文案（文件接口失败码固定，不看 message）
     */
    async doAction(promise, successText, opts = {}) {
      const { failureText = '操作失败' } = opts
      try {
        const json = await promise
        if (json?.data) {
          this.showMessage(successText, 'success')
          this.closeAllDialogs()
          await this.table.refresh()
        } else {
          this.showMessage(failureText, 'error')
        }
      } catch (error) {
        this.showMessage(error?.message || failureText, 'error')
      }
    },
    closeAllDialogs() {
      this.deleteDialog = false
      this.editDialog = false
    },
    // 加载文件列表数据
    loadItems() {
      return this.table.refresh()
    },
    // 重置筛选条件
    resetFilters() {
      this.fileTypeFilter = null
      this.statusFilter = null
      return this.table.reload()
    },
    // 获取文件类型文本
    getTypeText(type) {
      const typeMap = {
        0: '视频',
        1: '图片',
        2: '附件',
        3: '头像',
        4: '顶部大图',
        5: '其他',
      }
      return typeMap[type] || '未知'
    },
    // 获取文件类型对应的颜色
    getTypeColor(type) {
      const colorMap = {
        0: 'red',
        1: 'blue',
        2: 'green',
        3: 'purple',
        4: 'indigo',
        5: 'grey',
      }
      return colorMap[type] || 'grey'
    },
    // 格式化文件大小
    formatFileSize(size) {
      if (!size) return '0 B'

      const units = ['B', 'KB', 'MB', 'GB', 'TB']
      let i = 0

      while (size >= 1024 && i < units.length - 1) {
        size /= 1024
        i++
      }

      return `${size.toFixed(2)} ${units[i]}`
    },
    // 预览文件
    previewFile(item) {
      this.selectedFile = item
      this.previewDialog = true
    },
    // 编辑文件URL
    editFileUrl(item) {
      this.selectedFile = item
      this.editedFileUrl = item.fileUrl
      // 初始化编辑对象
      this.editedFile = {
        id: item.id,
        fileOriginalName: item.fileOriginalName,
        fileUrl: item.fileUrl,
        status: item.status,
        articleId: item.articleId || null,
      }
      this.editDialog = true
    },
    // 更新文件信息
    async updateFileInfo() {
      // 构建更新对象
      const updateData = {
        id: this.editedFile.id,
        fileOriginalName: this.editedFile.fileOriginalName,
        fileUrl: this.editedFile.fileUrl,
        status: this.editedFile.status,
        articleId: this.editedFile.articleId,
      }

      try {
        return await this.doAction(http.post('/admin/files/update', updateData), '文件信息已更新', {
          failureText: '更新文件信息失败',
        })
      } finally {
        // 与原来一致：无论成功失败都关闭编辑弹窗
        this.editDialog = false
      }
    },
    // 确认删除
    confirmDelete(item) {
      this.selectedFile = item
      this.deleteDialog = true
    },
    // 删除文件
    deleteFile() {
      const fileTableEntity = {
        id: this.selectedFile.id,
      }
      return this.doAction(
        http.post('/admin/files/delete', fileTableEntity),
        '文件已删除',
        { failureText: '删除文件失败，该文件关联了稿件或不存在' }
      )
    },
  },
}
</script>

<style scoped>
.preview-media {
  max-width: 100%;
  max-height: 400px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.search-options {
  border-radius: 0 0 8px 8px;
}

:deep(.v-data-table-server th) {
  font-weight: 600 !important;
  background-color: #f5f5f5;
}

.url-cell {
  max-width: 500px;
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
  line-height: 1.4;
}
</style>
