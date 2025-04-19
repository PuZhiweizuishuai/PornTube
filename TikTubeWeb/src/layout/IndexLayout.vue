<template>
  <div>
    <v-app-bar color="red">
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-app-bar-title>
        <span style="cursor: pointer" @click="goToHome()">{{ webInfo.name }}</span>
      </v-app-bar-title>
      <!-- <v-container>
        <v-row>
          <v-col cols="5"> -->
      <v-text-field
        density="comfortable"
        prepend-inner-icon="mdi-magnify"
        variant="solo-filled"
        flat
        hide-details
        single-line
        label="搜索"
        color="#F44336"
      ></v-text-field>
      <!-- </v-col>
        </v-row>
      </v-container> -->
      <!-- 占位分割 -->
      <v-spacer />
      <!-- 发布 -->
      <v-tooltip location="bottom" text="发布">
        <template v-slot:activator="{ props }">
          <v-btn v-bind="props" icon="mdi-video-plus" @click="goToPublish()"></v-btn>
        </template>
      </v-tooltip>
      <!-- 通知 -->
      <v-tooltip location="bottom" text="通知">
        <template v-slot:activator="{ props }">
          <v-btn v-if="notificationCount > 0" v-bind="props" stacked @click="goToNotification()">
            <v-badge color="error" :content="notificationCount">
              <v-icon>mdi-bell</v-icon>
            </v-badge>
          </v-btn>
          <v-btn v-else v-bind="props" icon="mdi-bell" @click="goToNotification()"></v-btn>
        </template>
      </v-tooltip>
      <AppBarHead v-if="userInfo.userData != null"></AppBarHead>
      <v-btn
        v-if="userInfo.userData == null"
        class="mr-2"
        prepend-icon="mdi-account-circle"
        variant="outlined"
        @click="goToLoginPage()"
        >登录</v-btn
      >
    </v-app-bar>
    <!-- temporary -->
    <v-navigation-drawer v-model="drawer">
      <div class="d-flex flex-column h-100">
        <v-list>
          <v-list-item
            v-for="item in items"
            :key="item.text"
            :prepend-icon="item.icon"
            :title="item.text"
            :value="item.link"
            :href="item.link"
            rounded="xl"
          />
        </v-list>

        <v-spacer></v-spacer>

        <v-footer class="flex-grow-0">
          <v-col class="text-center" cols="12">
            <a href="https://www.buguagaoshu.com" target="_blank"><strong>不挂高数</strong></a>
            ©2020 - {{ new Date().getFullYear() }} Created by
            <a href="https://vuetifyjs.com/" target="_blank"><strong>Vuetify</strong></a>
          </v-col>
        </v-footer>
      </div>
    </v-navigation-drawer>

    <v-main>
      <!--  fluid-->
      <router-view v-slot="{ Component }">
        <component :is="Component" ref="childRef" />
      </router-view>
    </v-main>
  </div>
</template>

<script>
import { useWebInfoStore } from '@/stores/webInfoStore'
import { useUserStore } from '@/stores/userStore'
import AppBarHead from '@/components/user/AppBarHead.vue'
export default {
  components: {
    AppBarHead,
  },
  data: () => ({
    drawer: true,
    theme: 'light',
    webInfo: {},
    items: [
      { icon: 'mdi-home', text: '首页', link: '/' },
      { icon: 'mdi-trending-up', text: '时下流行', link: '/hot' },
      { icon: 'mdi-youtube-subscription', text: '订阅', link: '/subscribe' },
      { icon: 'mdi-history', text: '历史记录', link: '/history' },
      { icon: 'mdi-heart', text: '我的收藏', link: '/playlist' },
      // { icon: 'mdi-cog', text: '设置', link: '/settings' },
      { icon: 'mdi-help-circle', text: '关于', link: '/about' },
    ],
    userInfo: useUserStore(),
    notificationCount: 0,
  }),
  created() {
    this.webInfo = useWebInfoStore().webInfo
    this.getNotificationCount()
  },
  methods: {
    onClick() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },
    goToLoginPage() {
      this.$router.push('/login')
    },
    goToHome() {
      this.$router.push('/')
    },
    goToPublish() {
      this.$router.push('/studio/upload')
    },
    goToNotification() {
      this.$router.push('/notification')
    },
    getNotificationCount() {
      if (!this.userInfo.userData) {
        return
      }
      this.httpGet('/notification/count', (json) => {
        this.notificationCount = json.data
      })
    },
  },
  watch: {
    group() {
      this.drawer = false
    },
  },
}
</script>
