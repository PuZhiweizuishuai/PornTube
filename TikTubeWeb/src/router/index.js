import { createRouter, createWebHistory } from 'vue-router'
import HomeLayout from '../layout/IndexLayout.vue'
import checkPower from '@/utils/check-power.vue'
// 默认网站名称
const DEFAULT_TITLE = 'TikTube'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
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
          path: '/v/:id',
          name: 'Category',
          component: () => import('@/views/category/CategoryView.vue'),
          meta: { title: '分区' }
        },
        {
          path: '/user/setting',
          name: 'UserSetting',
          component: () => import('@/views/user/SettingView.vue'),
          meta: {
            title: '个人设置',
            requireAuth: true
          }
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
          meta: { title: '稍后再看' }
        },
        {
          path: '/settings',
          name: 'Setting',
          component: () => import('@/views/home/SettingView.vue'),
          meta: { title: '设置' }
        },
        {
          path: '/about',
          name: 'About',
          component: () => import('@/views/home/AboutView.vue'),
          meta: { title: '关于' }
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

// 导航守卫，在路由跳转前进行权限判断
router.beforeEach((to, from, next) => {
  // 尝试从localStorage获取网站信息作为标题
  let siteTitle = DEFAULT_TITLE
  try {
    // App.vue中会将网站信息存储到localStorage，如果有则使用它
    const webInfoStr = localStorage.getItem('webInfo')
    if (webInfoStr) {
      const webInfo = JSON.parse(webInfoStr)
      if (webInfo && webInfo.name) {
        siteTitle = webInfo.name
      }
    }
  } catch (e) {
    console.error('获取网站标题失败', e)
  }

  // 设置页面标题
  document.title = to.meta.title ? `${siteTitle} - ${to.meta.title}` : siteTitle
  
  // 判断页面是否需要登录权限
  if (to.meta.requireAuth) {
    // 判断用户是否已登录，通过localStorage直接判断
    const userData = JSON.parse(localStorage.getItem('user'))
    
    if (userData && userData !== 'undefined' && userData !== '') {
      // 已登录，允许访问
      if (to.meta.requireAdmin) {
        if (checkPower.checkPower(userData) === "admin") {
          next()
        } else {
          next({
            path: '/',
          })
        }
      } else {
        next()
      }
    } else {
      // 未登录，重定向到登录页面
      next({
        path: '/login',
        // 将原本要访问的页面路径作为查询参数传递，便于登录后重定向回去
        query: { redirect: to.fullPath }
      })
    }
  } else {
    // 不需要登录权限的页面，直接访问
    next()
  }
})

export default router
