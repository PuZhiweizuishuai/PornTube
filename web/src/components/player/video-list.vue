<template>

  <v-row>
    <v-col cols="3">
      <router-link :to="`/video/${video.id}`">
        <v-img :src="video.imgUrl" aspect-ratio="1.77" max-width="400" />
      </router-link>
    </v-col>
    <v-col>
      <v-row>
        <router-link :to="`/video/${video.id}`">
          <h3>{{ video.title }}</h3>
        </router-link>
      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        播放： {{ video.viewCount }} <span v-html="'&nbsp;&nbsp;&nbsp;'" /> 弹幕： {{ video.danmakuCount }}
      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        发布于： <span v-text="TimeUtil.renderTime(video.createTime)" />
      </v-row>
      <v-row style="color:#606060;fount-size:12px;">
        {{ video.describes }}
      </v-row>
    </v-col>
  </v-row>

</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
export default {
  name: 'VideoList',
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
