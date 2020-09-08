<script>
function renderTime(date) {
  if (date === '' || date == null) {
    return ''
  }
  const da = new Date(date)
  return da.getFullYear() + '-' + (da.getMonth() + 1) + '-' + da.getDate() + ' ' + da.getHours() + ':' + da.getMinutes() + ':' + da.getSeconds()
}

function formateTimeToChinese(date) {
  if (date === '' || date == null) {
    return ''
  }
  const da = new Date(date)
  return da.getFullYear() + '年' + (da.getMonth() + 1) + '月' + da.getDate() + '日 ' + da.getHours() + '时' + da.getMinutes() + '分'
}

function formateTime(strat, end) {
  return formateTimeToChinese(strat) + ' ~ ' + formateTimeToChinese(end)
}

/**
  视频发布时间距当前时间间隔
 */
function timeToNowStrning(date) {
  const now = new Date().getTime()
  let t = now - date
  t = Math.trunc(t / 1000)
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

export default {
  timeToNowStrning,
  renderTime,
  formateTimeToChinese,
  formateTime
}
</script>
