<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="indigo">
        <v-toolbar-title class="text-h5 font-weight-medium">
          <v-icon icon="mdi-bullhorn" class="mr-2"></v-icon>
          广告公告管理
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn
          prepend-icon="mdi-plus"
          color="white"
          variant="tonal"
          @click="openDialog()"
          class="mr-2"
        >
          新增广告公告
        </v-btn>
      </v-toolbar>

      <v-alert type="info" variant="tonal" border="start" density="comfortable" class="ma-4">
        首页公告与首页弹窗广告仅支持一条，如有多条只显示第一条内容，只建议在视频贴片广告中加入视频，别的地方不建议。新闻轮播因为目前没有找到一个合适的投放位置，所以暂不生效。访问量统计目前仅支持贴片广告和弹窗广告！
      </v-alert>

      <!-- 筛选区域 -->
      <div class="search-options pa-4 bg-grey-lighten-5">
        <v-row>
          <v-col cols="12" sm="5">
            <v-select
              v-model="adType"
              :items="adTypeOptions"
              label="广告类型"
              variant="outlined"
              density="comfortable"
              hide-details
              prepend-inner-icon="mdi-filter-variant"
              @update:model-value="fetchAdvertisements"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="5">
            <v-select
              v-model="adStatus"
              :items="statusOptions"
              label="状态"
              variant="outlined"
              density="comfortable"
              hide-details
              prepend-inner-icon="mdi-check-circle-outline"
              @update:model-value="fetchAdvertisements"
            ></v-select>
          </v-col>
          <v-col cols="12" sm="2" class="d-flex align-center">
            <v-btn
              color="primary"
              variant="text"
              prepend-icon="mdi-refresh"
              @click="fetchAdvertisements"
              block
            >
              刷新
            </v-btn>
          </v-col>
        </v-row>
      </div>

      <!-- 数据表格 -->
      <v-card-text>
        <AdminDataTable
          :headers="headers"
          :items="table.items"
          :loading="table.loading"
          :page="table.page"
          :total-pages="table.totalPages"
          :total-count="table.totalCount"
          :page-size="table.pageSize"
          mobile-breakpoint="sm"
          empty-text="暂无广告数据"
          show-refresh-on-empty
          @update:page="table.load"
          @refresh="table.refresh"
        >
          <template #[`item.title`]="{ item }">
            <div class="d-flex flex-column">
              <div class="text-subtitle-1 font-weight-medium text-truncate">
                {{ item.title }}
              </div>
              <div class="text-caption text-grey d-flex align-center mt-1">
                <v-icon size="small" class="mr-1">mdi-calendar</v-icon>
                {{ formatTime(item.startTime) }} - {{ formatTime(item.endTime) }}
              </div>
            </div>
          </template>

          <template #[`item.type`]="{ item }">
            <v-chip :color="getTypeColor(item.type)" text-color="white" size="small">
              {{ getTypeText(item.type) }}
            </v-chip>
          </template>

          <template #[`item.status`]="{ item }">
            <v-chip
              :color="item.status === 1 ? 'success' : 'error'"
              text-color="white"
              size="small"
            >
              {{ item.status === 1 ? '启用' : '禁用' }}
            </v-chip>
          </template>

          <template #[`item.media`]="{ item }">
            <div class="d-flex flex-column">
              <div v-if="item.imageUrl" class="d-flex align-center mb-1">
                <v-icon size="small" class="mr-1">mdi-image</v-icon>
                <span class="text-caption text-truncate">有图片</span>
              </div>
              <div v-if="item.videoUrl" class="d-flex align-center">
                <v-icon size="small" class="mr-1">mdi-video</v-icon>
                <span class="text-caption text-truncate">有视频</span>
              </div>
              <div v-if="!item.imageUrl && !item.videoUrl" class="text-caption text-grey">
                无媒体
              </div>
            </div>
          </template>

          <template #[`item.actions`]="{ item }">
            <div class="d-flex justify-center">
              <v-tooltip location="top" text="编辑">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    color="primary"
                    variant="text"
                    class="mr-1"
                    @click="openDialog(item)"
                  >
                    <v-icon>mdi-pencil</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip location="top" text="预览">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    color="info"
                    variant="text"
                    class="mr-1"
                    @click="previewAd(item)"
                  >
                    <v-icon>mdi-eye</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip location="top" :text="item.status === 1 ? '禁用' : '启用'">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    :icon="item.status === 1 ? 'mdi-close' : 'mdi-check'"
                    size="small"
                    :color="item.status === 1 ? 'error' : 'success'"
                    variant="text"
                    @click="toggleStatus(item)"
                  >
                  </v-btn>
                </template>
              </v-tooltip>
            </div>
          </template>

          <template #no-data>
            <div class="d-flex flex-column align-center pa-4">
              <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
              <span class="text-body-1 text-grey">暂无广告数据</span>
              <v-btn color="primary" class="mt-4" @click="fetchAdvertisements">刷新数据</v-btn>
            </div>
          </template>
        </AdminDataTable>
      </v-card-text>
    </v-card>

    <!-- 新增/编辑对话框 -->
    <v-dialog v-model="dialog" max-width="800px" persistent>
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-row class="mt-1 mb-1">
            <v-icon class="mr-2">{{ isEdit ? 'mdi-pencil' : 'mdi-plus-circle' }}</v-icon>
            {{ isEdit ? '编辑广告公告' : '新增广告公告' }}
            <v-spacer></v-spacer>
            <v-btn icon="mdi-close" variant="text" color="white" @click="closeDialog"></v-btn>
          </v-row>
        </v-card-title>
        <v-card-text class="pt-4">
          <v-form ref="form" v-model="valid">
            <v-row>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="currentAd.title"
                  label="标题"
                  required
                  :rules="[(v) => !!v || '标题不能为空']"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-format-title"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="currentAd.type"
                  :items="adTypeOptions"
                  label="广告类型"
                  required
                  :rules="[(v) => v !== null || '请选择广告类型']"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-tag"
                ></v-select>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="currentAd.url"
                  label="链接地址"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-link"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-select
                  v-model="currentAd.status"
                  :items="statusOptions"
                  label="状态"
                  required
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-toggle-switch"
                ></v-select>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="currentAd.imageUrl"
                  label="图片地址"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-image"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="currentAd.videoUrl"
                  label="视频地址"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-video"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="startTimeStr"
                  label="开始时间"
                  type="datetime-local"
                  required
                  :rules="[(v) => !!v || '开始时间不能为空']"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-calendar-start"
                ></v-text-field>
              </v-col>
              <v-col cols="12" sm="6">
                <v-text-field
                  v-model="endTimeStr"
                  label="结束时间"
                  type="datetime-local"
                  required
                  :rules="[
                    (v) => !!v || '结束时间不能为空',
                    (v) =>
                      !startTimeStr ||
                      new Date(v) > new Date(startTimeStr) ||
                      '结束时间必须大于开始时间',
                  ]"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-calendar-end"
                ></v-text-field>
              </v-col>
              <v-col cols="12">
                <v-textarea
                  v-model="currentAd.content"
                  label="内容描述"
                  rows="4"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-text-box"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>
        <v-card-actions class="pa-4 bg-grey-lighten-5">
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="closeDialog" prepend-icon="mdi-close">
            取消
          </v-btn>
          <v-btn
            color="primary"
            :loading="saving"
            @click="saveAdvertisement"
            prepend-icon="mdi-content-save"
          >
            保存
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 预览对话框 -->
    <v-dialog v-model="previewDialog" max-width="700px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-eye</v-icon>
          广告预览
          <v-spacer></v-spacer>
          <v-btn
            icon="mdi-close"
            variant="text"
            color="white"
            @click="previewDialog = false"
          ></v-btn>
        </v-card-title>
        <v-card-text class="pa-4">
          <v-row v-if="previewItem">
            <v-col cols="12">
              <v-card variant="outlined" class="pa-4">
                <h3 class="text-h6 mb-2 d-flex align-center">
                  <v-icon class="mr-2" color="primary">mdi-bullhorn</v-icon>
                  {{ previewItem.title }}
                </h3>
                <v-chip
                  :color="getTypeColor(previewItem.type)"
                  text-color="white"
                  size="small"
                  class="mb-3"
                >
                  {{ getTypeText(previewItem.type) }}
                </v-chip>

                <div v-if="previewItem.imageUrl" class="mb-3">
                  <v-img
                    :src="previewItem.imageUrl"
                    max-height="300"
                    contain
                    class="bg-grey-lighten-3 rounded-lg"
                  ></v-img>
                </div>

                <div v-if="previewItem.videoUrl" class="mb-3">
                  <video controls width="100%" max-height="300" class="rounded-lg">
                    <source :src="previewItem.videoUrl" type="video/mp4" />
                    您的浏览器不支持视频播放
                  </video>
                </div>

                <div
                  v-if="previewItem.content"
                  class="mb-3 text-body-1 pa-2 bg-grey-lighten-5 rounded"
                >
                  {{ previewItem.content }}
                </div>

                <div v-if="previewItem.url" class="mb-3">
                  <v-btn
                    :href="previewItem.url"
                    target="_blank"
                    color="primary"
                    variant="text"
                    prepend-icon="mdi-open-in-new"
                  >
                    {{ previewItem.url }}
                  </v-btn>
                </div>

                <v-divider class="my-3"></v-divider>

                <div class="d-flex justify-space-between text-caption">
                  <v-chip size="x-small" color="primary" variant="outlined">
                    <v-icon start size="x-small">mdi-calendar-start</v-icon>
                    {{ formatTime(previewItem.startTime) }}
                  </v-chip>
                  <v-chip size="x-small" color="primary" variant="outlined">
                    <v-icon start size="x-small">mdi-calendar-end</v-icon>
                    {{ formatTime(previewItem.endTime) }}
                  </v-chip>
                </div>
              </v-card>
            </v-col>
          </v-row>
        </v-card-text>
      </v-card>
    </v-dialog>

    <AppSnackbar :state="toast" />
  </v-container>
