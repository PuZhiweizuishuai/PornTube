<template>
  <v-container fluid class="fill-height pa-6">
    <!-- 当前分区信息 -->
    <v-card flat class="mb-4 pa-3 rounded-lg">
      <v-row align="center">
        <v-col cols="12" sm="8">
          <div class="d-flex align-center">
            <v-btn
              icon="mdi-arrow-left-thick"
              color="primary"
              variant="outlined"
              class="mr-2"
              density="comfortable"
              @click="back()"
            ></v-btn>
            <v-btn
              class="font-weight-bold text-h6 text-sm-h5 mr-1"
              variant="text"
              color="primary"
              @click="setCategoryFather()"
            >
              {{ categoryList.name }}
            </v-btn>
            <template v-if="showChileName">
              <v-icon color="grey">mdi-chevron-right</v-icon>
              <v-btn
                class="font-weight-bold text-h6 text-sm-h5 ml-1"
                variant="text"
                color="primary"
              >
                {{ categoryChiled.name }}
              </v-btn>
            </template>
          </div>
        </v-col>
      </v-row>

      <v-row class="mt-2">
        <v-col cols="12">
          <v-btn
            v-for="item in categoryList.children"
            :key="item.id"
            variant="text"
            color="primary"
            @click="setCategory(item)"
          >
            {{ item.name }}
          </v-btn>
        </v-col>
      </v-row>
    </v-card>

    <v-divider class="mb-6"></v-divider>

    <!-- 视频卡片 -->
    <v-row v-if="videoList.length > 0" no-gutters>
      <v-col v-for="item in videoList" :key="item.id">
        <VideoCared :video="item" />
      </v-col>
    </v-row>
    <v-row v-else class="mt-4">
      <v-col cols="12" class="text-center">
        <v-icon size="x-large" color="grey" class="mb-2">mdi-video-off</v-icon>
        <p class="text-body-1">暂无视频内容</p>
      </v-col>
    </v-row>

    <!-- 分页部分 -->
    <v-container v-if="length > 1">
      <v-row justify="center">
        <v-pagination
          v-model="page"
          :length="length"
          :total-visible="7"
          rounded
          @update:model-value="pageChange"
        ></v-pagination>
      </v-row>
    </v-container>
  </v-container>
</template>
  
<script>
import VideoCared from '@/components/card/VideoCard.vue'

export default {
  name: 'CategoryView',
  components: {
    VideoCared,
  },
  data() {
    return {
      videoList: [],
      page: 1,
      size: 24,
      length: 0,
      categoryId: 0,
      categoryList: [],
      categoryChiled: {},
      showChileName: false,
    }
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = parseInt(this.$route.query.page)
    }
    this.categoryId = parseInt(this.$route.params.id)
    this.getCategory()
    this.getVideoList()
  },
  methods: {
    setCategoryFather() {
      if (this.categoryList.id === this.categoryId) {
        return
      }
      this.categoryId = this.categoryList.id
      this.showChileName = false
      this.getVideoList()
      // this.$router.push({ params: { id: this.categoryList.id }})
    },
    getCategory() {
      this.httpGet(`/category/tree?category=${this.categoryId}`, (json) => {
        this.categoryList = json.data
        // 判断是不是父分类
        if (this.categoryList.id !== this.categoryId) {
          this.showChileName = true
          this.getNowCategory()
        }
      })
    },
    getNowCategory() {
      for (let i = 0; i < this.categoryList.children.length; i++) {
        const child = this.categoryList.children[i]
        if (child.id === this.categoryId) {
          this.categoryChiled = child
          return
        }
      }
    },
    getVideoList() {
      this.httpGet(
        `/article/category/${this.categoryId}?page=${this.page}&limit=${this.size}`,
        (json) => {
          if (json.data == null) {
            this.videoList = []
            this.page = 1
            this.length = 0
          } else {
            this.videoList = json.data.list
            this.page = json.data.currPage
            this.length = json.data.totalPage
          }
        }
      )
    },
    setCategory(value) {
      this.categoryId = value.id
      this.showChileName = true
      this.categoryChiled = value
      this.getVideoList()
      // this.$router.push({ params: { id: this.categoryList.id }})
    },
    pageChange(value) {
      this.page = value
      this.$router.push({ query: { page: this.page } })
      this.getVideoList()
      window.scrollTo({ top: 0, behavior: 'smooth' })
    },
    back() {
      this.$router.go(-1)
    },
  },
}
</script>
  
<style>
a {
  text-decoration: none;
}

.v-chip--selected {
  background-color: var(--v-primary-base) !important;
  color: white !important;
}
</style>
  