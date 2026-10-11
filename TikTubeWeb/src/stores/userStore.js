import { defineStore } from 'pinia'
import { STORAGE_KEYS, readJSON, removeKey, writeJSON } from '@/utils/storage'
import { checkPower, checkVip } from '@/utils/check-power'

/**
 * 从持久化镜像中恢复用户信息。
 * 抽成模块级函数，state() 初始化与 hydrate() 共用同一套校验规则，
 * 保证「首次创建即 rehydrate」，不会出现 store 与 localStorage 不一致的窗口期。
 */
function readUser() {
  const cached = readJSON(STORAGE_KEYS.USER, null)
  // 脏数据（非对象 / 没有 id）一律视为未登录，并顺手清理
  if (!cached || typeof cached !== 'object' || Array.isArray(cached) || !cached.id) {
    return null
  }
  return cached
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userData: readUser(),
  }),

  getters: {
    getUserData: (state) => state.userData,
    isLogin: (state) => !!state.userData?.id,
    /** 角色：'admin' | 'vip' | 'user' | undefined */
    role: (state) => checkPower(state.userData),
    isAdmin: (state) => checkPower(state.userData) === 'admin',
    isVip: (state) => checkVip(state.userData),
  },

  actions: {
    /**
     * 显式重新同步（退出登录后、或需要强制以 localStorage 为准时调用）。
     * 正常业务不需要调用 —— state 初始化时已经做过一次。
     */
    hydrate() {
      this.userData = readUser()
    },

    setUserData(data) {
      if (!data || typeof data !== 'object' || !data.id) {
        this.deleteUserData()
        return
      }
      this.userData = data
      writeJSON(STORAGE_KEYS.USER, data)
    },

    deleteUserData() {
      this.userData = null
      removeKey(STORAGE_KEYS.USER)
    },
  },
})
