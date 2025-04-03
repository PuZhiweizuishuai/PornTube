<template>
  <v-container fluid>
    <!-- 分区 -->
    <v-row>
      <v-col>
        <v-menu v-for="item in categoryList" :key="item.id" open-on-hover offset-y>
          <template v-slot:activator="{ props }">
            <v-btn variant="text" color="primary" v-bind="props" @click="setCategory(item)">
              {{ item.name }}
            </v-btn>
          </template>
          <v-list>
            <v-list-item
              v-for="c in item.children"
              :key="c.id"
              :title="c.name"
              @click="setCategory(c)"
            >
            </v-list-item>
          </v-list>
        </v-menu>
      </v-col>
    </v-row>
    <v-row>
      <v-divider />
    </v-row>
    <v-col />
    <!-- 视频卡片 -->
    <v-row no-gutters>
      <v-col v-for="item in videoList" :key="item.id">
        <VideoCared :video="item" />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-pagination v-model="page" :length="length" @input="pageChange" />
    </v-row>
    <v-col> &nbsp; </v-col>
  </v-container>
</template>

<script>
import VideoCared from '@/components/card/VideoCard.vue'

export default {
  components: {
    VideoCared,
  },
  data() {
    return {
      videoList: [],
      page: 1,
      size: 24,
      length: 0,
      categoryList: [],
    }
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = this.$route.query.page
    }
    this.getCategory()
    this.getVideoList()
  },
  methods: {
    getCategory() {
      this.httpGet(`/category/tree`, (json) => {
        this.categoryList = json.data
      })
    },
    setCategory(value) {
      this.$router.push(`/v/${value.id}`)
    },
    getVideoList() {
      this.httpGet(`/article/home/list?page=${this.page}&limit=${this.size}`, (json) => {
        this.videoList = json.data.list
        this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    pageChange(value) {
      this.page = value
      this.$router.push({ query: { page: this.page } })
      this.getVideoList()
      this.$vuetify.goTo(0)
    },
  },
}
</script>

<style>
a {
  text-decoration: none;
}
</style>
