<template>
  <v-container fill-height>
    <v-tabs>
      <v-tab @click="setShow(0)">视频</v-tab>
      <v-tab @click="setShow(1)">图片</v-tab>
      <v-tab @click="setShow(3)">音乐</v-tab>
      <v-tab @click="setShow(2)">文章</v-tab>
    </v-tabs>
    <VideoUpload v-if="show == 0" />
    <VideoPhoto v-if="show == 1" />
    <VideoArticle v-if="show == 2" />
    <VideoMusic v-if="show == 3" />
  </v-container>
</template>

<script>
import VideoUpload from '@/components/upload/upload-video.vue'
import VideoPhoto from '@/components/upload/upload-photo.vue'
import VideoArticle from '@/components/upload/upload-article.vue'
import VideoMusic from '@/components/upload/upload-music.vue'
export default {
  name: 'Upload',
  components: {
    VideoUpload,
    VideoPhoto,
    VideoArticle,
    VideoMusic
  },
  data() {
    return {
      show: 0
    }
  },
  methods: {
    setShow(value) {
      this.show = value
    }
  }
}
</script>

<style>

</style>
