<template>
  <v-container fluid class="pa-4">
    <!-- 顶部标题与时间范围 -->
    <v-card class="mb-4" elevation="2" rounded="lg">
      <v-toolbar color="primary" density="comfortable">
        <v-toolbar-title class="text-subtitle-1 font-weight-medium">
          <v-icon icon="mdi-chart-areaspline" class="mr-2" />
          数据分析
        </v-toolbar-title>
        <v-spacer />
        <v-btn
          variant="text"
          size="small"
          class="text-white"
          prepend-icon="mdi-refresh"
          :loading="loading"
          @click="reloadAll"
        >
          刷新
        </v-btn>
      </v-toolbar>
      <v-card-text class="pa-4">
        <v-row align="center">
          <v-col cols="12" sm="auto">
            <v-btn-toggle
              v-model="days"
              mandatory
              divided
              variant="outlined"
              color="primary"
              @update:model-value="onDaysChange"
            >
              <v-btn v-for="d in DAY_OPTIONS" :key="d" :value="d">近 {{ d }} 天</v-btn>
            </v-btn-toggle>
          </v-col>
          <v-col>
            <div class="text-caption text-medium-emphasis">
              {{ periodText }}
              <v-tooltip location="top" text="观看人数按「用户 × 分P」去重统计，与稿件页的播放次数口径不同">
                <template #activator="{ props }">
                  <v-icon v-bind="props" icon="mdi-help-circle-outline" size="14" class="ml-1" />
                </template>
              </v-tooltip>
            </div>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- 核心指标 -->
    <v-row dense>
      <v-col v-for="card in metricCards" :key="card.key" cols="12" sm="6" md="4" lg="2">
        <v-card elevation="2" rounded="lg" class="h-100">
          <v-card-text class="pa-4">
            <div class="d-flex align-center mb-2">
              <v-avatar :color="card.color" variant="tonal" size="32" rounded="lg">
                <v-icon :icon="card.icon" size="18" />
              </v-avatar>
              <span class="text-caption text-medium-emphasis ml-2">{{ card.label }}</span>
            </div>
            <div v-if="loading" class="text-h6">
              <v-skeleton-loader type="text" />
            </div>
            <template v-else>
              <div class="text-h6 font-weight-bold">{{ formatCount(card.value) }}</div>
              <div class="text-caption mt-1 d-flex align-center ga-1">
                <v-icon
                  v-if="card.rate !== null"
                  :icon="card.up ? 'mdi-trending-up' : 'mdi-trending-down'"
                  size="14"
                  :color="card.up ? 'success' : 'error'"
                />
                <span :class="card.up ? 'text-success' : 'text-error'">{{ card.rateText }}</span>
                <span class="text-medium-emphasis">较上一周期</span>
              </div>
            </template>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <!-- 趋势图 -->
    <v-card class="mt-4" elevation="2" rounded="lg">
      <v-toolbar color="primary" density="comfortable">
        <v-toolbar-title class="text-subtitle-1 font-weight-medium">
          <v-icon icon="mdi-chart-line" class="mr-2" />
          每日趋势
        </v-toolbar-title>
      </v-toolbar>
      <v-card-text class="pa-4">
        <v-progress-linear v-if="loading" indeterminate color="primary" class="my-10" />

        <template v-else>
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
              :height="120"
            />
            <div class="d-flex justify-space-between text-caption text-medium-emphasis mt-2">
              <span>{{ formatDay(trendStart) }}</span>
              <span>日均 {{ formatCount(trendAverage) }} · 峰值 {{ formatCount(trendMax) }}</span>
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
            <div class="text-body-2 text-medium-emphasis mt-3">所选时间段内没有数据</div>
          </v-sheet>
        </template>
      </v-card-text>
    </v-card>

    <v-row class="mt-0">
      <!-- 稿件表现榜 -->
      <v-col cols="12" lg="8">
        <v-card elevation="2" rounded="lg" class="h-100">
          <v-toolbar color="red" density="comfortable">
            <v-toolbar-title class="text-subtitle-1 font-weight-medium">
              <v-icon icon="mdi-trophy" class="mr-2" />
              稿件表现
            </v-toolbar-title>
          </v-toolbar>

          <div class="d-flex align-center justify-end px-4 pt-3">
            <v-select
              v-model="sort"
              :items="SORT_OPTIONS"
              density="compact"
              variant="outlined"
              hide-details
              label="排序"
              style="max-width: 200px"
              @update:model-value="table.reload()"
            />
          </div>

          <v-data-table
            :headers="headers"
            :items="table.items"
            :loading="table.loading"
            hide-default-footer
            mobile-breakpoint="md"
          >
            <template #[`item.info`]="{ item }">
              <div class="d-flex align-center py-2">
                <v-img
                  v-if="item.type == 0"
                  :src="item.imgUrl"
                  width="88"
                  height="50"
                  cover
                  rounded
                  class="mr-3 flex-shrink-0 bg-grey-lighten-3"
                />
                <v-avatar v-else color="grey-lighten-3" size="40" rounded class="mr-3 flex-shrink-0">
                  <v-icon :icon="getArticleType(item.type).icon" size="18" />
                </v-avatar>
                <div class="overflow-hidden">
                  <a
                    :href="articlePath(item)"
                    target="_blank"
                    class="text-body-2 font-weight-medium text-decoration-none title-clamp"
                  >
                    {{ item.title }}
                  </a>
                  <div class="text-caption text-medium-emphasis">
                    {{ formatDay(item.createTime) }}
                    <v-chip
                      size="x-small"
                      variant="flat"
                      :color="getExamineStatusColor(item.examineStatus)"
                      class="text-white ml-1"
                    >
                      {{ getExamineStatusText(item.examineStatus) }}
                    </v-chip>
                  </div>
                </div>
              </div>
            </template>

            <template #[`item.periodView`]="{ item }">
              <div class="text-body-2 font-weight-medium">{{ formatCount(item.periodView) }}</div>
              <div class="text-caption text-medium-emphasis">
                累计 {{ formatCount(item.viewCount) }}
              </div>
            </template>

            <template #[`item.periodLike`]="{ item }">
              {{ formatCount(item.periodLike) }}
            </template>
            <template #[`item.periodComment`]="{ item }">
              {{ formatCount(item.periodComment) }}
            </template>
            <template #[`item.periodFavorite`]="{ item }">
              {{ formatCount(item.periodFavorite) }}
            </template>
            <template #[`item.periodDanmaku`]="{ item }">
              {{ formatCount(item.periodDanmaku) }}
            </template>

            <template #[`item.engagementRate`]="{ item }">
              <v-chip size="x-small" :color="rateColor(item.engagementRate)" variant="tonal">
                {{ (item.engagementRate * 100).toFixed(2) }}%
              </v-chip>
            </template>

            <template #no-data>
              <div class="d-flex flex-column align-center py-8">
                <v-icon icon="mdi-file-document-outline" size="42" color="grey-lighten-1" />
                <div class="text-body-2 text-medium-emphasis mt-2">还没有可统计的稿件</div>
              </div>
            </template>
          </v-data-table>

          <Pagination
            v-model="table.page"
            :length="table.length"
            :total-visible="5"
            block
            hide-on-single-page
            @update:model-value="table.handlePageChange()"
          />
        </v-card>
      </v-col>

      <!-- 分区分布 -->
      <v-col cols="12" lg="4">
        <v-card elevation="2" rounded="lg" class="h-100">
          <v-toolbar color="green-lighten-1" density="comfortable">
            <v-toolbar-title class="text-subtitle-1 font-weight-medium">
              <v-icon icon="mdi-shape-outline" class="mr-2" />
              分区分布
            </v-toolbar-title>
          </v-toolbar>
          <v-card-text class="pa-4">
            <v-progress-linear v-if="loading" indeterminate color="primary" class="my-8" />
            <template v-else-if="categoryRows.length">
              <div v-for="row in categoryRows" :key="row.category" class="mb-4">
                <div class="d-flex align-center justify-space-between mb-1">
                  <span class="text-body-2 text-truncate">
                    {{ row.name }}
                  </span>
                  <span class="text-caption text-medium-emphasis flex-shrink-0 ml-2">
                    {{ formatCount(row.articleCount) }} 篇 · {{ formatCount(row.periodView) }} 观看
                  </span>
                </div>
                <v-progress-linear
                  :model-value="row.percent"
                  :color="row.color"
                  height="8"
                  rounded
                  bg-color="surface-variant"
                />
              </div>
            </template>
            <v-sheet
              v-else
              class="d-flex flex-column align-center justify-center py-10"
              rounded="lg"
              color="surface-sunken"
            >
              <v-icon icon="mdi-shape-outline" size="42" color="grey-lighten-1" />
              <div class="text-body-2 text-medium-emphasis mt-3">暂无分区数据</div>
            </v-sheet>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>

    <AppSnackbar :state="toast" />
  </v-container>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useTheme } from 'vuetify'
