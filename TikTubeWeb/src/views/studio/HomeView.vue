<template>
  <v-container fluid class="pa-4">
    <!-- 欢迎条 -->
    <v-card class="mb-4" elevation="2" rounded="lg">
      <v-card-text class="pa-5">
        <v-row align="center">
          <v-col cols="auto">
            <v-avatar size="64" rounded="lg">
              <v-img :src="user.avatarUrl" :alt="user.username" cover />
            </v-avatar>
          </v-col>
          <v-col>
            <div class="text-h6 font-weight-bold">{{ greeting }}，{{ user.username }}</div>
            <div class="text-body-2 text-medium-emphasis mt-1">
              欢迎回到创作中心
              <template v-if="overview.lastPublishTime">
                · 上次投稿 {{ formatDay(overview.lastPublishTime) }}
              </template>
              <template v-else>· 还没有发布过稿件，发布第一篇试试吧</template>
            </div>
          </v-col>
          <v-col cols="auto" class="d-flex ga-2 flex-wrap">
            <v-btn
              color="primary"
              variant="elevated"
              prepend-icon="mdi-upload"
              to="/studio/upload"
            >
              去投稿
            </v-btn>
            <v-btn variant="outlined" prepend-icon="mdi-account" :to="`/user/${user.id}`">
              我的频道
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- 加载态 -->
    <v-row v-if="loading">
      <v-col v-for="i in 4" :key="`sk-${i}`" cols="12" sm="6" md="3">
        <v-card elevation="2" rounded="lg">
          <v-card-text>
            <v-skeleton-loader type="list-item-two-line" />
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <template v-else>
      <!-- 核心指标 -->
      <v-row dense>
        <v-col v-for="card in statCards" :key="card.key" cols="12" sm="6" md="3">
          <v-card elevation="2" rounded="lg" class="stat-card h-100">
            <v-card-text class="pa-4">
              <div class="d-flex align-center mb-2">
                <v-avatar :color="card.color" variant="tonal" size="36" rounded="lg">
                  <v-icon :icon="card.icon" size="20" />
                </v-avatar>
                <span class="text-body-2 text-medium-emphasis ml-3">{{ card.label }}</span>
              </div>
              <div class="text-h5 font-weight-bold">{{ formatCount(card.value) }}</div>
              <div class="text-caption mt-1 d-flex align-center ga-1">
                <v-icon
                  :icon="card.today > 0 ? 'mdi-arrow-up' : 'mdi-minus'"
                  size="14"
                  :color="card.today > 0 ? 'success' : 'grey'"
                />
                <span :class="card.today > 0 ? 'text-success' : 'text-medium-emphasis'">
                  今日 {{ card.today > 0 ? `+${formatCount(card.today)}` : '暂无新增' }}
                </span>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row class="mt-0">
        <!-- 近 7 日趋势 -->
        <v-col cols="12" md="8">
          <v-card elevation="2" rounded="lg" class="h-100">
            <v-toolbar color="primary" density="comfortable">
              <v-toolbar-title class="text-subtitle-1 font-weight-medium">
                <v-icon icon="mdi-chart-line" class="mr-2" />
                近 7 日数据趋势
              </v-toolbar-title>
            </v-toolbar>
            <v-card-text class="pa-4">
              <v-chip-group v-model="trendMetric" mandatory class="mb-2">
                <v-chip
                  v-for="m in TREND_METRICS"
                  :key="m.key"
                  :value="m.key"
                  size="small"
                  :variant="trendMetric === m.key ? 'flat' : 'tonal'"
                  :color="trendMetric === m.key ? 'primary' : undefined"
                >
                  {{ m.label }}
                </v-chip>
              </v-chip-group>

              <template v-if="trendSeries.length && trendMax > 0">
                <TrendSparkline
                  :points="trendPoints"
                  :label="currentMetricLabel"
                  :color="primaryColor"
                  :height="96"
                />
                <div class="d-flex justify-space-between text-caption text-medium-emphasis mt-2">
                  <span>{{ formatDay(trendStart) }}</span>
                  <span>日均 {{ formatCount(trendAverage) }}</span>
                  <span>今天</span>
                </div>
              </template>
              <v-sheet
                v-else
                class="d-flex flex-column align-center justify-center py-10"
                rounded="lg"
                color="surface-sunken"
              >
                <v-icon icon="mdi-chart-line-variant" size="48" color="grey-lighten-1" />
                <div class="text-body-2 text-medium-emphasis mt-3">近 7 日还没有数据</div>
              </v-sheet>

              <v-divider class="my-4" />

              <v-row dense>
                <v-col v-for="w in weekCards" :key="w.key" cols="6" sm="3">
                  <div class="text-caption text-medium-emphasis">{{ w.label }}</div>
                  <div class="text-subtitle-1 font-weight-bold">
                    {{ formatCount(w.value) }}
                  </div>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- 待办与提醒 -->
        <v-col cols="12" md="4">
          <v-card elevation="2" rounded="lg" class="h-100">
            <v-toolbar color="warning" density="comfortable">
              <v-toolbar-title class="text-subtitle-1 font-weight-medium">
                <v-icon icon="mdi-clipboard-check" class="mr-2" />
                待办与提醒
              </v-toolbar-title>
            </v-toolbar>
            <v-list lines="two" class="py-0">
              <!--
                VListItem 在 VList 的嵌套注册表里用 `props.value ?? link.href` 当节点 ID，
                「待审核」和「未通过」两条都指向 /studio/list，不显式给 value 就会撞 ID
                并触发 Vuetify 的 "Multiple nodes with the same ID" 报错
              -->
              <v-list-item
                v-for="todo in todos"
                :key="todo.key"
                :value="todo.key"
                :to="todo.to"
                :active="false"
                class="px-4"
              >
                <template #prepend>
                  <v-avatar :color="todo.color" variant="tonal" size="36" rounded="lg">
                    <v-icon :icon="todo.icon" size="20" />
                  </v-avatar>
                </template>
                <v-list-item-title class="text-body-2">
                  {{ todo.title }}
                </v-list-item-title>
                <v-list-item-subtitle>{{ todo.subtitle }}</v-list-item-subtitle>
                <template #append>
                  <v-chip
                    v-if="todo.count > 0"
                    :color="todo.color"
                    size="small"
                    variant="flat"
                    class="text-white"
                  >
                    {{ todo.count }}
                  </v-chip>
                  <v-icon v-else icon="mdi-check" color="success" />
                </template>
              </v-list-item>
            </v-list>
            <v-card-text class="pt-0">
              <v-divider class="mb-3" />
              <v-row dense>
                <v-col cols="6">
                  <div class="text-caption text-medium-emphasis">总稿件</div>
                  <div class="text-subtitle-1 font-weight-bold">
                    {{ formatCount(overview.articleCount) }}
                  </div>
                </v-col>
                <v-col cols="6">
                  <div class="text-caption text-medium-emphasis">粉丝</div>
                  <div class="text-subtitle-1 font-weight-bold">
                    {{ formatCount(overview.fansCount) }}
                  </div>
                </v-col>
              </v-row>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row class="mt-0">
        <!-- 最近投稿 -->
        <v-col cols="12" md="7">
          <v-card elevation="2" rounded="lg" class="h-100">
            <v-toolbar color="red" density="comfortable">
              <v-toolbar-title class="text-subtitle-1 font-weight-medium">
                <v-icon icon="mdi-filmstrip-box-multiple" class="mr-2" />
                最近投稿
              </v-toolbar-title>
              <v-spacer />
              <v-btn variant="text" size="small" class="text-white" to="/studio/list">
                全部稿件
                <v-icon icon="mdi-chevron-right" />
              </v-btn>
            </v-toolbar>

            <template v-if="recentArticles.length">
              <v-list lines="two" class="py-0">
                <v-list-item
                  v-for="item in recentArticles"
                  :key="item.id"
                  :href="articlePath(item)"
                  target="_blank"
                  class="px-4"
                >
                  <template #prepend>
                    <v-img
                      v-if="item.type == 0"
                      :src="item.imgUrl"
                      width="112"
                      height="63"
                      cover
                      rounded
                      class="mr-3 bg-grey-lighten-3"
                    />
                    <v-avatar
                      v-else
                      color="grey-lighten-3"
                      size="48"
                      rounded
                      class="mr-3"
                    >
                      <v-icon :icon="getArticleType(item.type).icon" />
                    </v-avatar>
                  </template>
                  <v-list-item-title class="text-body-2 font-weight-medium text-truncate">
                    {{ item.title }}
                  </v-list-item-title>
                  <v-list-item-subtitle>
                    <span class="mr-2">{{ formatDay(item.createTime) }}</span>
                    <v-chip
                      size="x-small"
                      variant="flat"
                      :color="getExamineStatusColor(item.examineStatus)"
                      class="text-white mr-2"
                    >
                      {{ getExamineStatusText(item.examineStatus) }}
                    </v-chip>
                    <span class="text-caption">
                      <v-icon icon="mdi-eye" size="12" class="mr-1" />
                      {{ formatCount(item.viewCount) }}
                      <v-icon icon="mdi-thumb-up" size="12" class="ml-2 mr-1" />
                      {{ formatCount(item.likeCount) }}
                    </span>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </template>
            <v-sheet
              v-else
              class="d-flex flex-column align-center justify-center py-10 mx-4 my-4"
              rounded="lg"
              color="surface-sunken"
            >
              <v-icon icon="mdi-video-off-outline" size="48" color="grey-lighten-1" />
              <div class="text-body-2 text-medium-emphasis mt-3">还没有稿件</div>
              <v-btn color="primary" variant="tonal" class="mt-4" to="/studio/upload">
                发布第一个稿件
              </v-btn>
            </v-sheet>
          </v-card>
        </v-col>

        <!-- 最新消息 -->
        <v-col cols="12" md="5">
          <v-card elevation="2" rounded="lg" class="h-100">
            <v-toolbar color="info" density="comfortable">
              <v-toolbar-title class="text-subtitle-1 font-weight-medium">
                <v-icon icon="mdi-bell-outline" class="mr-2" />
                最新消息
              </v-toolbar-title>
              <v-spacer />
              <v-btn variant="text" size="small" class="text-white" to="/notification">
                全部消息
                <v-icon icon="mdi-chevron-right" />
              </v-btn>
            </v-toolbar>

            <template v-if="notices.length">
              <v-list lines="two" class="py-0">
                <v-list-item v-for="item in notices" :key="item.notification.id" class="px-4">
                  <template #prepend>
                    <v-badge
                      :model-value="item.notification.status === 0"
                      dot
                      color="error"
                      offset-x="4"
                      offset-y="4"
                    >
                      <v-avatar size="36" rounded="lg">
                        <v-img :src="item.user.avatarUrl" :alt="item.user.username" cover />
                      </v-avatar>
                    </v-badge>
                  </template>
                  <v-list-item-title class="text-body-2">
                    <span class="font-weight-medium">{{ item.user.username }}</span>
                    <v-chip
                      size="x-small"
                      variant="flat"
                      :color="getNotificationType(item.notification.type).color"
                      class="text-white ml-2"
                    >
                      {{ getNotificationType(item.notification.type).text }}
                    </v-chip>
                  </v-list-item-title>
                  <v-list-item-subtitle class="text-truncate">
                    {{ noticePreview(item.notification) }}
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </template>
            <v-sheet
              v-else
              class="d-flex flex-column align-center justify-center py-10 mx-4 my-4"
              rounded="lg"
              color="surface-sunken"
            >
              <v-icon icon="mdi-bell-off-outline" size="48" color="grey-lighten-1" />
              <div class="text-body-2 text-medium-emphasis mt-3">暂时没有新消息</div>
            </v-sheet>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <AppSnackbar :state="toast" />
  </v-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useTheme } from 'vuetify'
