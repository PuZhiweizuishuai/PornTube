<!-- 视频播放组件 -->
<template>
  <div id="artRef" ref="artRef"></div>
</template>

<script>
import Artplayer from 'artplayer'
import artplayerPluginDanmuku from 'artplayer-plugin-danmuku'
import { useUserStore } from '@/stores/userStore'
export default {
  data() {
    return {
      instance: null,
      snackbar: false,
      // 播放记录事件
      playLogTask: null,
      seek: 0,
    }
  },
  props: {
    video: {
      type: Object,
      default: () => {},
    },
    picurl: {
      type: String,
      default: '',
    },
    article: {
      type: Number,
      default: 0,
    },
  },
  methods: {},
  mounted() {
    this.seek = parseInt(this.$route.query.seek)
    if (isNaN(this.seek)) {
      this.seek = 0
    }
    const videoId = this.video.id
    const API = this.SERVER_API_URL
    const userInfo = useUserStore()
    this.instance = new Artplayer({
      // 容器
      container: this.$refs.artRef,
      // 视频地址
      url: this.video.fileUrl + '?key=' + encodeURIComponent(this.video.key),
      // 视频封面
      poster: this.picurl,
      // 自动启用迷你窗口
      // autoMini: true,
      // 显示视频反转按钮
      flip: true,
      // 显示视频播放速度
      playbackRate: true,
      // 显示视频长宽比
      aspectRatio: true,
      // 显示视频截图功能
      screenshot: true,
      setting: true,
      hotkey: true,
      // 画中画
      pip: true,
      fullscreen: true,
      fullscreenWeb: true,
      // 移动端长按快进
      fastForward: true,
      // 移动端自动旋转播放器
      autoOrientation: true,
      // 自定义右键菜单
      contextmenu: [
        {
          html: '不挂高数出品',
          click: function (...args) {
            window.open('https://www.buguagaoshu.com/')
          },
        },
      ],
      plugins: [
        artplayerPluginDanmuku({
          danmuku: function () {
            return new Promise((resolve) => {
              let dList = []
              fetch(`${API}/danmaku/v1?id=${videoId}`, {
                headers: {
                  'Content-Type': 'application/json; charset=UTF-8',
                },
                method: 'GET',
                credentials: 'include',
              })
                .then((response) => response.json())
                .then((json) => {
                  if (json.status === 200) {
                    for (let d of json.data) {
                      let a = {
                        text: d.text,
                        time: d.time,
                        mode: d.type,
                        color: d.color,
                      }
                      dList.push(a)
                    }
                  }
                  resolve(dList)
                })
                .catch((error) => {
                  console.error('HTTP Post Error:', error)
                  resolve([])
                })
            })
          },
          // 这是用户在输入框输入弹幕文本，然后点击发送按钮后触发的函数
          // 你可以对弹幕做合法校验，或者做存库处理
          // 当返回true后才表示把弹幕加入到弹幕队列
          async beforeEmit(danmu) {
            if (userInfo.userData == null) {
              alert('请先登录')
              console.log('请先登录')
              return false
            }
            // 构造提交后端的数据
            const danmuData = {
              text: danmu.text,
              time: danmu.time,
              type: danmu.mode,
              color: danmu.color,
              id: videoId,
            }
            fetch(`${API}/danmaku/v1`, {
              headers: {
                'Content-Type': 'application/json; charset=UTF-8',
                //'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
              },
              method: 'POST',
              credentials: 'include',
              body: JSON.stringify(danmuData),
            })
              .then((response) => response.json())
              .then((json) => {
                if (json.code == 0) {
                  return true
                } else {
                  return false
                }
              })
              .catch((error) => {
                console.error('HTTP Post Error:', error)
                return null
              })
            return true
          },
        }),
      ],
    })

    this.instance.on('ready', () => {
      console.log('播放器准备完成')
      this.instance.seek = this.seek
    })

    this.instance.on('play', () => {
      console.info('play')
      const userId = userInfo.userData.id
      // 构造播放历史记录请求数据
      const data = {
        articleId: this.video.articleId,
        fileId: 1,
        videoTime: this.instance.currentTime,
        userId: userId,
        videoId: this.video.id,
      }
      if (userId == null) {
        clearInterval(this.playLogTask)
      } else {
        this.playLogTask = setInterval(() => {
          data.videoTime = this.instance.currentTime
          // console.log(data)
          this.httpPost('/user/playrecording/save', data, (json) => {
            // console.log(json)
          })
        }, 5000)
      }
    })

    this.instance.on('pause', () => {
      console.info('pause')
      clearInterval(this.playLogTask)
    })

    this.instance.on('destroy', () => {
      console.info('destroy')
      clearInterval(this.playLogTask)
    })

    this.$nextTick(() => {
      this.$emit('get-instance', this.instance)
    })
  },
  beforeUnmount() {
    if (this.instance && this.instance.destroy) {
      this.instance.destroy(false)
    }
  },
}
</script>

<style>
#artRef {
  height: 500px;
}
</style>