import http from '@/api/index'
import { useMessage } from '@/composables/useMessage'
import { useTable } from '@/composables/useTable'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import Pagination from '@/components/common/Pagination.vue'
import TrendSparkline from '@/components/studio/TrendSparkline.vue'
import { formatCount, formatDay } from '@/utils/format'
import {
  articlePath,
  buildCategoryMap,
  compareRate,
  getArticleType,
  getExamineStatusColor,
  getExamineStatusText,
  seriesOf,
} from '@/utils/studio'

const DAY_OPTIONS = [7, 30, 90]

const TREND_METRICS = [
  { key: 'view', label: '观看' },
  { key: 'like', label: '点赞' },
  { key: 'comment', label: '评论' },
  { key: 'favorite', label: '收藏' },
  { key: 'danmaku', label: '弹幕' },
  { key: 'fans', label: '涨粉' },
]

/**
 * 排序列与后端 CreatorStatsService.ORDER_SQL 白名单一一对应，
 * 后端只接受这些 key，传别的会静默回落到默认排序
 */
const SORT_OPTIONS = [
  { title: '按观看排序', value: 'periodView' },
  { title: '按点赞排序', value: 'periodLike' },
  { title: '按评论排序', value: 'periodComment' },
  { title: '按收藏排序', value: 'periodFavorite' },
  { title: '按弹幕排序', value: 'periodDanmaku' },
  { title: '按累计播放排序', value: 'view' },
  { title: '按发布时间排序', value: 'createTime' },
]

