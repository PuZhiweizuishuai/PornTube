<template>
  <v-menu>
    <template v-slot:activator="{ props }">
      <v-btn color="primary" v-bind="props" icon>
        <v-avatar size="32px" item>
          <v-img
            :src="userInfo.userData.avatarUrl"
            :alt="userInfo.userData.username"
            :title="userInfo.userData.username"
        /></v-avatar>
      </v-btn>
    </template>
    <v-list>
      <v-list-item v-for="(item, index) in headItem" :key="index" @click="headClick(item.id)">
        <v-list-item-title>
          <v-icon>{{ item.icon }}</v-icon>
          &nbsp;
          {{ item.text }}
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-menu>
</template>
  
  <script>
import { useUserStore } from '@/stores/userStore'
export default {
  name: 'AppBarHead',
  data() {
    return {
      userInfo: useUserStore(),
      headItem: [
        { icon: 'mdi-account', text: '个人主页', link: '/user/', id: 0 },
        { icon: 'mdi-cash', text: '付费会员', link: '/vip', id: 3 },
        { icon: 'mdi-application', text: '创作中心', link: '/studio', id: 1 },
        { icon: 'mdi-wrench', text: '自定义频道', link: '/user/setting', id: 4 },
        { icon: 'mdi-logout', text: '退出', link: '/logout', id: 2 },
      ],
    }
  },
  created() {},
  methods: {
    headClick(value) {
      if (value === 0) {
        // this.$router.push('/user/' + this.userInfo.id)
        location.replace('/user/' + this.userInfo.userData.id)
      } else if (value === 1) {
        if (this.$route.path === '/studio') {
          return
        }
        this.$router.push('/studio')
      } else if (value === 3) {
        this.$router.push('/vip')
      } else if (value === 4) {
        if (this.$route.path === '/user/setting') {
          return
        }
        this.$router.push('/user/setting')
        // location.replace('/user/setting')
      } else {
        this.logout()
      }
    },
    logout() {
      this.userInfo.deleteUserData()
      fetch(`/api/logout`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN'),
        },
        method: 'GET',
        credentials: 'include',
      })
        .then((response) => response.json())
        .then((json) => {
          if (json.status === 200) {
            this.userInfo.deleteUserData()
            if (this.$route.path === '/') {
              location.reload()
            } else {
              this.$router.push('/')
            }
          } else {
            //
          }
        })
        .catch((e) => {
          return null
        })
    },
  },
}
</script>
  
  <style>
</style>
  