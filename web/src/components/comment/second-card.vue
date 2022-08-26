<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" style="padding-top: 0px;">
        <router-link :to="`/user/${comment.userId}`">
          <v-avatar size="32" style="float: left;">
            <v-img :src="comment.avatarUrl" />
          </v-avatar>
        </router-link>
        <p style="margin-left: 60px;">
          <router-link :to="`/user/${comment.userId}`">
            {{ comment.username }}
          </router-link>
          <v-chip
            class="ma-2"
            color="primary"
            x-small
            text-color="white"
          >
            {{ parserUa() }}
          </v-chip>
          <span v-html="`&nbsp;&nbsp;&nbsp;&nbsp;`" />
          {{ TimeUtil.renderTime(comment.createTime) }}
        </p>
      </v-col>
    </v-row>
    <v-row justify="center">
      <v-col cols="12">
        <span v-if="comment.parentCommentId != father.id" style="float: left">
          回复<router-link :to="`/user/${comment.parentUserId}`" target="_blank">@ {{ comment.username }} :</router-link>
        </span>
        <!-- <span :id="'secondCommentView' + comment.id" /> -->
        <!-- {{ comment.content }} -->
        <ShowMarkdown :markdown="comment.comment" :anchor="0" />
      </v-col>
    </v-row>
    <v-row justify="end">
      <v-btn
        color="indigo"
        icon
        small
        @click="setComment"
      >
        <v-icon>mdi-comment</v-icon>
      </v-btn>
      {{ comment.commentCount }}

      <span v-html="`&nbsp;&nbsp;`" />
      <span v-html="`&nbsp;&nbsp;`" />

      <v-btn
        color="primary"
        icon
        small
        @click="like()"
      >
        <v-icon>mdi-thumb-up</v-icon>
      </v-btn>
      {{ comment.likeCount }}

      <span v-html="`&nbsp;&nbsp;`" />
      <!-- <span v-html="`&nbsp;&nbsp;`" />
      <v-btn
        icon

        small
      >
        <v-icon>mdi-thumb-down</v-icon>
      </v-btn>
      {{ comment.badCount }}

      <span v-html="`&nbsp;&nbsp;`" /> -->
      <span v-html="`&nbsp;&nbsp;`" />
    </v-row>
    <v-row>
      <v-divider />
    </v-row>
    <v-snackbar
      v-model="showMessage"
      :top="true"
      :timeout="3000"
    >
      {{ message }}

      <template v-slot:action="{ attrs }">
        <v-btn
          color="pink"
          text
          v-bind="attrs"
          @click="showMessage = false"
        >
          关闭
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
import TimeUtil from '@/utils/time-util.vue'
import ShowMarkdown from '@/components/vditor/show-markdown.vue'
var parser = require('ua-parser-js')
export default {
  components: {
    ShowMarkdown
  },
  props: {
    comment: {
      type: Object,
      default: null
    },
    father: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      TimeUtil,
      content: '',
      showMessage: false,
      message: ''
    }
  },
  created() {
    // Vditor.md2html(this.comment.content).then((data) => {
    //   this.content = data
    //   this.showComment()
    // })
  },
  mounted() {

  },
  methods: {
    parserUa() {
      const ua = parser(this.comment.ua)
      return `${ua.os.name} ${ua.browser.name}`
    },
    setComment() {
      this.$emit('comment', this.comment)
    },
    like() {
      const likeInfo = {
        targetId: this.comment.id,
        targetType: 1,
        type: 0
      }
      this.httpPost('/click/like', likeInfo, (json) => {
        if (json.status === 200) {
          this.message = '点赞成功'
          this.comment.likeCount = this.comment.likeCount + 1
          this.showMessage = true
        } else {
          this.message = json.message
          this.comment.likeCount = this.comment.likeCount - 1
          this.showMessage = true
        }
      })
    }
  }
}
</script>

<style scoped>

</style>
