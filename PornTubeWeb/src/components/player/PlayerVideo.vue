<!-- 视频播放组件 -->
<template>
  <div id="artRef" ref="artRef"></div>
</template>

<script>
import js from '@eslint/js'
import Artplayer from 'artplayer'
import artplayerPluginDanmuku from 'artplayer-plugin-danmuku'
export default {
  data() {
    return {
      instance: null,
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
    const videoId = this.video.id
    const API = this.SERVER_API_URL

    this.instance = new Artplayer(
      {
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
      },
      function onReady(art) {
        console.log('播放器准备完成')
      }
    )

    this.instance.on('play', () => {
      console.info('play')
    })

    this.instance.on('pause', () => {
      console.info('pause')
    })

    this.instance.on('destroy', () => {
      console.info('destroy')
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