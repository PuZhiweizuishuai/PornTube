<template>
  <div>
    <v-navigation-drawer
      v-model="drawer"
      app
      clipped
    >
      <router-link :to="`/user/${this.$store.state.userInfo.id}`">
        <v-row justify="center" align="center">
          <v-col cols="12" style="text-align: center">
            <v-avatar size="62">
              <v-img :src="this.$store.state.userInfo.avatarUrl" />
            </v-avatar>
          </v-col>
        </v-row>
        <v-row justify="center" align="center">
          <v-col cols="12" style="text-align: center">
            {{ this.$store.state.userInfo.username }}
          </v-col>
        </v-row>
      </router-link>
      <router-link v-for="item in items" :key="item.text" :to="item.link">
        <v-list-item
          link
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
        style="width: 300px"
        class="ml-0 pl-4"
      >
        <span class="hidden-sm-and-down">{{ this.$store.state.webInfo.name }} Studio 创作中心</span>
      </v-toolbar-title>
      <v-text-field
        flat
        solo-inverted
        hide-details
        prepend-inner-icon="mdi-magnify"
        label="搜索"
        class="hidden-sm-and-down"
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
            <v-icon>mdi-video</v-icon>
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
      <v-btn
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
      </v-btn>
      <!-- 未登录 -->
      <v-btn
        v-if="this.$store.state.userInfo == null"
        outlined
        @click="goToLoginPage"
      >
        <v-icon left dark>mdi-head</v-icon> 登录
      </v-btn>

    </v-app-bar>

    <v-main>

      <router-view />

    </v-main>
  </div>
</template>

<script>
export default {
  data: () => ({
    drawer: true,
    items: [
      { icon: 'mdi-application', text: '信息中心', link: '/studio' },
      { icon: 'mdi-upload', text: '投稿', link: '/studio/upload' },
      { icon: 'mdi-comment', text: '评论', link: '/studio/comment' }
      //   { icon: 'mdi-history', text: '历史记录', link: '/history' },
      //   { icon: 'mdi-playlist-play', text: '稍后再看', link: '/playlist' }

    ]
  }),
  mounted() {

  },
  created() {
    this.$vuetify.theme.dark = this.$store.state.darkThemOpen
  },
  methods: {
    goToLoginPage() {
      this.$router.push('/login')
    },
    goToPublish() {
      if (this.$route.path === '/studio/upload') {
        return
      }
      this.$router.push('/studio/upload')
    },
    goToUserHome() {
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
