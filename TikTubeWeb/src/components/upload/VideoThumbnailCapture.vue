<template>
  <div>
    <v-dialog v-model="showDialog" max-width="800" persistent>
      <v-card class="video-thumbnail-capture">
        <v-card-title class="text-h5 font-weight-bold d-flex align-center">
          <v-icon icon="mdi-image-filter" class="mr-2" color="primary"></v-icon>
          截取视频封面
          <v-spacer></v-spacer>
          <v-btn icon variant="text" @click="closeDialog">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>

        <v-divider></v-divider>

        <v-card-text>
          <div class="video-container">
            <video
              autoplay
              muted
              ref="videoPlayer"
              class="video-player"
              width="100%"
              controls
              @loadedmetadata="videoLoaded"
              @timeupdate="updateCurrentTime"
            ></video>

            <div class="timeline-container mt-2">
              <v-slider
                v-model="currentTime"
                :min="0"
                :max="videoDuration"
                step="0.1"
                thumb-label
                :thumb-size="20"
                color="primary"
                track-color="grey-lighten-2"
                @update:model-value="seekVideo"
              >
                <template v-slot:thumb-label="{ modelValue }">
                  {{ formatTime(modelValue) }}
                </template>
              </v-slider>
            </div>

            <div class="thumbnails-preview mt-4">
              <v-card-subtitle class="text-subtitle-1 font-weight-medium pb-2">
                视频预览帧
              </v-card-subtitle>
              <v-row>
                <v-col
                  v-for="(frame, index) in thumbnailFrames"
                  :key="index"
                  cols="4"
                  sm="3"
                  md="2"
                >
                  <v-card
                    variant="outlined"
                    :elevation="selectedFrameIndex === index ? 4 : 0"
                    :class="selectedFrameIndex === index ? 'border border-primary' : ''"
                    class="frame-thumbnail"
                    @click="selectFrame(index)"
                  >
                    <v-img :src="frame.dataUrl" aspect-ratio="16/9" cover height="70"></v-img>
                    <div class="text-caption text-center py-1">{{ formatTime(frame.time) }}</div>
                  </v-card>
                </v-col>
              </v-row>
            </div>
          </div>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4">
          <v-btn variant="text" color="grey-darken-1" @click="closeDialog"> 取消 </v-btn>
          <v-spacer></v-spacer>
          <v-btn
            color="primary"
            variant="flat"
            prepend-icon="mdi-camera"
            @click="captureFrame"
            :disabled="!isVideoLoaded"
          >
            截取当前帧
          </v-btn>
          <v-btn
            color="success"
            variant="flat"
            prepend-icon="mdi-check"
            @click="confirmThumbnail"
            :disabled="!hasCapturedFrame"
          >
            设为封面
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar
      v-model="showMessage"
      :color="messageType"
      variant="elevated"
      location="top"
      :timeout="3000"
    >
      {{ message }}
      <template v-slot:actions>
        <v-btn variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </div>
</template>

