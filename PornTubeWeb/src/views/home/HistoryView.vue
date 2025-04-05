<template>
  <v-container>
    <NotLoginCard v-if="userInfo.userData == null"></NotLoginCard>
    <div v-else>
      <v-row>
        <v-col>
          <h2>播放历史</h2>
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-divider />
        </v-col>
      </v-row>

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
          @input="pageChange"
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
      size: 20,
      length: 1,
      userInfo: useUserStore(),
    }
  },
  created() {
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
      this.init(this.type)
      this.$vuetify.goTo(0)
    },
  },
}
</script>

<style>
</style>