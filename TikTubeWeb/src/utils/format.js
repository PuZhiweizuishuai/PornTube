/**
 * 通用格式化工具
 *
 * 收敛各处重复的 formatDate / formatNumber / getUaInfo / renderTime 方法
 * （原先在 CommentControlView、DanmukuControlView、FileTableView、UserList、
 *   FavoriteCard、TextInfoCard、HistoryCard、UserLoginLog 各写一遍）
 */
import { UAParser } from 'ua-parser-js'

/** 默认的日期时间格式（24 小时制，zh-CN） */
const DEFAULT_DATE_TIME_OPTIONS = {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: false,
}

/**
 * 时间戳格式化为本地日期时间字符串
 * @param {number|string|Date} timestamp
 * @param {Intl.DateTimeFormatOptions} [options]
 * @param {string} [placeholder='-'] 空值占位
 */
export function formatDate(timestamp, options, placeholder = '-') {
  if (timestamp === null || timestamp === undefined || timestamp === '') return placeholder
  const date = timestamp instanceof Date ? timestamp : new Date(timestamp)
  if (Number.isNaN(date.getTime())) return placeholder
  return new Intl.DateTimeFormat('zh-CN', options || DEFAULT_DATE_TIME_OPTIONS).format(date)
}

/** 只格式化日期（年-月-日） */
export function formatDay(timestamp, placeholder = '-') {
  return formatDate(
    timestamp,
    { year: 'numeric', month: '2-digit', day: '2-digit', hour12: false },
    placeholder
  )
}

/**
 * 适合 <input type="datetime-local"> 的 yyyy-MM-ddTHH:mm
 * @param {number|string} timestamp
 */
export function formatDateTimeInput(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  if (Number.isNaN(date.getTime())) return ''
  const pad = (n) => String(n).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`
}

/**
 * 数字转字符串（补齐原实现缺 null/undefined 的坑）
 * @param {number|string} num
 */
export function formatNumber(num) {
  if (num === null || num === undefined || num === '') return '0'
  return String(num)
}

/**
 * 大数字转「万 / 亿」可读形式，用于播放量等展示
 * @param {number} num
 */
export function formatCount(num) {
  const n = Number(num)
  if (!Number.isFinite(n)) return '0'
  if (n < 10000) return String(n)
  if (n < 100000000) return `${(n / 10000).toFixed(1).replace(/\.0$/, '')}万`
  return `${(n / 100000000).toFixed(1).replace(/\.0$/, '')}亿`
}

/**
 * UA 解析：返回「操作系统 + 浏览器」简述
 * @param {string} uastr
 */
export function getUaInfo(uastr) {
  if (!uastr) return '未知设备'
  try {
    const ua = new UAParser(uastr)
    return `操作系统：${ua.getOS().name || '未知'} ${ua.getOS().version || ''} 浏览器：${
      ua.getBrowser().name || '未知'
    } ${ua.getBrowser().major || ''}`
  } catch (e) {
    return '未知设备'
  }
}

/** 极简版 UA（只返回 "OS Browser"），评论卡片用 */
export function getUaBrief(uastr) {
  if (!uastr) return '未知设备'
  try {
    const ua = new UAParser(uastr)
    return `${ua.getOS().name || '未知'} ${ua.getBrowser().name || '未知'}`
  } catch (e) {
    return '未知设备'
  }
}

/**
 * 时长（秒）转 mm:ss / hh:mm:ss
 * @param {number} seconds
 */
export function formatDuration(seconds) {
  const total = Math.max(0, Math.floor(Number(seconds) || 0))
  const pad = (n) => String(n).padStart(2, '0')
  const h = Math.floor(total / 3600)
  const m = Math.floor((total % 3600) / 60)
  const s = total % 60
  return h > 0 ? `${h}:${pad(m)}:${pad(s)}` : `${pad(m)}:${pad(s)}`
}

export default {
  formatDate,
  formatDay,
  formatDateTimeInput,
  formatNumber,
  formatCount,
  formatDuration,
  getUaInfo,
  getUaBrief,
}
