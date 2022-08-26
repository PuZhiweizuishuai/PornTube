<template>
  <div>
    <div ref="markdownView" />
  </div>
</template>

<script>
import Vditor from 'vditor'
import 'vditor/dist/index.css'
export default {
  name: 'ShowMarkdown',
  props: {
    markdown: {
      type: String,
      default: ''
    },
    speech: {
      type: Boolean,
      default: false
    },
    anchor: {
      type: Number,
      default: 1
    }
  },
  data() {
    return {
      markdownText: this.markdown,
      speechValue: this.speech,
      anchorNum: this.anchor
    }
  },
  mounted() {
    this.previevMarkdown()
  },
  updated() {

  },
  methods: {
    previevMarkdown() {
      Vditor.preview(this.$refs.markdownView,
        this.markdownText, {
          speech: {
            enable: this.speech
          },
          // cdn: '/vditor',
          // theme: {
          //   path: '/vditor/dist/css/content-theme'
          // },
          emojiPath: '/emoji',
          anchor: this.anchorNum
        })
    }
  }
}

</script>
