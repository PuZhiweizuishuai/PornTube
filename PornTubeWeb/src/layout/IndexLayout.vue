<template>
  <div>
    <v-app-bar color="red">
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-app-bar-title>PornTube</v-app-bar-title>
      <!-- <v-container>
        <v-row>
          <v-col cols="5"> -->
      <v-text-field
        density="compact"
        prepend-inner-icon="mdi-magnify"
        variant="solo"
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
          <v-btn v-bind="props" icon="mdi-video-plus"></v-btn>
        </template>
      </v-tooltip>
      <!-- 通知 -->
      <v-tooltip location="bottom" text="通知">
        <template v-slot:activator="{ props }">
          <v-btn v-bind="props" icon="mdi-bell"></v-btn>
        </template>
      </v-tooltip>
      <v-btn prepend-icon="mdi-account-circle" variant="outlined" @click="goToLoginPage()"
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
      <router-view />
    </v-main>
  </div>
</template>

<script>
export default {
  data: () => ({
    drawer: true,
    theme: 'light',
    items: [
      { icon: 'mdi-home', text: '首页', link: '/' },
      { icon: 'mdi-trending-up', text: '时下流行', link: '/hot' },
      { icon: 'mdi-youtube-subscription', text: '订阅', link: '/subscribe' },
      { icon: 'mdi-history', text: '历史记录', link: '/history' },
      { icon: 'mdi-playlist-play', text: '稍后再看', link: '/playlist' },
      { icon: 'mdi-cog', text: '设置', link: '/settings' },
      { icon: 'mdi-help-circle', text: '帮助', link: '/help' },
    ],
  }),
  methods: {
    onClick() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },
    goToLoginPage() {
      this.$router.push('/login')
    },
  },
  watch: {
    group() {
      this.drawer = false
    },
  },
}
</script>
