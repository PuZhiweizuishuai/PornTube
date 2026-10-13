<template>
  <v-container fluid>
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

    <v-divider class="mb-2"></v-divider>

    <!-- 视频 / 文章 切换标签，参考 YouTube 频道页 Tabs -->
    <v-tabs
      v-model="tab"
      class="category-tabs mb-4"
      color="on-surface"
      slider-color="primary"
      density="comfortable"
      @update:model-value="tabChange"
    >
      <v-tab value="video" class="text-none">
        <v-icon icon="mdi-video-outline" start class="me-1" />
        视频
      </v-tab>
      <v-tab value="text" class="text-none">
        <v-icon icon="mdi-text-box-outline" start class="me-1" />
        文章
      </v-tab>
    </v-tabs>

    <!-- 视频卡片 -->
    <template v-if="tab === 'video'">
      <v-row v-if="videoList.length > 0">
        <v-col cols="12" sm="6" md="4" lg="3" xxl="2" v-for="item in videoList" :key="item.id">
          <VideoCared :video="item" />
        </v-col>
      </v-row>
      <v-row v-else class="mt-4">
        <v-col cols="12" class="text-center">
          <v-icon size="x-large" color="grey" class="mb-2">mdi-video-off</v-icon>
          <p class="text-body-1">暂无视频内容</p>
        </v-col>
      </v-row>
    </template>

    <!-- 文章卡片 -->
    <template v-else>
      <v-row v-if="textList.length > 0">
        <v-col cols="12" sm="6" md="4" lg="3" xxl="2" v-for="item in textList" :key="item.id">
          <ArticleCard :text="item" />
        </v-col>
      </v-row>
      <v-row v-else class="mt-4">
        <v-col cols="12" class="text-center">
          <v-icon size="x-large" color="grey" class="mb-2">mdi-book-open-outline</v-icon>
          <p class="text-body-1">暂无文章内容</p>
        </v-col>
      </v-row>
    </template>

    <!-- 分页部分 -->
    <v-container v-if="length > 1">
      <v-row justify="center">
        <Pagination
          v-model="page"
          :length="length"
          :total-visible="7"
          rounded
          @update:model-value="pageChange"
        />
      </v-row>
    </v-container>
  </v-container>
</template>
  
<script>
import VideoCared from '@/components/card/VideoCard.vue'
import ArticleCard from '@/components/card/ArticleCard.vue'
import Pagination from '@/components/common/Pagination.vue'

export default {
  name: 'CategoryView',
  components: {
    VideoCared,
    ArticleCard,
    Pagination,
  },
  data() {
    return {
      tab: 'video',
      videoList: [],
      textList: [],
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
      this.resetLists()
      this.fetchNowTab()
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
    // 切换分区后两个列表都要重新拉取
    resetLists() {
      this.page = 1
      this.length = 0
      this.videoList = []
      this.textList = []
    },
    tabChange() {
      this.page = 1
      this.length = 0
      this.fetchNowTab()
    },
    fetchNowTab() {
      if (this.tab === 'video') {
        this.getVideoList()
      } else {
        this.getTextList()
      }
    },
    getVideoList() {
      this.httpGet(
        `/article/category/${this.categoryId}?page=${this.page}&limit=${this.size}&type=0`,
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
    getTextList() {
      this.httpGet(
        `/article/category/${this.categoryId}?page=${this.page}&limit=${this.size}&type=2`,
        (json) => {
          if (json.data == null) {
            this.textList = []
            this.page = 1
            this.length = 0
          } else {
            this.textList = json.data.list
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
      this.resetLists()
      this.fetchNowTab()
      // this.$router.push({ params: { id: this.categoryList.id }})
    },
    pageChange(value) {
      this.page = value
      this.$router.push({ query: { page: this.page } })
      this.fetchNowTab()
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

/* 原写法用的是 Vuetify 2 的 --v-primary-base，在 v3 中无效，改为 v3 的主题变量 */
.v-chip--selected {
  background-color: rgb(var(--v-theme-primary)) !important;
  color: rgb(var(--v-theme-on-primary)) !important;
}

/* YouTube 频道页风格的标签栏 */
.category-tabs .v-tab {
  opacity: 1;
  letter-spacing: normal;
}

.category-tabs .v-tab--selected {
  font-weight: 600;
}
</style>
