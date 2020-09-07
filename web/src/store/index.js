import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

export default new Vuex.Store({
  state: {
    webInfo: {
      // name: 'PornTube',
      // openNoVipLimit: 1,
      // noVipViewCount: 5,
      // logoUrl: '/logo.png',
      // openInvitationRegister: 1,
      // describe: '一个牛逼的视频网站',
      // openUploadVideoAddViewCount: 1,
      // openExamine: 1,
      // id: 1,
      // createTime: 0
    },
    darkThemOpen: false,
    userInfo: (localStorage.getItem('user') != null &&
    localStorage.getItem('user') !== 'undefined' &&
    localStorage.getItem('user') !== '')
      ? JSON.parse(localStorage.getItem('user')) : null,
    uploadVideoDateTemp: {}

  },
  mutations: {
    setUserInfo(state, userInfo) {
      // 将传递的数据先保存到 localStorage 中
      localStorage.setItem('user', JSON.stringify(userInfo))
      // 之后才是修改state中的状态
      state.userInfo = userInfo
    },
    setWebInfo(state, webInfo) {
      localStorage.setItem('webInfo', JSON.stringify(webInfo))
      state.userInfo = webInfo
    }
  },
  actions: {
  },
  modules: {
  }
})
