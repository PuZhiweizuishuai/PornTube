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

      <v-tabs
        v-model="activeTab"
        color="primary"
        align-tabs="center"
        class="mb-4"
        @click="filteredArticles"
      >
        <v-tab value="all">所有稿件</v-tab>
        <v-tab value="delete">已删除</v-tab>
        <!-- <v-tab value="passed">已通过</v-tab>
        <v-tab value="pending">待审核</v-tab>
        <v-tab value="rejected">未通过</v-tab> -->
      </v-tabs>

      <v-card-text>
        <AdminDataTable
          :headers="headers"
          :items="table.items"
          :loading="table.loading"
          :page="table.page"
          :total-pages="table.totalPages"
          :total-count="table.totalCount"
          :page-size="table.pageSize"
          mobile-breakpoint="md"
          empty-text="暂无稿件数据"
          show-refresh-on-empty
          @update:page="table.load"
          @refresh="table.refresh"
        >
          <template #[`item.imgUrl`]="{ item }">
            <v-img
              v-if="item.type == 0"
              :src="item.imgUrl"
              max-height="120px"
              max-width="213px"
              class="rounded-lg elevation-1 my-2"
            />
            <span v-if="item.type == 2" class="desc-wrap" v-text="item.describes"> </span>
          </template>

          <template #[`item.info`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-subtitle-1 font-weight-medium title-wrap">
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
                <span class="text-caption">{{ formatCount(item.viewCount) }}</span>
              </div>
              <div class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-1">mdi-thumb-up</v-icon>
                <span class="text-caption">{{ formatCount(item.likeCount) }}</span>
              </div>
              <div class="d-flex align-center">
                <v-icon size="small" class="mr-1">mdi-message</v-icon>
                <span class="text-caption">{{ formatCount(item.commentCount) }}</span>
              </div>
            </div>
          </template>

          <template #[`item.examineStatus`]="{ item }">
            <v-chip :color="getStatusColor(item.examineStatus)" size="small" class="text-white">
              {{ getStatusText(item.examineStatus) }}
            </v-chip>
            <v-chip v-if="item.status === 1" color="error" size="small" class="text-white mt-1">
              已删除
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
                    v-if="item.type == 0"
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
                  <v-btn
                    v-if="item.type == 2"
                    v-bind="props"
                    icon
                    size="small"
                    color="primary"
                    class="mr-1"
                    :href="`/text/${item.id}`"
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
              <span class="text-body-1 text-grey">暂无稿件数据</span>
              <v-btn color="primary" class="mt-4" @click="getList">刷新数据</v-btn>
            </div>
          </template>
        </AdminDataTable>
      </v-card-text>
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
                  item-title="message"
                  item-value="code"
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

    <!-- 删除确认对话框：正文含 HTML（加粗标题），用默认插槽 -->
    <ConfirmDialog
      v-model="deleteDialog"
      title="确认删除"
      confirm-text="删除"
      @confirm="deleteItem"
    >
      您确定要删除视频 <strong>{{ selectedItem?.title }}</strong> 吗？此操作不可撤销。
    </ConfirmDialog>

    <!-- 恢复确认对话框 -->
    <ConfirmDialog
      v-model="restoreDialog"
      title="确认恢复"
      confirm-text="恢复"
      color="success"
      @confirm="restoreItem"
    >
      您确定要恢复视频 <strong>{{ selectedItem?.title }}</strong> 吗？
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
import { formatDate, formatCount } from '@/utils/format'
import http from '@/api/index'

