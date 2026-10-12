<!--
  一级评论卡片
  原先 259 行自绘模板 + 重复的点赞/点踩逻辑，现在全部下沉到 BaseCommentCard，
  这里只保留一级评论特有的：up 主标识、回复抽屉（二级评论弹窗）、锚点。
-->
<template>
  <div>
    <BaseCommentCard
      :comment="comment"
      :author-id="authorId"
      :show-comment="showcomment"
      @comment="openSecond"
      @open="$emit('open', $event)"
    >
      <template #header-extra>
        <v-chip
          v-if="comment && comment.userId == authorId"
          size="x-small"
          variant="tonal"
          color="primary"
        >
          楼主
        </v-chip>
      </template>
    </BaseCommentCard>

    <v-dialog v-model="showSecond" max-width="1000">
      <v-card>
        <v-card-text class="pa-0">
          <SecondComment :key="secondCommendKey" :father="comment" />
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import BaseCommentCard from '@/components/card/comment/BaseCommentCard.vue'
import SecondComment from '@/components/card/comment/SecondComment.vue'

defineProps({
  comment: {
    type: Object,
    default: null,
  },
  authorId: {
    type: Number,
    default: -1,
  },
  showcomment: {
    type: Boolean,
    default: true,
  },
  ratings: {
    type: Boolean,
    default: false,
  },
  type: {
    type: Number,
    default: 1,
  },
})

defineEmits(['open'])

const showSecond = ref(false)
const secondCommendKey = ref(0)

function openSecond() {
  secondCommendKey.value = Math.random() * 10000
  showSecond.value = true
}
</script>
