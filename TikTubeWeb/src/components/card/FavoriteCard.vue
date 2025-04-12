<template>
  <v-card class="favorite-card" :elevation="0" @click="video ? navigateToVideo() : null">
    <v-row no-gutters>
      <!-- 左侧视频封面区域 -->
      <v-col cols="12">
        <div class="video-thumbnail position-relative">
          <template v-if="video">
            <v-img :src="video.imgUrl" :aspect-ratio="16 / 9" class="rounded-lg">
              <!-- 视频时长 -->
              <div class="video-duration">
                {{ formatDuration(video.duration) }}
              </div>
              <!-- 播放按钮悬浮效果 -->
              <div class="play-overlay">
                <v-icon size="48" color="white">mdi-play-circle</v-icon>
              </div>
            </v-img>
          </template>
          <template v-else>
            <div class="deleted-video-thumbnail rounded-lg">
              <v-icon size="64" color="white">mdi-video-off</v-icon>
              <div class="deleted-text">视频已删除</div>
            </div>
          </template>
        </div>
      </v-col>

      <!-- 右侧视频信息区域 -->
      <v-col cols="12" sm="7" class="pa-4">
        <div class="d-flex flex-column h-100">
          <template v-if="video">
            <!-- 视频标题 -->
            <div class="text-subtitle-1 font-weight-bold mb-2 text-truncate-2">
              {{ video.title }}
            </div>

            <!-- 视频统计信息 -->
            <div class="d-flex align-center mb-2 text-medium-emphasis">
              <v-icon size="small" class="mr-1">mdi-eye</v-icon>
              <span class="text-caption mr-3">{{ formatNumber(video.viewCount) }}次观看</span>
              <v-icon size="small" class="mr-1">mdi-comment</v-icon>
              <span class="text-caption">{{ formatNumber(video.commentCount) }}条评论</span>
            </div>
          </template>
          <template v-else>
            <div class="text-body-2 text-medium-emphasis">
              视频所有者可能已删除该视频或将其设为私密
            </div>
          </template>

          <!-- 收藏时间 -->
          <div class="mt-auto text-medium-emphasis">
            <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
            <span class="text-caption">收藏于 {{ formatDate(favorite.createTime) }}</span>
          </div>

          <!-- 删除的收藏操作按钮 -->
          <div v-if="!video" class="mt-4">
            <v-btn
              color="error"
              variant="tonal"
              size="small"
              prepend-icon="mdi-delete"
              @click.stop="removeFavorite"
            >
              移除收藏
            </v-btn>
          </div>
        </div>
      </v-col>
    </v-row>
  </v-card>
</template>

<script>
export default {
  name: 'FavoriteCard',
  props: {
    video: {
      type: Object,
      required: false,
      default: null,
    },
    favorite: {
      type: Object,
      required: true,
    },
  },
  methods: {
    formatDuration(seconds) {
      if (!seconds) return '0:00'
      const minutes = Math.floor(seconds / 60)
      const remainingSeconds = Math.floor(seconds % 60)
      return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
    },
    formatNumber(num) {
      if (!num) return '0'
      if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万'
      }
      return num.toString()
    },
    formatDate(timestamp) {
      const date = new Date(timestamp)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit',
      })
    },
    navigateToVideo() {
      if (this.video) {
        this.$router.push(`/video/${this.video.id}`)
      }
    },
    removeFavorite() {
      try {
        this.httpPost(`/favorites/toggle`, { articeId: this.favorite.articeId }, (json) => {
          if (json.data.id == null) {
            this.$emit('favorite-removed', this.favorite.id)
          }
        })
      } catch (error) {
        console.error('移除收藏失败:', error)
      }
    },
  },
}
</script>

<style scoped>
.favorite-card {
  transition: all 0.2s ease-in-out;
  cursor: pointer;
  border: 1px solid rgba(0, 0, 0, 0.12);
}

.favorite-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.video-thumbnail {
  position: relative;
  overflow: hidden;
}

.video-duration {
  position: absolute;
  bottom: 8px;
  right: 8px;
  background-color: rgba(0, 0, 0, 0.8);
  color: white;
  padding: 2px 4px;
  border-radius: 4px;
  font-size: 12px;
  z-index: 1;
}

.play-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease-in-out;
}

.favorite-card:hover .play-overlay {
  opacity: 1;
}

.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.deleted-video-thumbnail {
  aspect-ratio: 16/9;
  background-color: #f5f5f5;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #757575;
}

.deleted-text {
  margin-top: 8px;
  font-size: 14px;
}

@media (max-width: 600px) {
  .favorite-card {
    margin-bottom: 16px;
  }
}
</style>