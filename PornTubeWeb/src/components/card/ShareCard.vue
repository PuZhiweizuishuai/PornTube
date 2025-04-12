<template>
  <v-card class="share-card" elevation="2" rounded="lg">
    <v-card-title class="title pb-0">
      <v-icon icon="mdi-share-variant" class="mr-2" color="primary" />
      分享
    </v-card-title>
    <v-divider class="mt-2 mb-4"></v-divider>

    <v-card-text>
      <v-row no-gutters class="mb-4">
        <v-col cols="12">
          <div class="video-title text-truncate mb-2">{{ article.title }}</div>
          <div class="share-link text-body-2 text-grey">
            <span>{{ url }}</span>
            <v-btn
              density="compact"
              icon="mdi-content-copy"
              variant="text"
              size="small"
              color="primary"
              @click="copyLink"
              class="ml-2"
            ></v-btn>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" sm="5" md="4" class="qr-wrapper d-flex flex-column align-center">
          <div class="text-subtitle-2 text-center mb-2">扫描二维码查看</div>
          <div class="qr-code-container elevation-1 pa-2 rounded bg-white">
            <canvas ref="qrContainer" />
          </div>
        </v-col>

        <v-col cols="12" sm="7" md="8">
          <div class="text-subtitle-2 mb-2">分享到</div>
          <v-row dense>
            <v-col v-for="platform in platforms" :key="platform.name" cols="4" sm="3" class="pa-1">
              <v-btn
                :color="platform.color"
                @click="shareTo(platform.name)"
                block
                variant="elevated"
                class="share-btn"
              >
                <v-icon :icon="platform.icon" size="small" class="mr-1"></v-icon>
                {{ platform.label }}
              </v-btn>
            </v-col>
          </v-row>
        </v-col>
      </v-row>
    </v-card-text>

    <!-- 剪贴板复制提示 -->
    <v-snackbar v-model="showSnackbar" :timeout="2000" color="success" location="top">
      {{ snackbarText }}
    </v-snackbar>
  </v-card>
</template>

<script>
import QRCode from 'qrcode'

export default {
  props: {
    article: {
      type: Object,
      default: () => ({}),
    },
  },

  data() {
    return {
      url: '',
      showSnackbar: false,
      snackbarText: '',
      platforms: [
        { name: 'weibo', label: '微博', icon: 'mdi-sina-weibo', color: '#e6162d' },
        { name: 'wechat', label: '微信', icon: 'mdi-wechat', color: '#07c160' },
        { name: 'qq', label: 'QQ', icon: 'mdi-qqchat', color: '#12b7f5' },
        { name: 'qzone', label: 'QQ空间', icon: 'mdi-qqchat', color: '#fbbd08' },
        { name: 'tieba', label: '贴吧', icon: 'mdi-paw', color: '#3385ff' },
        { name: 'twitter', label: 'Twitter', icon: 'mdi-twitter', color: '#1da1f2' },
      ],
    }
  },

  mounted() {
    // 确保article对象有id属性
    if (this.article && this.article.id) {
      this.url = `${window.location.origin}/player/${this.article.id}`
      this.$nextTick(() => {
        this.createQrCode()
      })
    }
  },

  methods: {
    createQrCode() {
      if (this.$refs.qrContainer) {
        QRCode.toCanvas(
          this.$refs.qrContainer,
          this.url,
          {
            errorCorrectionLevel: 'H',
            margin: 2,
            width: 120,
            color: {
              dark: '#000000',
              light: '#FFFFFF',
            },
          },
          (err) => {
            if (err) console.error('QR码生成失败:', err)
          }
        )
      }
    },

    copyLink() {
      navigator.clipboard
        .writeText(this.url)
        .then(() => {
          this.showSnackbar = true
          this.snackbarText = '链接已复制到剪贴板'
        })
        .catch((err) => {
          console.error('复制失败:', err)
          this.showSnackbar = true
          this.snackbarText = '复制失败'
        })
    },

    shareTo(platform) {
      const title = encodeURIComponent(this.article.title)
      const url = encodeURIComponent(this.url)

      let shareUrl = ''

      switch (platform) {
        case 'weibo':
          shareUrl = `https://service.weibo.com/share/share.php?url=${url}&title=${title}`
          break
        case 'qq':
          shareUrl = `https://connect.qq.com/widget/shareqq/index.html?url=${url}&title=${title}`
          break
        case 'qzone':
          shareUrl = `https://sns.qzone.qq.com/cgi-bin/qzshare/cgi_qzshare_onekey?url=${url}&title=${title}`
          break
        case 'tieba':
          shareUrl = `https://tieba.baidu.com/f/commit/share/openShareApi?url=${url}&title=${title}`
          break
        case 'twitter':
          shareUrl = `https://twitter.com/intent/tweet?text=${title}&url=${url}`
          break
        case 'wechat':
          // 微信没有直接分享API，复制到剪贴板
          navigator.clipboard.writeText(`${this.article.title} ${this.url}`).then(() => {
            this.showSnackbar = true
            this.snackbarText = '链接已复制，请打开微信分享'
          })
          return
      }

      if (shareUrl) {
        window.open(shareUrl, '_blank', 'width=600,height=500')
      }
    },
  },
}
</script>

<style scoped>
.share-card {
  max-width: 600px;
  margin: 0 auto;
}

.title {
  font-size: 1.25rem;
  font-weight: 500;
}

.video-title {
  font-weight: 500;
  font-size: 1rem;
  max-width: 100%;
}

.share-link {
  display: flex;
  align-items: center;
  background-color: rgba(0, 0, 0, 0.04);
  border-radius: 4px;
  padding: 8px 12px;
  overflow: hidden;
}

.qr-code-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: fit-content;
}

.share-btn {
  text-transform: none;
  font-size: 0.75rem;
  height: 36px;
}

@media (max-width: 600px) {
  .qr-wrapper {
    margin-bottom: 16px;
  }
}
</style>