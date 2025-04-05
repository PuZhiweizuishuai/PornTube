<template>
  <v-card variant="flat" class="pa-3 my-2" color="grey-lighten-5">
    <a :name="comment.id" />
    <v-row>
      <v-col cols="12" class="pb-0">
        <v-row no-gutters>
          <v-col cols="auto">
            <router-link :to="`/user/${comment.userId}`">
              <v-avatar size="45" class="mr-3">
                <v-img :src="comment.avatarUrl" />
              </v-avatar>
            </router-link>
          </v-col>
          <v-col>
            <div class="d-flex align-center">
              <router-link :to="`/user/${comment.userId}`" class="text-decoration-none">
                <span class="text-subtitle-1 font-weight-medium">{{ comment.username }}</span>
              </router-link>
              <v-chip
                v-if="comment.userId == authorId"
                class="ml-2"
                color="green"
                size="x-small"
                text-color="white"
              >
                楼主
              </v-chip>
              <v-chip class="ml-2" color="primary" size="x-small" text-color="white">
                {{ parserUa() }}
              </v-chip>
              <v-spacer></v-spacer>
              <span class="text-caption text-grey">{{
                TimeUtil.renderTime(comment.createTime)
              }}</span>
            </div>
          </v-col>
        </v-row>
      </v-col>
    </v-row>

    <v-row>
      <v-col class="pl-12 pt-2 pb-2">
        <ShowMarkdown :markdown="comment.comment" :anchor="0" />
      </v-col>
    </v-row>

    <!-- 操作区域 -->
    <v-row>
      <v-col>
        <div class="d-flex justify-end align-center">
          <v-tooltip v-if="showcomment" text="评论" location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                variant="text"
                color="blue"
                @click="openSecond()"
                icon="mdi-comment"
                size="small"
                class="mr-1"
              ></v-btn>
            </template>
          </v-tooltip>
          <span v-if="showcomment" class="text-body-2 mr-4">{{ comment.commentCount }}</span>

          <v-tooltip text="赞" location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                size="small"
                variant="text"
                color="green"
                @click="like()"
                icon="mdi-thumb-up"
                class="mr-1"
              ></v-btn>
            </template>
          </v-tooltip>
          <span class="text-body-2">{{ comment.likeCount }}</span>
        </div>
      </v-col>
    </v-row>

    <v-dialog v-model="showSecond" max-width="1000">
      <v-card>
        <v-card-text class="pa-0">
          <SecondComment :key="secondCommendKey" :father="comment" />
        </v-card-text>
      </v-card>
    </v-dialog>

    <v-snackbar v-model="showMessage" location="top" :timeout="3000">
      {{ message }}
      <template v-slot:actions>
        <v-btn color="pink" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-card>
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
  