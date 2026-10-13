<!--
  可悬停查看数值的趋势折线图

  用 Vuetify 自带的 v-sparkline 画线，再叠一层「命中层」实现逐点 tooltip：
  v-sparkline 本身只有 show-labels（静态标签），没有悬停交互，所以这里按
  VTrendline 的真实坐标公式把容器切成 N 个热区，每个热区挂一个 v-tooltip。

  坐标公式（与 vuetify/lib/components/VSparkline/VTrendline.js:22 保持一致）：
    minX = padding
    maxX = width - padding
    x_i  = minX + i * (maxX - minX) / (n - 1)
  换算成百分比后，热区取相邻两点中垂线之间的范围，保证「鼠标离哪个点近就选中哪天」。
-->
<script setup>
import { computed, ref } from 'vue'
import { formatCount, formatDay } from '@/utils/format'
import { withAlpha } from '@/utils/studio'

const props = defineProps({
  /** 数据点：[{ date: 毫秒时间戳, value: 数值 }] */
  points: {
    type: Array,
    default: () => [],
  },
  /** 指标名称，显示在 tooltip 里，如「观看」 */
  label: {
    type: String,
    default: '',
  },
  /** 真实色值（SVG 呈现属性不支持 CSS 变量，所以由外部从 useTheme() 取好后传入） */
  color: {
    type: String,
    default: '#F44336',
  },
  height: {
    type: Number,
    default: 96,
  },
  /** viewBox 宽度，只决定绘图坐标系，实际渲染宽度由容器决定 */
  width: {
    type: Number,
    default: 900,
  },
  padding: {
    type: Number,
    default: 6,
  },
})

const hoverIndex = ref(-1)

const values = computed(() => props.points.map((p) => Number(p?.value) || 0))

const gradient = computed(() => [props.color, withAlpha(props.color, 0.15)])

/**
 * 每个数据点对应的热区（百分比），left/width 用于定位热区，center 用于画竖直指示线
 */
const hitZones = computed(() => {
  const n = props.points.length
  if (n === 0) return []
  if (n === 1) return [{ left: 0, width: 100, center: 50 }]

  const paddingRatio = props.padding / props.width
  const plotLeft = paddingRatio * 100
  const plotWidth = (1 - 2 * paddingRatio) * 100
  const centerOf = (i) => plotLeft + (i / (n - 1)) * plotWidth

  return props.points.map((_, i) => {
    const left = i === 0 ? 0 : (centerOf(i - 1) + centerOf(i)) / 2
    const right = i === n - 1 ? 100 : (centerOf(i) + centerOf(i + 1)) / 2
    return { left, width: right - left, center: centerOf(i) }
  })
})

function tooltipText(index) {
  const point = props.points[index]
  if (!point) return ''
  const value = `${formatCount(point.value)}${props.label ? ` ${props.label}` : ''}`
  return point.date ? `${formatDay(point.date)} · ${value}` : value
}
</script>

<template>
  <div class="trend-sparkline">
    <v-sparkline
      :model-value="values"
      :gradient="gradient"
      :color="color"
      type="trend"
      fill
      smooth
      auto-draw
      :line-width="2"
      :padding="padding"
      :height="height"
      :width="width"
      :min="0"
    />

    <div v-if="hitZones.length" class="trend-hover-layer">
      <!-- 悬停时在数据点位置画一条竖直指示线 -->
      <div
        v-if="hoverIndex >= 0"
        class="trend-guide"
        :style="{ left: `${hitZones[hoverIndex].center}%` }"
      />

      <v-tooltip
        v-for="(zone, index) in hitZones"
        :key="index"
        location="top"
        :open-delay="0"
        :text="tooltipText(index)"
      >
        <template #activator="{ props: activatorProps }">
          <div
            v-bind="activatorProps"
            class="trend-hit"
            :class="{ 'trend-hit--active': hoverIndex === index }"
            :style="{ left: `${zone.left}%`, width: `${zone.width}%` }"
            @mouseenter="hoverIndex = index"
            @mouseleave="hoverIndex = -1"
          />
        </template>
      </v-tooltip>
    </div>
  </div>
</template>

<style scoped>
.trend-sparkline {
  position: relative;
}

/* 命中层覆盖整个图表，本身不吃鼠标事件，只有热区吃 */
.trend-hover-layer {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.trend-hit {
  position: absolute;
  top: 0;
  bottom: 0;
  pointer-events: auto;
  transition: background-color 0.15s ease;
}

.trend-hit--active {
  background-color: rgba(var(--v-theme-primary), 0.08);
}

.trend-guide {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 1px;
  margin-left: -0.5px;
  background-color: rgb(var(--v-theme-primary));
  opacity: 0.4;
  pointer-events: none;
}
</style>
