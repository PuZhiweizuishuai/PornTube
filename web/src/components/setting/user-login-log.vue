<template>
  <v-row justify="center" align="center">
    <v-col>
      <v-card
        class="mx-auto"
        outlined
      >
        <v-row justify="center" style="padding-top: 12px; padding-bottom: 12px">
          <v-col cols="10">
            <span><h2>登录历史</h2>如果发现不熟悉设备，请及时修改密码</span>
          </v-col>
        </v-row>
        <v-row justify="center">
          <div id="top" />
          <v-col cols="12">
            <v-data-table
              :headers="headers"
              :items="logList"
              hide-default-footer
              :items-per-page="size"
              :page.sync="page"
            >
              <template v-slot:item.loginTime="{ item }">
                {{ TimeUtil.renderTime(item.loginTime) }}
              </template>

              <template v-slot:no-data>
                <v-btn color="primary" @click="getLog">Reset</v-btn>
              </template>
            </v-data-table>
          </v-col>
        </v-row>
        <v-row justify="center" style="padding-top: 12px; padding-bottom: 12px">
          <v-pagination
            v-model="page"
            :length="length"
            @input="pageChange"
          />
        </v-row>
      </v-card>
    </v-col>
  </v-row>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
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
          text: '登录时间',
          sortable: false,
          value: 'loginTime'
        },
        { text: 'IP', sortable: false, value: 'ip' },
        { text: '登录设备', sortable: false, value: 'ua' }
      ]
    }
  },
  created() {
    this.userInfo = this.$store.state.userInfo
    this.getLog()
  },
  methods: {
    getLog() {
      fetch(`/api/login/log/list?page=${this.page}&limit=${this.size}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          this.logList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
        })
        .catch(e => {
          return null
        })
    },
    pageChange(value) {
      this.page = value
      this.videoList()
      document.querySelector('#top').scrollIntoView()
    }
  }
}
</script>

<style>

</style>
