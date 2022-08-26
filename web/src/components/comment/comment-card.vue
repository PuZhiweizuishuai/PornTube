<template>
  <v-container>
    <a :name="comment.id" />
    <v-row justify="center">
      <v-col cols="12" style="padding-bottom: 0px;">
        <router-link :to="`/user/${comment.userId}`">
          <v-avatar size="45" style="float: left;">
            <v-img :src="comment.avatarUrl" />
          </v-avatar>
        </router-link>
        <p style="margin-left: 60px;">
          <router-link :to="`/user/${comment.userId}`">
            {{ comment.username }}
          </router-link>
          <v-chip
            v-if="comment.userId == authorId"
            class="ma-2"
            color="green"
            small
            text-color="white"
          >
            楼主
          </v-chip>
          <v-chip
            class="ma-2"
            color="primary"
            small
            text-color="white"
          >
            {{ parserUa() }}
          </v-chip>
        </p>
      </v-col>
    </v-row>
    <v-row justify="end">
      <v-col cols="12" style="padding-top: 0px;padding-left: 55px;">
        <ShowMarkdown :markdown="comment.comment" :anchor="0" />
      </v-col>
    </v-row>
    <!-- 操作 -->
    <v-row justify="end">
      {{ TimeUtil.renderTime(comment.createTime) }}
      <span v-html="`&nbsp;&nbsp;`" />
      <span v-html="`&nbsp;&nbsp;`" />
      <v-tooltip v-if="showcomment" bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            color="indigo"
            icon
            v-bind="attrs"
            small
            v-on="on"
            @click="openSecond"
          >
            <v-icon>mdi-comment</v-icon>
          </v-btn>
        </template>
        <span>评论</span>
      </v-tooltip>
      <span v-if="showcomment">{{ comment.commentCount }} </span>

      <span v-html="`&nbsp;&nbsp;`" />
      <span v-html="`&nbsp;&nbsp;`" />
      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            color="primary"
            icon
            v-bind="attrs"
            small
            v-on="on"
            @click="like()"
          >
            <v-icon>mdi-thumb-up</v-icon>
          </v-btn>
        </template>
        <span>赞</span>
      </v-tooltip>
      {{ comment.likeCount }}
      <span v-html="`&nbsp;&nbsp;`" />

      <!-- <span v-html="`&nbsp;&nbsp;`" />
      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            v-bind="attrs"
            small
            v-on="on"
          >
            <v-icon>mdi-thumb-down</v-icon>
          </v-btn>
        </template>
        <span>踩</span>
      </v-tooltip>
      {{ comment.badCount }} -->
      <span v-html="`&nbsp;&nbsp;`" />

    </v-row>
    <v-row>
      <v-divider />
    </v-row>
    <!-- <v-dialog
      v-model="showSecond"
      max-width="1000"
    >
      <v-card>
        <SecondComment :key="secondCommendKey" :father="comment" :type="type" />
      </v-card>
    </v-dialog> -->
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
import ShowMarkdown from '@/components/vditor/show-markdown.vue'
import TimeUtil from '@/utils/time-util.vue'
var parser = require('ua-parser-js')
export default {
  name: 'CommentCard',
  components: {
    ShowMarkdown
  },
  props: {
    comment: {
      type: Object,
      default: null
    },
    authorId: {
      type: Number,
      default: -1
    },
    showcomment: {
      type: Boolean,
      default: true
    },
    ratings: {
      type: Boolean,
      default: false
    },
    type: {
      type: Number,
      default: 1
    }
  },
  data() {
    return {
      TimeUtil,
      showSecond: false,
      secondCommendKey: 0,
      showMessage: false,
      message: ''
    }
  },
  created() {

  },
  methods: {
    parserUa() {
      const ua = parser(this.comment.ua)
      return `${ua.os.name} ${ua.browser.name}`
    },
    openSecond() {
      this.secondCommendKey += 1
      this.showSecond = true
    },
    like() {
      // const likeInfo = {
      //   targetId: this.comment.id,
      //   targetType: 1,
      //   type: 0
      // }
      // this.httpPost('/click/like', likeInfo, (json) => {
      //   if (json.status === 200) {
      //     this.message = '点赞成功'
      //     this.comment.likeCount = this.comment.likeCount + 1
      //     this.showMessage = true
      //   } else {
      //     this.message = json.message
      //     this.comment.likeCount = this.comment.likeCount - 1
      //     this.showMessage = true
      //   }
      // })
    }
  }
}
</script>

<style>

</style>