import http from '@/api/index'
import { useUserStore } from '@/stores/userStore'
import { useMessage } from '@/composables/useMessage'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import TrendSparkline from '@/components/studio/TrendSparkline.vue'
import { formatCount, formatDay } from '@/utils/format'
import {
  articlePath,
  getArticleType,
  getExamineStatusColor,
  getExamineStatusText,
  getNotificationType,
  plainText,
  seriesOf,
} from '@/utils/studio'

/** 趋势图可切换的指标 */
const TREND_METRICS = [
  { key: 'view', label: '观看' },
  { key: 'like', label: '点赞' },
  { key: 'comment', label: '评论' },
  { key: 'favorite', label: '收藏' },
]

const { toast, showErrorMessage } = useMessage()
const userStore = useUserStore()
const theme = useTheme()

const loading = ref(true)
const trendMetric = ref('view')
const overview = ref({})
const recentArticles = ref([])
const notices = ref([])

const user = computed(() => userStore.userData || {})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const statCards = computed(() => [
  {
    key: 'view',
    label: '累计播放',
    icon: 'mdi-eye',
    color: 'primary',
    value: overview.value.viewCount || 0,
    today: overview.value.todayView || 0,
  },
  {
    key: 'like',
    label: '累计点赞',
    icon: 'mdi-thumb-up',
    color: 'pink',
    value: overview.value.likeCount || 0,
    today: overview.value.todayLike || 0,
  },
  {
    key: 'comment',
    label: '累计评论',
    icon: 'mdi-comment-text',
    color: 'info',
    value: overview.value.commentCount || 0,
    today: overview.value.todayComment || 0,
  },
  {
    key: 'favorite',
    label: '累计收藏',
    icon: 'mdi-star',
    color: 'warning',
    value: overview.value.favoriteCount || 0,
    today: overview.value.todayFavorite || 0,
  },
])

