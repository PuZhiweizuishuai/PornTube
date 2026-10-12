<!--
  二级评论抽屉
  原先 236 行把「编辑器 + 排序 tab + 列表 + 空态 + 分页 + 提交 + 本地插入」整套逻辑
  与 views/comment/VideoComment.vue 各写一遍，现已统一到 useCommentList：
  本文件只保留二级评论特有的「回复 @xx」占位符与滚动到编辑器行为。
-->
<template>
  <v-card flat>
    <div id="commentTop" ref="commentTop" />
    <v-container>
      <v-row>
        <v-col>
          <div class="d-flex align-center">
            <h3 class="text-h6 font-weight-medium">{{ comments.total }} 条回复</h3>
            <v-spacer></v-spacer>
          </div>
        </v-col>
      </v-row>

      <v-row class="mb-4">
        <v-col>
          <SecondCommentVditor
            :key="secondCommentKey"
            ref="secondCommentView"
            :idname="`second-comment-${father?.id}`"
            :placeholder="commentPlaceholder"
            @vditor-input="getSecondCommentText"
          />
          <div class="d-flex justify-end mt-3">
            <v-btn
              color="primary"
              rounded="pill"
              :loading="comments.submitting"
              @click="submit"
              >回复</v-btn
            >
          </div>
        </v-col>
      </v-row>

      <v-divider class="my-4"></v-divider>

      <v-row>
        <v-col>
          <v-tabs v-model="comments.sort" color="primary" bg-color="transparent">
            <v-tab :value="1" @click="comments.setSort(1)">时间倒序</v-tab>
            <v-tab :value="0" @click="comments.setSort(0)">评论时间</v-tab>
            <v-tab :value="2" @click="comments.setSort(2)">最多点赞</v-tab>
            <v-tab :value="3" @click="comments.setSort(3)">最多评论</v-tab>
          </v-tabs>
        </v-col>
      </v-row>

      <v-divider class="mb-4"></v-divider>

      <v-row v-if="comments.isEmpty" justify="center" align="center" class="my-8">
        <v-icon icon="mdi-message-text-outline" size="28" class="text-medium-emphasis me-2" />
        <span class="text-body-2 text-medium-emphasis">暂无评论，来抢个沙发吧！</span>
      </v-row>

      <template v-else>
        <v-row v-for="item in comments.items" :key="item.id">
          <v-col>
            <Card
              :father="father"
              :comment="item"
              @comment="getComment"
              @open="showSecondReportDialogFun"
            />
          </v-col>
        </v-row>
      </template>

      <Pagination
        v-model="comments.page"
        :length="comments.length"
        @update:model-value="comments.load"
      />
    </v-container>

    <v-dialog v-model="showSecondReportDialog" width="50vh">
      <OpinionCard
        :targetId="selectSecondComment.id"
        :typeNum="1"
        :target-title="selectSecondComment.comment"
        :isReport="true"
        @close="showSecondReportDialog = false"
      />
    </v-dialog>

    <AppSnackbar :state="toast" />
  </v-card>
</template>

<script>
import Card from '@/components/card/comment/SecondCard.vue'
import SecondCommentVditor from '@/components/vditor/VditorComment.vue'
import OpinionCard from '@/components/card/OpinionCard.vue'
import Pagination from '@/components/common/Pagination.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useCommentList } from '@/composables/useCommentList'
import { useMessage } from '@/composables/useMessage'

export default {
  name: 'SecondComment',
  components: {
    Card,
    SecondCommentVditor,
    OpinionCard,
    Pagination,
    AppSnackbar,
  },
  props: {
    father: {
      type: Object,
      default: null,
    },
    type: {
      type: Number,
      default: 2,
    },
  },
  setup(props) {
    const { toast, showMessage } = useMessage()
    const comments = useCommentList({
      articleId: () => props.father?.articleId,
      fatherId: () => props.father?.id,
      type: props.type || 2,
      size: 10,
    })
    return { comments, toast, showMessage }
  },
  data() {
    return {
      commentPlaceholder: '请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。',
      secondCommentKey: 0,
      showSecondReportDialog: false,
      selectSecondComment: null,
    }
  },
  methods: {
    showSecondReportDialogFun(value) {
      this.selectSecondComment = value
      this.showSecondReportDialog = true
    },
    getComment(value) {
      if (this.$refs.commentTop) this.$refs.commentTop.scrollIntoView({ behavior: 'smooth' })
      this.commentPlaceholder = '回复 @' + value.username + ': ' + value.comment
      this.secondCommentKey++
      this.comments.reply(value)
    },
    getSecondCommentText(value) {
      this.comments.draft = value
    },
    async submit() {
      const result = await this.comments.submit()
      this.showMessage(result.message, result.ok ? 'success' : 'error')
      if (result.ok) {
        this.$refs.secondCommentView?.setTextValue('')
        this.commentPlaceholder = '请自觉遵守互联网相关的政策法规，严禁发布色情、暴力、反动的言论。'
        this.comments.clearReply()
      }
    },
  },
}
</script>