const CATEGORY_COLORS = ['primary', 'info', 'success', 'warning', 'pink', 'indigo', 'teal']

const headers = [
  { title: '稿件', key: 'info', sortable: false },
  { title: '观看', key: 'periodView', align: 'center', sortable: false, width: '110px' },
  { title: '点赞', key: 'periodLike', align: 'center', sortable: false, width: '90px' },
  { title: '评论', key: 'periodComment', align: 'center', sortable: false, width: '90px' },
  { title: '收藏', key: 'periodFavorite', align: 'center', sortable: false, width: '90px' },
  { title: '弹幕', key: 'periodDanmaku', align: 'center', sortable: false, width: '90px' },
  { title: '互动率', key: 'engagementRate', align: 'center', sortable: false, width: '100px' },
]

const { toast, showErrorMessage } = useMessage()
const theme = useTheme()

const days = ref(30)
const sort = ref('periodView')
const trendMetric = ref('view')
const loading = ref(true)
const summary = ref({})
const categories = ref([])
const categoryMap = ref({})

const table = useTable({
  url: '/studio/data/articles',
  size: 10,
  params: () => ({ days: days.value, sort: sort.value }),
  onError: (e) => {
    if (!e?.isUnauthorized) showErrorMessage(`稿件数据加载失败：${e.message}`)
  },
})

