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
      <v-divider />
      <div v-if="CheckPower.checkPower(this.$store.state.userInfo) == 'admin'">
        <router-link v-for="item in adminList" :key="item.text" :to="item.link">
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
      </div>
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
  </div>
</template>

<script>
import Head from '@/layout/components/head.vue'
import CheckPower from '@/utils/check-power.vue'
export default {
  components: {
    Head
  },
  data: () => ({
    CheckPower,
    drawer: true,
    items: [
      { icon: 'mdi-application', text: '信息中心', link: '/studio' },
      { icon: 'mdi-filmstrip-box-multiple', text: '投稿列表', link: '/studio/list' },
      { icon: 'mdi-upload', text: '投稿', link: '/studio/upload' },
      { icon: 'mdi-database', text: '数据分析', link: '/studio/data' },
      { icon: 'mdi-account-multiple', text: '粉丝管理', link: '/studio/fans' }

      //   { icon: 'mdi-history', text: '历史记录', link: '/history' },
      //   { icon: 'mdi-playlist-play', text: '稍后再看', link: '/playlist' }
    ],
    adminList: [
      { icon: 'mdi-application', text: '邀请码', link: '/studio/admin/invitation' },
      { icon: 'mdi-video', text: '待审核', link: '/studio/admin/examine' },
      { icon: 'mdi-filmstrip-box-multiple', text: '投稿列表', link: '/studio/admin/article/list' },
      { icon: 'mdi-file', text: '文件列表', link: '/studio/admin/file/list' },
      { icon: 'mdi-account-multiple', text: '用户列表', link: '/studio/admin/userlist' },
      { icon: 'mdi-square-edit-outline', text: '网页设置', link: '/studio/admin/websetting' },
      { icon: 'mdi-playlist-edit', text: '分类管理', link: '/studio/admin/category' }
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