<script>
export default {
  name: 'VideoThumbnailCapture',
  props: {
    file: {
      type: Object,
      default: null,
    },
    show: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      showDialog: false,
      isVideoLoaded: false,
      videoFile: null,
      videoDuration: 0,
      currentTime: 0,
      thumbnailFrames: [],
      selectedFrameIndex: -1,
      capturedImage: null,
      hasCapturedFrame: false,
      message: '',
      showMessage: false,
      messageType: 'info',
      videoUrl: '',
    }
  },
  watch: {
    show(newVal) {
      this.showDialog = newVal
      if (newVal && this.file) {
        this.loadVideoFile(this.file)
      }
    },
    file(newVal) {
      if (newVal) {
        this.loadVideoFile(newVal)
      }
    },
  },
  methods: {
    loadVideoFile(file) {
      try {
        // 检查是否是File对象或具有file属性的对象
        if (file instanceof File) {
          // 直接是File对象
          this.videoFile = file
        } else if (file && file.file && file.file instanceof File) {
          // FilePond可能返回包装后的对象，其中实际File在file属性中
          this.videoFile = file.file
        } else if (file && typeof file.getFile === 'function') {
          // 如果有getFile方法，尝试调用它
          const actualFile = file.getFile()
          if (actualFile instanceof File) {
            this.videoFile = actualFile
          }
        } else if (file && typeof file === 'object') {
          // 尝试在对象中查找File对象
          for (const key in file) {
            if (file[key] instanceof File) {
              this.videoFile = file[key]
              break
            }
          }

          if (!this.videoFile) {
            console.warn('未能从对象中提取File:', file)
            throw new Error('无法从提供的对象中获取视频文件')
          }
        } else {
          console.warn('未识别的文件对象:', file)
          throw new Error('无效的文件对象')
        }

        // 检查文件类型
        if (this.videoFile && !this.videoFile.type.startsWith('video/')) {
          throw new Error('文件不是视频格式')
        }

        if (this.showDialog) {
          this.loadVideo()
        }
      } catch (error) {
        console.error('加载视频文件时出错:', error)
        this.showErrorMessage(`加载视频文件失败: ${error.message}`)
      }
    },

    loadVideo() {
      if (!this.videoFile) {
        this.showErrorMessage('没有有效的视频文件')
        return
      }

      try {
        if (this.videoUrl) {
          URL.revokeObjectURL(this.videoUrl)
        }

        this.videoUrl = URL.createObjectURL(this.videoFile)
        this.$nextTick(() => {
          const video = this.$refs.videoPlayer
          if (video) {
            video.src = this.videoUrl
            video.load()

            // 添加错误处理
            video.onerror = (e) => {
              console.error('视频加载错误:', e)
              this.showErrorMessage('视频加载失败，请确保文件格式正确')
            }
          }
        })
      } catch (error) {
        console.error('加载视频时出错:', error)
        this.showErrorMessage(`加载视频失败: ${error.message}`)
      }
    },

    videoLoaded() {
      this.isVideoLoaded = true
      this.videoDuration = this.$refs.videoPlayer.duration
      this.generateThumbnails()
    },

    updateCurrentTime() {
      if (this.$refs.videoPlayer) {
        this.currentTime = this.$refs.videoPlayer.currentTime
      }
    },

    seekVideo(time) {
      if (this.$refs.videoPlayer) {
        this.$refs.videoPlayer.currentTime = time
      }
    },

    generateThumbnails() {
      try {
        // 生成视频关键帧预览
        this.thumbnailFrames = []

        // 根据视频时长生成6-10个预览帧
        // const frameCount = Math.min(Math.max(6, Math.floor(this.videoDuration / 10)), 10)
        const frameCount = 3
        for (let i = 0; i < frameCount; i++) {
          const time = (this.videoDuration * i) / (frameCount - 1 || 1)
          this.captureFrameAtTime(time, (dataUrl) => {
            this.thumbnailFrames.push({
              time,
              dataUrl,
            })

            // 按时间排序
            this.thumbnailFrames.sort((a, b) => a.time - b.time)
          })
        }
      } catch (error) {
        console.error('生成缩略图时出错:', error)
        this.showErrorMessage('生成预览帧失败')
      }
    },

    captureFrameAtTime(time, callback) {
      const video = this.$refs.videoPlayer
      const canvas = document.createElement('canvas')

      video.currentTime = time

      // 需要等待视频seek完成
      const seekHandler = () => {
        canvas.width = video.videoWidth
        canvas.height = video.videoHeight

        const ctx = canvas.getContext('2d')
        ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

        const dataUrl = canvas.toDataURL('image/jpeg', 0.8)
        callback(dataUrl)

        video.removeEventListener('seeked', seekHandler)
      }

      video.addEventListener('seeked', seekHandler)
    },

    selectFrame(index) {
      this.selectedFrameIndex = index
      if (index >= 0 && index < this.thumbnailFrames.length) {
        this.seekVideo(this.thumbnailFrames[index].time)
        this.capturedImage = this.thumbnailFrames[index].dataUrl
        this.hasCapturedFrame = true
      }
    },

    captureFrame() {
      const video = this.$refs.videoPlayer
      const canvas = document.createElement('canvas')

      canvas.width = video.videoWidth
      canvas.height = video.videoHeight

      const ctx = canvas.getContext('2d')
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height)

      this.capturedImage = canvas.toDataURL('image/jpeg', 0.8)
      this.hasCapturedFrame = true

      // 添加到缩略图列表中
      this.thumbnailFrames.push({
        time: this.currentTime,
        dataUrl: this.capturedImage,
      })

      // 按时间排序
      this.thumbnailFrames.sort((a, b) => a.time - b.time)

      // 选中当前帧
      this.selectedFrameIndex = this.thumbnailFrames.findIndex(
        (frame) => frame.time === this.currentTime
      )

      this.showSuccessMessage('已截取当前帧')
    },

    confirmThumbnail() {
      if (!this.hasCapturedFrame || !this.capturedImage) {
        this.showErrorMessage('请先截取一帧画面')
        return
      }

      // 将DataURL转换为File对象
      const fileName = this.file.name
      const nameWithoutExtension = fileName.replace(/\.[^/.]+$/, '')
      this.dataURLtoFile(this.capturedImage, `${nameWithoutExtension}_thumbnail.jpg`, (file) => {
        this.$emit('thumbnail-selected', file)
        this.closeDialog()
      })
    },

    dataURLtoFile(dataUrl, filename, callback) {
      fetch(dataUrl)
        .then((res) => res.blob())
        .then((blob) => {
          const file = new File([blob], filename, { type: 'image/jpeg' })
          callback(file)
        })
        .catch((err) => {
          this.showErrorMessage('生成文件失败: ' + err)
        })
    },

    formatTime(seconds) {
      const mins = Math.floor(seconds / 60)
      const secs = Math.floor(seconds % 60)
      return `${mins}:${secs.toString().padStart(2, '0')}`
    },

    closeDialog() {
      if (this.videoUrl) {
        URL.revokeObjectURL(this.videoUrl)
      }
      this.showDialog = false
      this.$emit('close')

      // 重置状态
      this.isVideoLoaded = false
      this.videoDuration = 0
      this.currentTime = 0
      this.thumbnailFrames = []
      this.selectedFrameIndex = -1
      this.capturedImage = null
      this.hasCapturedFrame = false
    },

    showSuccessMessage(msg) {
      this.message = msg
      this.messageType = 'success'
      this.showMessage = true
    },

    showErrorMessage(msg) {
      this.message = msg
      this.messageType = 'error'
      this.showMessage = true
    },
  },
}
</script>

<style scoped>
.video-thumbnail-capture {
  overflow: hidden;
}

.video-container {
  position: relative;
}

.video-player {
  background-color: #000;
  border-radius: 4px;
  width: 100%;
}

.frame-thumbnail {
  cursor: pointer;
  transition: all 0.2s ease;
  overflow: hidden;
}

.frame-thumbnail:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.border-primary {
  border: 2px solid rgb(var(--v-theme-primary)) !important;
}
</style> 