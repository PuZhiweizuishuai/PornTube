<template>
  <div id="dplayer" ref="dplayer" />
</template>

<script>
import DPlayer from 'dplayer'
const Hls = require('hls.js')
export default {
  name: 'Play',
  props: {
    video: {
      type: Object,
      default: () => {}
    },
    picurl: {
      type: String,
      default: ''
    },
    article: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      Hls,
      videoData: this.video,
      pic: this.picurl,
      id: this.article
    }
  },
  mounted() {
    console.log(this.videoData.sub)
    this.init()
  },
  methods: {
    init() {
      new DPlayer({
        container: document.querySelector('#dplayer'),
        lang: 'zh-cn',
        screenshot: true,
        video: {
          url: this.videoData.fileUrl + '?key=' + encodeURIComponent(this.videoData.key), // 'https://api.dogecloud.com/player/get.mp4?vcode=5ac682e6f8231991&userId=17&ext=.mp4',
          pic: this.pic
        },
        logo: '/logo.png',
        danmaku: {
          id: this.videoData.id,
          api: '/api/danmaku/',
          token: 'mjmnb',
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
