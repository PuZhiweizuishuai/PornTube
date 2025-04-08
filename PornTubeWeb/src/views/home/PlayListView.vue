<template>
  <v-container>
    <NotLoginCard v-if="userInfo.userData == null"></NotLoginCard>
    <div v-else>
      <!-- 页面标题 -->
      <v-row align="center">
        <v-col>
          <h2>我的收藏</h2>
        </v-col>
        <v-col cols="auto">
          <v-chip color="primary" variant="tonal" size="small"> 共 {{ totalItems }} 个视频 </v-chip>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-divider />
        </v-col>
      </v-row>

      <!-- 加载状态 -->
      <v-row v-if="loading" justify="center" class="mt-8">
        <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
      </v-row>

      <!-- 空状态 -->
      <v-row v-else-if="videoList.length === 0" justify="center" class="mt-8">
        <v-col cols="12" sm="8" md="6" class="text-center">
          <v-icon size="64" color="grey">mdi-playlist-play</v-icon>
          <h2 class="text-h5 mt-4">暂无收藏视频</h2>
          <p class="text-body-1 text-medium-emphasis mt-2">快去收藏一些喜欢的视频吧</p>
        </v-col>
      </v-row>

      <!-- 视频列表 -->
      <v-row v-else no-gutters>
        <v-col v-for="item in videoList" :key="item.id" cols="12" sm="6" md="4" class="pa-2">
          <FavoriteCard
            :video="item.articleEntity"
            :favorite="item.favorite"
            @favorite-removed="handleFavoriteRemoved"
          />
        </v-col>
      </v-row>

      <!-- 分页器 -->
      <v-row v-if="videoList.length > 0" justify="center" class="mt-6">
        <v-pagination
          rounded="circle"
          v-model="page"
          :length="length"
          :total-visible="7"
          color="primary"
          @update:model-value="pageChange"
          class="rounded-pill"
        ></v-pagination>
      </v-row>
    </div>
  </v-container>
</template>
  
<script>
import NotLoginCard from '@/components/card/NotLoginCard.vue'
import FavoriteCard from '@/components/card/FavoriteCard.vue'
import { useUserStore } from '@/stores/userStore'

export default {
  components: {
    NotLoginCard,
    FavoriteCard,
  },
  data() {
    return {
      userInfo: useUserStore(),
      page: 1,
      size: 24,
      videoList: [],
      length: 0,
      loading: false,
      totalItems: 0,
    }
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = parseInt(this.$route.query.page)
    }
    this.getFavoriteList()
  },
  methods: {
    async getFavoriteList() {
      this.loading = true
      try {
        await this.httpGet(`/favorites/user/list?limit=${this.size}&page=${this.page}`, (json) => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.totalItems = json.data.totalCount || 0
        })
      } finally {
        this.loading = false
      }
    },
    pageChange(value) {
      this.page = value
      this.$router.push({ query: { page: value } })
      this.getFavoriteList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
    handleFavoriteRemoved(favoriteId) {
      this.videoList = this.videoList.filter((item) => item.favorite.id !== favoriteId)
      if (this.videoList.length === 0 && this.page > 1) {
        this.page--
        this.getFavoriteList()
      }
    },
  },
}
</script>
  
<style scoped>
.v-pagination {
  background: transparent;
}
</style>