const weekCards = computed(() => [
  { key: 'view', label: '7 日观看', value: overview.value.weekView || 0 },
  { key: 'like', label: '7 日点赞', value: overview.value.weekLike || 0 },
  { key: 'comment', label: '7 日评论', value: overview.value.weekComment || 0 },
  { key: 'fans', label: '7 日涨粉', value: overview.value.weekFans || 0 },
])

const todos = computed(() => [
  {
    key: 'pending',
    title: '待审核稿件',
    subtitle: '审核通过后才会公开可见',
    icon: 'mdi-clock-outline',
    color: 'info',
    count: overview.value.pendingCount || 0,
    to: '/studio/list',
  },
  {
    key: 'rejected',
    title: '未通过稿件',
    subtitle: '按审核意见修改后可重新投稿',
    icon: 'mdi-close-circle',
    color: 'error',
    count: overview.value.rejectedCount || 0,
    to: '/studio/list',
  },
  {
    key: 'notice',
    title: '未读消息',
    subtitle: '评论、点赞与系统通知',
    icon: 'mdi-bell-outline',
    color: 'primary',
    count: overview.value.unreadNotification || 0,
    to: '/notification',
  },
])

const trend = computed(() => overview.value.trend || [])
const trendSeries = computed(() => seriesOf(trend.value, trendMetric.value))
const trendMax = computed(() => trendSeries.value.reduce((max, v) => Math.max(max, v), 0))
const trendStart = computed(() => (trend.value.length ? trend.value[0].date : 0))
const trendAverage = computed(() => {
  if (!trendSeries.value.length) return 0
  const total = trendSeries.value.reduce((sum, v) => sum + v, 0)
  return Math.round(total / trendSeries.value.length)
})
// sparkline 的 gradient 会写进 SVG 的 stop-color 属性，不能带 CSS 变量，
// 所以这里从 vuetify 主题里取真实色值，浅色/深色都能自动跟上
const primaryColor = computed(() => theme.current.value?.colors?.primary || '#F44336')

