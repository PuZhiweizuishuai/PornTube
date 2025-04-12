<template>
  <div>
    <v-app-bar color="red">
      <v-app-bar-nav-icon @click.stop="drawer = !drawer"></v-app-bar-nav-icon>
      <v-app-bar-title>
        <span style="cursor: pointer" @click="goToHome()">{{ webInfo.name }} Studio</span>
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
        <v-list v-if="CheckPower.checkPower(userInfo.userData) == 'admin'">
          <v-list-item
            :key="'admin'"
            :prepend-icon="'mdi-security'"
            :title="'管理中心'"
            :value="'/admin'"
            :href="'/admin'"
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
import CheckPower from '@/utils/check-power.vue'
export default {
  components: {
    AppBarHead,
  },
  data: () => ({
    CheckPower,
    drawer: true,
    theme: 'light',
    webInfo: {},
    items: [
      { icon: 'mdi-application', text: '信息中心', link: '/studio' },
      { icon: 'mdi-filmstrip-box-multiple', text: '投稿列表', link: '/studio/list' },
      { icon: 'mdi-upload', text: '投稿', link: '/studio/upload' },
      { icon: 'mdi-database', text: '数据分析', link: '/studio/data' },
      { icon: 'mdi-account-multiple', text: '粉丝管理', link: '/studio/fans' },
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
      this.$router.push('/studio')
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
  