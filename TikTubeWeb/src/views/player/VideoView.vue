<template>
  <div>
    <v-container fluid>
      <!-- 添加 Snackbar 组件 -->
      <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="2000" location="top">
        {{ snackbar.text }}
      </v-snackbar>

      <v-row style="padding-top: 12px; padding-bottom: 12px">
        <v-col style="padding-bottom: 0px">
          <PlayerVideo
            v-if="playVideoData != null && adsInfoStatus"
            :article="parseInt(id)"
            :video="playVideoData"
            :picurl="videoData.imgUrl"
            :open-ads="openAds"
            :ads-info="adsInfo"
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
              <div class="d-flex flex-wrap align-center w-100">
                <div class="d-flex flex-wrap">
                  <v-btn
                    prepend-icon="mdi-thumb-up"
                    :variant="isLiked ? 'flat' : 'tonal'"
                    color="primary"
                    @click="likeVideo"
                    class="ma-1"
                  >
                    {{ videoData.likeCount }}
                  </v-btn>
                  <v-btn
                    prepend-icon="mdi-thumb-down"
                    :variant="isDisliked ? 'flat' : 'tonal'"
                    color="error"
                    @click="dislikeVideo"
                    class="ma-1"
                  >
                    {{ videoData.dislikeCount }}
                  </v-btn>
                  <v-btn prepend-icon="mdi-share" variant="tonal" @click="shareDialog = true" class="ma-1">
                    分享
                  </v-btn>
                  <v-btn
                    prepend-icon="mdi-content-save"
                    color="orange-lighten-2"
                    :variant="isFavorited ? 'flat' : 'tonal'"
                    @click="favoritesClick"
                    class="ma-1"
                  >
                    <span v-if="isFavorited">取消收藏</span>
                    <span v-else>收藏</span>
                  </v-btn>
                </div>
                
                <v-spacer class="d-none d-sm-block"></v-spacer>
                
                <div class="d-flex flex-wrap mt-2 mt-sm-0">
                  <v-chip variant="outlined" color="orange-lighten-2" class="ma-1">
                    {{ videoData.favoriteCount }} 次收藏
                  </v-chip>
                  <v-chip variant="outlined" color="blue" class="ma-1">
                    {{ videoData.viewCount }} 次观看
                  </v-chip>
                  <v-chip variant="outlined" class="ma-1">
                    {{ videoData.danmakuCount }} 弹幕
                  </v-chip>
                </div>
              </div>
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

              <v-btn class="ml-2" color="red" size="small" variant="text" @click="showReportDialog=true">
                举报
              </v-btn>
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
                    <div class="text-caption text-grey">个性签名: {{ videoData.introduction }}</div>
                  </div>
                  <v-spacer></v-spacer>
                  <v-btn
                    v-if="videoData.userId !== userInfo.userData?.id"
                    :color="isFollowed ? 'grey' : 'primary'"
                    :variant="isFollowed ? 'flat' : 'outlined'"
                    density="comfortable"
                    class="ml-2"
                    :prepend-icon="isFollowed ? 'mdi-account-check' : 'mdi-account-plus'"
                    @click="toggleFollow"
                  >
                    {{ videoData.fansCount }} {{ isFollowed ? '已关注' : '关注' }}
                  </v-btn>
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

          <Comment
            :article="id"
            :author-id="videoData.userId"
            :typenum="1"
            :count="videoData.commentCount"
          />

          <!-- 评论区 -->
        </v-col>

        <!-- 右侧推荐视频（在宽屏下显示） -->
        <v-col v-if="colsWidth === 8" cols="4">
          <v-card class="mb-4">
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-playlist-play</v-icon>
              推荐视频
            </v-card-title>
            <v-card-text>
              <p
                class="text-center text-body-2 text-grey mb-3"
                v-if="videoData.similar && videoData.similar.length === 0"
              >
                暂无推荐视频
              </p>
              <p
                class="text-center text-body-2 text-grey mb-3"
                v-else-if="videoData.similar && allItemsHaveSort"
              >
                暂未发现可以推荐的视频，为你推荐热门内容
              </p>
              <v-row>
                <v-col cols="12" v-for="item in videoData.similar" :key="item.id" class="py-2">
                  <div class="position-relative">
                    <VideoCared :video="item" />
                    <v-chip
                      v-if="item.sort !== null && item.sort !== undefined"
                      color="red"
                      size="small"
                      class="position-absolute top-0 start-0 ma-2 pa-1"
                      variant="elevated"
                    >
                      <v-icon size="x-small" start>mdi-fire</v-icon>
                      热门
                    </v-chip>
                  </div>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- 小屏幕下的推荐视频（在评论区后面显示） -->
      <v-row v-if="colsWidth === 12 && videoData != null">
        <v-col cols="12">
          <v-card class="mb-4">
            <v-card-title class="d-flex align-center">
              <v-icon class="mr-2">mdi-playlist-play</v-icon>
              推荐视频
            </v-card-title>
            <v-card-text>
              <p
                class="text-center text-body-2 text-grey mb-3"
                v-if="videoData.similar && videoData.similar.length === 0"
              >
                暂无推荐视频
              </p>
              <p
                class="text-center text-body-2 text-grey mb-3"
                v-else-if="videoData.similar && allItemsHaveSort"
              >
                暂未发现可以推荐的视频，为你推荐热门内容
              </p>
              <v-row>
                <v-col
                  cols="12"
                  sm="6"
                  md="4"
                  v-for="item in videoData.similar"
                  :key="item.id"
                  class="py-2"
                >
                  <div class="position-relative">
                    <VideoCared :video="item" />
                    <v-chip
                      v-if="item.sort !== null && item.sort !== undefined"
                      color="red"
                      size="small"
                      class="position-absolute top-0 start-0 ma-2 pa-1"
                      variant="elevated"
                    >
                      <v-icon size="x-small" start>mdi-fire</v-icon>
                      热门
                    </v-chip>
                  </div>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-dialog v-model="showReportDialog" width="50vh">
        <OpinionCard 
          :targetId="id" 
          :typeNum="0"
          :target-title="videoData.title"  
          :isReport="true"
          @close="showReportDialog = false"
          @success="handleReportSuccess"
        />
      </v-dialog>

      <!-- 分享卡片弹窗 -->
      <v-dialog max-width="600" v-model="shareDialog">
        <ShareCard :article="{ id: id, title: videoData.title }" />
      </v-dialog>
    </v-container>
  </div>
