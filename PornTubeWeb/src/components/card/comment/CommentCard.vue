<template>
  <v-container>
    <a :name="comment.id" />
    <v-row justify="center">
      <v-col cols="12" style="padding-bottom: 0px">
        <router-link :to="`/user/${comment.userId}`">
          <v-avatar size="45" style="float: left">
            <v-img :src="comment.avatarUrl" />
          </v-avatar>
        </router-link>
        <p style="margin-left: 60px">
          <router-link :to="`/user/${comment.userId}`">
            {{ comment.username }}
          </router-link>
          <v-chip
            v-if="comment.userId == authorId"
            class="ma-2"
            color="green"
            size="small"
            text-color="white"
          >
            楼主
          </v-chip>
          <v-chip class="ma-2" color="primary" size="small" text-color="white">
            {{ parserUa() }}
          </v-chip>
        </p>
      </v-col>
    </v-row>
    <v-row justify="end">
      <v-col cols="12" style="padding-top: 0px; padding-left: 55px">
        <ShowMarkdown :markdown="comment.comment" :anchor="0" />
      </v-col>
    </v-row>
    <!-- 操作 -->
    <v-row justify="end">
      {{ TimeUtil.renderTime(comment.createTime) }}
      <span v-html="`&nbsp;&nbsp;`" />
      <span v-html="`&nbsp;&nbsp;`" />

      <v-tooltip v-if="showcomment" text="评论" location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            variant="flat"
            color="blue"
            @click="openSecond()"
            icon="mdi-comment"
            size="x-small"
          >
          </v-btn>
        </template>
      </v-tooltip>
      <span v-if="showcomment">{{ comment.commentCount }} </span>

      <span v-html="`&nbsp;&nbsp;`" />
      <span v-html="`&nbsp;&nbsp;`" />

      <v-tooltip text="赞" location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            size="x-small"
            variant="flat"
            color="green"
            @click="like()"
            icon="mdi-thumb-up"
          ></v-btn>
        </template>
      </v-tooltip>
      <span>{{ comment.likeCount }} </span>

      <span v-html="`&nbsp;&nbsp;`" />
    </v-row>

    <v-col></v-col>
    <v-row>
      <v-divider />
    </v-row>

    <v-dialog v-model="showSecond" max-width="1000">
      <v-card>
        <SecondComment :key="secondCommendKey" :father="comment" />
      </v-card>
    </v-dialog>
    <v-snackbar v-model="showMessage" location="top" :timeout="3000">
      {{ message }}

      <template v-slot:actions>
        <v-btn color="pink" variant="text" @click="showMessage = false"> 关闭 </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
  
<script>
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import TimeUtil from '@/utils/time-util.vue'
import SecondComment from '@/components/card/comment/SecondComment.vue'
import { UAParser } from 'ua-parser-js'
export default {
  name: 'CommentCard',
  components: {
    ShowMarkdown,
    SecondComment,
  },
  props: {
    comment: {
      type: Object,
      default: null,
    },
    authorId: {
      type: Number,
      default: -1,
    },
    showcomment: {
      type: Boolean,
      default: true,
    },
    ratings: {
      type: Boolean,
      default: false,
    },
    type: {
      type: Number,
      default: 1,
    },
  },
  data() {
    return {
      TimeUtil,
      showSecond: false,
      secondCommendKey: 0,
      showMessage: false,
      message: '',
    }
  },
  created() {},
  methods: {
    parserUa() {
      const ua = new UAParser(this.comment.ua)
      return `${ua.getOS().name} ${ua.getBrowser().name}`
    },
    openSecond() {
      this.secondCommendKey = Math.random() * 10000
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
    },
  },
}
</script>
  
<style>
</style>
  