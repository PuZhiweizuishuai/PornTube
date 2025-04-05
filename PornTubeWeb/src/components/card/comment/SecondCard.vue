<template>
  <v-container>
    <v-row justify="center">
      <v-col cols="12" class="pt-0">
        <router-link :to="`/user/${comment.userId}`">
          <v-avatar size="32" style="float: left">
            <v-img :src="comment.avatarUrl" />
          </v-avatar>
        </router-link>
        <p style="margin-left: 60px">
          <router-link :to="`/user/${comment.userId}`">
            {{ comment.username }}
          </router-link>
          <v-chip class="ma-2" color="primary" size="x-small" text-color="white">
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
          回复<router-link :to="`/user/${comment.parentUserId}`" target="_blank"
            >@ {{ comment.username }} :</router-link
          >
        </span>
        <!-- <span :id="'secondCommentView' + comment.id" /> -->
        <!-- {{ comment.content }} -->
        <ShowMarkdown :markdown="comment.comment" :anchor="0" />
      </v-col>
    </v-row>
    <v-row justify="end">
      <v-tooltip text="评论" location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            variant="flat"
            color="blue"
            @click="setComment()"
            icon="mdi-comment"
            size="x-small"
          >
          </v-btn>
        </template>
      </v-tooltip>
      <span>{{ comment.commentCount }} </span>

      <span v-html="`&nbsp;&nbsp;`" />
      <span v-html="`&nbsp;&nbsp;`" />

      <v-tooltip text="赞" location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-bind="props"
            size="x-small"
            variant="flat"
            color="green"
            @click="like()"
            icon="mdi-thumb-up"
          ></v-btn>
        </template>
      </v-tooltip>
      <span>{{ comment.likeCount }} </span>

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
    <v-snackbar v-model="showMessage" location="top" :timeout="3000">
      {{ message }}

      <template v-slot:actions>
        <v-btn color="pink" variant="text" @click="showMessage = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
  
<script>
import TimeUtil from '@/utils/time-util.vue'
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import { UAParser } from 'ua-parser-js'

export default {
  components: {
    ShowMarkdown,
  },
  props: {
    comment: {
      type: Object,
      default: null,
    },
    father: {
      type: Object,
      default: null,
    },
  },
  data() {
    return {
      TimeUtil,
      content: '',
      showMessage: false,
      message: '',
      likeCount: this.comment?.likeCount || 0,
    }
  },
  created() {
    // Vditor.md2html(this.comment.content).then((data) => {
    //   this.content = data
    //   this.showComment()
    // })
  },
  mounted() {},
  methods: {
    parserUa() {
      const ua = new UAParser(this.comment.ua)
      return `${ua.getOS().name} ${ua.getBrowser().name}`
    },
    setComment() {
      this.$emit('comment', this.comment)
    },
    like() {
      const likeInfo = {
        targetId: this.comment.id,
        targetType: 1,
        type: 0,
      }
      this.httpPost('/click/like', likeInfo, (json) => {
        if (json.status === 200) {
          this.message = '点赞成功'
          this.likeCount += 1
          this.showMessage = true
        } else {
          this.message = json.message
          this.showMessage = true
        }
      })
    },
  },
}
</script>
  
<style scoped>
</style>
  