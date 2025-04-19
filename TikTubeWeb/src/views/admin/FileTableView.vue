<template>
  <v-container fluid>
    <v-card>
      <v-card-title class="d-flex align-center justify-space-between">
        <span>文件管理，当前共有：{{ totalItems }} 个文件</span>
        <!-- <v-text-field
          v-model="search"
          append-icon="mdi-magnify"
          label="搜索"
          single-line
          hide-details
          density="compact"
          class="ml-auto"
          style="max-width: 300px"
        ></v-text-field> -->
      </v-card-title>

      <v-data-table-server
        v-model:items-per-page="itemsPerPage"
        v-model:page="page"
        :headers="headers"
        :items="items"
        :items-length="totalItems"
        :loading="loading"
        item-value="id"
        @update:options="loadItems"
        class="elevation-1"
      >
        <!-- 文件类型列 -->
        <template #[`item.type`]="{ item }">
          <v-chip :color="getTypeColor(item.type)" size="small">
            {{ getTypeText(item.type) }}
          </v-chip>
        </template>

        <!-- 文件状态列 -->
        <template #[`item.status`]="{ item }">
          <v-chip :color="item.status === 0 ? 'warning' : 'success'" size="small">
            {{ item.status === 0 ? '临时文件' : '已发布' }}
          </v-chip>
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
          <div class="d-flex">
            <v-tooltip location="top" text="预览">
              <template #activator="{ props }">
                <v-btn
                  v-bind="props"
                  icon
                  variant="text"
                  size="small"
                  class="mr-1"
                  color="success"
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
      </v-data-table-server>
    </v-card>

    <!-- 预览对话框 -->
    <v-dialog v-model="previewDialog" max-width="800px">
      <v-card>
        <v-card-title>文件预览</v-card-title>
        <v-card-text>
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
              <div><strong>文件名：</strong>{{ selectedFile.fileOriginalName }}</div>
              <div><strong>URL：</strong>{{ selectedFile.fileUrl }}</div>
              <div><strong>大小：</strong>{{ formatFileSize(selectedFile.size) }}</div>
              <div><strong>上传时间：</strong>{{ formatDate(selectedFile.uploadTime) }}</div>
              <div><strong>文件类型：</strong>{{ getTypeText(selectedFile.type) }}</div>
              <div>
                <strong>状态：</strong>{{ selectedFile.status === 0 ? '临时文件' : '已发布' }}
              </div>
            </div>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="previewDialog = false">关闭</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 修改URL对话框 -->
    <v-dialog v-model="editDialog" max-width="500px">
      <v-card>
        <v-card-title>修改文件URL</v-card-title>
        <v-card-text>
          <v-text-field v-model="editedFileUrl" label="文件URL" required></v-text-field>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="editDialog = false">取消</v-btn>
          <v-btn color="primary" variant="text" @click="updateFileUrl">保存</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除确认对话框 -->
    <v-dialog v-model="deleteDialog" max-width="500px">
      <v-card>
        <v-card-title>确认删除</v-card-title>
        <v-card-text>
          您确定要删除这个文件吗？此操作不可撤销。
          <div class="mt-2" v-if="selectedFile">
            <strong>文件名：</strong> {{ selectedFile.fileOriginalName }}
          </div>
        </v-card-text>
        <v-card-actions>
          <v-spacer></v-spacer>
          <v-btn color="primary" variant="text" @click="deleteDialog = false">取消</v-btn>
          <v-btn color="error" variant="text" @click="deleteFile">删除</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 通知提示 -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" :timeout="3000">
      {{ snackbarText }}
      <template #actions>
        <v-btn variant="text" @click="snackbar = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  name: 'FileTableView',
  data() {
    return {
      // 表格数据
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
      items: [],
      loading: false,
      itemsPerPage: 25,
      page: 1,
      totalItems: 0,
      search: '',

      // 对话框控制
      previewDialog: false,
      editDialog: false,
      deleteDialog: false,

      // 选中的文件和编辑数据
      selectedFile: null,
      editedFileUrl: '',

      // 通知提示
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
    }
  },

  mounted() {
    this.loadItems()
  },

  methods: {
    // 加载文件列表数据
    loadItems() {
      this.loading = true
      this.httpGet(`/admin/files/list?limit=${this.itemsPerPage}&page=${this.page}`, (json) => {
        if (json.status === 200) {
          this.items = json.data.list
          this.totalItems = json.data.totalCount
        } else {
          this.showSnackbar('加载文件列表失败', 'error')
        }
        this.loading = false
      })
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

    // 格式化日期
    formatDate(timestamp) {
      if (!timestamp) return '--'

      const date = new Date(timestamp)
      return new Intl.DateTimeFormat('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit',
      }).format(date)
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
      this.editDialog = true
    },

    // 更新文件URL
    updateFileUrl() {
      // 这里可以添加实际的API调用来更新文件URL
      // 示例代码:
      /*
      this.httpPost('/admin/files/update', {
        id: this.selectedFile.id,
        fileUrl: this.editedFileUrl
      }, (json) => {
        if (json.status === 200) {
          // 更新本地数据
          const index = this.items.findIndex(item => item.id === this.selectedFile.id)
          if (index !== -1) {
            this.items[index].fileUrl = this.editedFileUrl
          }
          this.showSnackbar('文件URL已更新', 'success')
        } else {
          this.showSnackbar('更新文件URL失败', 'error')
        }
        this.editDialog = false
      })
      */

      // 模拟更新成功
      const index = this.items.findIndex((item) => item.id === this.selectedFile.id)
      if (index !== -1) {
        this.items[index].fileUrl = this.editedFileUrl
      }
      this.showSnackbar('文件URL已更新', 'success')
      this.editDialog = false
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
      this.httpPost('/admin/files/delete', fileTableEntity, (json) => {
        if (json.data === true) {
          // 从本地列表中移除
          this.items = this.items.filter((item) => item.id !== this.selectedFile.id)
          this.showSnackbar('文件已删除', 'success')
        } else {
          this.showSnackbar('删除文件失败，该文件关联了稿件或不存在', 'error')
        }
        this.deleteDialog = false
      })
    },

    // 显示通知提示
    showSnackbar(text, color = 'success') {
      this.snackbarText = text
      this.snackbarColor = color
      this.snackbar = true
    },
  },
}
</script>

<style>
.preview-media {
  max-width: 100%;
  max-height: 400px;
  object-fit: contain;
}

.v-data-table-server th {
  white-space: nowrap;
}

.v-data-table-server td {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>