const periodText = computed(() => {
  const start = summary.value.startTime
  if (!start) return `统计区间：近 ${days.value} 天`
  return `统计区间：${formatDay(start)} ~ ${formatDay(Date.now())}`
})

const trend = computed(() => summary.value.trend || [])
const trendSeries = computed(() => seriesOf(trend.value, trendMetric.value))
const trendMax = computed(() => trendSeries.value.reduce((max, v) => Math.max(max, v), 0))
const trendStart = computed(() => (trend.value.length ? trend.value[0].date : 0))
const trendAverage = computed(() => {
  if (!trendSeries.value.length) return 0
  return Math.round(trendSeries.value.reduce((sum, v) => sum + v, 0) / trendSeries.value.length)
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

const metricCards = computed(() => {
  const totals = summary.value.totals || {}
  const previous = summary.value.previous || {}
  return [
    { key: 'view', label: '观看人数', icon: 'mdi-eye', color: 'primary', value: totals.view },
    { key: 'like', label: '点赞', icon: 'mdi-thumb-up', color: 'pink', value: totals.like },
    { key: 'comment', label: '评论', icon: 'mdi-comment-text', color: 'info', value: totals.comment },
    {
      key: 'favorite',
      label: '收藏',
      icon: 'mdi-star',
      color: 'warning',
      value: totals.favorite,
    },
    { key: 'danmaku', label: '弹幕', icon: 'mdi-message-text', color: 'indigo', value: totals.danmaku },
    { key: 'fans', label: '涨粉', icon: 'mdi-account-plus', color: 'success', value: totals.fans },
  ].map((card) => {
    const rate = compareRate(card.value, previous[card.key])
    return { ...card, rate: rate.rate, rateText: rate.text, up: rate.up }
  })
})

const categoryRows = computed(() => {
  const list = categories.value || []
  const max = list.reduce((m, row) => Math.max(m, Number(row.periodView) || 0), 0)
  return list.map((row, index) => {
    const info = categoryMap.value[row.category]
    return {
      ...row,
      name: info ? `${info.parentName} · ${info.childName}` : '未分类',
      percent: max > 0 ? Math.round(((Number(row.periodView) || 0) / max) * 100) : 0,
      color: CATEGORY_COLORS[index % CATEGORY_COLORS.length],
    }
  })
})

function rateColor(rate) {
  const v = Number(rate) || 0
  if (v >= 0.1) return 'success'
  if (v >= 0.03) return 'info'
  return 'grey'
}

async function loadSummary() {
  try {
    const json = await http.get('/studio/data/summary', { days: days.value })
    summary.value = json?.data || {}
  } catch (e) {
    if (!e?.isUnauthorized) showErrorMessage(`统计数据加载失败：${e.message}`)
  }
}

async function loadCategories() {
  try {
    const json = await http.get('/studio/data/category', { days: days.value })
    categories.value = Array.isArray(json?.data) ? json.data : []
  } catch (e) {
    if (!e?.isUnauthorized) showErrorMessage(`分区数据加载失败：${e.message}`)
  }
}

async function loadCategoryTree() {
  if (Object.keys(categoryMap.value).length) return
  try {
    const json = await http.get('/category/tree')
    categoryMap.value = buildCategoryMap(json?.data)
  } catch (e) {
    if (!e?.isUnauthorized) showErrorMessage(`分区信息加载失败：${e.message}`)
  }
}

function onDaysChange() {
  reloadAll()
}

async function reloadAll() {
  loading.value = true
  await Promise.all([loadSummary(), loadCategories(), loadCategoryTree(), table.reload()])
  loading.value = false
}

onMounted(() => {
  reloadAll()
})
</script>

<style scoped>
.title-clamp {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
