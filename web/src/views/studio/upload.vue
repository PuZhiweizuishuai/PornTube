<template>
  <v-container fill-height>
    <VideoUpload />
  </v-container>
</template>

<script>
import VideoUpload from '@/components/upload/upload-video.vue'
export default {
  name: 'Upload',
  components: {
    VideoUpload
  },
  data() {
    return {

    }
  }
}
</script>

<style>

</style>
