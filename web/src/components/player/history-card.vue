<template>
  <v-row>
    <v-col cols="3">
      <router-link :to="`/video/${video.articleEntity.id}?seek=${video.playRecordingEntity.videoTime}`">
        <v-img :src="video.articleEntity.imgUrl" aspect-ratio="1.77" max-width="400" class="white--text align-end">
          <span style="background-color: rgba(0,0,0,0.4);border-radius: 5px;">

            {{ TimeUtil.timeCover(videoInfo.articleEntity.duration) }}
          </span>
        </v-img>
      </router-link>
    </v-col>
    <v-col>
      <v-row>
        <router-link :to="`/video/${video.articleEntity.id}?seek=${video.playRecordingEntity.videoTime}`">
          <h3>{{ video.articleEntity.title }}</h3>
        </router-link>
      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        <strong>观看到：

          {{ TimeUtil.timeCover(video.playRecordingEntity.videoTime) }}

        </strong>

        &nbsp;&nbsp;

        播放时间：{{ TimeUtil.renderTime(video.playRecordingEntity.updateTime) }}

      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        <span
          style="
    margin-top: 8px;
"
        >播放设备： </span>
        <v-chip
          class="ma-2"
          color="primary"
          small
        >
          {{ getUaInfo(video.playRecordingEntity.ua) }}
          <!-- {{ parser(video.playRecordingEntity.ua) }} -->
          <!-- {{ video.playRecordingEntity.ua }} -->
        </v-chip>

      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        {{ video.articleEntity.describes }}
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
var parser = require('ua-parser-js')
export default {
  name: 'History',
  props: {
    video: {
      type: Object,
      default: () => {}
    }
  },
  data() {
    return {
      parser,
      TimeUtil,
      videoInfo: this.video
    }
  },
  methods: {
    getUaInfo(uastr) {
      const ua = parser(uastr)
      return `操作系统：${ua.os.name} ${ua.os.version} 浏览器：${ua.browser.name} ${ua.browser.major}`
    }
  }
}
</script>

<style>

</style>
