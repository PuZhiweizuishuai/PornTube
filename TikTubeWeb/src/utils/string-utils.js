/**
 * 字符串 / 清晰度 相关工具
 * 由 utils/string-utils.vue 迁移而来：这里只放纯 JS，不再用 .vue 承载逻辑。
 */
/** 像素数量 -> 清晰度标签 */
export function clarityDisplay(pixelsNumber) {
  if (pixelsNumber == null || pixelsNumber === '') {
    return undefined
  }
  if (pixelsNumber >= 8294400) {
    return '4K'
  }
  if (pixelsNumber >= 2073600) {
    return 'HD'
  }
  return 'SD'
}

/** 后端 @Valid 校验失败时，把 data 中的字段错误拼成一段文案 */
export function dataErrorMessage(data) {
  if (data == null) {
    return '数据校验错误，请检查数据后重新提交'
  }
  let result = ''
  for (const key in data) {
    result += data[key] + '\n'
  }
  return result
}

export default {
  clarityDisplay,
  dataErrorMessage,
}
