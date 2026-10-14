<!--
  用户主页（频道页）
  版式对齐 YouTube 频道：封面 banner + 头像/名称/粉丝信息区 + 吸顶分页签 + 缩略卡片网格。
  原先视频用整行列表（VideoList）、文章用另一套 TextInfoCard，两种排版混排导致风格割裂，
  现在统一走首页在用的 VideoCard / ArticleCard 网格；接口按页返回混合列表，
  因此「首页」签把当页内容拆成「投稿视频」「文章」两个网格分组展示。
-->
<template>
  <div class="channel">
    <!-- 频道封面 -->
    <div class="channel__banner">
      <v-img
        v-if="userInfo && userInfo.topImgUrl"
        :src="userInfo.topImgUrl"
        cover
        class="h-100 w-100"
      />
    </div>

    <v-container>
      <!-- 封禁提示 -->
      <v-banner
        v-if="userInfo && showLockBanner()"
        class="my-4"
        color="warning"
        icon="$warning"
        lines="one"
      >
        <v-banner-text>
          该用户因违反社区规定，账号已被
          <span v-if="userInfo.blockEndTime == 0">永久封禁</span>
          <span v-else>封禁至： {{ TimeUtil.renderTime(userInfo.blockEndTime) }}</span>
        </v-banner-text>
      </v-banner>

      <!-- 频道信息区 -->
      <div v-if="userInfo" class="channel__head">
        <v-avatar :size="96" color="surface-variant" class="channel__avatar flex-shrink-0">
          <v-img v-if="userInfo.avatarUrl" :src="userInfo.avatarUrl" :alt="userInfo.username" cover />
          <v-icon v-else icon="mdi-account-circle-outline" size="56" class="text-medium-emphasis" />
        </v-avatar>

        <div class="channel__identity">
          <div class="d-flex align-center flex-wrap ga-2">
            <h1 class="text-h4 font-weight-bold">{{ userInfo.username }}</h1>
            <v-chip
              v-if="Power.checkPower(userInfo) === 'vip'"
              size="small"
              variant="tonal"
              color="warning"
              prepend-icon="mdi-crown"
              @click="goTOVIP"
            >
              VIP
            </v-chip>
          </div>
          <div class="text-body-2 text-medium-emphasis mt-1">
            {{ formatCount(userInfo.fansCount) }} 位粉丝
            <span class="mx-2">•</span>
            {{ formatCount(userInfo.submitCount) }} 个投稿
            <span class="mx-2">•</span>
            加入于 {{ formatDay(userInfo.createTime) }}
          </div>
          <div v-if="userInfo.introduction" class="channel__desc text-body-2 text-medium-emphasis mt-1">
            个性签名：{{ userInfo.introduction }}
          </div>
        </div>

        <div class="channel__actions">
          <template v-if="loginUser && loginUser.id == id">
            <v-btn variant="tonal" rounded="pill" prepend-icon="mdi-cog" @click="goToSetting">
              自定义频道
            </v-btn>
            <v-btn variant="tonal" rounded="pill" prepend-icon="mdi-video-outline" @click="goToStudio">
              创作中心
            </v-btn>
          </template>
          <v-btn
            v-else
            :color="isFollowed ? undefined : 'primary'"
            :variant="isFollowed ? 'tonal' : 'flat'"
            rounded="pill"
            :prepend-icon="isFollowed ? 'mdi-account-check' : 'mdi-account-plus'"
            @click="toggleFollow"
          >
            {{ isFollowed ? '已关注' : '关注' }}
          </v-btn>
        </div>
      </div>

      <!-- 分页签：吸顶，滚动时始终可切换 -->
      <div class="channel__tabs">
        <v-tabs v-model="type" bg-color="transparent" color="primary" slider-color="primary" show-arrows>
          <v-tab v-for="item in tabs" :key="item.type" :value="item.type">
            {{ item.title }}
          </v-tab>
        </v-tabs>
      </div>
      <v-divider />

      <div id="top" />

      <!-- 简介 -->
      <v-card v-if="type === 4" class="channel__about mt-6" rounded="lg" border>
        <v-card-title class="text-h6 font-weight-medium">频道简介</v-card-title>
        <v-divider />
        <v-card-text class="pa-0">
          <div v-for="row in aboutRows" :key="row.label" class="channel__about-row">
            <span class="channel__about-label">{{ row.label }}</span>
            <span class="channel__about-value">{{ row.value }}</span>
          </div>
        </v-card-text>
      </v-card>

      <!-- 公开收藏夹 -->
      <template v-else-if="type === 5">
        <!-- 收藏夹列表 -->
        <v-row v-if="!openedLabel" class="mt-6">
          <v-col v-if="!favLabels.length" cols="12">
            <div class="channel__empty">
              <v-icon icon="mdi-bookmark-multiple-outline" size="32" class="text-medium-emphasis mb-2" />
              <span class="text-body-2 text-medium-emphasis">这个用户还没有公开收藏夹</span>
            </div>
          </v-col>
          <v-col v-for="label in favLabels" :key="label.id" cols="12" sm="6" md="4" lg="3">
            <v-card class="fav-label-card pa-4" rounded="lg" border @click="openFavLabel(label)">
              <div class="d-flex align-center">
                <v-icon icon="mdi-bookmark-multiple" color="primary" size="32" class="mr-3" />
                <div class="min-width-0">
                  <div class="text-subtitle-1 font-weight-bold text-truncate">{{ label.name }}</div>
                  <div class="text-body-2 text-medium-emphasis">{{ label.count }} 个收藏</div>
                </div>
              </div>
            </v-card>
          </v-col>
        </v-row>

        <!-- 收藏夹内容 -->
        <template v-else>
          <div class="d-flex align-center mt-6">
            <v-btn icon="mdi-arrow-left" variant="text" size="small" @click="openedLabel = null" />
            <h2 class="text-h6 font-weight-medium ml-2">{{ openedLabel.name }}</h2>
            <v-chip size="small" variant="tonal" color="primary" class="ml-3">{{ openedLabel.count }} 个收藏</v-chip>
          </div>
          <v-progress-linear v-if="labelLoading" indeterminate color="primary" class="mt-4" rounded />
          <template v-else>
            <section v-if="labelVideoItems.length" class="mt-4">
              <h3 class="text-subtitle-1 font-weight-medium mb-2">视频</h3>
              <v-row>
                <v-col v-for="item in labelVideoItems" :key="item.articleEntity.id" cols="12" sm="6" md="4" lg="3" xxl="2">
                  <VideoCard :video="item.articleEntity" :show-author="false" />
                </v-col>
              </v-row>
            </section>
            <section v-if="labelArticleItems.length" :class="labelVideoItems.length ? 'mt-6' : 'mt-4'">
              <h3 class="text-subtitle-1 font-weight-medium mb-2">文章</h3>
              <v-row>
                <v-col v-for="item in labelArticleItems" :key="item.articleEntity.id" cols="12" sm="6" md="4" lg="3" xxl="2">
                  <ArticleCard :text="item.articleEntity" />
                </v-col>
              </v-row>
            </section>
            <div v-if="!labelVideoItems.length && !labelArticleItems.length" class="channel__empty">
              <v-icon icon="mdi-bookmark-outline" size="32" class="text-medium-emphasis mb-2" />
              <span class="text-body-2 text-medium-emphasis">这个收藏夹还没有可展示的内容</span>
            </div>
            <div v-if="labelLength > 1" class="channel__pager">
              <Pagination v-model="labelPage" :total-visible="7" :length="labelLength" rounded="circle" @update:model-value="labelPageChange" />
            </div>
          </template>
        </template>
      </template>

      <!-- 内容列表 -->
      <template v-else>
        <section v-if="videoItems.length" class="mt-6">
          <h2 class="text-h6 font-weight-medium mb-2">投稿视频</h2>
          <v-row>
            <v-col
              v-for="item in videoItems"
              :key="item.id"
              cols="12"
              sm="6"
              md="4"
              lg="3"
              xxl="2"
            >
              <VideoCard :video="item" :show-author="false" />
            </v-col>
          </v-row>
        </section>

        <section v-if="articleItems.length" :class="videoItems.length ? 'mt-8' : 'mt-6'">
          <h2 class="text-h6 font-weight-medium mb-2">文章</h2>
          <v-row>
            <v-col
              v-for="item in articleItems"
              :key="item.id"
              cols="12"
              sm="6"
              md="4"
              lg="3"
              xxl="2"
            >
              <ArticleCard :text="item" />
            </v-col>
          </v-row>
        </section>

        <div v-if="isEmpty" class="channel__empty">
          <v-icon icon="mdi-movie-open-outline" size="32" class="text-medium-emphasis mb-2" />
          <span class="text-body-2 text-medium-emphasis">这个频道还没有内容</span>
        </div>
      </template>

      <div v-if="type !== 4 && type !== 5" class="channel__pager">
        <Pagination
          v-model="page"
          :total-visible="7"
          :length="length"
          rounded="circle"
          @update:model-value="pageChange"
        />
      </div>
    </v-container>

    <!-- 消息提示组件 -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" @click="snackbar.show = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
