<!--
  稿件评论区（一级评论）
  原先 220 行与 SecondComment.vue 重复实现「编辑器 + 排序 + 列表 + 分页 + 提交」。
  现在统一由 useCommentList 提供数据，本文件只负责一级评论的排版与举报弹窗。
-->
<template>
  <v-card>
    <v-container>
      <v-row>
        <v-col>
          <div class="d-flex align-center mb-4">
            <h2 class="text-h5 font-weight-medium">{{ comments.total }} 条评论</h2>
            <v-spacer></v-spacer>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col class="mb-4">
          <Vditor ref="commentVditor" :height="150" @vditor-input="getCommentText" />
          <div class="d-flex justify-end mt-3">
            <v-btn
              color="primary"
              rounded="pill"
              :loading="comments.submitting"
              @click="submitComment"
              >发表评论</v-btn
            >
          </div>
        </v-col>
      </v-row>

      <v-divider class="mb-4"></v-divider>

      <v-row class="mb-3">
        <v-col>
          <v-tabs v-model="comments.sort" color="primary" bg-color="transparent">
            <v-tab :value="1" @click="comments.setSort(1)">时间倒序</v-tab>
            <v-tab :value="2" @click="comments.setSort(2)">最多点赞</v-tab>
            <v-tab :value="3" @click="comments.setSort(3)">最多评论</v-tab>
          </v-tabs>
        </v-col>
      </v-row>

      <v-divider></v-divider>

      <v-row v-if="comments.isEmpty" justify="center" align="center" class="my-8">
        <v-icon icon="mdi-message-text-outline" size="28" class="text-medium-emphasis me-2" />
        <span class="text-body-2 text-medium-emphasis">暂无评论，来抢个沙发吧！</span>
      </v-row>

      <template v-else>
        <v-row v-for="item in comments.items" :key="item.id">
          <v-col>
            <CommentCard :author-id="authorId" :comment="item" @open="showReportDialogFun" />
          </v-col>
        </v-row>
      </template>

      <Pagination
        v-model="comments.page"
        :length="comments.length"
        @update:model-value="comments.load"
      />

      <v-divider class="mt-2"></v-divider>
    </v-container>

    <v-dialog v-model="showReportDialog" width="50vh">
      <OpinionCard
        :targetId="selectComment.id"
        :typeNum="1"
        :target-title="selectComment.comment"
        :isReport="true"
        @close="showReportDialog = false"
      />
    </v-dialog>

    <AppSnackbar :state="toast" />
  </v-card>
</template>

<script>
import Vditor from '@/components/vditor/VditorComponents.vue'
import CommentCard from '@/components/card/comment/CommentCard.vue'
import OpinionCard from '@/components/card/OpinionCard.vue'
import Pagination from '@/components/common/Pagination.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useCommentList } from '@/composables/useCommentList'
import { useMessage } from '@/composables/useMessage'

export default {
  name: 'VideoComment',
  components: {
    Vditor,
    CommentCard,
    OpinionCard,
    Pagination,
    AppSnackbar,
  },
  props: {
    count: {
      type: Number,
      default: 0,
    },
    article: {
      type: Number,
      default: 0,
    },
    authorId: {
      type: Number,
      default: 0,
    },
    typenum: {
      type: Number,
      default: 1,
    },
  },
  setup(props) {
    const { toast, showMessage } = useMessage()
    const comments = useCommentList({
      articleId: () => props.article,
      type: props.typenum || 1,
      size: 15,
      sort: 1,
    })
    comments.setTotal(props.count)
    return { comments, toast, showMessage }
  },
  data() {
    return {
      showReportDialog: false,
      selectComment: null,
    }
  },
  methods: {
    showReportDialogFun(value) {
      this.selectComment = value
      this.showReportDialog = true
    },
    getCommentText(value) {
      this.comments.draft = value
    },
    async submitComment() {
      const result = await this.comments.submit()
      this.showMessage(result.message, result.ok ? 'success' : 'error')
      if (result.ok) this.$refs.commentVditor?.setTextValue('')
    },
  },
}
</script>
