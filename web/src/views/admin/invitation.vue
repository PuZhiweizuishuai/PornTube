<template>
  <v-container fill-height>
    <v-row justify="center" align="center">
      <v-col>
        <v-card
          class="mx-auto"
          outlined
        >
          <div id="top" />
          <v-row justify="center">
            <v-col cols="10" style="margin-top: 12px;margin-bottom: 12px;">
              <v-btn block color="secondary" @click="create">生成邀请码</v-btn>
            </v-col>
          </v-row>
          <v-divider />
          <v-row justify="center">
            <div id="top" />
            <v-col cols="12">
              <v-data-table
                :headers="headers"
                :items="invitationList"
                hide-default-footer
                :items-per-page="size"
                :page.sync="page"
              >
                <template v-slot:item.createUser="{ item }">
                  <a :href="`/user/${item.createUser}`" target="_blank"> {{ item.createUser }} </a>
                </template>
                <template v-slot:item.createTime="{ item }">
                  {{ TimeUtil.renderTime(item.createTime) }}
                </template>
                <template v-slot:item.useTime="{ item }">
                  {{ TimeUtil.renderTime(item.useTime) }}
                </template>
                <template v-slot:item.useStatus="{ item }">
                  {{ getStatus(item.useStatus) }}
                </template>
                <template v-slot:item.useUser="{ item }">
                  <a :href="`/user/${item.useUser}`" target="_blank"> {{ item.useUser }} </a>
                </template>
                <template v-slot:no-data>
                  <v-btn color="primary" @click="getList">重新加载</v-btn>
                </template>
              </v-data-table>
            </v-col>
          </v-row>

          <v-row justify="center" style="padding-top: 12px; padding-bottom: 24px">
            <v-pagination
              v-model="page"
              :length="length"
              @input="pageChange"
            />
          </v-row>

        </v-card>
      </v-col>
    </v-row>
    <v-snackbar
      v-model="showMessage"
      :top="true"
      :timeout="3000"
    >
      {{ message }}

      <template v-slot:action="{ attrs }">
        <v-btn
          color="pink"
          text
          v-bind="attrs"
          @click="showMessage = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
export default {
  name: 'Invitation',
  data() {
    return {
      showMessage: false,
      message: '',
      TimeUtil,
      invitationList: [],
      headers: [
        {
          text: 'id',
          sortable: false,
          value: 'id'
        },
        { text: '创建时间', sortable: false, value: 'createTime' },
        { text: '生成人', sortable: false, value: 'createUser' },
        { text: '邀请码', sortable: false, value: 'code' },
        { text: '是否使用', sortable: false, value: 'useStatus' },
        { text: '使用人', sortable: false, value: 'useUser' },
        { text: '使用时间', sortable: false, value: 'useTime' }
      ],
      page: 1,
      size: 20,
      length: 1
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
        return '不可用'
      }
    },
    getList() {
      fetch(`/api/admin/invitation/list?page=${this.page}&limit=${this.size}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          this.invitationList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
        })
        .catch(e => {
          return null
        })
    },
    create() {
      fetch(`/api/admin/invitation/create`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            //
            this.message = '生成成功，请到列表查看'
            this.showMessage = true
            this.getList()
          } else {
            //
            this.message = '生成失败，' + json.message
            this.showMessage = true
          }
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
