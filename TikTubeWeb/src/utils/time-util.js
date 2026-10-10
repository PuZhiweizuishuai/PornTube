/**
 * 时间格式化工具
 * 由 utils/time-util.vue 迁移而来（纯 JS，不再挂在 .vue 里）。
 */
export function renderTime(date) {
  if (date === '' || date == null) {
    return ''
  }
  const da = new Date(date)
  return (
    da.getFullYear() +
    '-' +
    (da.getMonth() + 1) +
    '-' +
    da.getDate() +
    ' ' +
    da.getHours() +
    ':' +
    da.getMinutes() +
    ':' +
    da.getSeconds()
  )
}

export function formateTimeToChinese(date) {
  if (date === '' || date == null) {
    return ''
  }
  const da = new Date(date)
  return (
    da.getFullYear() +
    '年' +
    (da.getMonth() + 1) +
    '月' +
    da.getDate() +
    '日 ' +
    da.getHours() +
    '时' +
    da.getMinutes() +
    '分'
  )
}

export function formateTime(strat, end) {
  return formateTimeToChinese(strat) + ' ~ ' + formateTimeToChinese(end)
}

/** 距今多久：秒/分/时/天，超过 30 天回落到具体时间 */
export function timeToNowStrning(date) {
  let t = Math.trunc((new Date().getTime() - date) / 1000)
  if (t < 60) {
    return t + '秒前'
  }
  t = Math.trunc(t / 60)
  if (t < 60) {
    return t + '分钟前'
  }
  t = Math.trunc(t / 60)
  if (t < 24) {
    return t + '小时前'
  }
  t = Math.trunc(t / 24)
  if (t < 30) {
    return t + '天前'
  }
  return renderTime(date)
}

/** 秒 -> m:ss / h:mm:ss */
export function timeCover(data) {
  if (data == null || data === '' || data === 'null') {
    return ''
  }
  const second = parseInt(data % 60, 10)
  let minute = Math.floor(data / 60)
  if (minute >= 60) {
    const hour = Math.floor(minute / 60)
    minute = minute % 60
    return hour + ':' + minute + ':' + second
  }
  return minute + ':' + second
}

export default {
  timeToNowStrning,
  renderTime,
  formateTimeToChinese,
  formateTime,
  timeCover,
}
