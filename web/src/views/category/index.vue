<template>
  <v-container fill-height fluid style="padding-left: 24px; padding-right: 24px">
    <v-row>
      <v-col>

        <v-btn style="font-weight: bold;font-size: 20px" large text color="primary" @click="setCategoryFather()"> {{ categoryList.name }} </v-btn>
        <span v-if="showChileName" style="font-weight: bold;font-size: 20px">
          /
          <v-btn style="font-weight: bold;font-size: 20px" large text color="primary"> {{ categoryChiled.name }} </v-btn>
        </span>

      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-btn v-for="item in categoryList.children" :key="item.id" text color="primary" @click="setCategory(item)">
          {{ item.name }}
        </v-btn>
      </v-col>
    </v-row>
    <v-row>
      <v-divider />
    </v-row>
    <v-col />

    <v-row no-gutters style="padding-top: 12px; padding-bottom: 12px">
      <v-col
        v-for="item in videoList"
        :key="item.id"
      >
        <VideoCared :video="item" />
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-pagination
        v-model="page"
        :length="length"
        @input="pageChange"
      />
    </v-row>
    <v-col>
      &nbsp;
    </v-col>
  </v-container>
</template>

<script>
import VideoCared from '@/components/player/video-card.vue'

export default {
  name: 'Category',
  components: {
    VideoCared
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
      showChileName: false
    }
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = this.$route.query.page
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
      this.httpGet(`/article/category/${this.categoryId}?page=${this.page}&limit=${this.size}`, (json) => {
        if (json.data == null) {
          this.videoList = []
          this.page = 1
          this.length = 0
        } else {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
        }
      })
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
      this.$router.push({ query: { page: this.page }})
      this.getVideoList()
      this.$vuetify.goTo(0)
    }
  }
}
</script>

<style>
a {
  text-decoration: none;
}
</style>
