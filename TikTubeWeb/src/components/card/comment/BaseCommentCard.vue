<!--
  评论卡片基类组件
  背景：CommentCard.vue(259 行) 与 SecondCard.vue(215 行) 模板 ~85% 相同，
        点赞/点踩/查询点赞状态/未登录提示/snackbar 逻辑更是逐行重复。
  现在两者都基于本组件（它内部已经接好 useLike + useMessage + UA 解析 + 时间格式化），
  各自只负责「一级 vs 二级」的排版差异与额外操作按钮。

  插槽：
    #header-extra    标题行右侧额外 chip（一级评论用它放「楼主」）
    #content-above   正文上方（二级评论用它放「回复 @某人」）
    #actions-extra   操作区额外按钮
    #extra           卡片底部额外部件（一级评论用它挂二级评论弹窗）
-->
<script setup>
import { computed } from 'vue'
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useLike } from '@/composables/useLike'
import { useMessage } from '@/composables/useMessage'
import { formatDate, getUaBrief } from '@/utils/format'

const props = defineProps({
  /** 评论对象 */
  comment: {
    type: Object,
    default: null,
  },
  /** 二级评论的父评论，用于判断是否展示「回复 @xx」 */
  father: {
    type: Object,
    default: null,
  },
  /** 是否为二级评论（影响头像尺寸、背景色、是否展示分界线） */
  isReply: {
    type: Boolean,
    default: false,
  },
  /** 是否展示「评论」按钮（二级评论不可再展开下一层） */
  showComment: {
    type: Boolean,
    default: true,
  },
  /** like 的业务类型，沿用后端约定：1 */
  likeType: {
    type: Number,
    default: 1,
  },
  /** 是否展示举报按钮 */
  showReport: {
    type: Boolean,
    default: true,
  },
  /** 一级评论用它标记是否为 up 主 */
  authorId: {
    type: Number,
    default: -1,
  },
})

const emit = defineEmits(['comment', 'open'])

const { toast, showMessage } = useMessage()
// 拆解后可以直接在模板里用 liked / likeCount（顶层 ref 会自动解包）
const { liked, likeCount, loading: likeLoading, toggle: toggleLikeRequest } = useLike(
  () => props.comment?.id,
  { type: props.likeType, count: () => props.comment?.likeCount || 0 }
)

const uaLabel = computed(() => getUaBrief(props.comment?.ua))
const createTimeLabel = computed(() => formatDate(props.comment?.createTime))

/** 是否需要展示「回复 @某人」：存在父评论且当前评论不是直接回复父评论 */
const showReplyTarget = computed(
  () => props.father && props.comment && props.comment.parentCommentId !== props.father.id
)

async function toggleLike() {
  const result = await toggleLikeRequest()
  showMessage(result.message, result.ok ? 'success' : 'error')
}

function report() {
  emit('open', props.comment)
}

function reply() {
  emit('comment', props.comment)
}
</script>

<template>
  <v-card
    v-if="comment"
    variant="flat"
    class="pa-3 my-2"
    :color="isReply ? 'grey-lighten-4' : 'grey-lighten-5'"
    rounded="lg"
  >
    <v-row>
      <v-col cols="12" class="pb-0">
        <v-row no-gutters>
          <v-col cols="auto">
            <router-link :to="`/user/${comment.userId}`">
              <v-avatar :size="isReply ? 32 : 45" class="mr-3">
                <v-img :src="comment.avatarUrl" />
              </v-avatar>
            </router-link>
          </v-col>
          <v-col>
            <div class="d-flex align-center">
              <router-link :to="`/user/${comment.userId}`" class="text-decoration-none">
                <span :class="isReply ? 'text-subtitle-2' : 'text-subtitle-1'" class="font-weight-medium">
                  {{ comment.username }}
                </span>
              </router-link>
              <slot name="header-extra" />
              <v-chip class="ml-2" color="primary" size="x-small" text-color="white">
                {{ uaLabel }}
              </v-chip>
              <v-spacer />
              <span class="text-caption text-grey">{{ createTimeLabel }}</span>
            </div>
          </v-col>
        </v-row>
      </v-col>
    </v-row>

    <v-row>
      <v-col class="pl-12 py-2">
        <div v-if="showReplyTarget" class="text-body-2 text-primary mb-1">
          回复
          <router-link
            :to="`/user/${comment.parentUserId}`"
            class="text-decoration-none"
            target="_blank"
          >
            @{{ comment.parentUserName }}:
          </router-link>
        </div>
        <slot name="content-above" />
        <ShowMarkdown :markdown="comment.comment" :anchor="0" />
      </v-col>
    </v-row>

    <!-- 操作区 -->
    <v-row>
      <v-col>
        <div class="d-flex justify-end align-center">
          <template v-if="showComment">
            <v-tooltip text="评论" location="top">
              <template #activator="{ props: tipProps }">
                <v-btn
                  v-bind="tipProps"
                  variant="text"
                  :color="isReply ? 'indigo' : 'blue'"
                  icon="mdi-comment"
                  size="small"
                  class="mr-1"
                  @click="reply"
                />
              </template>
            </v-tooltip>
            <span class="text-body-2 mr-4">{{ comment.commentCount }}</span>
          </template>

          <v-tooltip text="赞" location="top">
            <template #activator="{ props: tipProps }">
              <v-btn
                v-bind="tipProps"
                size="small"
                :variant="liked ? 'flat' : 'text'"
                :color="liked ? 'success' : 'green'"
                icon="mdi-thumb-up"
                class="mr-1"
                :loading="likeLoading"
                @click="toggleLike"
              />
            </template>
          </v-tooltip>
          <span class="text-body-2" :class="{ 'text-success': liked }">{{ likeCount }}</span>

          <slot name="actions-extra" />

          <v-btn
            v-if="showReport"
            class="ml-2"
            color="red"
            size="small"
            variant="text"
            @click="report"
          >
            举报
          </v-btn>
        </div>
      </v-col>
    </v-row>

    <v-divider v-if="isReply" class="mt-2" />

    <slot name="extra" />

    <AppSnackbar :state="toast" />
  </v-card>
</template>
