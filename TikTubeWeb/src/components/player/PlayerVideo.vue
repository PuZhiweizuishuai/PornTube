<!-- 视频播放组件 -->
<template>
  <div>
    <div id="artRef" ref="artRef"></div>
    <AppSnackbar :state="toast" />
  </div>
</template>

<script>
import Artplayer from 'artplayer'
import { useUserStore } from '@/stores/userStore'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import http from '@/api/index'
import { buildArtplayerOptions } from '@/utils/artplayer'
import { useMessage } from '@/composables/useMessage'

export default {
  name: 'PlayerVideo',
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
    openAds: {
      type: Boolean,
      default: false,
    },
    adsInfo: {
      type: Object,
      default: () => {},
    },
  },
  components: { AppSnackbar },
  setup() {
    const { toast, showMessage } = useMessage()
    return { toast, showMessage }
  },
  data() {
    return {
      instance: null,
      // 播放记录上报定时器
      playLogTask: null,
      seek: 0,
    }
  },
  computed: {
    videoUrl() {
      return `${this.video?.fileUrl}?key=${encodeURIComponent(this.video?.key ?? '')}`
    },
    ads() {
      // 只有明确开启且配置了广告内容时才挂载广告插件
      return this.openAds && this.adsInfo && this.adsInfo.id ? this.adsInfo : null
    },
  },
  mounted() {
    this.seek = Number.parseInt(this.$route.query.seek, 10)
    if (Number.isNaN(this.seek)) this.seek = 0

    const userInfo = useUserStore()
    const videoId = this.video?.id

    this.instance = new Artplayer(
      buildArtplayerOptions({
        container: this.$refs.artRef,
        url: this.videoUrl,
        poster: this.picurl,
        videoId,
        ads: this.ads,
        isLogin: () => !!userInfo.userData,
        danmakuHooks: {
          onLoginRequired: (message) => this.showMessage(message || '请先登录', 'warning'),
          onPending: (message) => this.showMessage(message, 'info'),
          onError: (message) => this.showMessage(message || '弹幕发送失败', 'error'),
        },
      })
    )

    this.bindEvents(userInfo)
    this.$nextTick(() => this.$emit('get-instance', this.instance))
  },
  beforeUnmount() {
    clearInterval(this.playLogTask)
    if (this.instance && this.instance.destroy) {
      this.instance.destroy(false)
    }
  },
  methods: {
    bindEvents(userInfo) {
      if (this.ads) {
        // 广告点击计数
        this.instance.on('artplayerPluginAds:click', () => {
          http.post(`/web/notice/click/${this.ads.id}`, {}).catch((e) => {
            console.warn('[ads] 广告点击统计失败:', e?.message || e)
          })
        })

        this.instance.on('artplayerPluginAds:skip', () => {
          if (this.$refs.artRef == null) {
            setTimeout(() => {
              this.instance.pause()
              this.instance.destroy(false)
            }, 10)
          }
        })
      }

      this.instance.on('ready', () => {
        this.instance.seek = this.seek
      })

      this.instance.on('play', () => {
        this.reportView()
        this.startPlayLogTask(userInfo)
      })

      this.instance.on('pause', () => clearInterval(this.playLogTask))
      this.instance.on('video:ended', () => clearInterval(this.playLogTask))
      this.instance.on('destroy', () => clearInterval(this.playLogTask))
    },
    /** 播放量上报（失败不影响观看） */
    reportView() {
      http.post(`/article/playrecording/view/${this.video?.articleId}`, {}).catch((e) => {
        console.warn('[play] 播放量上报失败:', e?.message || e)
      })
    },
    /** 每 5 秒上报一次续播位置，未登录不上报 */
    startPlayLogTask(userInfo) {
      clearInterval(this.playLogTask)
      const userId = userInfo?.userData?.id
      if (!userId) return

      const data = {
        articleId: this.video?.articleId,
        fileId: 1,
        videoTime: this.instance.currentTime,
        userId,
        videoId: this.video?.id,
      }
      this.playLogTask = setInterval(() => {
        data.videoTime = this.instance.currentTime
        http.post('/user/playrecording/save', data).catch((e) => {
          console.warn('[play] 播放进度上报失败:', e?.message || e)
        })
      }, 5000)
    },
  },
}
</script>

<style>
#artRef {
  height: 80vh; /* 视口高度的80%作为最大高度 */
}
</style>
