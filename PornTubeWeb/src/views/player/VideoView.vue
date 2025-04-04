<template>
  <div>
    <v-container fluid>
      <v-row style="padding-top: 12px; padding-bottom: 12px">
        <v-col style="padding-bottom: 0px">
          <PlayerVideo
            v-if="videoData != null"
            :article="parseInt(id)"
            :video="videoData.video[0]"
            :picurl="videoData.imgUrl"
          />
        </v-col>
      </v-row>
    </v-container>
    <!-- 视频详情 -->
    <v-container v-if="videoData != null" style="padding-top: 0px">
      <v-row>
        <v-col :cols="colsWidth">
          <!-- 视频标题 -->
          <v-card flat>
            <v-card-title class="text-h5 px-0">
              {{ videoData.title }}
            </v-card-title>

            <!-- 视频交互工具栏 -->
            <v-card-actions class="px-0 py-2">
              <v-btn
                prepend-icon="mdi-thumb-up"
                variant="tonal"
                color="primary"
                :disabled="isLiked"
              >
                {{ videoData.likeCount }}
              </v-btn>
              <v-btn
                prepend-icon="mdi-thumb-down"
                variant="tonal"
                color="error"
                :disabled="isDisliked"
              >
                {{ videoData.dislikeCount }}
              </v-btn>
              <v-btn prepend-icon="mdi-share" variant="tonal"> 分享 </v-btn>
              <v-btn prepend-icon="mdi-content-save" variant="tonal"> 收藏 </v-btn>
              <v-spacer></v-spacer>
              <v-chip variant="outlined" color="blue" class="mr-2">
                {{ videoData.viewCount }} 次观看
              </v-chip>
              <v-chip variant="outlined"> {{ videoData.danmakuCount }} 弹幕 </v-chip>
            </v-card-actions>

            <!-- 视频分类信息 -->
            <v-card-subtitle class="px-0 py-2">
              <router-link
                v-if="videoData.childrenCategory.fatherId !== 0"
                :to="`/v/${videoData.fatherCategory.id}`"
                class="category-link text-decoration-none"
              >
                {{ videoData.fatherCategory.name }}
              </router-link>
              /
              <router-link
                :to="`/v/${videoData.childrenCategory.id}`"
                class="category-link text-decoration-none"
              >
                {{ videoData.childrenCategory.name }}
              </router-link>
              &nbsp;&nbsp;&nbsp;&nbsp; 发布于：
              {{ TimeUtil.renderTime(videoData.createTime) }}
            </v-card-subtitle>

            <v-divider class="my-2"></v-divider>
          </v-card>

          <!-- 用户信息和视频描述集成 -->
          <v-card class="my-2" variant="elevated">
            <v-row no-gutters>
              <!-- 左侧用户信息 -->
              <v-col cols="12" sm="auto" class="pa-2">
                <div class="d-flex align-center">
                  <router-link :to="`/user/${videoData.userId}`">
                    <v-avatar size="48" class="mr-2">
                      <v-img :src="videoData.avatarUrl" alt="用户头像"></v-img>
                    </v-avatar>
                  </router-link>
                  <div>
                    <router-link :to="`/user/${videoData.userId}`" class="text-decoration-none">
                      <div class="text-subtitle-1 font-weight-bold">{{ videoData.username }}</div>
                    </router-link>
                    <div class="text-caption text-grey">个性签名</div>
                  </div>
                  <v-spacer></v-spacer>
                  <v-btn variant="text" density="comfortable" color="primary" class="ml-2"
                    >关注：{{ videoData.followCount }}</v-btn
                  >
                </div>
              </v-col>
            </v-row>

            <v-divider></v-divider>

            <!-- 视频描述 -->
            <v-card-text class="py-2">
              <div class="d-flex align-start">
                <div class="text-body-2">{{ videoData.describes }}</div>
              </div>
            </v-card-text>

            <!-- 标签 -->
            <v-card-text class="pt-0 pb-2">
              <v-chip-group>
                <v-chip
                  v-for="item in videoData.tag"
                  :key="item"
                  size="small"
                  variant="flat"
                  color="primary"
                  density="comfortable"
                  class="mr-1"
                >
                  {{ item }}
                </v-chip>
              </v-chip-group>
            </v-card-text>
          </v-card>

          <!-- 评论区 -->
        </v-col>

        <!-- 右侧推荐视频（在宽屏下显示） -->
        <v-col v-if="colsWidth === 8" cols="4">
          <v-card class="mb-4" variant="elevated">
            <v-card-title>推荐视频</v-card-title>
            <v-card-text>
              <p class="text-center text-body-2 text-grey">暂无推荐视频</p>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
import PlayerVideo from '@/components/player/PlayerVideo.vue'
export default {
  name: 'VideoView',
  components: {
    PlayerVideo,
  },
  data() {
    return {
      score: 0,
      TimeUtil,
      id: 0,
      videoData: null,
      windowSize: {},
      colsWidth: 8,
      isLiked: false,
      isDisliked: false,
    }
  },
  created() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
    this.id = parseInt(this.$route.params.id)
    this.videoInfo()
    this.onResize()
    window.addEventListener('resize', this.onResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.onResize)
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
      this.httpGet(`/article/video/${this.id}`, (json) => {
        if (json.status === 200 && json.data.isShow) {
          this.videoData = json.data
          document.title = json.data.title
          // 假设数据中包含点赞和点踩数量，如果没有则使用默认值
          this.likeCount = json.data.likeCount || 0
          this.dislikeCount = json.data.dislikeCount || 0
        } else {
          // TODO 显示 404
          this.$router.push('/')
        }
      })
    },
    // 点赞功能
    likeVideo() {
      // TODO 待实现
    },
    // 点踩功能
    dislikeVideo() {
      // TODO 待实现
    },
  },
}
</script>

<style>
.category-link {
  color: #1867c0;
}
</style>