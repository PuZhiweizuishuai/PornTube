<!--
  统一分页组件
  背景：全站 45 处 v-pagination，参数与 @update 处理各不相同（pageChange / handlePageChange /
  loadItems / fetchUsers…），且 length 取值有 Math.ceil(total/perPage) 之类的内联表达式。
  统一由本组件 + usePagination 承担：
    <Pagination v-model="page" :length="totalPages" @update:model-value="load" />
-->
<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 1,
  },
  length: {
    type: Number,
    default: 1,
  },
  totalVisible: {
    type: [Number, String],
    default: 7,
  },
  color: {
    type: String,
    default: 'primary',
  },
  rounded: {
    type: [Boolean, String],
    default: 'circle',
  },
  density: {
    type: String,
    default: 'default',
  },
  activeColor: {
    type: String,
    default: '',
  },
  /** 只有一页时是否隐藏 */
  hideOnSinglePage: {
    type: Boolean,
    default: false,
  },
  disabled: {
    type: Boolean,
    default: false,
  },
  /** 外层容器是否需要 justify-center + 上下留白（后台卡片里常见） */
  block: {
    type: Boolean,
    default: false,
  },
})

const emit = defineEmits(['update:modelValue', 'change'])

const visible = computed(() => !props.hideOnSinglePage || Number(props.length) > 1)

const safeLength = computed(() => Math.max(1, Number(props.length) || 1))

function onChange(value) {
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<template>
  <div v-if="visible" class="d-flex justify-center" :class="block ? 'pa-4' : 'my-4'">
    <v-pagination
      :model-value="modelValue"
      :length="safeLength"
      :total-visible="totalVisible"
      :color="color"
      :active-color="activeColor || undefined"
      :rounded="rounded"
      :density="density"
      :disabled="disabled"
      @update:model-value="onChange"
    />
  </div>
</template>
