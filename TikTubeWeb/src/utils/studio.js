/**
 * 创作中心公共工具
 *
 * 收敛「稿件审核状态」「通知类型」「分区映射」这三组在创作中心各页面
 * （信息中心 / 数据分析 / 稿件管理 / 粉丝管理）都会用到的展示逻辑，
 * 避免每个页面各写一遍 getStatusText / buildCategoryMap。
 */

/** 稿件审核状态 → 文案 */
const EXAMINE_STATUS = {
  0: { text: '审核中', color: 'info', icon: 'mdi-clock-outline' },
  1: { text: '已通过', color: 'success', icon: 'mdi-check-circle' },
  2: { text: '未通过', color: 'error', icon: 'mdi-close-circle' },
}

/** 通知类型 → 文案，与后端 NotificationType 枚举一一对应 */
const NOTIFICATION_TYPE = {
  0: { text: '帖子评论', icon: 'mdi-message-reply-text', color: 'info' },
  1: { text: '评论回复', icon: 'mdi-comment-text', color: 'indigo' },
  2: { text: '帖子点赞', icon: 'mdi-thumb-up', color: 'pink' },
  3: { text: '评论点赞', icon: 'mdi-thumb-up-outline', color: 'pink-lighten-2' },
  10: { text: '系统通知', icon: 'mdi-bell', color: 'success' },
  11: { text: '举报 / 申诉', icon: 'mdi-flag', color: 'error' },
}

/** 稿件类型 → 文案，见后端 FileTypeEnum */
const ARTICLE_TYPE = {
  0: { text: '视频', icon: 'mdi-video', path: '/video/' },
  1: { text: '图片', icon: 'mdi-image', path: '/video/' },
  2: { text: '文章', icon: 'mdi-text-box', path: '/text/' },
}

function pick(map, key) {
  return map[key] || map[Object.keys(map)[0]]
}

export function getExamineStatus(status) {
  if (status === 0) return EXAMINE_STATUS[0]
  if (status === 1) return EXAMINE_STATUS[1]
  return EXAMINE_STATUS[2]
}

export function getExamineStatusText(status) {
  return getExamineStatus(status).text
}

export function getExamineStatusColor(status) {
  return getExamineStatus(status).color
}

export function getNotificationType(type) {
  return NOTIFICATION_TYPE[type] || { text: '消息', icon: 'mdi-bell-outline', color: 'grey' }
}

export function getArticleType(type) {
  return pick(ARTICLE_TYPE, type)
}

/** 稿件详情页地址 */
export function articlePath(item) {
  const { path } = getArticleType(item && item.type)
  return `${path}${item ? item.id : ''}`
}

/**
 * 把 /api/category/tree 返回的树拍平成 { 二级分区id: { parentName, childName } }
 * 与 ListView.vue 原来的实现保持一致，只是抽出来共用
 */
export function buildCategoryMap(categoryTree) {
  const map = {}
  if (!categoryTree) return map
  Object.values(categoryTree).forEach((parent) => {
    if (!parent || !Array.isArray(parent.children)) return
    parent.children.forEach((child) => {
      map[child.id] = {
        parentId: parent.id,
        parentName: parent.name,
        childName: child.name,
      }
    })
  })
  return map
}

/**
 * 环比变化百分比
 * @returns {{ rate: number|null, text: string, up: boolean }}
 *          rate 为 null 表示上一周期没有数据（页面上显示「新增」而不是百分比）
 */
export function compareRate(current, previous) {
  const now = Number(current) || 0
  const before = Number(previous) || 0
  if (before === 0) {
    return { rate: null, text: now === 0 ? '持平' : '新增', up: now > 0 }
  }
  const rate = Math.round(((now - before) / before) * 1000) / 10
  return { rate, text: `${rate > 0 ? '+' : ''}${rate}%`, up: rate >= 0 }
}

/** 趋势序列 → 指标数组 */
export function seriesOf(trend, key) {
  if (!Array.isArray(trend)) return []
  return trend.map((point) => Number(point?.[key]) || 0)
}

/**
 * 给颜色加透明度
 *
 * Vuetify 的 v-sparkline 把 gradient 直接写进 SVG 的 stop-color 属性，
 * 而 SVG 呈现属性不支持 CSS 变量（var(--v-theme-primary) 不会生效），
 * 所以这里从 vuetify 的 useTheme() 取到真实色值后再转 rgba。
 *
 * @param {string} color #RGB / #RRGGBB / rgb()
 * @param {number} alpha 0~1
 */
export function withAlpha(color, alpha = 1) {
  const value = String(color || '').trim()
  if (value.startsWith('#')) {
    let hex = value.slice(1)
    if (hex.length === 3) {
      hex = hex
        .split('')
        .map((c) => c + c)
        .join('')
    }
    if (hex.length >= 6) {
      const r = parseInt(hex.slice(0, 2), 16)
      const g = parseInt(hex.slice(2, 4), 16)
      const b = parseInt(hex.slice(4, 6), 16)
      return `rgba(${r}, ${g}, ${b}, ${alpha})`
    }
  }
  if (value.startsWith('rgb')) {
    const nums = value.match(/[\d.]+/g) || []
    if (nums.length >= 3) {
      return `rgba(${nums[0]}, ${nums[1]}, ${nums[2]}, ${alpha})`
    }
  }
  return value
}

/** 去掉 HTML 标签，用于消息预览（后端通知正文可能带链接） */
export function plainText(html) {
  if (!html) return ''
  return String(html)
    .replace(/<[^>]+>/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
}
