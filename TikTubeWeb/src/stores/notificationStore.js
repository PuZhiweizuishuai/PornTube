import { defineStore } from 'pinia'
import http from '@/api/index'
import { useUserStore } from '@/stores/userStore'

/**
 * 未读消息数。
 * 角标渲染在顶栏（AppHeader），标记已读的操作在消息页（NotificationView），
 * 两边各自持有本地副本会导致读完消息后角标不消失，所以放到 store 里共享。
 */
export const useNotificationStore = defineStore('notification', {
  state: () => ({
    unreadCount: 0,
  }),

  actions: {
    /** 以服务端为准刷新未读数 */
    async refreshUnreadCount() {
      if (!useUserStore().isLogin) {
        this.unreadCount = 0
        return
      }
      try {
        const json = await http.get('/notification/count')
        this.unreadCount = Math.max(0, Number(json?.data) || 0)
      } catch {
        // 拉取失败时保留原值，避免一次网络抖动就把角标清空
      }
    },

    /** 「全部已读」成功后直接归零，省一次请求 */
    clearUnreadCount() {
      this.unreadCount = 0
    },
  },
})
