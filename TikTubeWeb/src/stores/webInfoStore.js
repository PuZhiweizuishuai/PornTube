import { defineStore } from 'pinia'
import { STORAGE_KEYS, readJSON, writeJSON } from '@/utils/storage'

/** 兜底的站点配置，接口未返回时不至于渲染出 undefined */
const DEFAULT_WEB_INFO = {
  id: 1,
  name: 'TikTube',
  webDescribe: '视频网站',
  logoUrl: '/favicon.png',
  openNoVipLimit: 1,
  noVipViewCount: 5,
  openInvitationRegister: 0,
  openUploadVideoAddViewCount: 1,
  openExamine: 1,
  homeMaxVideoCount: 50,
  createTime: 0,
}

function readWebInfo() {
  const cached = readJSON(STORAGE_KEYS.WEB_INFO, null)
  if (!cached || typeof cached !== 'object') {
    return { ...DEFAULT_WEB_INFO }
  }
  // 与默认值合并：老版本缓存缺字段时不会渲染出 undefined
  return { ...DEFAULT_WEB_INFO, ...cached }
}

export const useWebInfoStore = defineStore('webInfo', {
  state: () => ({
    webInfo: readWebInfo(),
  }),

  getters: {
    getName: (state) => state.webInfo?.name || DEFAULT_WEB_INFO.name,
    getWebDescribe: (state) => state.webInfo?.webDescribe || DEFAULT_WEB_INFO.webDescribe,
    getLogoUrl: (state) => state.webInfo?.logoUrl || DEFAULT_WEB_INFO.logoUrl,
  },

  actions: {
    hydrate() {
      this.webInfo = readWebInfo()
    },

    /**
     * 更新站点信息。
     * 采用「增量合并」而不是整体替换，避免后端返回字段不全时把已有配置抹掉。
     */
    updateWebInfo(newInfo) {
      if (!newInfo || typeof newInfo !== 'object') return
      this.webInfo = { ...this.webInfo, ...newInfo }
      writeJSON(STORAGE_KEYS.WEB_INFO, this.webInfo)
    },
  },
})
