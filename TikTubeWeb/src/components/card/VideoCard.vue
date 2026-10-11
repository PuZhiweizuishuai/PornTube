<template>
  <!-- padding-left: 10px; padding-right: 10px; 100vh style="width: 100vh"-->
  <div class="video-card rounded-lg">
    <router-link :to="`/video/${videoInfo.id}`">
      <v-img
        :alt="videoInfo.title"
        :src="videoInfo.imgUrl"
        :aspect-ratio="16 / 9"
        class="rounded"
        height="100%"
      >
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
    <!-- 修改视频信息布局 -->
    <v-row class="video-info pa-3" no-gutters>
      <v-col cols="auto" class="mr-3">
        <router-link :to="`/user/${videoInfo.userId}`">
          <v-avatar size="48">
            <v-img :alt="videoInfo.username" :src="videoInfo.avatarUrl" />
          </v-avatar>
        </router-link>
      </v-col>
      <v-col>
        <div class="video-title">
          <v-tooltip :text="videoInfo.title" location="top" open-delay="500">
            <template v-slot:activator="{ props }">
              <router-link :to="`/video/${videoInfo.id}`" class="text-decoration-none">
                <span
                  v-bind="props"
                  class="text-subtitle-1 font-weight-medium text-black title-text"
                >
                  {{ videoInfo.title }}
                </span>
              </router-link>
            </template>
          </v-tooltip>
        </div>

        <div class="video-meta text-caption text-medium-emphasis mt-1">
          {{ videoInfo.viewCount }} 观看
          <span class="mx-1">•</span>
          {{ videoInfo.danmakuCount }} 条弹幕
          <span class="mx-1">•</span>
          <router-link
            v-if="videoInfo.childrenCategory.fatherId !== 0"
            :to="`/v/${videoInfo.fatherCategory.id}`"
            class="category-link"
          >
            <span v-text="videoInfo.fatherCategory.name" />
          </router-link>
          /
          <router-link :to="`/v/${videoInfo.childrenCategory.id}`" class="category-link">
            <span v-text="videoInfo.childrenCategory.name" />
          </router-link>
          <br />
          <router-link :to="`/user/${videoInfo.userId}`" class="text-decoration-none">
            {{ videoInfo.username }}
          </router-link>
          <span class="mx-1">•</span>
          <span v-text="TimeUtil.timeToNowStrning(videoInfo.createTime)" />
          <v-chip
            v-if="videoInfo.pixelsNumber >= 2073600"
            class="ml-2"
            color="orange"
            size="x-small"
            text-color="white"
          >
            {{ StringUtils.clarityDisplay(videoInfo.pixelsNumber) }}
          </v-chip>
        </div>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import TimeUtil from '@/utils/time-util'
import StringUtils from '@/utils/string-utils'
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
  /*padding-bottom: 0.5em;*/
}

.video-card:hover {
  transform: translateY(-0.125em);
  box-shadow: 0 0.25em 0.5em rgba(0, 0, 0, 0.1);
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

.video-info {
  min-height: 5em;
}

.video-title {
  height: 3em; /* 固定高度，适合两行文字 */
  margin-bottom: 0.5em;
  overflow: hidden;
}

.title-text {
  /* 约两行文字的高度 */
  overflow: hidden;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  /* 限制最多显示2行 */
  -webkit-box-orient: vertical;
  line-height: 1.2;
}

.video-meta {
  height: 4.5em; /* 固定高度 */
  overflow: hidden;
  line-height: 1.4;
}

.meta-content {
  display: -webkit-box;
  line-clamp: 3;
  -webkit-line-clamp: 3; /* 最多显示3行 */
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 颜色交由 Main.css 的全局规则统一到主题变量，这里不再写死色值 */
.category-link {
  color: inherit;
  text-decoration: none;
}
</style>