</template>

<script>
import { ref } from 'vue'
import AdminDataTable from '@/components/admin/AdminDataTable.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useTable } from '@/composables/useTable'
import { useMessage } from '@/composables/useMessage'
import { formatDate, formatDateTimeInput } from '@/utils/format'
import http from '@/api/index'

/** 广告起止时间只精确到分钟，沿用原来的展示粒度 */
const AD_DATE_OPTIONS = {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  hour12: false,
}

export default {
  name: 'AdvertisementView',
  components: {
    AdminDataTable,
    AppSnackbar,
  },
  setup() {
    const { toast, showMessage } = useMessage()
    const adType = ref(null)
    const adStatus = ref(null)

    // 分页、加载态、筛选联动全部由 useTable 承担
    const table = useTable({
      url: '/admin/ads/list',
      size: 10,
      params: () => ({
        type: adType.value,
        status: adStatus.value,
      }),
      onError: () => showMessage('获取广告列表失败', 'error'),
    })

    return {
      toast,
      showMessage,
      adType,
      adStatus,
      table,
      // 模板里直接使用的格式化函数
      formatDateTimeInput,
    }
  },
  data() {
    return {
      // 保存状态
      saving: false,
      // 对话框显示状态
      dialog: false,
      // 预览对话框
      previewDialog: false,
      // 预览项
      previewItem: null,
      // 是否编辑模式
      isEdit: false,
      // 表单验证
      valid: false,
      // 当前编辑的广告
      currentAd: {
        id: null,
        title: '',
        url: '',
        imageUrl: '',
        content: '',
        videoUrl: '',
        status: 1,
        type: 0,
        startTime: null,
        endTime: null,
        position: 0,
      },
      // 开始时间字符串
      startTimeStr: '',
      // 结束时间字符串
      endTimeStr: '',
      // 表格头
      headers: [
        { title: 'ID', key: 'id', sortable: false, width: '80px' },
        { title: '标题', key: 'title', sortable: false, width: '200px' },
        { title: '内容', key: 'content', sortable: false, width: '300px' },
        { title: '类型', key: 'type', sortable: false, width: '120px' },
        { title: '访问量', key: 'viewCount', sortable: false, width: '120px' },
        { title: '状态', key: 'status', sortable: false, width: '100px' },
        { title: '操作', key: 'actions', sortable: false, width: '120px', align: 'center' },
      ],
      // 广告类型选项
      adTypeOptions: [
        { title: '全部类型', value: null },
        { title: '首页公告', value: 0 },
        { title: '首页弹窗广告', value: 1 },
        { title: '视频贴片广告', value: 2 },
        { title: '轮播新闻', value: 3 },
      ],
      // 状态选项
      statusOptions: [
        { title: '全部状态', value: null },
        { title: '启用', value: 1 },
        { title: '禁用', value: 0 },
      ],
    }
  },
  methods: {
    /** 统一的写操作：成功后自动刷新列表并提示 */
    async doAction(promise, successText, opts = {}) {
      const { isSuccess, errorText = '操作失败', onSuccess } = opts
      try {
        const json = await promise
        // 广告接口失败时也可能回 message:'成功'，这里统一以 status 为准
        if (isSuccess ? isSuccess(json) : !!json?.data) {
          if (typeof onSuccess === 'function') onSuccess(json)
          this.showMessage(successText, 'success')
          await this.table.refresh()
        } else {
          this.showMessage(json?.message || errorText, 'error')
        }
      } catch (error) {
        this.showMessage(error?.message || errorText, 'error')
      }
    },
    // 获取广告列表
    fetchAdvertisements() {
      return this.table.reload()
    },

    // 打开对话框
    openDialog(item) {
      this.resetForm()

      if (item) {
        // 编辑模式
        this.isEdit = true
        this.currentAd = { ...item }

        // 转换时间戳为日期字符串
        if (this.currentAd.startTime) {
          this.startTimeStr = formatDateTimeInput(this.currentAd.startTime)
        }

        if (this.currentAd.endTime) {
          this.endTimeStr = formatDateTimeInput(this.currentAd.endTime)
        }
      } else {
        // 新增模式
        this.isEdit = false

        // 设置默认值
        const now = new Date()
        const tomorrow = new Date()
        tomorrow.setDate(now.getDate() + 7)

        this.startTimeStr = formatDateTimeInput(now.getTime())
        this.endTimeStr = formatDateTimeInput(tomorrow.getTime())
        this.currentAd.startTime = now.getTime()
        this.currentAd.endTime = tomorrow.getTime()
      }

      this.dialog = true
    },

    // 关闭对话框
    closeDialog() {
      this.dialog = false
      this.resetForm()
    },

    // 重置表单
    resetForm() {
      this.currentAd = {
        id: null,
        title: '',
        url: '',
        imageUrl: '',
        content: '',
        videoUrl: '',
        status: 1,
        type: 0,
        startTime: null,
        endTime: null,
        position: 0,
      }
      this.startTimeStr = ''
      this.endTimeStr = ''
      this.isEdit = false

      if (this.$refs.form) {
        this.$refs.form.reset()
      }
    },

    // 保存广告
    async saveAdvertisement() {
      // 表单验证
      const { valid } = await this.$refs.form.validate()
      if (!valid) {
        return
      }

      // 转换时间
      if (this.startTimeStr) {
        this.currentAd.startTime = new Date(this.startTimeStr).getTime()
      }

      if (this.endTimeStr) {
        this.currentAd.endTime = new Date(this.endTimeStr).getTime()
      }

      this.saving = true

      // 根据是否编辑模式选择接口
      const url = this.isEdit ? '/admin/ads/update' : '/admin/ads/save'

      try {
        await this.doAction(http.post(url, this.currentAd), this.isEdit ? '更新成功' : '添加成功', {
          // 广告接口失败时也可能回 message:'成功'，这里统一以 status 为准
          isSuccess: (json) => json?.status === 200,
          errorText: this.isEdit ? '更新失败' : '添加失败',
          onSuccess: () => {
            this.dialog = false
          },
        })
      } finally {
        this.saving = false
      }
    },

    // 切换状态
    toggleStatus(item) {
      const newStatus = item.status === 1 ? 0 : 1
      const statusText = newStatus === 1 ? '启用' : '禁用'

      const updateData = {
        id: item.id,
        status: newStatus,
        type: item.type,
      }

      return this.doAction(
        http.post('/admin/ads/update', updateData),
        `${statusText}成功`,
        // 广告接口失败时也可能回 message:'成功'，这里统一以 status 为准
        { isSuccess: (json) => json?.status === 200, errorText: `${statusText}失败` }
      )
    },

    // 预览广告
    previewAd(item) {
      this.previewItem = { ...item }
      this.previewDialog = true
    },

    // 格式化时间
    formatTime(timestamp) {
      return formatDate(timestamp, AD_DATE_OPTIONS, '未设置')
    },

    // 获取类型文本
    getTypeText(type) {
      if (type == 0) return '首页公告'
      else if (type == 1) return '首页弹窗'
      else if (type == 2) return '视频贴片'
      else if (type == 3) return '轮播新闻'
      else return '未知类型'
    },
    // 获取类型颜色
    getTypeColor(type) {
      switch (type) {
        case 0:
          return 'primary'
        case 1:
          return 'warning'
        case 2:
          return 'error'
        case 3:
          return 'success'
        default:
          return 'grey'
      }
    },
  },
}
</script>

<style>
.search-options {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}

.v-btn--size-small {
  text-transform: none;
  letter-spacing: normal;
}
</style>