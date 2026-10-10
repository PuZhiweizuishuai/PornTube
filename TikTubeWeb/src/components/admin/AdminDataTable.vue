<!--
  后台 CRUD 通用表格
  背景：CommentControlView(697) / DanmukuControlView(756) / FileTableView(722) /
        AdvertisementView(749) / OpinionView(682) / ArticleListView(576) / UserList(1255)
        七个视图各自复制粘贴了一遍同样的骨架：
          v-toolbar + 筛选区 + v-data-table-server(headers/items/loading/no-data)
          + 独立 v-pagination + 若干确认对话框 + snackbar
  本组件抽出「表格 + 分页 + 空态」三件套，业务差异通过 headers 配置与具名插槽透传：

    <AdminDataTable
      v-model:page="page"
      :headers="headers"
      :items="items"
      :loading="loading"
      :total-pages="totalPages"
      :total-count="totalCount"
      @update:page="load"
    >
      <template #item.status="{ item }"> … </template>
      <template #no-data> … </template>
      <template #toolbar> 刷新 / 搜索 </template>
    </AdminDataTable>

  所有插槽都会原样透传给内部的 v-data-table-server，因此迁移时几乎不用改模板。
-->
<script setup>
import { computed, useSlots } from 'vue'
import Pagination from '@/components/common/Pagination.vue'

const slots = useSlots()

/** 这几个插槽由本组件自己消费，不再往内层表格透传，避免重复定义同一个插槽 */
const OWN_SLOTS = ['no-data', 'extra', 'empty']

const forwardedSlots = computed(() => Object.keys(slots).filter((name) => !OWN_SLOTS.includes(name)))

const props = defineProps({
  headers: {
    type: Array,
    required: true,
  },
  items: {
    type: Array,
    default: () => [],
  },
  loading: {
    type: Boolean,
    default: false,
  },
  /** 当前页码 */
  page: {
    type: Number,
    default: 1,
  },
  totalPages: {
    type: Number,
    default: 1,
  },
  totalCount: {
    type: [Number, String],
    default: 0,
  },
  pageSize: {
    type: Number,
    default: 20,
  },
  /** 是否显示内部分页（部分视图把分页放在卡片底部时需要关掉） */
  showPagination: {
    type: Boolean,
    default: true,
  },
  hideDefaultFooter: {
    type: Boolean,
    default: true,
  },
  hover: {
    type: Boolean,
    default: true,
  },
  elevation: {
    type: [String, Number],
    default: 0,
  },
  mobileBreakpoint: {
    type: [String, Number],
    default: 'md',
  },
  /** 传给内部表格的额外属性，如 items-per-page / sort-by 等 */
  itemValue: {
    type: String,
    default: 'id',
  },
  emptyText: {
    type: String,
    default: '暂无数据',
  },
  showRefreshOnEmpty: {
    type: Boolean,
    default: false,
  },
  density: {
    type: String,
    default: 'default',
  },
})

const emit = defineEmits(['update:page', 'change', 'refresh'])

function onChange(value) {
  emit('update:page', value)
  emit('change', value)
}
</script>

<template>
  <div>
    <!-- 工具栏插槽：标题、刷新、筛选 -->
    <slot name="extra" />

    <v-data-table-server
      :headers="headers"
      :items="items"
      :items-length="Number(totalCount) || items.length"
      :items-per-page="pageSize"
      :loading="loading"
      :hover="hover"
      :density="density"
      :item-value="itemValue"
      :elevation="elevation"
      :mobile-breakpoint="mobileBreakpoint"
      :hide-default-footer="hideDefaultFooter"
      class="rounded-lg"
    >
      <!-- 透传所有自定义列插槽：#[`item.xxx`]、#top、#bottom 等原样交给内层表格 -->
      <template v-for="name in forwardedSlots" #[name]="slotProps">
        <slot :name="name" v-bind="slotProps || {}" />
      </template>

      <template #no-data>
        <slot name="no-data">
          <div class="d-flex flex-column align-center pa-4">
            <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
            <span class="text-body-1 text-grey">{{ emptyText }}</span>
            <v-btn v-if="showRefreshOnEmpty" color="primary" class="mt-4" @click="emit('refresh')">
              刷新数据
            </v-btn>
          </div>
        </slot>
      </template>
    </v-data-table-server>

    <Pagination
      v-if="showPagination"
      :model-value="page"
      :length="Math.max(1, Number(totalPages) || 1)"
      block
      rounded
      density="comfortable"
      @update:model-value="onChange"
    />
  </div>
</template>

<style scoped>
:deep(.v-data-table-server th),
:deep(.v-data-table th) {
  font-weight: 600 !important;
}
</style>
