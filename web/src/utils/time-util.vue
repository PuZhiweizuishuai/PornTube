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

export default {
  renderTime,
  formateTimeToChinese,
  formateTime
}
</script>
