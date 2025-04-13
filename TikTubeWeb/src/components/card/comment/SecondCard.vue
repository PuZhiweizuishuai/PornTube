<template>
  <v-card variant="flat" class="pa-3 my-2" color="grey-lighten-4">
    <v-row>
      <v-col cols="12" class="pb-0">
        <v-row no-gutters>
          <v-col cols="auto">
            <router-link :to="`/user/${comment.userId}`">
              <v-avatar size="32" class="mr-3">
                <v-img :src="comment.avatarUrl" />
              </v-avatar>
            </router-link>
          </v-col>
          <v-col>
            <div class="d-flex align-center">
              <router-link :to="`/user/${comment.userId}`" class="text-decoration-none">
                <span class="text-subtitle-2 font-weight-medium">{{ comment.username }}</span>
              </router-link>
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
      <v-col class="pl-12 py-2">
        <div v-if="comment.parentCommentId != father.id" class="text-body-2 text-primary mb-1">
          回复
          <router-link
            :to="`/user/${comment.parentUserId}`"
            class="text-decoration-none"
            target="_blank"
          >
            @{{ comment.username }}:
          </router-link>
        </div>
        <ShowMarkdown :markdown="comment.comment" :anchor="0" />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <div class="d-flex justify-end align-center">
          <v-tooltip text="评论" location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                variant="text"
                color="indigo"
                @click="setComment"
                icon="mdi-comment"
                size="small"
                class="mr-1"
              ></v-btn>
            </template>
          </v-tooltip>
          <span class="text-body-2 mr-4">{{ comment.commentCount }}</span>

          <v-tooltip text="赞" location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                size="small"
                :variant="isLiked ? 'flat' : 'text'"
                :color="isLiked ? 'success' : 'primary'"
                @click="like()"
                icon="mdi-thumb-up"
                class="mr-1"
              ></v-btn>
            </template>
          </v-tooltip>
          <span class="text-body-2" :class="{ 'text-success': isLiked }">{{ likeCount }}</span>
          <!-- <v-tooltip text="踩" location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                v-bind="props"
                size="small"
                :variant="isDisliked ? 'flat' : 'text'"
                :color="isDisliked ? 'error' : 'red'"
                @click="dislike()"
                icon="mdi-thumb-down"
                class="mr-1"
              ></v-btn>
            </template>
          </v-tooltip>
          <span class="text-body-2" :class="{ 'text-error': isDisliked }">{{ dislikeCount }}</span> -->
        </div>
      </v-col>
    </v-row>

    <v-divider class="mt-2"></v-divider>

    <v-snackbar v-model="showMessage" location="top" :timeout="3000">
      {{ message }}
      <template v-slot:actions>
        <v-btn color="pink" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-card>
</template>
  
<script>
import TimeUtil from '@/utils/time-util.vue'
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import { UAParser } from 'ua-parser-js'
import { useUserStore } from '@/stores/userStore'

export default {
  components: {
    ShowMarkdown,
  },
  props: {
    comment: {
      type: Object,
      default: null,
    },
    father: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      TimeUtil,
      content: '',
      showMessage: false,
      message: '',
      likeCount: this.comment?.likeCount || 0,
      isLiked: false,
      userInfo: useUserStore(),
      dislikeCount: this.comment?.dislikeCount || 0,
      isDisliked: false,
    }
  },
  created() {
    this.checkLike()
  },
  mounted() {},
  methods: {
    parserUa() {
      const ua = new UAParser(this.comment.ua)
      return `${ua.getOS().name} ${ua.getBrowser().name}`
    },
    setComment() {
      this.$emit('comment', this.comment)
    },
    like() {
      if (this.userInfo.userData == null) {
        this.showMessage = true
        this.message = '请先登录后再点赞'
        return
      }
      this.httpPost(`/like/toggle?likeObjId=${this.comment.id}&type=1`, {}, (json) => {
        if (json.status === 200) {
          this.isLiked = json.data.like
          this.likeCount += json.data.like ? 1 : -1
          this.message = json.data.info
          this.showMessage = true
        } else {
          this.message = json.data.info || '操作失败'
          this.showMessage = true
        }
      })
    },
    dislike() {
      if (this.userInfo.userData == null) {
        this.showMessage = true
        this.message = '请先登录后再点赞'
        return
      }
      this.httpPost(`/dislike/toggle?dislikeObjId=${this.comment.id}&type=1`, {}, (json) => {
        if (json.status === 200) {
          this.isDisliked = json.data.dislike
          this.dislikeCount += json.data.dislike ? 1 : -1
          this.message = json.data.info
          this.showMessage = true
        } else {
          this.message = json.data.info || '操作失败'
          this.showMessage = true
        }
      })
    },
    checkLike() {
      if (this.userInfo.userData == null) {
        this.isLiked = false
        return
      }
      this.httpGet(`/like/status?likeObjId=${this.comment.id}&type=1`, (json) => {
        if (json.status === 200) {
          this.isLiked = json.data
        }
      })
    },
  },
}
</script>
  
<style scoped>
</style>
  