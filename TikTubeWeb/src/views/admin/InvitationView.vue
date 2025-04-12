<!-- 邀请码 -->
<template>
  <v-container class="fill-height">
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="red">
        <v-toolbar-title class="text-h5 font-weight-medium">邀请码管理</v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn class="mr-2" prepend-icon="mdi-plus" color="white" variant="tonal" @click="create">
          生成邀请码
        </v-btn>
      </v-toolbar>

      <v-divider></v-divider>

      <v-card-text>
        <v-data-table-server
          :headers="headers"
          :itemsLength="invitationList.length"
          :items="invitationList"
          :items-per-page="size"
          v-model:page="page"
          hover
          @update:options="pageChange"
        >
          <template #[`item.createUser`]="{ item }">
            <v-btn
              :to="`/user/${item.createUser}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
            >
              {{ item.createUser }}
            </v-btn>
          </template>

          <template #[`item.createTime`]="{ item }">
            <v-chip size="small">
              {{ TimeUtil.renderTime(item.createTime) }}
            </v-chip>
          </template>

          <template #[`item.useTime`]="{ item }">
            <v-chip size="small" color="grey-lighten-3" v-if="item.useTime">
              {{ TimeUtil.renderTime(item.useTime) }}
            </v-chip>
            <span v-else>-</span>
          </template>

          <template #[`item.useStatus`]="{ item }">
            <v-chip
              size="small"
              :color="item.useStatus === 1 ? 'success' : 'error'"
              :text="getStatus(item.useStatus)"
            ></v-chip>
          </template>

          <template #[`item.useUser`]="{ item }">
            <v-btn
              v-if="item.useUser"
              :to="`/user/${item.useUser}`"
              variant="text"
              color="primary"
              density="comfortable"
              target="_blank"
            >
              {{ item.useUser }}
            </v-btn>
            <span v-else>-</span>
          </template>

          <template #[`item.code`]="{ item }">
            <v-chip color="primary-lighten-1" class="font-weight-medium monospace">
              {{ item.code }}
            </v-chip>
          </template>

          <template #no-data>
            <div class="d-flex justify-center pa-4">
              <v-btn color="primary" @click="getList">重新加载</v-btn>
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

      <v-divider></v-divider>
    </v-card>

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
  name: 'InvitationView',
  data() {
    return {
      showMessage: false,
      message: '',
      TimeUtil,
      invitationList: [],
      headers: [
        {
          title: 'ID',
          align: 'start',
          sortable: false,
          key: 'id',
        },
        { title: '创建时间', sortable: false, key: 'createTime', align: 'center' },
        { title: '生成人', sortable: false, key: 'createUser', align: 'center' },
        { title: '邀请码', sortable: false, key: 'code', align: 'center' },
        { title: '状态', sortable: false, key: 'useStatus', align: 'center' },
        { title: '使用人', sortable: false, key: 'useUser', align: 'center' },
        { title: '使用时间', sortable: false, key: 'useTime', align: 'center' },
      ],
      page: 1,
      size: 20,
      length: 1,
    }
  },
  created() {
    this.getList()
  },
  methods: {
    getStatus(code) {
      if (code === 1) {
        return '可用'
      } else {
        return '已使用'
      }
    },
    getList() {
      this.httpGet(`/admin/invitation/list?page=${this.page}&limit=${this.size}`, (json) => {
        this.invitationList = json.data.list
        this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    create() {
      this.httpPost('/admin/invitation/create', null, (json) => {
        if (json.status === 200) {
          this.message = '生成成功，请到列表查看'
          this.showMessage = true
          this.getList()
        } else {
          this.message = '生成失败，' + json.message
          this.showMessage = true
        }
      })
    },
    pageChange({ page, itemsPerPage, sortBy }) {
      this.loading = true
      this.page = page
      this.getList()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
  },
}
</script>
  
<style scoped>
.monospace {
  font-family: monospace;
}
</style>
  