</template>

<script>
import TimeUtil from '@/utils/time-util'
import PlayerVideo from '@/components/player/PlayerVideo.vue'
import Comment from '@/views/comment/VideoComment.vue'
import { useUserStore } from '@/stores/userStore'
import ShareCard from '@/components/card/ShareCard.vue'
import VideoCared from '@/components/card/VideoCard.vue'
import Power from '@/utils/check-power'
import OpinionCard from '@/components/card/OpinionCard.vue'
export default {
  name: 'VideoView',
  components: {
    PlayerVideo,
    Comment,
    ShareCard,
    VideoCared,
    OpinionCard
  },
  data() {
    return {
      score: 0,
      TimeUtil,
      id: 0,
      videoData: null,
      playVideoData: null,
      windowSize: {},
      colsWidth: 8,
      isLiked: false,
      isDisliked: false,
      isFavorited: false,
      isFollowed: false,
      userInfo: useUserStore(),
      adsInfo: null,
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
      adsInfoStatus: false,
      shareDialog: false,
      openAds: false,
      showReportDialog: false,
    }
  },
  computed: {
    allItemsHaveSort() {
      if (!this.videoData || !this.videoData.similar || this.videoData.similar.length === 0) {
        return false
      }
      return this.videoData.similar.every((item) => item.sort !== null && item.sort !== undefined)
    },
  },
  created() {
    window.scrollTo({
      top: 0,
      behavior: 'smooth',
    })
    this.id = parseInt(this.$route.params.id)
    this.getAllVideoInfo()
    this.onResize()
    window.addEventListener('resize', this.onResize)
  },
  beforeUnmount() {
    window.removeEventListener('resize', this.onResize)
  },
  methods: {
    handleReportSuccess() {
      // 处理举报成功的逻辑
      //this.showReportDialog = false
    },
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
          this.playVideoData = this.videoData.video.find((item) => item.type === 0)
          document.title = json.data.title
          // 假设数据中包含点赞和点踩数量，如果没有则使用默认值
          this.likeCount = json.data.likeCount || 0
          this.dislikeCount = json.data.dislikeCount || 0
          this.checkLike()
          this.checkDislike()
          this.checkFavorites()
          this.checkFollow()
        } else {
          // TODO 显示 404
          this.$router.push('/')
        }
      })
    },
    getAdsInfo() {
      this.httpGet('/web/notice?type=2', (json) => {
        if (json.data != null && json.data.length > 0) {
          this.adsInfo = json.data[this.getRandomInt(0, json.data.length - 1)]
          this.openAds = true
        } else {
          this.openAds = false
        }
        this.adsInfoStatus = true
      })
    },
    getAllVideoInfo() {
      // 未登录
      if (this.userInfo.userData == null) {
        this.getAdsInfo()
        this.videoInfo()
      } else {
        if (Power.checkPower(this.userInfo.userData) === 'user') {
          this.getAdsInfo()
          this.videoInfo()
        } else {
          this.openAds = false
          this.adsInfoStatus = true
          this.videoInfo()
        }
      }
    },
    getRandomInt(min, max) {
      if (min == max) {
        return min
      }
      min = Math.ceil(min)
      max = Math.floor(max)
      return Math.floor(Math.random() * (max - min + 1)) + min
    },
    // 点赞功能
    likeVideo() {
      // 如果用户未登录
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再点赞', 'warning')
        return
      }
      this.httpPost(`/like/toggle?likeObjId=${this.id}&type=0`, {}, (json) => {
        if (json.status === 200) {
          // 更新点赞状态
          if (json.data.like) {
            this.isLiked = !this.isLiked
            // 更新点赞数量
            this.videoData.likeCount += this.isLiked ? 1 : -1
            // 显示消息提示
            if (this.isLiked) {
              this.showMessage('点赞成功', 'success')
            } else {
              this.showMessage('取消点赞成功', 'info')
            }
          }
        } else {
          this.showMessage(json.data.info || '操作失败', 'error')
        }
      })
    },
    favoritesClick() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再收藏', 'warning')
        return
      }
      this.httpPost('/favorites/toggle', { articeId: this.id }, (json) => {
        if (json.data.id != null) {
          this.showMessage('收藏成功！', 'success')
          this.isFavorited = true
          this.videoData.favoriteCount += 1
        } else {
          this.showMessage('取消收藏成功！', 'info')
          this.isFavorited = false
          this.videoData.favoriteCount -= 1
        }
      })
    },
    checkFavorites() {
      if (this.userInfo.userData == null) {
        this.isFavorited = false
        return
      }
      this.httpGet(`/favorites/check?articleId=${this.id}`, (json) => {
        if (json.status === 200) {
          this.isFavorited = json.data
        }
      })
    },
    // 关注功能
    toggleFollow() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再关注', 'warning')
        return
      }

      if (this.isFollowed) {
        this.unfollowUser()
      } else {
        this.followUser()
      }
    },

    followUser() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再关注', 'warning')
        return
      }

      const data = {
        followUser: this.videoData.userId,
        createUser: this.userInfo.userData.id,
      }

      this.httpPost('/follow/add', data, (json) => {
        if (json.data) {
          this.isFollowed = true
          if (this.videoData.fansCount !== undefined) {
            this.videoData.fansCount += 1
          }
          this.showMessage('关注成功', 'success')
        } else {
          this.showMessage('关注失败，请稍后重试', 'error')
        }
      })
    },

    unfollowUser() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录', 'warning')
        return
      }

      const data = {
        followUser: this.videoData.userId,
        createUser: this.userInfo.userData.id,
      }

      this.httpPost('/follow/delete', data, (json) => {
        if (json.data) {
          this.isFollowed = false
          if (this.videoData.fansCount !== undefined && this.videoData.fansCount > 0) {
            this.videoData.fansCount -= 1
          }
          this.showMessage('已取消关注', 'info')
        } else {
          this.showMessage('取消关注失败，请稍后重试', 'error')
        }
      })
    },

    checkFollow() {
      if (this.userInfo.userData == null || this.videoData.userId === this.userInfo.userData.id) {
        this.isFollowed = false
        return
      }

      this.httpGet(`/follow/check?followId=${this.videoData.userId}`, (json) => {
        this.isFollowed = json.data
      })
    },

    checkLike() {
      if (this.userInfo.userData == null) {
        this.isLiked = false
        return
      }
      this.httpGet(`/like/status?likeObjId=${this.id}&type=0`, (json) => {
        if (json.status === 200) {
          this.isLiked = json.data
        }
      })
    },
    checkDislike() {
      if (this.userInfo.userData == null) {
        this.isDisliked = false
        return
      }
      this.httpGet(`/dislike/status?dislikeObjId=${this.id}&type=0`, (json) => {
        if (json.status === 200) {
          this.isDisliked = json.data
        }
      })
    },
    // 点踩功能
    dislikeVideo() {
      if (this.userInfo.userData == null) {
        this.showMessage('请先登录后再点踩', 'warning')
        return
      }
      this.httpPost(`/dislike/toggle?dislikeObjId=${this.id}&type=0`, {}, (json) => {
        if (json.status === 200) {
          // 更新点赞状态
          if (json.data.dislike) {
            this.isDisliked = !this.isDisliked
            // 更新点赞数量
            this.videoData.dislikeCount += this.isDisliked ? 1 : -1
            // 显示消息提示
            this.showMessage(json.data.info, 'success')
          }
        } else {
          this.showMessage(json.data.info || '操作失败', 'error')
        }
      })
    },
    // 显示消息提示
    showMessage(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
  },
}
</script>

<style>
/* .category-link 的颜色已统一到 Main.css，跟随主题变化 */

.position-relative {
  position: relative;
}

.position-absolute {
  position: absolute;
  z-index: 1;
}

.top-0 {
  top: 0;
}

.start-0 {
  left: 0;
}
</style>