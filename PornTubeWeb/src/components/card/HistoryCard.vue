<template>
  <v-card class="mb-4 history-card" variant="flat" :elevation="0" :hover="true">
    <v-row no-gutters>
      <v-col cols="12" sm="4" md="3" class="pa-2">
        <router-link
          :to="`/video/${video.articleEntity.id}?seek=${video.playRecordingEntity.videoTime}`"
          class="text-decoration-none"
        >
          <v-img
            :src="video.articleEntity.imgUrl"
            :aspect-ratio="16 / 9"
            class="rounded"
            height="100%"
          >
            <div class="d-flex fill-height align-end">
              <v-chip
                variant="text"
                color="white"
                size="small"
                class="mb-1 mr-1 bg-black-transparent"
              >
                {{ TimeUtil.timeCover(videoInfo.articleEntity.duration) }}
              </v-chip>

              <v-progress-linear
                :model-value="getLengthPercent"
                color="red-darken-2"
                height="6"
                class="mt-auto mb-0 video-progress"
              ></v-progress-linear>
            </div>
          </v-img>
        </router-link>
      </v-col>

      <v-col cols="12" sm="8" md="9" class="pa-3">
        <div class="d-flex flex-column h-100">
          <router-link
            :to="`/video/${video.articleEntity.id}?seek=${video.playRecordingEntity.videoTime}`"
            class="text-decoration-none"
          >
            <div class="text-h6 text-truncate-2 mb-1 text-black">
              {{ video.articleEntity.title }}
            </div>
          </router-link>

          <div class="text-body-2 text-medium-emphasis mb-1">
            <v-icon size="small" icon="mdi-timer-outline" class="mr-1"></v-icon>
            <strong>观看到：{{ TimeUtil.timeCover(video.playRecordingEntity.videoTime) }}</strong>
            <span class="mx-2">|</span>
            <span>播放时间：{{ TimeUtil.renderTime(video.playRecordingEntity.updateTime) }}</span>
          </div>

          <div class="text-body-2 text-medium-emphasis d-flex align-center mb-1">
            <span class="mr-1">播放设备：</span>
            <v-chip size="x-small" color="primary" variant="flat" class="ml-1">
              {{ getUaInfo(video.playRecordingEntity.ua) }}
            </v-chip>
          </div>

          <div class="text-body-2 text-medium-emphasis text-truncate-3 mt-auto">
            {{ video.articleEntity.describes }}
          </div>
        </div>
      </v-col>
    </v-row>
  </v-card>
</template>
  
<script>
import TimeUtil from '@/utils/time-util.vue'
import { UAParser } from 'ua-parser-js'

export default {
  name: 'HistoryCard',
  props: {
    video: {
      type: Object,
      default: () => ({}),
    },
  },
  data() {
    return {
      TimeUtil,
      videoInfo: this.video,
    }
  },
  computed: {
    getLengthPercent() {
      if (!this.video?.playRecordingEntity?.videoTime || !this.video?.articleEntity?.duration) {
        return 0
      }
      return (this.video.playRecordingEntity.videoTime / this.video.articleEntity.duration) * 100
    },
  },
  methods: {
    getUaInfo(uastr) {
      const ua = new UAParser(uastr)
      return `操作系统：${ua.getOS().name} ${ua.getOS().version} 浏览器：${ua.getBrowser().name} ${
        ua.getBrowser().major
      }`
    },
  },
}
</script>
  
<style scoped>
.history-card {
  transition: all 0.2s ease;
}

.history-card:hover {
  background-color: rgba(0, 0, 0, 0.03);
}

.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.text-truncate-3 {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.bg-black-transparent {
  background-color: rgba(0, 0, 0, 0.6) !important;
}

.video-progress {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
}
</style>
  