<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="red">
        <v-toolbar-title class="text-h5 font-weight-medium">内容审核管理</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn class="mr-2" prepend-icon="mdi-refresh" color="white" variant="tonal" @click="init">
          刷新数据
        </v-btn>
      </v-toolbar>

      <v-divider></v-divider>

      <v-card-text>
        <v-data-table-server
          :headers="headers"
          :itemsLength="videoList.length"
          :items="videoList"
          :items-per-page="size"
          v-model:page="page"
          hover
          @update:options="pageChange"
        >
          <template #[`item.createTime`]="{ item }">
            <v-chip size="small">
              {{ TimeUtil.renderTime(item.createTime) }}
            </v-chip>
          </template>

          <template #[`item.type`]="{ item }">
            <v-chip size="small" :color="getTypeColor(item.type)" class="text-white">
              {{ getType(item.type) }}
            </v-chip>
          </template>

          <template #[`item.userId`]="{ item }">
            <v-btn
              :to="`/user/${item.userId}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
            >
              {{ item.userId }}
            </v-btn>
          </template>

          <template #[`item.imgUrl`]="{ item }">
            <v-img
              :src="item.imgUrl"
              max-height="160px"
              max-width="285px"
              class="rounded-lg elevation-1"
              cover
            />
          </template>

          <template #[`item.category`]="{ item }">
            <div class="d-flex flex-column">
              <v-btn
                v-if="item.childrenCategory && item.childrenCategory.fatherId !== 0"
                :to="`/v/${item.fatherCategory.id}`"
                variant="text"
                color="primary"
                size="small"
                class="mb-1"
                density="comfortable"
              >
                {{ item.fatherCategory.name }}
              </v-btn>
              <v-divider
                v-if="item.childrenCategory && item.childrenCategory.fatherId !== 0"
                class="mb-1"
              ></v-divider>
              <v-btn
                v-if="item.childrenCategory"
                :to="`/v/${item.childrenCategory.id}`"
                variant="text"
                color="primary"
                size="small"
                density="comfortable"
              >
                {{ item.childrenCategory.name }}
              </v-btn>
            </div>
          </template>

          <template #[`item.actions`]="{ item }">
            <div class="d-flex align-center">
              <v-btn
                :href="`/video/${item.id}`"
                target="_blank"
                icon
                color="primary"
                size="small"
                class="mr-2"
              >
                <v-icon>mdi-video</v-icon>
              </v-btn>

              <v-tooltip location="top">
                <template #activator="{ props }">
                  <v-btn icon v-bind="props" color="warning" size="small" @click="examine(item)">
                    <v-icon>mdi-pencil</v-icon>
                  </v-btn>
                </template>
                <span>审核内容</span>
              </v-tooltip>
            </div>
          </template>

          <template #no-data>
            <div class="d-flex justify-center pa-4">
              <v-btn color="primary" @click="init">加载数据</v-btn>
            </div>
          </template>

          <template v-slot:bottom>
            <div class="text-center pt-2">
              <v-pagination
                v-model="page"
                :length="length"
                :total-visible="7"
                rounded
              ></v-pagination>
            </div>
          </template>
        </v-data-table-server>
      </v-card-text>
    </v-card>

    <v-dialog v-model="dialog" persistent max-width="600px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">mdi-clipboard-check</v-icon>
          审核 - {{ nowExamineItem.title }}
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
                  @update:model-value="getexamineStatus"
                ></v-select>
              </v-col>
            </v-row>
            <v-row v-if="showError">
              <v-col>
                <v-select
                  v-model="errorType"
                  :items="examineItem"
                  label="不通过原因"
                  variant="outlined"
                  @update:model-value="getErrorString"
                ></v-select>
              </v-col>
            </v-row>
            <v-row v-if="showError">
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
          <v-btn color="grey" variant="text" @click="dialog = false">取消</v-btn>
          <v-btn color="primary" variant="elevated" @click="save">确认</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="showMessage" :timeout="3000" location="top" color="primary">
      {{ message }}
      <template #actions>
        <v-btn color="white" variant="text" @click="showMessage = false"> 关闭 </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
  
<script>
import TimeUtil from '@/utils/time-util.vue'

export default {
  name: 'ExamineView',
  data() {
    return {
      showMessage: false,
      message: '',
      errorType: '',
      selectedStatus: null,
      examineMessage: '',
      examineStatus: ['通过', '不通过'],
      showError: false,
      examineItem: [],
      dialog: false,
      nowExamineItem: {},
      TimeUtil,
      videoList: [],
      headers: [
        {
          title: '视频ID',
          align: 'start',
          sortable: false,
          key: 'id',
        },
        { title: '创建时间', sortable: false, key: 'createTime', align: 'center' },
        { title: '用户ID', sortable: false, key: 'userId', align: 'center' },
        { title: '标题', sortable: false, key: 'title', align: 'center' },
        { title: '封面', sortable: false, key: 'imgUrl', align: 'center' },
        { title: '分区', sortable: false, key: 'category', align: 'center' },
        { title: '类型', sortable: false, key: 'type', align: 'center' },
        { title: '标签', sortable: false, key: 'tag', align: 'center' },
        { title: '操作', key: 'actions', sortable: false, align: 'center' },
      ],
      page: 1,
      size: 15,
      length: 1,
    }
  },
  created() {
    this.init()
    this.setExamineItem()
  },
  methods: {
    save() {
      let result = this.selectedStatus === '通过'

      const data = {
        videoId: this.nowExamineItem.id,
        result,
        type: this.errorType,
        message: this.examineMessage,
      }

      this.httpPost(`/admin/examine`, data, (json) => {
        if (json.status === 200) {
          this.message = '审核结束'
          this.showMessage = true
          this.init()
          this.dialog = false
          this.resetForm()
        } else {
          this.message = '审核失败：' + json.message
          this.showMessage = true
        }
      })
    },
    resetForm() {
      this.selectedStatus = null
      this.errorType = ''
      this.examineMessage = ''
      this.showError = false
    },
    getErrorString(value) {
      this.errorType = value
    },
    getexamineStatus(value) {
      this.showError = value === '不通过'
    },
    examine(value) {
      this.dialog = true
      this.nowExamineItem = value
      this.resetForm()
    },
    init() {
      this.httpGet(`/admin/article/list?page=${this.page}&limit=${this.size}`, (json) => {
        this.videoList = json.data.list
        this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    setExamineItem() {
      this.httpGet('/examine/item', (json) => {
        this.examineItem = json.data
      })
    },
    pageChange({ page }) {
      this.loading = true
      this.page = page
      this.init()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    getType(code) {
      if (code === 0) {
        return '视频'
      } else if (code === 1) {
        return '图片'
      } else if (code === 2) {
        return '文章'
      } else {
        return '音乐'
      }
    },
    getTypeColor(code) {
      if (code === 0) {
        return 'red'
      } else if (code === 1) {
        return 'blue'
      } else if (code === 2) {
        return 'green'
      } else {
        return 'purple'
      }
    },
  },
}
</script>
  
<style scoped>
.v-data-table-server .v-img {
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transition: transform 0.2s ease;
}

.v-data-table-server .v-img:hover {
  transform: scale(1.02);
}
</style>
  