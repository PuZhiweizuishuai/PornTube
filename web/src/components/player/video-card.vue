<template>
  <!-- padding-left: 10px; padding-right: 10px; -->
  <div style="width: 350px">
    <router-link target="_blank" :to="`/video/${videoInfo.id}`">
      <v-img
        :src="videoInfo.imgUrl"
        outlined
        aspect-ratio="1.77"
      />
    </router-link>
    <v-row>
      <v-col cols="2">
        <router-link target="_blank" :to="`/user/${videoInfo.userId}`">
          <v-avatar size="48">
            <v-img :src="videoInfo.avatarUrl" />
          </v-avatar>
        </router-link>
      </v-col>
      <v-col cols="10">
        <p style="font-size: 20px; margin-bottom: 0px;color: black;">
          <router-link target="_blank" :to="`/video/${videoInfo.id}`" style="color: black;"> {{ videoInfo.title }} </router-link>
        </p>
        <p style="font-size: 10px; color: #606060;">
          {{ videoInfo.viewCount }} 观看 <span v-html="`&nbsp;&nbsp;`" />
          {{ videoInfo.danmakuCount }} 条弹幕 <br>
          <router-link :to="`/user/${videoInfo.userId}`"> {{ videoInfo.username }}</router-link>
          <br>
          <span v-text="TimeUtil.timeToNowStrning(videoInfo.createTime)" />
        </p>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
export default {
  name: 'VideoCard',
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
  },
  created() {

  }
}
</script>

<style>

</style>
