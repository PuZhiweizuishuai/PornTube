import { createRouter, createWebHistory } from 'vue-router'
import HomeLayout from '../layout/IndexLayout.vue'
import { useUserStore } from '@/stores/userStore'
import { useWebInfoStore } from '@/stores/webInfoStore'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  // 换页回到顶部；浏览器前进/后退时恢复原滚动位置
  scrollBehavior(to, from, savedPosition) {
    if (to.hash) return { el: to.hash, behavior: 'smooth' }
    if (savedPosition) {
      restoreScrollPosition(to, savedPosition)
      return false
    }
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeLayout,
      children: [
        {
          path: '/',
          name: 'Index',
          component: () => import('@/views/home/IndexView.vue'),
          meta: { title: '首页' },
        },
        {
          path: '/video/:id',
          name: 'Vide',
          component: () => import('@/views/player/VideoView.vue'),
          meta: { title: '播放' }
        },
        {
          path: '/text/:id',
          name: 'Text',
          component: () => import('@/views/player/TextView.vue'),
          meta: { title: '文章' }
        },
        {
          path: '/v/:id',
          name: 'CategoryView',
          component: () => import('@/views/category/CategoryView.vue'),
          meta: { title: '分区' }
        },
        {
          path: '/vip',
          name: 'VipView',
          component: () => import('@/views/home/VipView.vue'),
          meta: { title: 'VIP' }
        },
        {
          path: '/notification',
          name: 'Notification',
          component: () => import('@/views/notification/NotificationView.vue'),
          meta: { 
            title: '消息通知',
            requireAuth: true
          }
        },
        // 个人设置（原「自定义频道」）已并入 /settings，旧地址按分区深链跳转
        {
          path: '/user/setting',
          redirect: { path: '/settings', query: { section: 'profile' } },
        },
        {
          path: '/search',
          name: 'SearchView',
          component: () => import('@/views/search/SearchIndex.vue'),
          meta: { title: '搜索' }
        },
        {
          path: '/user/:id',
          name: 'User',
          component: () => import('@/views/user/UserIndexView.vue'),
          meta: { title: '个人主页' }
        },
        {
          path: '/history',
          name: 'History',
          component: () => import('@/views/home/HistoryView.vue'),
          meta: { title: '播放历史' }
        },
        {
          path: '/hot',
          name: 'Hot',
          component: () => import('@/views/home/HotView.vue'),
          meta: { title: '时下流行' }
        },
        {
          path: '/subscribe',
          name: 'Subscribe',
          component: () => import('@/views/home/SubscribeView.vue'),
          meta: { title: '订阅' }
        },
        {
          path: '/playlist',
          name: 'Playlist',
          component: () => import('@/views/home/PlayListView.vue'),
          meta: { title: '我的收藏' }
        },
        {
          path: '/settings',
          name: 'Setting',
          component: () => import('@/views/home/SettingView.vue'),
          meta: { title: '设置',  requireAuth: true }
        },
        {
          path: '/appeal',
          name: 'AppealView',
          component: () => import('@/views/appeal/AppealView.vue'),
          meta: { title: '申诉',  requireAuth: true }
        },
        {
          path: '/about',
          name: 'About',
          component: () => import('@/views/home/AboutView.vue'),
          meta: { title: '关于' }
        },
        {
          path: '/articles',
          name: 'Articles',
          component: () => import('@/views/home/ArticlesView.vue'),
          meta: { title: '文章列表' }
        },
      ],
    },
    // 创作者中心
    {
      path: '/studio',
      name: 'Studio',
      component: () => import('@/layout/StudioLayout.vue'),
      meta: { title: '创作中心' },
      children: [
        {
          path: '/studio',
          name: 'StudioIndex',
          component: () => import('@/views/studio/HomeView.vue'),
          meta: {
            title: '创作中心',
            requireAuth: true
          }
        },
        {
          path: '/studio/upload',
          name: 'Upload',
          component: () => import('@/views/studio/UploadView.vue'),
          meta: {
            title: '投稿',
            requireAuth: true
          }
        },
        {
          path: '/studio/list',
          name: 'UploadList',
          component: () => import('@/views/studio/ListView.vue'),
          meta: {
            title: '稿件管理',
            requireAuth: true
          }
        },
        {
          path: '/studio/fans',
          name: 'FansView',
          component: () => import('@/views/studio/FansView.vue'),
          meta: {
            title: '粉丝管理',
            requireAuth: true
          }
        },
        {
          path: '/studio/data',
          name: 'DataView',
          component: () => import('@/views/studio/DataInfoView.vue'),
          meta: {
            title: '数据分析',
            requireAuth: true
          }
        },
      ] 
    },
    // 系统管理
    {
      path: '/admin',
      name: 'Admin',
      component: () => import('@/layout/AdminLayout.vue'),
      meta: { title: '管理中心' },
      children: [
        {
          path: '/admin',
          name: 'AdminIndex',
          component: () => import('@/views/admin/IndexView.vue'),
          meta: {
            title: '管理中心',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/ai',
          name: 'AIControl',
          component: () => import('@/views/admin/AIConfigView.vue'),
          meta: {
            title: 'AI模型管理',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/runinfo',
          name: 'RunInfoView',
          component: () => import('@/views/admin/RunInfoView.vue'),
          meta: {
            title: '运行状态',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/invitation',
          name: 'invitation',
          component: () => import('@/views/admin/InvitationView.vue'),
          meta: {
            title: '邀请码',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/websetting',
          name: 'WebSetting',
          component: () => import('@/views/admin/WebSetting.vue'),
          meta: {
            title: '网页设置',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/examine',
          name: 'Examine',
          component: () => import('@/views/admin/ExamineView.vue'),
          meta: {
            title: '内容审核',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/article/list',
          name: 'ArticleList',
          component: () => import('@/views/admin/ArticleListView.vue'),
          meta: {
            title: '稿件列表',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/file/list',
          name: 'FileList',
          component: () => import('@/views/admin/FileTableView.vue'),
          meta: {
            title: '文件列表',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/oss',
          name: 'OSSView',
          component: () => import('@/views/admin/OssView.vue'),
          meta: {
            title: '对象存储管理',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/opinion',
          name: 'OpinionView',
          component: () => import('@/views/admin/OpinionView.vue'),
          meta: {
            title: '举报与意见反馈',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/ads',
          name: 'ADSView',
          component: () => import('@/views/admin/AdvertisementView.vue'),
          meta: {
            title: '广告公告管理',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/userlist',
          name: 'UserList',
          component: () => import('@/views/admin/UserList.vue'),
          meta: {
            title: '用户列表',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/danmuku',
          name: 'Danmuku', 
          component: () => import('@/views/admin/DanmukuControlView.vue'),
          meta: {
            title: '弹幕管理',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/comment',
          name: 'Comment',
          component: () => import('@/views/admin/CommentControlView.vue'),
          meta: {
            title: '评论管理',
            requireAuth: true,
            requireAdmin: true
          }
        },
        {
          path: '/admin/category',
          name: 'Category',
          component: () => import('@/views/admin/CategoryController.vue'),
          meta: {
            title: '分类管理',
            requireAuth: true,
            requireAdmin: true
          }
        }
      ]
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: {
        title: '登录',
      },
    },
    {
      path: '/:pathMatch(.*)*',
      name: '404',
      component: () => import('@/views/404.vue'),
      meta: {
        title: '404'
      }
    }
  ],
})

/**
 * 恢复历史滚动位置（自己滚，不用 vue-router 的一次性恢复）。
 *
 * 列表页（首页 / 分区 / 时下流行 …）的卡片是组件 created() 里异步拉回来的，
 * 后退时页面过渡（mode="out-in"）会先移除旧页面、再挂上空的新页面，
 * 文档高度在这一瞬不够容纳目标位置，浏览器把滚动截断成 0，
 * 等内容长回来之后已经没人再滚回去了。
 *
 * 所以这里在过渡期间反复把位置「按住」：每轮都尽量往目标滚，直到真正到位
 * 且文档不再长高才收手。不能一到位就停 —— 旧页面还撑高着文档时滚到目标没有
 * 意义，它一移除位置就又没了。超时或用户自己操作（滚动 / 点击 / 按键）时交还控制权。
 *
 * @param {object} to 本次要进入的路由
 * @param {object} savedPosition 历史里记录的位置
 * @param {number} [timeout] 最长按住毫秒数
 */
let restoreTimer = null

// 至少按住这么久再考虑收手：out-in 过渡（离场 140ms + 入场 220ms）期间旧页面还撑高着文档
const MIN_HOLD_MS = 500

function stopHolding() {
  clearTimeout(restoreTimer)
  window.removeEventListener('wheel', onUserInput)
  window.removeEventListener('touchstart', onUserInput)
  window.removeEventListener('pointerdown', onUserInput)
  window.removeEventListener('keydown', onUserInput)
}

// 只认真人的操作：播放器 / 组件加载时也会派发合成事件，别被它们打断
function onUserInput(event) {
  if (event.isTrusted) stopHolding()
}

function restoreScrollPosition(to, savedPosition, timeout = 4000) {
  const left = savedPosition.left ?? savedPosition.x ?? 0
  const top = savedPosition.top ?? savedPosition.y ?? 0
  stopHolding()
  // 用户自己滚了就让给他，不要抢
  window.addEventListener('wheel', onUserInput, { passive: true })
  window.addEventListener('touchstart', onUserInput, { passive: true })
  window.addEventListener('pointerdown', onUserInput)
  window.addEventListener('keydown', onUserInput)

  let lastHeight = -1
  let quietTicks = 0
  const startedAt = performance.now()

  const tick = () => {
    // 用户又跳去别的页面了，别再去滚它
    if (router.currentRoute.value.fullPath !== to.fullPath) return stopHolding()
    const height = document.documentElement.scrollHeight
    quietTicks = height === lastHeight ? quietTicks + 1 : 0
    lastHeight = height
    // 内容还没长出来时只能滚到当前能到的最大位置，等它长够再滚到真正的目标
    const target = Math.min(top, Math.max(0, height - window.innerHeight))
    if (Math.abs(window.scrollY - target) > 1) {
      window.scrollTo(left, target)
    } else if (
      performance.now() - startedAt > MIN_HOLD_MS &&
      target === top &&
      quietTicks >= 3
    ) {
      return stopHolding() // 过渡已结束、内容不再变化且确实回到位了
    }
    if (performance.now() - startedAt > timeout) return stopHolding()
    restoreTimer = setTimeout(tick, 30)
  }

  tick()
}

// 导航守卫：权限判断一律以 Pinia 为唯一数据源，不再裸读 localStorage
router.beforeEach((to, from, next) => {
  let siteTitle = 'TikTube'
  let isLogin = false
  let isAdmin = false

  try {
    // Pinia 在 main.js 中最先安装，这里拿到的 store 已经是 rehydrate 之后的状态
    const webInfoStore = useWebInfoStore()
    const userStore = useUserStore()
    siteTitle = webInfoStore.getName || 'TikTube'
    isLogin = userStore.isLogin
    isAdmin = userStore.isAdmin
  } catch (e) {
    // Pinia 尚未就绪（极端情况）：放行非鉴权页面，鉴权页面退回登录
    console.warn('[router] 读取 store 失败，按未登录处理', e)
  }

  document.title = to.meta.title ? `${siteTitle} - ${to.meta.title}` : siteTitle

  if (!to.meta.requireAuth) {
    return next()
  }

  if (!isLogin) {
    // 未登录，重定向到登录页并带上回跳地址
    return next({ path: '/login', query: { redirect: to.fullPath } })
  }

  if (to.meta.requireAdmin && !isAdmin) {
    return next({ path: '/' })
  }

  next()
})

export default router
