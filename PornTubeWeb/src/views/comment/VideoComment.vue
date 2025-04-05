<!-- 视频评论组件 -->
<template>
  <v-card>
    <v-container>
      <v-row>
        <v-col>
          <div class="d-flex align-center mb-4">
            <h2 class="text-h5 font-weight-medium">{{ commentCount }} 条评论</h2>
            <v-spacer></v-spacer>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-card variant="text" class="pa-3 mb-3">
            <Vditor ref="commentVditor" :height="150" @vditor-input="getCommentText" />
            <div class="d-flex justify-end mt-3">
              <v-btn color="primary" @click="submitComment">发表评论</v-btn>
            </div>
          </v-card>
        </v-col>
      </v-row>

      <v-divider class="mb-4"></v-divider>

      <v-row class="mb-3">
        <v-col>
          <v-tabs v-model="activeTab" color="primary" bg-color="transparent">
            <v-tab value="1" @click="setType(1)">时间倒序</v-tab>
            <v-tab value="2">最多点赞</v-tab>
            <v-tab value="3" @click="setType(3)">最多评论</v-tab>
          </v-tabs>
        </v-col>
      </v-row>

      <v-divider></v-divider>

      <v-row v-if="total == 0" justify="center" class="my-6">
        <v-icon
          icon="mdi-message-text-outline"
          size="large"
          color="grey-lighten-1"
          class="mr-2"
        ></v-icon>
        <h3 class="text-grey-lighten-1">暂无评论</h3>
      </v-row>

      <template v-else>
        <v-row v-for="item in commentsList" :key="item.id">
          <v-col>
            <CommentCard :author-id="authorId" :comment="item" />
          </v-col>
        </v-row>
      </template>

      <v-row justify="center" class="my-4">
        <v-pagination
          v-model="page"
          rounded="circle"
          :length="length"
          color="blue"
          :total-visible="7"
          @update:model-value="pageChange"
        />
      </v-row>

      <v-divider class="mt-2"></v-divider>
    </v-container>

    <v-snackbar v-model="showMessage" location="top" :timeout="3000">
      {{ message }}
      <template v-slot:actions>
        <v-btn color="pink" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>
  
<script>
import Vditor from '@/components/vditor/VditorComponents.vue'
import CommentCard from '@/components/card/comment/CommentCard.vue'

export default {
  name: 'VideoComment',
  components: {
    Vditor,
    CommentCard,
  },
  props: {
    count: {
      type: Number,
      default: 0,
    },
    article: {
      type: Number,
      default: 0,
    },
    authorId: {
      type: Number,
      default: 0,
    },
    typenum: {
      type: Number,
      default: 1,
    },
  },
  data() {
    return {
      commentCount: this.count,
      activeTab: '1',
      commentData: {
        articleId: this.article,
        comment: '',
        type: this.typenum,
        parentCommentId: 0,
      },
      showMessage: false,
      message: '',
      commentsList: [],
      page: 1,
      total: 0,
      length: 15,
      type: 0,
      sort: 1,
      size: 15,
    }
  },
  created() {
    this.getCommentList()
  },
  methods: {
    getCommentList() {
      this.httpGet(
        `/comment/list?article=${this.article}&type=1&sort=${this.sort}&page=${this.page}&limit=${this.size}`,
        (json) => {
          this.commentsList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.total = json.data.totalCount
        }
      )
    },
    getCommentText(value) {
      this.commentData.comment = value
    },
    submitComment() {
      if (!this.$store.state.userInfo) {
        this.message = '请先登录后再评论！'
        this.showMessage = true
        return
      }
      if (
        this.commentData.comment === '' ||
        this.commentData.comment == null ||
        this.commentData.comment === '\n'
      ) {
        this.message = '评论内容不能为空！'
        this.showMessage = true
        return
      }
      this.httpPost('/comment/save', this.commentData, (json) => {
        if (json.status === 200) {
          this.message = '评论成功'
          this.showMessage = true
          this.commentData.comment = ''
          this.$refs.commentVditor.setTextValue('')
          this.getCommentList()
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    pageChange(page) {
      this.page = page
      this.getCommentList()
    },
    setType(value) {
      this.sort = value
      this.activeTab = value.toString()
      this.getCommentList()
    },
  },
}
</script>
  
<style>
</style>
  