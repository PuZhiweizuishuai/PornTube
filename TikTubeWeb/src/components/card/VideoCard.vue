<!--
  视频缩略卡片（首页 / 分区 / 热门 / 订阅 / 搜索 / 相关推荐 / 频道页共用）
  - show-author=false 用于「频道页」这类同一次展示者场景：隐藏头像与用户名，避免整屏重复。
  - 时长角标固定封面右下角，颜色与文章卡片的「文章」角标一致。
-->
<template>
  <div class="video-card rounded-lg">
    <router-link :to="`/video/${videoInfo.id}`" class="video-card__media rounded-lg overflow-hidden">
      <v-img
        :alt="videoInfo.title"
        :src="videoInfo.imgUrl"
        :aspect-ratio="16 / 9"
        class="video-card__thumb"
        cover
      />
      <span class="video-card__duration">{{ TimeUtil.timeCover(videoInfo.duration) }}</span>
      <div class="play-overlay">
        <v-icon size="48" color="white">mdi-play-circle</v-icon>
      </div>
    </router-link>

    <div class="video-card__info d-flex pt-3 px-2 pb-3">
      <router-link
        v-if="showAuthor"
        :to="`/user/${videoInfo.userId}`"
        class="video-card__avatar flex-shrink-0 me-3"
      >
        <v-avatar :size="40" color="surface-variant">
          <v-img v-if="videoInfo.avatarUrl" :alt="videoInfo.username" :src="videoInfo.avatarUrl" cover />
          <v-icon v-else size="24">mdi-account-circle-outline</v-icon>
        </v-avatar>
      </router-link>

      <div class="video-card__body">
        <div class="video-title">
          <v-tooltip :text="videoInfo.title" location="top" open-delay="500">
            <template #activator="{ props }">
              <router-link :to="`/video/${videoInfo.id}`" class="text-decoration-none">
                <span v-bind="props" class="text-subtitle-1 font-weight-medium title-text">
                  {{ videoInfo.title }}
                </span>
              </router-link>
            </template>
          </v-tooltip>
        </div>

        <div class="video-meta text-caption text-medium-emphasis mt-1">
          {{ formatCount(videoInfo.viewCount) }} 观看
          <span class="mx-1">•</span>
          {{ formatCount(videoInfo.danmakuCount) }} 条弹幕
          <span class="mx-1">•</span>
          <span v-text="TimeUtil.timeToNowStrning(videoInfo.createTime)" />
          <template v-if="showAuthor">
            <span class="mx-1">•</span>
            <router-link :to="`/user/${videoInfo.userId}`" class="video-meta__link text-decoration-none">
              {{ videoInfo.username }}
            </router-link>
          </template>
        </div>

        <div v-if="hasCategory" class="video-meta text-caption text-medium-emphasis">
          <v-icon icon="mdi-tag-outline" size="14" class="me-1" />
          <router-link
            v-if="videoInfo.childrenCategory.fatherId !== 0"
            :to="`/v/${videoInfo.fatherCategory.id}`"
            class="category-link"
          >
            <span v-text="videoInfo.fatherCategory.name" />
          </router-link>
          <span v-if="videoInfo.childrenCategory.fatherId !== 0">/</span>
          <router-link :to="`/v/${videoInfo.childrenCategory.id}`" class="category-link">
            <span v-text="videoInfo.childrenCategory.name" />
          </router-link>
          <v-chip v-if="isHd" class="ms-2" color="warning" size="x-small" variant="tonal">
            {{ StringUtils.clarityDisplay(videoInfo.pixelsNumber) }}
          </v-chip>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import TimeUtil from '@/utils/time-util'
import StringUtils from '@/utils/string-utils'
import { formatCount } from '@/utils/format'
export default {
  name: 'VideoCard',
  props: {
    video: {
      type: Object,
      default: () => {},
    },
    /** 频道页等同一发布者的列表隐藏头像与用户名 */
    showAuthor: {
      type: Boolean,
      default: true,
    },
  },
  data() {
    return {
      TimeUtil,
      StringUtils,
      videoInfo: this.video,
    }
  },
  computed: {
    // 后端可能不带分区（或只带子分区），缺字段时整行不渲染，避免读取 undefined
    hasCategory() {
      const children = this.videoInfo && this.videoInfo.childrenCategory
      if (!children) return false
      return children.fatherId === 0 ? true : !!this.videoInfo.fatherCategory
    },
    isHd() {
      return Number(this.videoInfo && this.videoInfo.pixelsNumber) >= 2073600
    },
  },
  methods: {
    formatCount,
  },
}
</script>

<style scoped>
.video-card {
  transition: all 0.2s ease-in-out;
  cursor: pointer;
}

.video-card:hover {
  transform: translateY(-0.125em);
  box-shadow: 0 0.25em 0.5em rgba(0, 0, 0, 0.1);
}

.video-card__media {
  position: relative;
  display: block;
}

.video-card__duration {
  position: absolute;
  right: 8px;
  bottom: 8px;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 0.75rem;
  line-height: 1.5;
  color: #fff;
  background-color: rgba(0, 0, 0, 0.75);
  pointer-events: none;
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

.video-card__body {
  min-width: 0;
  flex-grow: 1;
}

.video-title {
  height: 3em; /* 固定两行高度，保证同排卡片信息起始位置一致 */
  margin-bottom: 0.5em;
  overflow: hidden;
}

.title-text {
  color: rgb(var(--v-theme-on-surface));
  overflow: hidden;
  display: -webkit-box;
  line-clamp: 2;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  line-height: 1.2;
}

.title-text:hover {
  color: rgb(var(--v-theme-primary));
}

.video-meta {
  line-height: 1.4;
}

.video-meta__link {
  color: inherit;
}

.video-meta__link:hover {
  color: rgb(var(--v-theme-primary));
}

/* 颜色交由 Main.css 的全局规则统一到主题变量，这里不再写死色值 */
.category-link {
  color: inherit;
  text-decoration: none;
}
</style>
