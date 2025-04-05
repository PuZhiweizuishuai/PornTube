import { defineStore } from 'pinia'

export const useWebInfoStore = defineStore('webInfo', {
  state: () => ({
    webInfo: {
      name: 'PornTube',
      openNoVipLimit: 1,
      noVipViewCount: 5,
      logoUrl: '/favicon.png',
      openInvitationRegister: 0,
      webDescribe: '视频网站',
      openUploadVideoAddViewCount: 1,
      openExamine: 1,
      id: 1,
      createTime: 0,
      homeMaxVideoCount: 50,
    },
  }),
  getters: {
    // 可根据需要添加 getter 方法
    getName: (state) => state.webInfo.name,
    getWebDescribe: (state) => state.webInfo.webDescribe,
  },
  actions: {
    // 可根据需要添加 action 方法
    updateWebInfo(newInfo) {
      localStorage.setItem('webInfo', JSON.stringify(newInfo))
      this.webInfo = newInfo
    },
  },
})
