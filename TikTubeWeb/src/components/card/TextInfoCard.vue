<template>
  <v-card class="text-card rounded-lg elevation-1 h-100" variant="elevated">
    <!-- 卡片内容 -->
    <v-row class="text-info pa-3" no-gutters>
      <v-col v-if="textInfo.avatarUrl != null" cols="auto" class="mr-3">
        <router-link :to="`/user/${textInfo.userId}`">
          <v-avatar size="48" class="elevation-1">
            <v-img :alt="textInfo.username" :src="textInfo.avatarUrl" cover />
          </v-avatar>
        </router-link>
      </v-col>
      <v-col>
        <!-- 标题 -->
        <div class="text-title">
          <v-tooltip
            :text="textInfo.title"
            location="top"
            open-delay="500"
          >
            <template v-slot:activator="{ props }">
              <router-link :to="`/text/${textInfo.id}`" class="text-decoration-none">
                <span 
                  v-bind="props"
                  class="text-subtitle-1 font-weight-bold text-black title-text"
                >
                  {{ textInfo.title }}
                </span>
              </router-link>
            </template>
          </v-tooltip>
        </div>

        <!-- 描述 -->
        <div class="text-description text-body-2 text-medium-emphasis mt-1">
          <span class="description-text">{{ textInfo.describes }}</span>
        </div>

        <!-- 元数据 -->
        <div class="text-meta text-caption text-medium-emphasis mt-2">
          <div class="d-flex align-center">
            <v-icon size="small" class="mr-1">mdi-eye-outline</v-icon>
            <span>{{ formatNumber(textInfo.viewCount) }}</span>
            <v-divider vertical class="mx-2"></v-divider>
            <v-icon size="small" class="mr-1">mdi-comment-outline</v-icon>
            <span>{{ formatNumber(textInfo.commentCount) }}</span>
            
            <template v-if="textInfo.childrenCategory && textInfo.childrenCategory.fatherId !== 0">
              <v-divider vertical class="mx-2"></v-divider>
              <v-icon size="small" class="mr-1">mdi-tag-outline</v-icon>
              <router-link
                :to="`/v/${textInfo.fatherCategory.id}`"
                class="category-link"
              >
                <span v-text="textInfo.fatherCategory.name" />
              </router-link>
              /
              <router-link :to="`/v/${textInfo.childrenCategory.id}`" class="category-link">
                <span v-text="textInfo.childrenCategory.name" />
              </router-link>
            </template>
          </div>
          
          <div class="d-flex align-center mt-1">
            <router-link :to="`/user/${textInfo.userId}`" class="text-decoration-none d-flex align-center">
              <span class="username-text">{{ textInfo.username }}</span>
            </router-link>
            <v-divider vertical class="mx-2"></v-divider>
            <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
            <span v-text="TimeUtil.timeToNowStrning(textInfo.createTime)" />
          </div>
        </div>
      </v-col>
    </v-row>
    
    <!-- 卡片操作区 -->
    <v-divider></v-divider>
    <v-card-actions class="pa-2">
      <v-spacer></v-spacer>
      <v-btn variant="text" size="small" color="primary" :to="`/text/${textInfo.id}`">
        <v-icon size="small" class="mr-1">mdi-book-open-page-variant</v-icon>
        阅读全文
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script>
import TimeUtil from '@/utils/time-util'
export default {
  name: 'TextInfoCard',
  props: {
    text: {
      type: Object,
      default: () => {},
    },
  },
  data() {
    return {
      TimeUtil,
      textInfo: this.text,
    }
  },
  methods: {
    formatNumber(num) {
      if (num >= 10000) {
        return (num / 10000).toFixed(1) + '万';
      } else if (num >= 1000) {
        return (num / 1000).toFixed(1) + 'k';
      }
      return num;
    }
  }
}
</script>

<style scoped>
.text-card {
  transition: all 0.2s ease-in-out;
  cursor: pointer;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.text-card:hover {
  transform: translateY(-0.125em);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1) !important;
}

.text-info {
  min-height: 5em;
  flex-grow: 1;
}

.text-title {
  height: 3em; /* 固定高度，适合两行文字 */
  margin-bottom: 0.5em;
  overflow: hidden;
}

.title-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  line-height: 1.2;
}

.text-description {
  height: 3em; /* 固定高度，适合两行文字 */
  overflow: hidden;
}

.description-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  word-break: break-word;
  line-height: 1.3;
}

.text-meta {
  overflow: hidden;
  line-height: 1.4;
}

.category-link {
  color: inherit;
  text-decoration: none;
}

/* 颜色交由 Main.css 的全局规则统一到主题变量，这里不再写死色值 */
.category-link:hover,
.username-text:hover {
  color: rgb(var(--v-theme-primary));
  text-decoration: underline;
}

.username-text {
  max-width: 120px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: inline-block;
  vertical-align: middle;
}
</style>