export default {
  name: 'ArticleListView',
  components: {
    AdminDataTable,
    ConfirmDialog,
    AppSnackbar,
  },
  setup() {
    const { toast, showMessage } = useMessage()
    const activeTab = ref('all')

    // 分页、加载态、tab 联动全部由 useTable 承担
    const table = useTable({
      url: '/studio/article/list',
      size: 10,
      params: () => ({
        type: 'admin',
        active: activeTab.value,
      }),
      onError: () => showMessage('获取数据失败', 'error'),
    })

    return {
      toast,
      showMessage,
      activeTab,
      table,
      // 模板里直接使用这两个格式化函数
      formatDate,
      formatCount,
    }
  },
  data() {
    return {
      examineDialog: false,
      deleteDialog: false,
      restoreDialog: false,
      selectedItem: null,
      selectedStatus: null,
      showRejectionOptions: false,
      rejectionType: 0,
      examineMessage: '',
      examineStatus: ['通过', '不通过'],
      examineItems: [],
      categoryList: {},
      categoryMap: {},
      headers: [
        { title: '封面/简介', key: 'imgUrl', align: 'center', sortable: false, width: '220px' },
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
  computed: {},
  mounted() {
    // 列表首屏由 useTable 自动加载，这里只拉两类辅助数据
    this.getCategoryList()
    this.getExamineItems()
  },
  methods: {
    filteredArticles() {
      return this.table.reload()
    },
    getList() {
      return this.table.refresh()
    },
    async getCategoryList() {
      try {
        const json = await http.get('/category/tree')
        if (json && json.data) {
          this.categoryList = json.data
          this.buildCategoryMap()
        }
      } catch (error) {
        console.error('[ArticleListView] 加载分区失败:', error?.message || error)
      }
    },
    async getExamineItems() {
      try {
        const json = await http.get('/examine/item')
        if (json && json.data) {
          this.examineItems = json.data
        }
      } catch (error) {
        console.error('[ArticleListView] 加载审核项失败:', error?.message || error)
      }
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
    async deleteItem() {
      if (!this.selectedItem) return

      try {
        const json = await http.post('/studio/article/delete', this.selectedItem)
        // 该接口用 data 里的结果码区分：0=成功 1=没有权限 其它=已经删除
        if (json?.data == 0) {
          this.showMessage('删除成功', 'success')
          this.table.items = this.table.items.filter((item) => item.id !== this.selectedItem.id)
        } else if (json?.data == 1) {
          this.showMessage('没有权限', 'success')
        } else {
          this.showMessage('已经删除', 'success')
        }
        this.deleteDialog = false
        this.selectedItem = null
      } catch (error) {
        this.showMessage(error?.message || '删除失败', 'error')
      }
    },
    handleExamineStatusChange(value) {
      this.showRejectionOptions = value === '不通过'
    },
    resetExamineForm() {
      this.selectedStatus = null
      this.rejectionType = 0
      this.examineMessage = ''
      this.showRejectionOptions = false
    },
    async saveExamine() {
      if (!this.selectedItem) return

      const result = this.selectedStatus === '通过'

      const data = {
        videoId: this.selectedItem.id,
        result,
        type: this.rejectionType,
        message: this.examineMessage,
      }

      try {
        // 审核接口只回 { status, message }，不带 data 信封，故以 status 判定
        const json = await http.post('/admin/examine', data)
        if (json?.status === 200) {
          this.showMessage('审核完成', 'success')
          this.examineDialog = false
          await this.table.refresh()
        } else {
          this.showMessage('审核失败：' + (json?.message || ''), 'error')
        }
      } catch (error) {
        this.showMessage('审核失败：' + (error?.message || ''), 'error')
      }
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
    confirmRestore(item) {
      this.selectedItem = item
      this.restoreDialog = true
    },
    async restoreItem() {
      if (!this.selectedItem) return

      try {
        // 恢复接口只回 true/false，不带 data 信封
        const json = await http.post('/admin/article/restore', this.selectedItem)
        if (json?.data === true) {
          this.showMessage('恢复成功', 'success')
          await this.table.refresh()
        } else {
          this.showMessage('恢复失败: ' + (json?.message || ''), 'error')
        }
      } catch (error) {
        this.showMessage('恢复失败: ' + (error?.message || ''), 'error')
      }
      this.restoreDialog = false
      this.selectedItem = null
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

/* 文本稿件简介：长串无空格内容也要在封面列内换行 */
.desc-wrap {
  display: block;
  max-width: 220px;
  margin: 0 auto;
  text-align: left;
  white-space: normal;
  word-break: break-all;
  overflow-wrap: anywhere;
}
</style>