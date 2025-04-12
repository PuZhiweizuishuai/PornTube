<template>
  <v-container>
    <NotLoginCard v-if="userInfo.userData == null"></NotLoginCard>
    <div v-else>
      <v-row align="center" class="mb-4">
        <v-col>
          <h2 class="text-h5 font-weight-bold">
            <v-icon icon="mdi-history" color="red" class="mr-2"></v-icon>
            播放历史
          </h2>
          <p class="text-body-2 text-grey">你之前看过的内容</p>
        </v-col>
      </v-row>

      <v-divider />
      <v-row>
        <v-col v-for="item in videoList" :key="item.id" cols="12">
          <HistoryCard :video="item" />
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-pagination
          rounded="circle"
          :total-visible="7"
          v-model="page"
          :length="length"
          @update:model-value="pageChange"
        />
      </v-row>
    </div>
  </v-container>
</template>

<script>
import NotLoginCard from '@/components/card/NotLoginCard.vue'
import HistoryCard from '@/components/card/HistoryCard.vue'
import { useUserStore } from '@/stores/userStore'
export default {
  components: {
    NotLoginCard,
    HistoryCard,
  },
  data() {
    return {
      videoList: [],
      page: 1,
      size: 10,
      length: 1,
      userInfo: useUserStore(),
    }
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = parseInt(this.$route.query.page)
    }
    if (this.userInfo.userData != null) {
      this.init()
    }
  },
  methods: {
    init() {
      this.httpGet(`/user/playrecording/list?page=${this.page}&limit=${this.size}`, (json) => {
        this.videoList = json.data.list
        this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    pageChange(page) {
      this.page = page
      this.$router.push({ query: { page: page } })
      this.init(this.type)
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
  },
}
</script>

<style>
</style>