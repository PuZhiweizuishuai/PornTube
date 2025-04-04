import { createRouter, createWebHistory } from 'vue-router'
import HomeLayout from '../layout/IndexLayout.vue'

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
      ],
    },
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/LoginView.vue'),
      meta: {
        title: '登录',
      },
    },
  ],
})

export default router
