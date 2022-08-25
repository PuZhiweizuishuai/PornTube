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
        <Vditor ref="childvditor" :height="150" @vditor-input="getCommentText" />
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-btn color="primary" style=" float:right " @click="submitComment">评论</v-btn>
      </v-col>
    </v-row>
    <v-col />
    <v-divider />
    <v-row>
      <v-col cols="9" style="padding-bottom: 0px;">
        <v-tabs>
          <v-tab @click="setType(0)">评论时间</v-tab>
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
        liebiao
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
export default {
  name: 'VideoComment',
  components: {
    Vditor
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
      length: 15,
      type: 0
    }
  },
  methods: {
    getCommentText(value) {
      this.commentData.comment = value
    },
    submitComment() {
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
        } else {
          //
          this.message = json.message
          this.showMessage = true
        }
      })
    },
    pageChange(page) {

    },
    setType(value) {

    }
  }
}
</script>

<style>

</style>
