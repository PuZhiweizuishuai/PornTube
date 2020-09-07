import Vue from 'vue'
import VueRouter from 'vue-router'
import Index from '@/layout/index.vue'
import Home from '@/views/home/index.vue'
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
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login.vue'),
    meta: { title: '登录' }
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
    if (router.app.$options.store.state.userInfo != null && router.app.$options.store.state.userInfo.expireTime > date) {
      return next()
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
