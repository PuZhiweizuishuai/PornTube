<template>
  <v-container fill-height fluid style="padding-left: 24px; padding-right: 24px">
    <!-- 分区 -->
    <v-row>
      <v-col>

        <v-menu
          v-for="item in categoryList"
          :key="item.id"
          open-on-hover

          offset-y
        >
          <template v-slot:activator="{ on, attrs }">
            <v-btn
              text
              color="primary"
              v-bind="attrs"
              @click="setCategory(item)"
              v-on="on"
            >
              {{ item.name }}
            </v-btn>
          </template>

          <v-list>
            <v-list-item
              v-for="c in item.children"
              :key="c.id"
            >
              <v-list-item-title>
                <router-link :to="`/v/${c.id}`">
                  {{ c.name }}
                </router-link>
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>

      </v-col>
    </v-row>
    <v-row>
      <v-divider />
    </v-row>
    <v-col />
    <!-- <v-row>
      <div
        class="d-flex flex-wrap"
        color="grey lighten-2"
        flat
        tile
      >
        <div
          v-for="item in videoList"
          :key="item.id"
          class="pa-2"
          outlined
          tile
        >
          <VideoCared :video="item" />
        </div>
      </div>
    </v-row> -->
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
  name: 'Index',
  components: {
    VideoCared
  },
  data() {
    return {
      videoList: [],
      page: 1,
      size: 24,
      length: 0,
      categoryList: []
    }
  },
  created() {
    console.log(this.$route.query.page)
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
      fetch(`/api/article/home/list?page=${this.page}&limit=${this.size}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include'
      }).then(response => response.json())
        .then(json => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
        })
        .catch(e => {
          return null
        })
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
