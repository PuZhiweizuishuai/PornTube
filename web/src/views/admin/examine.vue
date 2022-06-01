<template>
  <v-container fill-height fluid>
    <v-row justify="center" align="center">
      <v-col>
        <v-card
          class="mx-auto"
          outlined
        >
          <div id="top" />
          <v-row justify="center">
            <div id="top" />
            <v-col cols="12">
              <v-data-table
                :headers="headers"
                :items="videoList"
                hide-default-footer
                :items-per-page="size"
                :page.sync="page"
              >
                <template v-slot:item.createTime="{ item }">
                  {{ TimeUtil.renderTime(item.createTime) }}
                </template>
                <template v-slot:item.type="{ item }">
                  {{ getType(item.type) }}
                </template>
                <template v-slot:item.userId="{ item }">
                  <a :href="`/user/${item.userId}`" target="_blank"> {{ item.userId }} </a>
                </template>
                <template v-slot:item.imgUrl="{ item }">
                  <v-img :src="item.imgUrl" max-height="200px" aspect-ratio="1.77" />
                </template>
                <template v-slot:item.category="{ item }">
                  <router-link v-if="item.childrenCategory.fatherId !== 0" :to="`/v/${item.fatherCategory.id}`" class="category-link">
                    <span v-text="item.fatherCategory.name" />
                  </router-link>
                  /
                  <router-link :to="`/v/${item.childrenCategory.id}`" class="category-link">
                    <span v-text="item.childrenCategory.name" />
                  </router-link>
                </template>
                <template v-slot:item.actions="{ item }">
                  <a :href="`/video/${item.id}`" target="_blank">
                    <v-icon
                      class="mr-2"
                    >
                      mdi-video
                    </v-icon>
                  </a>
                  <v-tooltip left>
                    <template v-slot:activator="{ on, attrs }">
                      <v-btn
                        icon
                        class="mr-2"
                        v-bind="attrs"
                        v-on="on"
                        @click="examine(item)"
                      >
                        <v-icon>mdi-pencil</v-icon>
                      </v-btn>

                    </template>
                    <span>审核意见</span>
                  </v-tooltip>
                </template>

                <template v-slot:no-data>
                  <v-btn color="primary" @click="init">重新加载</v-btn>
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
    <v-dialog v-model="dialog" persistent max-width="600px">
      <v-card>
        <v-card-title>
          <span class="headline">审核 - {{ nowExamineItem.title }}</span>
        </v-card-title>
        <v-card-text>
          <v-container>
            <v-row>
              <v-col>
                <v-select
                  :items="examineStatus"
                  label="审核结果"
                  @change="getexamineStatus"
                />
              </v-col>
            </v-row>
            <v-row v-if="showError">
              <v-col>
                <v-select
                  :items="examineItem"
                  label="不通过原因"
                  @change="getErrorString"
                />
              </v-col>
            </v-row>
            <v-row v-if="showError">
              <v-col>
                <v-text-field
                  v-model="examineMessage"
                  label="处理意见"
                />
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>
        <v-card-actions>
          <v-spacer />
          <v-btn color="blue darken-1" text @click="dialog = false">取消</v-btn>
          <v-btn color="blue darken-1" text @click="save">确认</v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
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
  name: 'Examine',
  data() {
    return {
      showMessage: false,
      message: '',
      errorType: '',
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
          text: '视频ID',
          sortable: false,
          value: 'id'
        },
        { text: '创建时间', sortable: false, value: 'createTime' },
        { text: '用户ID', sortable: false, value: 'userId' },
        { text: '标题', sortable: false, value: 'title' },
        { text: '封面', sortable: false, value: 'imgUrl' },
        { text: '分区id', sortable: false, value: 'category' },
        { text: '类型', sortable: false, value: 'type' },
        { text: '标签', sortable: false, value: 'tag' },
        { text: '操作', value: 'actions', sortable: false }
      ],
      page: 1,
      size: 20,
      length: 1
    }
  },
  created() {
    this.init()
    this.setExamineItem()
  },
  methods: {
    save() {
      let result = true

      if (this.showError === false) {
        result = true
      } else {
        result = false
      }

      const data = {
        videoId: this.nowExamineItem.id,
        result,
        type: this.errorType,
        message: this.examineMessage
      }

      fetch(`/api/admin/examine`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(data)
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            //
            this.message = '审核结束'
            this.showMessage = true
            this.init()
            this.dialog = false
          } else {
            //
            this.message = '审核失败' + json.message
            this.showMessage = true
            this.init()
            this.dialog = false
          }
        })
        .catch(e => {
          return null
        })
    },
    getErrorString(value) {
      this.errorType = value
      console.log(value)
    },
    getexamineStatus(value) {
      if (value === '不通过') {
        this.showError = true
      } else {
        this.showError = false
      }
      console.log(value)
    },
    examine(value) {
      this.dialog = true
      this.nowExamineItem = value
    },
    init() {
      fetch(`/api/admin/article/list?page=${this.page}&limit=${this.size}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
        })
        .catch(e => {
          return null
        })
    },
    setExamineItem() {
      fetch(`/api/examine/item`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          this.examineItem = json.data
        })
        .catch(e => {
          return null
        })
    },
    pageChange(value) {
      this.page = value
      this.videoList()
      document.querySelector('#top').scrollIntoView()
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
    }
  }
}
</script>

<style>

</style>