import Power from '@/utils/check-power'
import TimeUtil from '@/utils/time-util'
import { useUserStore } from '@/stores/userStore'
import { useWebInfoStore } from '@/stores/webInfoStore'
import VideoCard from '@/components/card/VideoCard.vue'
import ArticleCard from '@/components/card/ArticleCard.vue'
import Pagination from '@/components/common/Pagination.vue'
import { formatCount, formatDay } from '@/utils/format'

// 分页签与接口 type 参数的对应关系（-1 混合 / 0 视频 / 2 文章 / 4 只看简介 / 5 公开收藏夹）
const TABS = [
  { type: -1, title: '首页' },
  { type: 0, title: '视频' },
  { type: 2, title: '讨论' },
  { type: 5, title: '收藏夹' },
  { type: 4, title: '简介' },
]

export default {
  name: 'UserHome',
  components: {
    VideoCard,
    ArticleCard,
    Pagination,
  },
  data() {
    return {
      TimeUtil,
      Power,
      tabs: TABS,
      id: 0,
      userInfo: null,
      videoList: [],
      page: 1,
      size: 20,
      length: 1,
      totalCount: 0,
      type: -1,
      loginUser: useUserStore().userData,
      isFollowed: false, // 是否已关注
      favLabels: [],
      openedLabel: null,
      labelList: [],
      labelPage: 1,
      labelLength: 1,
      labelLoading: false,
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
    }
  },
  computed: {
    videoItems() {
      return this.videoList.filter((item) => Number(item.type) === 0)
    },
    articleItems() {
      return this.videoList.filter((item) => Number(item.type) === 2)
    },
    isEmpty() {
      return !this.videoItems.length && !this.articleItems.length
    },
    labelVideoItems() {
      return this.labelList.filter((item) => item.articleEntity && Number(item.articleEntity.type) === 0)
    },
    labelArticleItems() {
      return this.labelList.filter((item) => item.articleEntity && Number(item.articleEntity.type) === 2)
    },
    aboutRows() {
      const info = this.userInfo || {}
      return [
        { label: '用户名', value: info.username },
        { label: '个性签名', value: info.introduction || '这位UP主很懒，什么都没写' },
        { label: '视频总数', value: formatCount(info.submitCount) },
        { label: '粉丝数', value: formatCount(info.fansCount) },
        { label: '关注数', value: formatCount(info.followCount) },
        { label: '加入时间', value: formatDay(info.createTime) },
      ]
    },
  },
  watch: {
    type() {
      this.page = 1
      this.openedLabel = null
      if (this.type === 4) return
      if (this.type === 5) {
        this.getPublicLabels()
        window.scrollTo({ top: 0, behavior: 'smooth' })
        return
      }
      this.geVideoList()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
  },
  created() {
    this.id = parseInt(this.$route.params.id)
    this.getUserInfo()
    this.geVideoList()
    this.checkFollow()
  },
  methods: {
    formatCount,
    formatDay,
    getUserInfo() {
      this.httpGet(`/user/info/${this.id}`, (json) => {
        if (json.data.id === -1) {
          this.$router.push('/')
          return
        }
        this.userInfo = json.data
        document.title = useWebInfoStore().webInfo.name + ' - ' + json.data.username
      })
    },
    showLockBanner() {
      if (this.userInfo.status == 0) {
        return false
      } else {
        if (this.userInfo.blockEndTime == 0) {
          return true
        }
        if (new Date().getTime() > this.userInfo.blockEndTime) {
          return false
        }
        return true
      }
    },
    geVideoList() {
      this.httpGet(
        `/article/user/list/${this.id}?page=${this.page}&limit=${this.size}&type=${this.type}`,
        (json) => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.totalCount = json.data.totalCount
        }
      )
    },
    followUser() {
      if (!this.loginUser) {
        this.showMessage('请先登录后再关注', 'error')
        return
      }
      const data = {
        followUser: this.id,
        createUser: this.loginUser.id,
      }
      this.httpPost('/follow/add', data, (json) => {
        if (json.data) {
          this.isFollowed = true
          this.userInfo.fansCount++
          this.showMessage('关注成功', 'success')
        } else {
          this.showMessage('关注失败，请稍后重试', 'error')
        }
      })
    },
    delelteFollow() {
      if (!this.loginUser) {
        this.showMessage('请先登录', 'error')
        return
      }
      const data = {
        followUser: this.id,
        createUser: this.loginUser.id,
      }
      this.httpPost('/follow/delete', data, (json) => {
        if (json.data) {
          this.isFollowed = false
          this.userInfo.fansCount--
          this.showMessage('已取消关注', 'info')
        } else {
          this.showMessage('取消关注失败，请稍后重试', 'error')
        }
      })
    },
    checkFollow() {
      if (!this.loginUser) {
        this.isFollowed = false
        return
      }

      this.httpGet(`/follow/check?followId=${this.id}`, (json) => {
        this.isFollowed = json.data
      })
    },
    // 切换关注状态
    toggleFollow() {
      if (!this.loginUser) {
        this.showMessage('请先登录后再关注', 'error')
        return
      }

      if (this.isFollowed) {
        this.delelteFollow()
      } else {
        this.followUser()
      }
    },
    // 公开收藏夹
    getPublicLabels() {
      this.httpGet(`/favorites/label/public/list?userId=${this.id}`, (json) => {
        if (json.status === 200) {
          this.favLabels = json.data || []
        }
      })
    },
    openFavLabel(label) {
      this.openedLabel = label
      this.labelPage = 1
      this.getLabelArticles()
    },
    getLabelArticles() {
      if (!this.openedLabel) return
      this.labelLoading = true
      this.httpGet(
        `/favorites/label/public/article/list?labelId=${this.openedLabel.id}&page=${this.labelPage}&limit=${this.size}`,
        (json) => {
          this.labelLoading = false
          if (json.status === 200) {
            this.labelList = json.data.list
            this.labelPage = json.data.currPage
            this.labelLength = json.data.totalPage
          } else {
            this.showMessage(json.message || '加载收藏夹失败', 'error')
          }
        },
        () => {
          this.labelLoading = false
          this.showMessage('加载收藏夹失败', 'error')
        }
      )
    },
    labelPageChange(page) {
      this.labelPage = page
      this.getLabelArticles()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    // 显示消息提示
    showMessage(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
    pageChange(page) {
      this.page = page
      this.geVideoList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
    goTOVIP() {
      this.$router.push('/vip')
    },
    goToStudio() {
      this.$router.push('/studio')
    },
    goToSetting() {
      this.$router.push('/user/setting')
    },
  },
}
</script>

<style scoped>
/* ---------- 封面 ---------- */
.channel__banner {
  height: clamp(112px, 15vw, 216px);
  overflow: hidden;
  background-color: rgb(var(--v-theme-surface-variant));
}

/* ---------- 信息区 ---------- */
.channel__head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 24px;
  padding: 24px 0 16px;
}

.channel__avatar {
  border: 4px solid rgb(var(--v-theme-background));
}

.channel__identity {
  flex: 1 1 260px;
  min-width: 0;
}

.channel__desc {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  overflow: hidden;
  word-break: break-word;
}

.channel__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-inline-start: auto;
}

/* ---------- 吸顶分页签 ---------- */
.channel__tabs {
  position: sticky;
  /* 顶栏高 64px 但实际占位 65px（含 1px 下边框），上移 1px 避免滚动时透出内容 */
  top: calc(var(--app-header-height) - 1px);
  z-index: 2;
  background-color: rgb(var(--v-theme-background));
}

/* ---------- 简介 ---------- */
.channel__about-row {
  display: flex;
  gap: 16px;
  padding: 12px 16px;
  border-bottom: 1px solid rgb(var(--v-theme-outline));
}

.channel__about-row:last-child {
  border-bottom: none;
}

.channel__about-label {
  flex: 0 0 96px;
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
  font-size: 0.8125rem;
}

.channel__about-value {
  min-width: 0;
  word-break: break-word;
  font-size: 0.875rem;
}

/* ---------- 空态 / 分页 ---------- */
.channel__empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 64px 0;
}

.fav-label-card {
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}

.fav-label-card:hover {
  transform: translateY(-2px);
  border-color: rgba(var(--v-border-color), 1);
}

.min-width-0 {
  min-width: 0;
}

.channel__pager {
  display: flex;
  justify-content: center;
  padding: 24px 0 12px;
}

@media (max-width: 920px) {
  .channel__actions {
    margin-inline-start: 0;
    width: 100%;
  }
}
</style>
