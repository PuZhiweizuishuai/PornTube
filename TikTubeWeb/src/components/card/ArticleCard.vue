<template>
  <!-- 紧凑卡：首页「文章」横滑行用 -->
  <v-card
    v-if="compact"
    class="article-card--compact"
    rounded="lg"
    hover
    @click="openArticle"
  >
    <div class="d-flex align-center pa-3">
      <!-- 有封面就显示封面，没有再退回按 id 散色的渐变占位 -->
      <v-img
        v-if="article.imgUrl"
        :src="article.imgUrl"
        :alt="article.title"
        :aspect-ratio="16 / 9"
        width="96"
        cover
        class="article-card__thumb rounded-lg me-3 flex-shrink-0"
      />
      <div v-else class="article-card__mark rounded-lg me-3" :style="markStyle">
        <v-icon icon="mdi-book-open-outline" size="22" />
      </div>
      <div class="article-card__body">
        <div class="article-card__title text-body-2 font-weight-medium">
          <router-link :to="`/text/${article.id}`" class="text-decoration-none">
            {{ article.title }}
          </router-link>
        </div>
        <div class="text-caption text-medium-emphasis mt-1 text-truncate">
          {{ article.username }}
          <span class="mx-1">•</span>
          {{ TimeUtil.timeToNowStrning(article.createTime) }}
        </div>
        <div v-if="article.childrenCategory" class="article-card__category text-caption">
          <v-icon icon="mdi-tag-outline" size="14" class="me-1" />
          <router-link
            v-if="article.childrenCategory.fatherId !== 0"
            :to="`/v/${article.fatherCategory.id}`"
            class="category-link text-decoration-none"
            @click.stop
          >
            {{ article.fatherCategory.name }}
          </router-link>
          <span v-if="article.childrenCategory.fatherId !== 0">/</span>
          <router-link
            :to="`/v/${article.childrenCategory.id}`"
            class="category-link text-decoration-none"
            @click.stop
          >
            {{ article.childrenCategory.name }}
          </router-link>
        </div>
      </div>
    </div>
  </v-card>

  <!-- 网格卡：文章列表页 / 搜索结果用，带 16:9 封面 -->
  <v-card v-else class="article-card" rounded="lg" hover @click="openArticle">
    <div class="article-card__media rounded-lg">
      <v-img
        v-if="article.imgUrl"
        :src="article.imgUrl"
        :alt="article.title"
        :aspect-ratio="16 / 9"
        cover
        class="rounded-lg"
      />
      <div v-else class="article-card__placeholder rounded-lg" :style="markStyle">
        <v-icon icon="mdi-book-open-outline" size="40" class="article-card__placeholder-icon" />
      </div>
      <v-chip class="article-card__badge text-caption" size="x-small" variant="flat">文章</v-chip>
    </div>

    <div class="d-flex pt-3 pb-1">
      <v-avatar :size="36" class="mt-1 me-3 flex-shrink-0">
        <v-img :src="article.avatarUrl" :alt="article.username" cover />
      </v-avatar>
      <div class="article-card__body">
        <div class="article-card__title text-subtitle-2 font-weight-medium">
          <router-link :to="`/text/${article.id}`" class="text-decoration-none">
            {{ article.title }}
          </router-link>
        </div>
        <div class="article-card__excerpt text-caption text-medium-emphasis">
          {{ article.describes }}
        </div>
        <div class="text-caption text-medium-emphasis mt-1">
          {{ formatCount(article.viewCount) }} 阅读
          <span class="mx-1">•</span>
          {{ formatCount(article.commentCount) }} 评论
          <span class="mx-1">•</span>
          {{ TimeUtil.timeToNowStrning(article.createTime) }}
        </div>
        <div class="text-caption text-medium-emphasis">
          <router-link :to="`/user/${article.userId}`" class="category-link text-decoration-none" @click.stop>
            {{ article.username }}
          </router-link>
        </div>
        <div v-if="article.childrenCategory" class="article-card__category text-caption">
          <v-icon icon="mdi-tag-outline" size="14" class="me-1" />
          <router-link
            v-if="article.childrenCategory.fatherId !== 0"
            :to="`/v/${article.fatherCategory.id}`"
            class="category-link text-decoration-none"
            @click.stop
          >
            {{ article.fatherCategory.name }}
          </router-link>
          <span v-if="article.childrenCategory.fatherId !== 0">/</span>
          <router-link
            :to="`/v/${article.childrenCategory.id}`"
            class="category-link text-decoration-none"
            @click.stop
          >
            {{ article.childrenCategory.name }}
          </router-link>
        </div>
      </div>
    </div>
  </v-card>
</template>

<script>
import TimeUtil from '@/utils/time-util'
import { formatCount } from '@/utils/format'

export default {
  name: 'ArticleCard',
  props: {
    text: {
      type: Object,
      default: () => ({}),
    },
    compact: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      TimeUtil,
    }
  },
  computed: {
    article() {
      return this.text || {}
    },
    // 文章大多没有封面，占位色相按 id 散开，避免整屏卡片一个颜色
    markStyle() {
      return { '--article-hue': (Number(this.article.id) * 47) % 360 }
    },
  },
  methods: {
    formatCount,
    openArticle() {
      this.$router.push(`/text/${this.article.id}`)
    },
  },
}
</script>

<style scoped>
.article-card,
.article-card--compact {
  cursor: pointer;
}

.article-card__body {
  min-width: 0;
  flex-grow: 1;
}

.article-card__title a {
  color: rgb(var(--v-theme-on-surface));
}

.article-card__title a:hover {
  color: rgb(var(--v-theme-primary));
}

.article-card__category {
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.article-card__title,
.article-card__excerpt {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  line-height: 1.35;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  max-height: 2.7em;
}

.article-card__excerpt {
  margin-top: 2px;
}

/* ---------- 紧凑卡 ---------- */
.article-card--compact {
  height: 100%;
}

.article-card__thumb {
  /* v-img 自身带 flex-grow: 1，不压住会把 96px 拉成半张卡片宽 */
  flex: 0 0 96px;
  width: 96px;
}

.article-card__mark {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  color: hsl(var(--article-hue) 55% 42%);
  background-color: rgb(var(--v-theme-surface-variant));
  background-image:
    radial-gradient(120% 110% at 18% 0%, hsl(var(--article-hue) 75% 55% / 0.35), transparent 70%);
}

.v-theme--dark .article-card__mark {
  color: hsl(var(--article-hue) 65% 78%);
}

/* ---------- 网格卡 ---------- */
.article-card__media {
  position: relative;
}

.article-card__placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  aspect-ratio: 16 / 9;
  color: hsl(var(--article-hue) 45% 45% / 0.75);
  background-color: rgb(var(--v-theme-surface-variant));
  background-image:
    radial-gradient(120% 110% at 18% 0%, hsl(var(--article-hue) 75% 55% / 0.3), transparent 65%),
    linear-gradient(135deg, rgb(var(--v-theme-surface-variant)), rgb(var(--v-theme-background)));
}

.v-theme--dark .article-card__placeholder {
  color: hsl(var(--article-hue) 60% 72% / 0.8);
}

.article-card__badge {
  position: absolute;
  right: 8px;
  bottom: 8px;
  background-color: rgba(0, 0, 0, 0.75) !important;
  color: #fff !important;
  letter-spacing: 0.5px;
}
</style>
