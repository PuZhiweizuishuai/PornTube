<template>
  <v-card rounded="lg">
    <v-card-title class="d-flex align-center flex-wrap ga-2">
      <v-icon start>mdi-history</v-icon>
      登录历史
      <v-spacer />
      <span class="text-caption text-medium-emphasis">
        如果发现不熟悉设备，请及时修改密码
      </span>
    </v-card-title>
    <v-divider />

    <v-data-table
      :headers="headers"
      :items="logList"
      mobile-breakpoint="sm"
      :hide-default-header="$vuetify.display.xs"
      class="elevation-0"
      hide-default-footer
      :items-per-page="size"
    >
      <template #[`item.loginTime`]="{ item }">
        {{ TimeUtil.renderTime(item.loginTime) }}
      </template>

      <template #[`item.ua`]="{ item }">
        <v-chip size="small" color="primary" variant="tonal">
          {{ getUaInfo(item.ua) }}
        </v-chip>
      </template>

      <template v-slot:no-data>
        <v-btn color="primary" @click="getLog">Reset</v-btn>
      </template>
    </v-data-table>

    <v-divider />

    <v-card-actions class="pa-4 d-flex justify-center">
      <Pagination
        rounded="circle"
        :total-visible="7"
        v-model="page"
        :length="length"
        color="primary"
        @update:model-value="pageChange"
      />
    </v-card-actions>
  </v-card>
</template>
  
<script>
import { useUserStore } from '@/stores/userStore'
import TimeUtil from '@/utils/time-util'
import { UAParser } from 'ua-parser-js'
import Pagination from '@/components/common/Pagination.vue'
export default {
  name: 'LoginLog',
  components: {
    Pagination,
  },
  data() {
    return {
      TimeUtil,
      userInfo: {},
      logList: [],
      size: 20,
      length: 1,
      page: 1,
      headers: [
        {
          title: '登录时间',
          align: 'start',
          sortable: false,
          key: 'loginTime',
        },
        { title: 'IP', align: 'start', sortable: false, key: 'ip' },
        { title: 'City', align: 'start', sortable: false, key: 'city' },
        { title: '登录设备', align: 'start', sortable: false, key: 'ua' },
      ],
    }
  },
  created() {
    this.userInfo = useUserStore().userData
    this.getLog()
  },
  methods: {
    getUaInfo(uastr) {
      const ua = new UAParser(uastr)
      return `操作系统：${ua.getOS().name} ${ua.getOS().version} 浏览器：${ua.getBrowser().name} ${
        ua.getBrowser().major
      }`
    },
    getLog() {
      this.httpGet(`/login/log/list?page=${this.page}&limit=${this.size}`, (json) => {
        this.logList = json.data.list
        //this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    pageChange(value) {
      this.page = value
      this.getLog()
      
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
  },
}
</script>
  