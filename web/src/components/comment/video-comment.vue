<template>
  <div>
    <v-row>
      <v-col>
        {{ commentCount }} 条评论
      </v-col>
    </v-row>
    <v-row>
      <!-- <v-col cols="2" style="padding-top: 0px;">
        <router-link v-if="this.$store.state.userInfo" :to="`/user/${this.$store.state.userInfo.id}`">
          <v-avatar size="62">
            <v-img
              :src="this.$store.state.userInfo.avatarUrl"
              :alt="this.$store.state.userInfo.username"
              :title="this.$store.state.userInfo.username"
            />
          </v-avatar>
        </router-link>
        <v-avatar v-if="this.$store.state.userInfo == null" size="62">
          <v-img
            src="/images/head.png"
            alt="头像"
            title="请登录后评论"
          />
        </v-avatar>
      </v-col> -->
      <v-col style="padding-top: 0px;">
        <!-- <v-textarea
          placeholder="发表公开评论"
          label="评论"
          rows="2"
          :no-resize="true"
        /> -->
        <Vditor ref="commentVditor" :height="150" @vditor-input="getCommentText" />
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-btn color="primary" style=" float:right " @click="submitComment">评论</v-btn>
      </v-col>
    </v-row>
    <v-col />
    <v-row>
      <v-col cols="9" style="padding-bottom: 0px;">
        <v-tabs>
          <v-tab @click="setType(1)">时间倒序</v-tab>
          <v-tab>最多点赞</v-tab>
          <v-tab @click="setType(3)">最多评论</v-tab>
        </v-tabs>
      </v-col>
    </v-row>
    <!-- 分割线 -->
    <v-row>

      <v-divider />

    </v-row>
    <v-row v-for="item in commentsList" :key="item.id">
      <v-col cols="12">
        <!-- <Card :comment="item" :artice="artice" /> -->
        <CommentCard :author-id="authorId" :comment="item" />

      </v-col>
    </v-row>
    <v-row v-if="total == 0" justify="center">
      <h3> 暂无评论 </h3>
    </v-row>
    <!-- 页码 -->
    <v-row justify="center">
      <v-pagination
        v-if="length != 1"
        v-model="page"
        :length="length"
        @input="pageChange"
      />
    </v-row>
    <v-row>
      <v-col>
        <v-divider />
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
  </div>
</template>

<script>
import Vditor from '@/components/vditor/vditor.vue'
import CommentCard from '@/components/comment/comment-card.vue'
export default {
  name: 'VideoComment',
  components: {
    Vditor,
    CommentCard
  },
  props: {
    count: {
      type: Number,
      default: 0
    },
    article: {
      type: Number,
      default: 0
    },
    authorId: {
      type: Number,
      default: 0
    },
    typenum: {
      type: Number,
      default: 1
    }
  },
  data() {
    return {
      commentCount: this.count,
      commentData: {
        articleId: this.article,
        comment: '',
        type: this.typenum,
        parentCommentId: 0
      },
      showMessage: false,
      message: '',
      commentsList: [],
      page: 1,
      total: 0,
      length: 15,
      type: 0,
      sort: 1,
      size: 15
    }
  },
  created() {
    this.getCommentList()
  },
  methods: {
    getCommentList() {
      this.httpGet(`/comment/list?article=${this.article}&type=1&sort=${this.sort}&page=${this.page}&limit=${this.size}`, (json) => {
        this.commentsList = json.data.list
        this.page = json.data.currPage
        this.length = json.data.totalPage
        this.total = json.data.totalCount
      })
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
      if (this.commentData.comment === '' || this.commentData.comment == null || this.commentData.comment === '\n') {
        this.message = '评论内容不能为空！'
        this.showMessage = true
        return
      }
      this.httpPost('/comment/save', this.commentData, (json) => {
        if (json.status === 200) {
          //
          this.message = '评论成功'
          this.showMessage = true
          this.commentData.comment = ''
          this.$refs.commentVditor.setTextValue('')

          this.getCommentList()
        } else {
          //
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
      this.getCommentList()
    }
  }
}
</script>

<style>

</style>
