<template>
  <v-container>
    <div id="commentTop" ref="commentTop" />
    <v-row justify="center">
      <v-col cols="11">
        <h3>评论</h3>
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="11">
        <!-- <v-textarea
          v-model="comment.content"
          :placeholder="commentPlaceholder"
          label="评论"
          auto-grow
        /> -->
        <SecondCommentVditor
          :key="secondCommentKey"
          ref="secondCommentView"
          :idname="`second-comment-${father.id}`"
          :placeholder="commentPlaceholder"
          @vditor-input="getSecondCommentText"
        />
      </v-col>
    </v-row>
    <v-row justify="end">

      <v-col cols="3">
        <!-- <v-row justify="center">
          <img :src="verifyImageUrl" alt="验证码" title="点击刷新" style="cursor:pointer;" @click="getVerifyImage">
        </v-row> -->
      </v-col>

      <v-col cols="3" />
      <v-col cols="3">
        <v-row style="margin-bottom: 12px;" justify="end">
          <v-btn depressed color="success" @click="submit">
            评论
          </v-btn>
        </v-row>
      </v-col>
      <v-col cols="1" />
    </v-row>

    <v-row>
      <v-divider />
    </v-row>
    <v-row>
      <v-col cols="10" style="padding-bottom: 0px;">
        <v-tabs>
          <v-tab @click="setSort(1)">时间倒序</v-tab>
          <v-tab @click="setSort(0)">评论时间</v-tab>

          <v-tab>最多点赞</v-tab>
          <v-tab @click="setSort(3)">最多评论</v-tab>
        </v-tabs>
      </v-col>
    </v-row>
    <v-row>
      <v-divider />
    </v-row>
    <v-row v-for="item in secondList" :key="item.id" justify="center">
      <v-col cols="11">
        <Card :father="father" :comment="item" :type="type" @comment="getComment" />
      </v-col>
    </v-row>

    <v-row v-if="total == 0" justify="center">
      <h3>暂无评论，来抢个沙发吧！</h3>
    </v-row>
    <v-row justify="center">
      <v-pagination
        v-if="length != 1"
        v-model="page"
        :length="length"
        @input="pageChange"
      />
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
import Card from '@/components/comment/second-card.vue'
import SecondCommentVditor from '@/components/vditor/comment.vue'

export default {
  name: 'SecondComment',
  components: {
    Card,
    SecondCommentVditor
  },
  props: {
    father: {
      type: Object,
      default: null
    },
    type: {
      type: Number,
      default: 2
    }
  },
  data() {
    return {
      // verifyImageUrl: this.SERVER_API_URL + '/verifyImage',
      secondList: [],
      page: 1,
      size: 10,
      length: 1,
      sort: 1,
      total: 0,
      comment: {
        comment: '',
        // verifyCode: '',
        type: this.type,
        articleId: this.father.articleId,
        parentCommentId: this.father.id,
        parentUserId: this.father.userId
      },
      commentPlaceholder: '',
      message: '',
      showMessage: false,
      secondCommentKey: 0,
      uploadurl: this.SERVER_API_URL + '/uploads/file'
    }
  },
  created() {
    this.getSecondList()
    console.log(this.father)
  },
  methods: {
    getComment(value) {
      this.$refs.commentTop.scrollIntoView()
      this.commentPlaceholder = '回复 @' + value.username + ': ' + value.comment
      this.secondCommentKey++
      this.comment.parentCommentId = value.id
      // this.comment.fatherId = value.fatherId
    },
    getVerifyImage() {
      this.verifyImageUrl = this.SERVER_API_URL + '/verifyImage?t=' + new Date().getTime()
    },
    getSecondList() {
      this.httpGet(`/comment/list/?article=${this.father.articleId}&fatherId=${this.father.id}&page=${this.page}&limit=${this.size}&sort=${this.sort}&type=2`, (json) => {
        if (json.status === 200) {
          //
          this.secondList = json.data.list
          this.length = json.data.totalPage
          this.total = json.data.totalCount
        } else {
          //
        }
      })
    },
    submit() {
      //
      if (!this.$store.state.userInfo) {
        this.message = '请先登录后再评论！'
        this.showMessage = true
        return
      }
      if (this.comment.comment == null || this.comment.comment === '' || this.comment.comment === '\n') {
        this.message = '评论内容不能为空！'
        this.showMessage = true
        return
      }
      // if (this.comment.verifyCode == null || this.comment.verifyCode === '') {
      //   this.message = '验证码不能为空！'
      //   this.showMessage = true
      //   return
      // }

      this.httpPost('/comment/save', this.comment, (json) => {
        if (json.status === 200) {
          this.message = '评论成功！'
          this.showMessage = true
          this.comment.verifyCode = ''
          this.comment.comment = ''
          this.$refs.secondCommentView.setTextValue('')
          this.getSecondList()
        } else {
          //
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    pageChange(value) {
      this.page = value
      this.getSecondList()
    },
    getSecondCommentText(value) {
      this.comment.comment = value
    },
    setSort(sort) {
      this.sort = sort
      this.getSecondList()
    }
  }
}
</script>

<style >

</style>
