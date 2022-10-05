<template>
  <v-container fill-height>
    <NoLoginShow v-if="this.$store.state.userInfo == null" />
    <div v-else>
      <v-row>
        <v-col>
          <h3>播放历史</h3>
        </v-col>

      </v-row>
      <v-row>
        <v-col>
          <v-divider />
        </v-col>

      </v-row>

      <v-row>
        <v-col
          v-for="item in videoList"
          :key="item.id"
          cols="12"
        >

          <HistoryCard :video="item" />
        </v-col>
      </v-row>
      <v-row justify="center">
        <v-pagination

          v-model="page"
          :length="length"
          @input="pageChange"
        />
      </v-row>
    </div>
    <v-col>
      &nbsp;
    </v-col>
  </v-container>
</template>

<script>
import NoLoginShow from '@/components/no-login-show.vue'
import HistoryCard from '@/components/player/history-card.vue'
export default {
  name: 'History',
  components: {
    NoLoginShow,
    HistoryCard

  },
  data() {
    return {
      videoList: [],
      page: 1,
      size: 20,
      length: 1
    }
  },
  created() {
    this.init()
  },
  methods: {
    init() {
      fetch(`/api/user/playrecording/list?page=${this.page}&limit=${this.size}`, {
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
    pageChange(page) {
      this.page = page
      this.init(this.type)
      this.$vuetify.goTo(0)
    }
  }

}
</script>

<style>

</style>
