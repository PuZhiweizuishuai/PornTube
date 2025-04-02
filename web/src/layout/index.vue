<template>
  <div>
    <v-navigation-drawer
      v-model="drawer"
      app
      clipped
    >
      <router-link v-for="item in items" :key="item.text" :to="item.link">
        <v-list-item
          linkz
        >
          <v-list-item-action>
            <v-icon>{{ item.icon }}</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>
              {{ item.text }}
            </v-list-item-title>
          </v-list-item-content>

        </v-list-item>
      </router-link>
    </v-navigation-drawer>

    <v-app-bar
      :clipped-left="$vuetify.breakpoint.lgAndUp"
      app
      color="red"
      dark
    >
      <v-app-bar-nav-icon @click.stop="drawer = !drawer" />
      <v-toolbar-title
        class="hidden-sm-and-down ml-0 pl-4"
        style="width: 300px"
      >
        <span style="cursor:pointer" @click="goToHome()">{{ this.$store.state.webInfo.name }}</span>
      </v-toolbar-title>
      <v-text-field
        flat
        solo-inverted
        hide-details
        prepend-inner-icon="mdi-magnify"
        label="搜索"
      />
      <v-spacer />
      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            v-bind="attrs"
            v-on="on"
            @click="goToPublish"
          >
            <v-icon>mdi-video-plus</v-icon>
          </v-btn>
        </template>
        <span>发布</span>
      </v-tooltip>
      <v-tooltip bottom>
        <template v-slot:activator="{ on, attrs }">
          <v-btn
            icon
            v-bind="attrs"
            v-on="on"
          >
            <v-icon>mdi-bell</v-icon>
          </v-btn>
        </template>
        <span>通知</span>
      </v-tooltip>
      <!--  登陆后 -->
      <!-- <v-btn
        v-if="this.$store.state.userInfo"
        icon
        large
        @click="goToUserHome"
      >
        <v-avatar
          size="32px"
          item
        >
          <v-img
            :src="this.$store.state.userInfo.avatarUrl"
            :alt="this.$store.state.userInfo.username"
            :title="this.$store.state.userInfo.username"
          /></v-avatar>
      </v-btn> -->
      <Head v-if="this.$store.state.userInfo" />
      <!-- 未登录 -->
      <v-btn
        v-if="this.$store.state.userInfo == null"
        outlined
        @click="goToLoginPage"
      >
        <v-icon left dark>mdi-account</v-icon> 登录
      </v-btn>

    </v-app-bar>

    <v-main>

      <router-view />

    </v-main>
    <v-footer

      class="font-weight-medium"
    >
      <v-col
        class="text-center"
        cols="12"
      >
        <a href="https://www.buguagaoshu.com" target="_blank"><strong>不挂高数</strong></a> ©2020 - {{ new Date().getFullYear() }}  Created by
        <a href="https://vuetifyjs.com/" target="_blank"><strong>Vuetify</strong></a>
      </v-col>
    </v-footer>
  </div>
</template>

<script>
import Head from '@/layout/components/head.vue'
export default {
  // TODO 增加分类页
  components: {
    Head
  },
  data: () => ({
    userInfo: {},
    drawer: false,
    items: [
      { icon: 'mdi-home', text: '首页', link: '/' },
      { icon: 'mdi-trending-up', text: '时下流行', link: '/hot' },
      { icon: 'mdi-youtube-subscription', text: '订阅', link: '/subscribe' },
      { icon: 'mdi-history', text: '历史记录', link: '/history' },
      { icon: 'mdi-playlist-play', text: '稍后再看', link: '/playlist' }

    ],
    headItem: [
      { icon: 'mdi-head', text: '个人主页', link: '/user/', id: 0 },
      { icon: 'mdi-wrench', text: '创作中心', link: '/studio', id: 1 },
      { icon: 'mdi-logout', text: '退出', link: '/logout', id: 2 }
    ]
  }),
  mounted() {

  },
  created() {
    this.userInfo = this.$store.state.userInfo
    this.$vuetify.theme.dark = this.$store.state.darkThemOpen
  },
  methods: {
    headClick(value) {
      if (value === 0) {
        this.$router.push('/user/' + this.userInfo.id)
      } else if (value === 1) {
        this.$router.push('/studio')
      } else {
        this.logout()
      }
    },
    logout() {
      fetch(`/api/logout`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          if (json.status === 200) {
            this.$store.commit('setUserInfo', null)
            if (this.$route.path === '/') {
              location.reload()
            } else {
              this.$router.push('/')
            }
          } else {
            //
          }
        })
        .catch(e => {
          return null
        })
    },
    goToLoginPage() {
      this.$router.push('/login')
    },
    goToPublish() {
      this.$router.push('/studio/upload')
    },
    goToHome() {
      if (this.$route.path === '/') {
        return
      }
      this.$router.push('/')
    },
    goToUserHome() {
      if (this.$route.path === '/user/' + this.$store.state.userInfo.id) {
        return
      }
      this.$router.push('/user/' + this.$store.state.userInfo.id)
    }
  }
}
</script>

<style scoped>
a {
  text-decoration: none;
}
</style>
