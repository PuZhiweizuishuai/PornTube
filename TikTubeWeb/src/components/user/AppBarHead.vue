<template>
  <v-menu location="bottom end" offset="8">
    <template #activator="{ props }">
      <v-btn v-bind="props" icon variant="text" class="ml-1" :aria-label="userInfo.userData.username">
        <v-avatar size="32">
          <v-img
            :src="userInfo.userData.avatarUrl"
            :alt="userInfo.userData.username"
            cover
          />
        </v-avatar>
      </v-btn>
    </template>

    <v-card min-width="220" rounded="lg">
      <v-list density="comfortable" nav>
        <v-list-item
          v-for="item in headItem"
          :key="item.id"
          :prepend-icon="item.icon"
          :title="item.text"
          rounded="lg"
          @click="headClick(item.id)"
        />
      </v-list>
    </v-card>
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
        { icon: 'mdi-account', text: '个人主页', id: 0 },
        { icon: 'mdi-cash', text: '付费会员', id: 3 },
        { icon: 'mdi-application', text: '创作中心', id: 1 },
        { icon: 'mdi-wrench', text: '自定义频道', id: 4 },
        { icon: 'mdi-cog-outline', text: '设置', id: 5 },
        { icon: 'mdi-logout', text: '退出', id: 2 },
      ],
    }
  },
  methods: {
    headClick(value) {
      switch (value) {
        case 0:
          this.$router.push('/user/' + this.userInfo.userData.id)
          break
        case 1:
          if (this.$route.path.startsWith('/studio')) return
          this.$router.push('/studio')
          break
        case 3:
          this.$router.push('/vip')
          break
        case 4:
          if (this.$route.path === '/user/setting') return
          this.$router.push('/user/setting')
          break
        case 5:
          this.$router.push('/settings')
          break
        default:
          this.logout()
      }
    },
    logout() {
      this.userInfo.deleteUserData()
      this.httpPost(`/logout`, {}, () => {
        if (this.$route.path === '/') {
          location.reload()
        } else {
          this.$router.push('/')
        }
      })
    },
  },
}
</script>
