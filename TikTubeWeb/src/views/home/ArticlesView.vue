<template>
  <v-container fluid>
    <v-row align="center" class="mb-2">
      <v-col>
        <h2 class="text-h5 font-weight-bold">
          <v-icon icon="mdi-text-box-multiple-outline" color="red" class="me-2" />
          文章列表
        </h2>
        <p class="text-body-2 text-medium-emphasis mt-1">查看最新文章投稿</p>
      </v-col>
    </v-row>
    <v-row>
      <v-divider />
    </v-row>

    <!-- 文章卡片 -->
    <v-row>
      <v-col
        cols="12"
        sm="6"
        md="4"
        lg="3"
        xxl="2"
        v-for="item in textList"
        :key="item.id"
      >
        <ArticleCard :text="item" />
      </v-col>
    </v-row>
    <v-container>
      <v-row justify="center">
        <Pagination
          rounded="circle"
          :total-visible="7"
          v-model="page"
          :length="length"
          color="red"
          @update:model-value="pageChange"
        />
      </v-row>
    </v-container>
  </v-container>
</template>

<script>
import ArticleCard from '@/components/card/ArticleCard.vue'
import Pagination from '@/components/common/Pagination.vue'

export default {
  components: {
    ArticleCard,
    Pagination
  },
  data() {
    return {
      textList: [],
      page: 1,
      size: 24,
      length: 0,
    }
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = parseInt(this.$route.query.page)
    }
    this.getTextList()
  },
  methods: {
    getTextList() {
      this.httpGet(`/article/home/list?page=${this.page}&limit=${this.size}&type=2`, (json) => {
        this.textList = json.data.list
        this.page = json.data.currPage
        this.length = json.data.totalPage
      })
    },
    pageChange(value) {
      this.page = value
      this.$router.push({ query: { page: this.page } })
      this.getTextList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
  },
}
</script>

<style scoped>
a {
  text-decoration: none;
}
</style>