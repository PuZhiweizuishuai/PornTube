<!--
  评论卡片基类组件
  背景：CommentCard.vue(259 行) 与 SecondCard.vue(215 行) 模板 ~85% 相同，
        点赞/点踩/查询点赞状态/未登录提示/snackbar 逻辑更是逐行重复。
  现在两者都基于本组件（它内部已经接好 useLike + useMessage + UA 解析 + 时间格式化），
  各自只负责「一级 vs 二级」的排版差异与额外操作按钮。

  排版参考 YouTube：卡片不铺底色（只在 hover 时轻微高亮），头像一列、内容一列，
  操作按钮紧跟正文左对齐；二级评论用左侧竖线 + 缩进表达层级，不再用灰底。

  插槽：
    #header-extra    标题行右侧额外 chip（一级评论用它放「楼主」）
    #content-above   正文上方（二级评论用它放「回复 @某人」）
    #actions-extra   操作区额外按钮
    #extra           卡片底部额外部件
-->
<script setup>
import { computed } from 'vue'
import ShowMarkdown from '@/components/vditor/ShowMarkdown.vue'
import AppSnackbar from '@/components/common/AppSnackbar.vue'
import { useLike } from '@/composables/useLike'
import { useMessage } from '@/composables/useMessage'
import TimeUtil from '@/utils/time-util'
import { getUaBrief } from '@/utils/format'

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
  /** 是否为二级评论（影响头像尺寸与缩进） */
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
const relativeTimeLabel = computed(() => TimeUtil.timeToNowStrning(props.comment?.createTime))

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
    flat
    rounded="lg"
    variant="text"
    :class="['comment-card', isReply && 'comment-card--reply']"
  >
    <div class="d-flex pa-3">
      <router-link :to="`/user/${comment.userId}`" class="flex-shrink-0 me-3">
        <v-avatar :size="isReply ? 28 : 40" color="surface-variant">
          <v-img v-if="comment.avatarUrl" :src="comment.avatarUrl" :alt="comment.username" cover />
          <v-icon v-else :size="isReply ? 18 : 24">mdi-account-circle-outline</v-icon>
        </v-avatar>
      </router-link>

      <div class="comment-card__main">
        <!-- 作者行 -->
        <div class="d-flex align-center flex-wrap" style="column-gap: 8px">
          <router-link
            :to="`/user/${comment.userId}`"
            class="comment-card__author text-body-2 font-weight-medium text-decoration-none"
          >
            {{ comment.username }}
          </router-link>
          <slot name="header-extra" />
          <span class="text-caption text-medium-emphasis">
            {{ relativeTimeLabel }}
          </span>
          <span class="text-caption text-medium-emphasis comment-card__ua">{{ uaLabel }}</span>
        </div>

        <!-- 回复对象 -->
        <div v-if="showReplyTarget" class="comment-card__reply-to text-caption mt-1">
          <v-icon icon="mdi-reply" size="14" class="me-1" />
          回复
          <router-link :to="`/user/${comment.parentUserId}`" class="text-decoration-none ms-1">
            @{{ comment.parentUserName }}
          </router-link>
        </div>

        <slot name="content-above" />

        <div class="comment-card__content text-body-2 mt-1">
          <ShowMarkdown :markdown="comment.comment" :anchor="0" />
        </div>

        <!-- 操作区：紧跟正文左侧 -->
        <div class="comment-card__actions d-flex align-center mt-1">
          <v-tooltip text="赞" location="top">
            <template #activator="{ props: tipProps }">
              <v-btn
                v-bind="tipProps"
                size="small"
                variant="text"
                :class="['comment-card__action', liked && 'comment-card__action--liked']"
                :icon="liked ? 'mdi-thumb-up' : 'mdi-thumb-up-outline'"
                :loading="likeLoading"
                @click="toggleLike"
              />
            </template>
          </v-tooltip>
          <span class="comment-card__count text-caption me-4">{{ likeCount }}</span>

          <template v-if="showComment">
            <v-tooltip text="评论" location="top">
              <template #activator="{ props: tipProps }">
                <v-btn
                  v-bind="tipProps"
                  size="small"
                  variant="text"
                  class="comment-card__action"
                  icon="mdi-comment-outline"
                  @click="reply"
                />
              </template>
            </v-tooltip>
            <span class="comment-card__count text-caption me-4">{{ comment.commentCount }}</span>
          </template>

          <slot name="actions-extra" />

          <v-spacer />

          <v-btn
            v-if="showReport"
            class="comment-card__action"
            size="x-small"
            variant="text"
            prepend-icon="mdi-flag-outline"
            @click="report"
          >
            举报
          </v-btn>
        </div>
      </div>
    </div>

    <slot name="extra" />

    <AppSnackbar :state="toast" />
  </v-card>
</template>

<style scoped>
/* 卡片本身不铺底色，hover 才出现浅层高亮（两套主题都用 on-surface 透明度） */
.comment-card {
  background-color: transparent;
  transition: background-color var(--app-transition-fast) var(--app-easing);
}

.comment-card:hover {
  background-color: rgba(var(--v-theme-on-surface), var(--v-hover-opacity));
}

/* 二级评论：左侧竖线 + 缩进表达层级 */
.comment-card--reply {
  margin-left: 12px;
  border-inline-start: 2px solid rgb(var(--v-theme-outline));
  border-start-start-radius: 0;
}

.comment-card__main {
  min-width: 0;
  flex-grow: 1;
}

.comment-card__author {
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
}

.comment-card__author:hover {
  color: rgb(var(--v-theme-on-surface));
}

.comment-card__ua {
  margin-inline-start: auto;
}

@media (max-width: 600px) {
  .comment-card__ua {
    display: none;
  }
}

.comment-card__reply-to {
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
}

.comment-card__reply-to a {
  color: rgb(var(--v-theme-primary));
}

/* 评论正文：压掉 vditor 预览的默认字号与首尾留白 */
.comment-card__content :deep(.vditor-reset) {
  font-size: 0.875rem;
  line-height: 1.6;
}

.comment-card__content :deep(.vditor-reset > *:first-child) {
  margin-top: 0 !important;
}

.comment-card__content :deep(.vditor-reset > *:last-child) {
  margin-bottom: 0 !important;
}

.comment-card__count {
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
}

/* Vuetify 只会为主题里的标准色名生成 text-* 工具类，on-surface-variant 这种
   自定义名传进 color 属性不会生效，所以按钮颜色在这里显式声明。 */
.comment-card__action {
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
}

.comment-card__action:hover {
  color: rgb(var(--v-theme-on-surface));
}

.comment-card__action--liked,
.comment-card__action--liked:hover {
  color: rgb(var(--v-theme-primary));
}
</style>
