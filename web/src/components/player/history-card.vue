<template>
  <v-row>
    <v-col cols="3">
      <router-link :to="`/video/${video.articleEntity.id}`">
        <v-img :src="video.articleEntity.imgUrl" aspect-ratio="1.77" max-width="400" />
      </router-link>
    </v-col>
    <v-col>
      <v-row>
        <router-link :to="`/video/${video.articleEntity.id}`">
          <h3>{{ video.articleEntity.title }}</h3>
        </router-link>
      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        播放时间： {{ TimeUtil.renderTime(video.playRecordingEntity.updateTime) }}
      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        播放设备： {{ video.playRecordingEntity.ua }}
      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        {{ video.articleEntity.describes }}
      </v-row>
    </v-col>
  </v-row>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
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
      TimeUtil,
      videoInfo: this.video
    }
  }
}
</script>

<style>

</style>
