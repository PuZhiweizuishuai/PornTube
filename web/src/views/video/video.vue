<template>
  <div>
    <v-container fill-height fluid style="padding-bottom: 0px;">
      <v-row style="padding-top: 12px; padding-bottom: 12px">
        <v-col style="padding-bottom: 0px;">
          <DPlayer :article="parseInt(id)" :video="videoData.video[0]" :picurl="videoData.imgUrl" />
        </v-col>
      </v-row>
    </v-container>
    <v-container fill-height style="padding-top: 0px;">
      <v-row v-resize="onResize" no-gutters style="padding-top: 12px; padding-bottom: 12px">
        <v-col :cols="colsWidth">
          <v-row>
            <v-col>
              <h3 v-text="videoData.title" />
            </v-col>
          </v-row>
          <v-row>
            <v-col style="color: #999;font-size: 12px;padding-top: 0px;">
              <router-link v-if="videoData.childrenCategory.fatherId !== 0" :to="`/v/${videoData.fatherCategory.id}`" class="category-link">
                <span v-text="videoData.fatherCategory.name" />
              </router-link>
              /
              <router-link :to="`/v/${videoData.childrenCategory.id}`" class="category-link">
                <span v-text="videoData.childrenCategory.name" />
              </router-link>
              <span v-html="'&nbsp;&nbsp;&nbsp;&nbsp;'" />
              发布于：
              <span v-text="TimeUtil.renderTime(videoData.createTime)" />
              <br>
              <span v-text="videoData.viewCount" /> 次观看 <span v-html="'&nbsp;&nbsp;&nbsp;&nbsp;'" /> <span v-text="videoData.danmakuCount" /> 弹幕
            </v-col>
          </v-row>
          <v-divider />
          <v-row style="padding-top: 5px; padding-bottom: 5px">
            <v-col cols="2" align-self="end">
              <router-link :to="`/user/${videoData.userId}`">
                <v-avatar size="62">
                  <v-img :src="videoData.avatarUrl" />
                </v-avatar>
              </router-link>
            </v-col>
            <v-col>
              <router-link :to="`/user/${videoData.userId}`">
                <span v-text="videoData.username" />
              </router-link>
              <span v-html="'&nbsp;&nbsp;&nbsp;&nbsp;'" />
              <span style="color: #999;font-size: 12px;" v-text="videoData.introduction" /><span v-html="'&nbsp;&nbsp;&nbsp;&nbsp;'" />
              <v-btn small outlined color="primary">
                <span>关注</span><span v-html="'&nbsp;&nbsp;'" />
                <span v-text="videoData.followCount" />
              </v-btn><br>
              <br>
              <span v-text="videoData.describes" />
            </v-col>
          </v-row>
          <v-col />
          <v-divider />
          <v-row>
            <v-col>
              标签：
              <span v-for="item in videoData.tag" :key="item">
                <v-btn rounded small text color="primary" dark>{{ item }}</v-btn>
              </span>
            </v-col>
          </v-row>
        </v-col>
        <v-col cols="1" />
        <v-col cols="3">
          <v-rating v-model="score" color="orange" background-color="orange lighten-3" />
          <v-tooltip bottom>

            <template v-slot:activator="{ on, attrs }">
              <v-btn
                icon

                v-bind="attrs"
                v-on="on"
              >
                <v-icon>mdi-thumb-up</v-icon>
                <span v-text="videoData.likeCount" />
              </v-btn>
            </template>
            <span>顶一下</span>
          </v-tooltip>
          <span v-html="'&nbsp;&nbsp;&nbsp;&nbsp;'" />
          <v-tooltip bottom>
            <template v-slot:activator="{ on, attrs }">
              <v-btn
                icon

                v-bind="attrs"
                v-on="on"
              >
                <v-icon>mdi-thumb-down</v-icon>
                <span v-text="videoData.dislikeCount" />
              </v-btn>
            </template>
            <span>踩一下</span>
          </v-tooltip>

        </v-col>
      </v-row>
      <v-col />
      <v-row style="padding-top: 12px; padding-bottom: 12px">
        <v-col :cols="colsWidth">
          <Comment :article="id" :author-id="videoData.userId" :typenum="1" :count="videoData.commentCount" />
        </v-col>
        <v-col>
          相关视频：
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
import DPlayer from '@/components/player/player.vue'
import TimeUtil from '@/utils/time-util.vue'
import Comment from '@/components/comment/video-comment.vue'

export default {
  name: 'Video',
  components: {
    DPlayer,
    Comment
  },
  data() {
    return {
      score: 0,
      TimeUtil,
      id: 0,
      videoData: {},
      windowSize: {

      },
      colsWidth: 8
    }
  },
  created() {
    this.$vuetify.goTo(0)
    this.id = parseInt(this.$route.params.id)
    this.videoInfo()
    this.onResize()
  },
  methods: {
    // 控制页面大小
    onResize() {
      this.windowSize = { x: window.innerWidth, y: window.innerHeight }
      if (this.windowSize.x < 900) {
        this.colsWidth = 12
      } else {
        this.colsWidth = 8
      }
    },
    videoInfo() {
      fetch(`/api/article/video/${this.id}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200 && json.data.isShow) {
            this.videoData = json.data
            document.title = json.data.title
          } else {
            // TODO 显示 404
            this.$router.push('/')
          }
        })
        .catch(e => {
          return null
        })
    }
  }
}
</script>

<style>
.category-link {
  color: #999;
}
a {
  text-decoration: none;
}
</style>
