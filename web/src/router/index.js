import Vue from 'vue'
import VueRouter from 'vue-router'
import Index from '@/layout/index.vue'
import Home from '@/views/home/index.vue'
import checkPower from '@/utils/check-power.vue'
Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'Index',
    component: Index,
    meta: { title: 'PornTube' },
    children: [
      {
        path: '/',
        name: 'Index',
        component: Home,
        meta: { title: 'PornTube' }
      },
      {
        path: '/hot',
        name: 'Hot',
        component: () => import('@/views/home/hot.vue'),
        meta: { title: 'PornTube 时下流行' }
      },
      {
        path: '/subscribe',
        name: 'Subscribe',
        component: () => import('@/views/home/subscribe.vue'),
        meta: { title: 'PornTube 订阅' }
      },
      {
        path: '/history',
        name: 'History',
        component: () => import('@/views/home/history.vue'),
        meta: { title: 'PornTube 播放历史' }
      },
      {
        path: '/playlist',
        name: 'Playlist',
        component: () => import('@/views/home/playlist.vue'),
        meta: { title: 'PornTube 稍后再看' }
      },
      {
        path: '/video/:id',
        name: 'Vide',
        component: () => import('@/views/video/video.vue'),
        meta: { title: '播放' }
      },
      {
        path: '/user/setting',
        name: 'UserSetting',
        component: () => import('@/views/user/setting.vue'),
        meta: {
          title: '个人设置',
          requireAuth: true
        }
      },
      {
        path: '/user/:id',
        name: 'User',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '个人主页' }
      }
    ]
  },
  {
    path: '/studio',
    name: 'Studio',
    component: () => import('@/layout/studio.vue'),
    meta: { title: '创作中心' },
    children: [
      {
        path: '/studio',
        name: 'StudioIndex',
        component: () => import('@/views/studio/index.vue'),
        meta: {
          title: '创作中心',
          requireAuth: true
        }
      },
      {
        path: '/studio/upload',
        name: 'Upload',
        component: () => import('@/views/studio/upload.vue'),
        meta: {
          title: '投稿',
          requireAuth: true
        }
      },
      {
        path: '/studio/comment',
        name: 'Comment',
        component: () => import('@/views/studio/comment.vue'),
        meta: {
          title: '评论',
          requireAuth: true
        }
      },
      {
        path: '/studio/admin/invitation',
        name: 'invitation',
        component: () => import('@/views/admin/invitation.vue'),
        meta: {
          title: '邀请码',
          requireAuth: true
        }
      },
      {
        path: '/studio/admin/examine',
        name: 'Examine',
        component: () => import('@/views/admin/examine.vue'),
        meta: {
          title: '审核视频',
          requireAuth: true
        }
      },
      {
        path: '/studio/admin/userlist',
        name: 'Examine',
        component: () => import('@/views/admin/user-list.vue'),
        meta: {
          title: '用户列表',
          requireAuth: true
        }
      },
      {
        path: '/studio/admin/websetting',
        name: 'Examine',
        component: () => import('@/views/admin/web-setting.vue'),
        meta: {
          title: '网页设置',
          requireAuth: true
        }
      },
      {
        path: '/studio/admin/category',
        name: 'Examine',
        component: () => import('@/views/admin/category.vue'),
        meta: {
          title: '分区设置',
          requireAuth: true
        }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login.vue'),
    meta: {
      title: '登录'
    }
  }

]

const router = new VueRouter({
  mode: 'history',
  routes
})

// 路由导航守卫
router.beforeEach((to, from, next) => {
  // const token = window.localStorage.getItem('user')
  // console.log(this.$store.state.webInfo.name)
  // router.app.$options.store
  // 获取网页信息
  if (router.app.$options.store.state.webInfo.name == null) {
    fetch(`/api/web/info`, {
      headers: {
        'Content-Type': 'application/json; charset=UTF-8'
      },
      method: 'GET',
      credentials: 'include'
    }).then(response => response.json())
      .then(json => {
        router.app.$options.store.state.webInfo = json.data
      })
      .catch(e => {
        return null
      })
  }
  // 路由发生变化修改页面title
  if (to.meta.title) {
    document.title = to.meta.title
  }
  if (to.meta.requireAuth) {
    const date = new Date().getTime()
    if (router.app.$options.store.state.userInfo != null) {
      if (router.app.$options.store.state.userInfo.expireTime > date) {
        // console.log(to.path)
      // TODO 登录后不能访问登录界面
        // if (to.path === '/login') {
        //   return next({ path: '/' })
        // }
        // TODO TEST
        // 如果VIP到期，更新VIP数据
        if (checkPower.updateUserRole(router.app.$options.store.state.userInfo)) {
          router.app.$options.store.state.userInfo.userRoleEntity.role = 'ROLE_USER'
          router.app.$options.store.commit('setUserInfo', router.app.$options.store.state.userInfo)
        }
        return next()
      } else {
        router.app.$options.store.commit('setUserInfo', null)
        return next({
          path: '/login',
          query: { redirect: to.fullPath }
        })
      }
    } else {
      return next({
        path: '/login',
        query: { redirect: to.fullPath }
      })
    }
  } else {
    return next()
  }
})

export default router
