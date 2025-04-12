<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card class="mx-auto" elevation="2" rounded="lg">
        <v-card-title class="d-flex align-center py-4">
          <v-icon icon="mdi-login-variant" class="mr-2" size="large" color="primary" />
          <h2>登录历史</h2>
          <v-chip color="warning" variant="outlined" class="ml-4">
            <v-icon start icon="mdi-alert-circle"></v-icon>
            如果发现不熟悉设备，请及时修改密码
          </v-chip>
        </v-card-title>

        <v-divider></v-divider>

        <v-row justify="center">
          <div id="top" />
          <v-col cols="12">
            <v-data-table
              :headers="headers"
              :items="logList"
              hide-default-footer
              :items-per-page="size"
              v-model:page="page"
            >
              <template v-slot:item.loginTime="{ item }">
                {{ TimeUtil.renderTime(item.loginTime) }}
              </template>

              <template v-slot:item.ua="{ item }">
                <v-chip color="blue">
                  {{ getUaInfo(item.ua) }}
                </v-chip>
              </template>

              <template v-slot:no-data>
                <v-btn color="primary" @click="getLog">Reset</v-btn>
              </template>
            </v-data-table>
          </v-col>
        </v-row>

        <v-card-actions class="pa-4 d-flex justify-center">
          <v-pagination
            v-model="page"
            :length="length"
            :total-visible="7"
            @update:model-value="pageChange"
            rounded="circle"
            density="comfortable"
            active-color="primary"
            class="my-4"
          />
        </v-card-actions>
      </v-card>
    </v-col>
  </v-row>
</template>
  
<script>
import { useUserStore } from '@/stores/userStore'
import TimeUtil from '@/utils/time-util.vue'
import { UAParser } from 'ua-parser-js'
export default {
  name: 'LoginLog',
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
        this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    pageChange(value) {
      this.page = value
      this.getLog()
      document.querySelector('#top').scrollIntoView({ behavior: 'smooth' })
    },
  },
}
</script>
  
<style>
.v-card-title {
  border-bottom: 1px solid rgba(0, 0, 0, 0.12);
}
</style>
  