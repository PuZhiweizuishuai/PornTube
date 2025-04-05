<template>
  <v-container>
    <v-tabs>
      <v-tab @click="setShow(0)">视频</v-tab>
      <!-- <v-tab @click="setShow(1)">图片</v-tab>
        <v-tab @click="setShow(3)">音乐</v-tab>
        <v-tab @click="setShow(2)">文章</v-tab> -->
    </v-tabs>

    <VideoUpload v-if="show == 0" />
  </v-container>
</template>
  
  <script>
import VideoUpload from '@/components/upload/UploadVideo.vue'
export default {
  name: 'UploadView',
  components: {
    VideoUpload,
  },
  data() {
    return {
      show: 0,
    }
  },
  methods: {
    setShow(value) {
      this.show = value
    },
  },
}
</script>
  
  <style>
</style>
  