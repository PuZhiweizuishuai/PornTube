<!--
  二级评论卡片
  原先 215 行与 CommentCard 高度雷同，现在复用 BaseCommentCard，
  只保留二级评论特有样式（小头像、灰底、分界线）与「回复 @xx」展示。
-->
<template>
  <BaseCommentCard
    :comment="comment"
    :father="father"
    is-reply
    :show-comment="true"
    @comment="$emit('comment', $event)"
    @open="$emit('open', $event)"
  />
</template>

<script setup>
import BaseCommentCard from '@/components/card/comment/BaseCommentCard.vue'

defineProps({
  comment: {
    type: Object,
    default: null,
  },
  father: {
    type: Object,
    default: null,
  },
})

defineEmits(['comment', 'open'])
</script>
