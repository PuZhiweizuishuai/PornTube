<template>
  <div id="dplayer" ref="dplayer" />
</template>

<script>
import DPlayer from 'dplayer'

export default {
  name: 'Play',
  props: {
    videourl: {
      type: String,
      default: ''
    },
    picurl: {
      type: String,
      default: ''
    },
    id: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      url: this.videourl,
      pic: this.picurl,
      videoId: this.id,
      user: this.user
    }
  },
  mounted() {
    this.init()
  },
  methods: {
    init() {
      new DPlayer({
        container: document.querySelector('#dplayer'),
        lang: 'zh-cn',
        video: {
          url: this.url,
          pic: this.pic
        },
        logo: '/logo.png',
        danmaku: {
          id: this.videoId,
          api: '/api/danmaku/',
          token: 'tokendemo',
          maximum: 1000,
          // addition: ['/api/danmaku/post'],
          user: () => {
            if (this.$store.state.userInfo.id == null) {
              return 'null'
            } else {
              return this.$store.state.userInfo.id
            }
          },
          bottom: '15%',
          unlimited: true
        }
      })
    }
  }
}
</script>

<style>
#dplayer {
  height: 500px;
}
</style>
