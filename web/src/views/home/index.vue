<template>
  <v-container fill-height fluid style="padding-left: 24px; padding-right: 24px">
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
    <v-row no-gutters>
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
      length: 0
    }
  },
  created() {
    console.log(this.$route.query.page)
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = this.$route.query.page
    }

    this.getVideoList()
  },
  methods: {
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
