<template>

  <div :id="idName" />
</template>

<script>
import Vditor from 'vditor'
import 'vditor/dist/index.css'

export default {
  name: 'Vditor',
  props: {
    idname: {
      type: String,
      default: 'Vditor'
    },
    placeholder: {
      type: String,
      default: '请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。'
    },
    uploadurl: {
      type: String,
      default: 'http://127.0.0.1:8080/uploads/file'
    },
    markdown: {
      type: String,
      default: ''
    },
    cache: {
      type: Boolean,
      default: false
    },
    cacheid: {
      type: String,
      default: 'Vditor'
    },
    height: {
      type: Number,
      default: 500
    },
    hide: {
      type: Boolean,
      default: false
    },
    uploadsize: {
      type: Number,
      default: 20 * 1024 * 1024
    }
  },
  data() {
    return {
      idName: this.idname,
      cacheId: this.cacheid,
      cacheEnable: this.cache,
      markdownStr: this.markdown,
      maxHeight: this.height,
      hideBar: this.hide,
      contentEditor: null,
      defaultPlaceholder: '请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。',
      emojis: {
        '0': '😀',
        '1': '😁',
        '2': '😂',
        '3': '😃',
        '4': '😄',
        '5': '😅',
        '6': '😆',
        '7': '😉',
        '8': '😊',
        '9': '😋',
        '10': '😎',
        '11': '😍',
        '12': '😘',
        '13': '😗',
        '14': '😙',
        '15': '😚',
        '16': '☺',
        '17': '😇',
        '18': '😐',
        '19': '😑',
        '20': '😶',
        '21': '😏',
        '22': '😣',
        '23': '😥',
        '24': '😮',
        '25': '😯',
        '26': '😪',
        '27': '😫',
        '28': '😴',
        '29': '😌',
        '30': '😛',
        '31': '😜',
        '32': '😝',
        '33': '😒',
        '34': '😓',
        '35': '😔',
        '36': '😕',
        '37': '😲',
        '38': '😷',
        '39': '😖',
        '40': '😞',
        '41': '😟',
        '42': '😤',
        '43': '😢',
        '44': '😭',
        '45': '😦',
        '46': '😧',
        '47': '😨',
        '48': '😬',
        '49': '😰',
        '50': '😱',
        '51': '😳',
        '52': '😵',
        '53': '😡',
        '54': '😠',
        'doge': '/emoji/doge.png',
        'huaji': '/emoji/huaji.gif',
        'trollface': '/emoji/trollface.png',
        '+1': '👍',
        '-1': '👎',
        'heart': '❤'
      }
    }
  },
  mounted() {
    this.contentEditor = new Vditor(this.idName, {
      // cdn: '/vditor',
      // theme: {
      //   path: '/vditor/dist/css/content-theme'
      // },
      toolbarConfig: {
        pin: false,
        hide: this.hideBar
      },
      toolbar: [
        'emoji',
        'headings',
        'bold',
        'italic',
        'strike',
        '|',
        'line',
        'quote',
        'list',
        'edit-mode',
        {
          name: 'more',
          toolbar: [
            'insert-after',
            'fullscreen',
            // 'preview',
            'info',
            'help'
          ]
        }
      ],
      height: this.maxHeight,
      hint: {
        emojiPath: '/emoji',
        emoji: this.emojis
      },
      cache: {
        enable: this.cacheEnable,
        id: this.cacheId
      },
      // minHeight: this.minHeight,
      placeholder: this.placeholder, // '请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。',
      // upload: {
      //   max: this.uploadsize,
      //   withCredentials: true,
      //   headers: {
      //     'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
      //   },
      //   accept: 'image/*, .wav, .mp4, .zip, .rar, .7z, .docx, .dox, .ppt, .pptx, .xls, .xlsx, .pdf, .apk, .mp3, .txt',
      //   url: this.uploadurl,

      //   filename(name) {
      //     return name.replace(/[^(a-zA-Z0-9\u4e00-\u9fa5\.)]/g, '')
      //       .replace(/[\?\\/:|<>\*\[\]\(\)\$%\{\}@~]/g, '')
      //       .replace('/\\s/g', '')
      //   }
      // },
      after: () => {
        this.contentEditor.setValue(this.markdownStr)
        this.$emit('after', true)
      },
      blur: (input) => {
        this.$emit('vditor-input', this.contentEditor.getValue())
      }
    })
  },
  methods: {
    setTextValue(value) {
      this.contentEditor.setValue(value)
    },
    call(value) {
      console.log(value)
    }
  }
}
</script>
