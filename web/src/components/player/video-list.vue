<template>

  <v-row style="padding-top: 12px; padding-bottom: 12px">
    <v-col cols="3">
      <router-link :to="`/video/${video.id}`">
        <v-img :src="video.imgUrl" aspect-ratio="1.77" max-width="400" />
      </router-link>
    </v-col>
    <v-col>
      <v-row style="padding-top: 6px; ">
        <router-link :to="`/video/${video.id}`">
          <h3>{{ video.title }}</h3>
        </router-link>
      </v-row>
      <v-row style="color:#606060;fount-size:12px;padding-top: 6px;">
        播放： {{ video.viewCount }} <span v-html="'&nbsp;&nbsp;&nbsp;'" /> 弹幕： {{ video.danmakuCount }}
      </v-row>
      <v-row style="color:#606060;fount-size:12px;padding-top: 6px;">
        <!-- 分区信息 -->
        分区：
        <router-link v-if="videoInfo.childrenCategory.fatherId !== 0" :to="`/v/${videoInfo.fatherCategory.id}`" class="category-link">
          <span v-text="videoInfo.fatherCategory.name" />
        </router-link>
        /
        <router-link :to="`/v/${videoInfo.childrenCategory.id}`" class="category-link">
          <span v-text="videoInfo.childrenCategory.name" />
        </router-link><br>
      </v-row>
      <v-row style="color:#606060;fount-size:12px;padding-top: 6px;">
        发布于： <span v-text="TimeUtil.renderTime(video.createTime)" />
      </v-row>
      <v-row style="color:#606060;fount-size:12px;padding-top: 6px;">
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
