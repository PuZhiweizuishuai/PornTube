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
      id: this.article,
      dp: null,
      playLogTask: null,
      seek: 0
    }
  },
  mounted() {
    console.log('mount')
    console.log(this.videoData.sub)
    this.init()
    // setInterval(() => {
    //   console.log(this.dp.video.currentTime)
    // }, 3000)
  },
  created() {
    console.log('create')
    this.seek = parseInt(this.$route.query.seek)
    if (isNaN(this.seek)) {
      this.seek = 0
    }
  },
  methods: {
    init() {
      this.dp = new DPlayer({
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

      if (this.seek !== 0) {
        this.dp.seek(this.seek)
      }

      let playtask = null
      const userId = this.$store.state.userInfo.id
      // 构造播放历史记录请求数据
      const data = {
        articleId: this.videoData.articleId,
        fileId: 1,
        videoTime: this.dp.video.currentTime,
        userId: this.$store.state.userInfo.id,
        videoId: this.videoData.id
      }
      // console.log(data)
      this.dp.on('play', () => {
        if (userId == null) {
          clearInterval(playtask)
        } else {
          playtask = setInterval(() => {
            data.videoTime = this.dp.video.currentTime
            // console.log(data)
            this.httpPost('/user/playrecording/save', data, (json) => {
              // console.log(json)
            })
          }, 5000)
        }
      })

      this.dp.on('pause', () => {
        console.log('abort')
        clearInterval(playtask)
        data.videoTime = this.dp.video.currentTime
        this.httpPost('/user/playrecording/save', data, (json) => {
          console.log(json)
        })
      })

      this.dp.on('abort', () => {
        console.log('abort')
        clearInterval(playtask)
        data.videoTime = this.dp.video.currentTime
        this.httpPost('/user/playrecording/save', data, (json) => {
          console.log(json)
        })
      })
    }
  }
}
// sendPlayLog() {
// 构造播放历史记录请求数据
// const data = {
//   articleId: this.videoData.articleId,
//   fileId: 1,
//   videoTime: this.dp.video.currentTime,
//   userId: this.$store.state.userInfo.id,
//   videoId: this.videoData.id
// }
// this.httpPost('/user/playrecording/save', data, (json) => {
//   //
//   console.log(json)
// })
</script>

<style>
#dplayer {
  height: 500px;
}
</style>