/** 折线图数据点：日期 + 当前选中指标的数值 */
const trendPoints = computed(() =>
  trend.value.map((point) => ({ date: point.date, value: Number(point?.[trendMetric.value]) || 0 }))
)

const currentMetricLabel = computed(
  () => TREND_METRICS.find((m) => m.key === trendMetric.value)?.label || ''
)

function noticePreview(notification) {
  return plainText(notification.content || notification.title)
}

async function loadOverview() {
  try {
    const json = await http.get('/studio/overview')
    overview.value = json?.data || {}
  } catch (e) {
    if (!e?.isUnauthorized) showErrorMessage(`总览数据加载失败：${e.message}`)
  }
}

async function loadRecentArticles() {
  try {
    const json = await http.get('/studio/article/list', { page: 1, limit: 5 })
    recentArticles.value = json?.data?.list || []
  } catch (e) {
    if (!e?.isUnauthorized) showErrorMessage(`最近投稿加载失败：${e.message}`)
  }
}

async function loadNotices() {
  try {
    const json = await http.get('/notification/get', { page: 1, limit: 6 })
    notices.value = json?.data?.list || []
  } catch (e) {
    if (!e?.isUnauthorized) showErrorMessage(`消息加载失败：${e.message}`)
  }
}

onMounted(async () => {
  loading.value = true
  await Promise.all([loadOverview(), loadRecentArticles(), loadNotices()])
  loading.value = false
})
</script>

<style scoped>
.stat-card {
  transition: box-shadow 0.2s ease;
}

.stat-card:hover {
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.12) !important;
}

/* v-chip-group 在 toolbar 里默认有内边距，这里对齐标题 */
.trend-metric-group :deep(.v-slide-group__content) {
  justify-content: flex-end;
}
</style>
