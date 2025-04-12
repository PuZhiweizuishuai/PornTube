<template>
  <!-- padding-left: 10px; padding-right: 10px; -->
  <div class="video-card" style="width: 350px">
    <router-link :to="`/video/${videoInfo.id}`">
      <v-img :src="videoInfo.imgUrl" :aspect-ratio="16 / 9" class="rounded" height="100%">
        <div class="d-flex fill-height align-end">
          <v-chip
            style="background-color: rgba(0, 0, 0, 50%)"
            variant="text"
            color="white"
            size="small"
            class="mb-1 mr-1 bg-black-transparent"
          >
            {{ TimeUtil.timeCover(videoInfo.duration) }}
          </v-chip>
        </div>
        <!-- 添加播放按钮悬浮效果 -->
        <div class="play-overlay">
          <v-icon size="48" color="white">mdi-play-circle</v-icon>
        </div>
      </v-img>
      <!-- <span> {{ TimeUtil.timeCover(videoInfo.duration) }} </span> -->
    </router-link>
    <v-row style="padding-top: 12px; padding-bottom: 12px">
      <v-col cols="2">
        <router-link :to="`/user/${videoInfo.userId}`" class="ml-2">
          <v-avatar size="48">
            <v-img :alt="videoInfo.username" :src="videoInfo.avatarUrl" />
          </v-avatar>
        </router-link>
      </v-col>
      <v-col cols="10">
        <p style="font-size: 20px; margin-bottom: 0px; color: black">
          <router-link :to="`/video/${videoInfo.id}`" style="color: black">
            {{ videoInfo.title }}
            <v-chip
              v-if="videoInfo.pixelsNumber >= 2073600"
              class="ma-2"
              color="orange"
              x-small
              text-color="white"
            >
              {{ StringUtils.clarityDisplay(videoInfo.pixelsNumber) }}
            </v-chip>
          </router-link>
        </p>
        <p style="font-size: 10px; color: #606060">
          {{ videoInfo.viewCount }} 观看 <span v-html="`&nbsp;&nbsp;`" />
          {{ videoInfo.danmakuCount }} 条弹幕 <span v-html="`&nbsp;&nbsp;`" />

          <!-- 分区信息 -->
          <router-link
            v-if="videoInfo.childrenCategory.fatherId !== 0"
            :to="`/v/${videoInfo.fatherCategory.id}`"
            class="category-link"
          >
            <span v-text="videoInfo.fatherCategory.name" />
          </router-link>
          /
          <router-link :to="`/v/${videoInfo.childrenCategory.id}`" class="category-link">
            <span v-text="videoInfo.childrenCategory.name" /> </router-link
          ><br />
          <!-- 发布人 -->
          <router-link :to="`/user/${videoInfo.userId}`"> {{ videoInfo.username }}</router-link>
          <br />
          <span v-text="TimeUtil.timeToNowStrning(videoInfo.createTime)" />
        </p>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
import StringUtils from '@/utils/string-utils.vue'
export default {
  name: 'VideoCard',
  props: {
    video: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      TimeUtil,
      StringUtils,
      videoInfo: this.video,
    }
  },
  created() {},
}
</script>

<style scoped>
.video-card {
  transition: all 0.2s ease-in-out;
  cursor: pointer;
}

.video-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
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

.video-card:hover .play-overlay {
  opacity: 1;
}
</style>
