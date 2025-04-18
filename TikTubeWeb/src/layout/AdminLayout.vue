<template>
  <div>
    <v-app-bar color="red">
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-app-bar-title>
        <span style="cursor: pointer" @click="goToHome()">{{ webInfo.name }} Admin</span>
      </v-app-bar-title>
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
          <v-btn v-bind="props" icon="mdi-video-plus" @click="goToPublish()"></v-btn>
        </template>
      </v-tooltip>
      <!-- 通知 -->
      <v-tooltip location="bottom" text="通知">
        <template v-slot:activator="{ props }">
          <v-btn v-bind="props" icon="mdi-bell"></v-btn>
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
        <router-link :to="`/user/${userInfo.userData.id}`">
          <v-row justify="center" align="center">
            <v-col cols="12" style="text-align: center; margin-top: 12px">
              <v-avatar size="62">
                <v-img :src="userInfo.userData.avatarUrl" />
              </v-avatar>
            </v-col>
          </v-row>
          <v-row justify="center" align="center">
            <v-col cols="12" style="text-align: center; color: #2196f3">
              {{ userInfo.userData.username }}
            </v-col>
          </v-row>
        </router-link>
        <v-list class="flex-grow-1 overflow-y-auto">
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
      { icon: 'mdi-home', text: '首页', link: '/admin' },
      { icon: 'mdi-application', text: '邀请码', link: '/admin/invitation' },
      { icon: 'mdi-video', text: '待审核', link: '/admin/examine' },
      { icon: 'mdi-filmstrip-box-multiple', text: '投稿列表', link: '/admin/article/list' },
      { icon: 'mdi-file', text: '文件列表', link: '/admin/file/list' },
      { icon: 'mdi-account-multiple', text: '用户列表', link: '/admin/userlist' },
      { icon: 'mdi-comment', text: '评论管理', link: '/admin/comment' },
      { icon: 'mdi-airplane', text: '弹幕管理', link: '/admin/danmuku' },
      { icon: 'mdi-square-edit-outline', text: '网页设置', link: '/admin/websetting' },
      { icon: 'mdi-playlist-edit', text: '分类管理', link: '/admin/category' },
    ],
    userInfo: useUserStore(),
  }),
  created() {
    this.webInfo = useWebInfoStore().webInfo
  },
  methods: {
    onClick() {
      this.theme = this.theme === 'light' ? 'dark' : 'light'
    },
    goToLoginPage() {
      this.$router.push('/login')
    },
    goToHome() {
      this.$router.push('/admin')
    },
    goToPublish() {
      this.$router.push('/studio/upload')
    },
  },
  watch: {
    group() {
      this.drawer = false
    },
  },
}
</script>
    