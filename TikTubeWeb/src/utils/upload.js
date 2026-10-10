/**
 * 上传相关工具
 *
 * 背景：UploadText.vue:496-501 与 UploadVideo.vue:634-639 的封面上传逻辑逐行重复
 * （组 FormData → uploadFiles('/upload/photo') → 取 json.data[0] → 写 imageId/imgUrl → 提示）。
 * 这里收敛成一个 async 函数：成功返回 {imageId,imgUrl}，失败抛出带文案的 Error，
 * 调用方只需 await + try/catch，展示统一交给 useMessage。
 */
import http from '@/api/index'

/**
 * 上传封面图
 *
 * @param {File[]|File} files FilePond 的文件列表
 * @param {object} [options]
 * @param {string} [options.endpoint='/upload/photo']
 * @param {string} [options.field='file[]'] FormData 字段名
 * @param {number} [options.max=8] 最多允许的张数
 * @param {string} [options.emptyMessage] 未选择文件时的提示
 * @returns {Promise<{imageId:number, imgUrl:string, raw:object}>}
 */
export async function uploadCover(files, options = {}) {
  const {
    endpoint = '/upload/photo',
    field = 'file[]',
    max = 8,
    emptyMessage = '请先选择封面，然后上传！',
    timeout = 60000,
  } = options

  let list = Array.isArray(files) ? files : []
  if (!Array.isArray(files) && files) list = [files]
  // 过滤掉被 FilePond 标记删除的占位项
  list = list.filter((f) => f && (f.file || f instanceof File || f instanceof Blob))

  if (list.length === 0) throw new Error(emptyMessage)
  if (list.length > max) throw new Error(`最多只能上传 ${max} 张图片`)

  const formData = new FormData()
  list.forEach((item) => {
    const file = item && item.file ? item.file : item
    formData.append(field, file)
  })

  const json = await http.upload(endpoint, formData, { timeout })
  const first = json && json.data && json.data[0]
  if (json?.status !== 200 || !first) {
    throw new Error('上传失败，请重试！' + (json?.message || ''))
  }
  return { imageId: first.id, imgUrl: first.fileUrl, raw: first }
}

export default { uploadCover }
