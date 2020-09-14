<template>
  <v-container fill-height fluid>
    <v-row>
      <v-col cols="8">
        <v-text-field
          v-model="url"
          placeholder="请输入视频地址"
          label="视频地址"
          :rules="[() => url != null || '请输入视频地址']"
          clearable
        />
      </v-col>
      <v-col cols="2">
        <v-btn color="primary" @click="play">直接打开</v-btn>
      </v-col>
      <v-col cols="2">
        <v-btn color="primary" @click="proxyPlay">尝试代理</v-btn>
      </v-col>
    </v-row>
    <v-row>
      <v-col cols="12">
        <DPlayer v-if="show" :video="video" />
      </v-col>
    </v-row>
    <v-row v-if="show" justify="center">

      <v-btn color="primary" @click="show = false">关闭视频</v-btn>

    </v-row>
    <v-row justify="center">

      说明：输入播放地址后，请先选择直接打开，如果不能打开请再选择代理<br>
      每次需要播放新的视频，请先选择关闭播放器，然后再输入链接打开视频<br><br>

      <br>
      CCTV-1到CCTV-14，改为相应cctv1hd就可以
      <br>
      http://ivi.bupt.edu.cn/hls/cctv1hd.m3u8
      <br>
      CHC高清电影
      <br>
      http://ivi.bupt.edu.cn/hls/chchd.m3u8
      <br>
      北京卫视高清<br>

      http://ivi.bupt.edu.cn/hls/btv1hd.m3u8
      <br>
      北京文艺高清
      <br>
      http://ivi.bupt.edu.cn/hls/btv2hd.m3u8
      <br>
      北京体育高清
      <br>
      http://ivi.bupt.edu.cn/hls/btv6hd.m3u8
      <br>
      北京纪实高清
      <br>
      http://ivi.bupt.edu.cn/hls/btv11hd.m3u8
      <br>
      湖南卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/hunanhd.m3u8
      <br>
      浙江卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/zjhd.m3u8
      <br>
      江苏卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/jshd.m3u8
      <br>
      东方卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/dfhd.m3u8
      <br>
      安徽卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/ahhd.m3u8
      <br>
      黑龙江卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/hljhd.m3u8
      <br>
      辽宁卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/lnhd.m3u8
      <br>
      深圳卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/szhd.m3u8
      <br>
      广东卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/gdhd.m3u8
      <br>
      天津卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/tjhd.m3u8
      <br>
      湖北卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/hbhd.m3u8
      <br>
      山东卫视高清
      <br>
      http://ivi.bupt.edu.cn/hls/sdhd.m3u8

      <br>
      测试视频链接：<br>
      https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8 <br>
      https://zhstatic.zhihu.com/cfe/griffith/zhihu2018_hd.mp4
    </v-row>
  </v-container>
</template>

<script>
import DPlayer from '@/components/player/player.vue'
export default {
  name: 'Live',
  components: {
    DPlayer
  },
  data() {
    return {
      show: false,
      video: {
        fileUrl: ''
      },
      url: ''
    }
  },
  methods: {
    play() {
      if (this.url === '') {
        return
      }

      // encodeURIComponent(this.url)
      //  'https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8'
      // https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8
      // https://zhstatic.zhihu.com/cfe/griffith/zhihu2018_hd.mp4
      this.video.fileUrl = this.url
      this.show = true
    },
    proxyPlay() {
      if (this.url === '') {
        return
      }
      this.video.fileUrl = `/api/proxy?url=${encodeURIComponent(this.url)}`
      this.show = true
    }
  }
}
</script>

<style>

</style>
