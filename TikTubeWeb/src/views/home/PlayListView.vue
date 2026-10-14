<template>
  <v-container>
    <NotLoginCard v-if="userInfo.userData == null"></NotLoginCard>
    <div v-else>
      <v-row align="center" class="mb-4">
        <v-col>
          <h2 class="text-h5 font-weight-bold">
            <v-icon icon="mdi-heart" color="red" class="mr-2"></v-icon>
            收藏
          </h2>
          <p class="text-body-2 text-grey">
            <v-chip color="primary" variant="tonal" size="small">
              {{ currentLabelName ? `「${currentLabelName}」共` : '共' }} {{ totalItems }} 个稿件
            </v-chip>
          </p>
        </v-col>
      </v-row>

      <v-row>
        <!-- 收藏夹侧栏（与首页侧边导航同款 nav 风格，无外框卡片） -->
        <v-col cols="12" md="3" lg="2" class="pa-2">
          <div class="fav-panel">
            <div class="d-flex align-center px-3 mb-1 mt-2">
              <v-icon icon="mdi-bookmark-multiple" color="primary" size="20" class="mr-2" />
              <span class="text-subtitle-2 font-weight-bold">我的收藏夹</span>
              <v-spacer />
              <v-btn
                icon="mdi-plus"
                size="small"
                variant="tonal"
                color="primary"
                rounded="circle"
                title="新建收藏夹"
                @click="openCreateLabel"
              />
            </div>
            <v-list nav density="compact" class="fav-panel__list">
              <v-list-item
                :active="selectedLabelId === null"
                rounded="lg"
                @click="selectLabel(null)"
              >
                <template v-slot:title>
                  <span class="fav-label-title">
                    <v-icon icon="mdi-playlist-check" size="18" class="flex-shrink-0" />
                    <span class="text-truncate">全部收藏</span>
                  </span>
                </template>
              </v-list-item>
              <v-list-item
                v-for="label in labels"
                :key="label.id"
                :active="selectedLabelId === label.id"
                rounded="lg"
                @click="selectLabel(label.id)"
              >
                <template v-slot:title>
                  <span class="fav-label-title">
                    <v-icon
                      :icon="label.isPublic === 1 ? 'mdi-eye-outline' : 'mdi-lock-outline'"
                      :color="selectedLabelId === label.id ? 'primary' : label.isPublic === 1 ? 'success' : 'grey'"
                      size="18"
                      class="flex-shrink-0"
                    />
                    <span class="text-truncate" :title="label.name">{{ label.name }}</span>
                  </span>
                </template>
                <template v-slot:append>
                  <div class="d-flex align-center">
                    <span class="text-caption fav-panel__count">{{ label.count }}</span>
                    <v-menu :offset="4">
                      <template v-slot:activator="{ props }">
                        <v-btn
                          v-bind="props"
                          icon="mdi-dots-vertical"
                          size="x-small"
                          variant="text"
                          class="fav-panel__menu-btn"
                          @click.stop
                        />
                      </template>
                      <v-list density="compact">
                        <v-list-item @click="openEditLabel(label)">
                          <v-list-item-title>重命名 / 公开设置</v-list-item-title>
                        </v-list-item>
                        <v-list-item :disabled="label.isDefault === 1" @click="confirmDeleteLabel(label)">
                          <v-list-item-title :class="label.isDefault === 1 ? 'text-medium-emphasis' : 'text-error'">
                            删除收藏夹
                          </v-list-item-title>
                        </v-list-item>
                      </v-list>
                    </v-menu>
                  </div>
                </template>
              </v-list-item>
            </v-list>
          </div>
        </v-col>

        <!-- 收藏列表 -->
        <v-col cols="12" md="9" lg="10" class="pa-2">
          <!-- 加载状态 -->
          <v-row v-if="loading" justify="center" class="mt-8">
            <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
          </v-row>

          <!-- 空状态 -->
          <v-row v-else-if="videoList.length === 0" justify="center" class="mt-8">
            <v-col cols="12" sm="8" md="6" class="text-center">
              <v-icon size="64" color="grey">mdi-playlist-play</v-icon>
              <h2 class="text-h5 mt-4">{{ selectedLabelId === null ? '暂无收藏视频' : '该收藏夹是空的' }}</h2>
              <p class="text-body-1 text-medium-emphasis mt-2">快去收藏一些喜欢的视频吧</p>
            </v-col>
          </v-row>

          <!-- 视频列表 -->
          <v-row v-else>
            <v-col v-for="item in videoList" :key="item.favorite.id" cols="12" sm="6" xl="4" class="pa-2">
              <FavoriteCard
                :video="item.articleEntity"
                :favorite="item.favorite"
                :label-name="labelNameOf(item.favorite.favoritesLabelId)"
                @favorite-removed="handleFavoriteRemoved"
              />
            </v-col>
          </v-row>

          <!-- 分页器 -->
          <v-row v-if="videoList.length > 0" justify="center" class="mt-6">
            <Pagination
              rounded="circle"
              v-model="page"
              :length="length"
              :total-visible="7"
              color="primary"
              @update:model-value="pageChange"
            />
          </v-row>
        </v-col>
      </v-row>

      <!-- 新建 / 编辑收藏夹弹窗 -->
      <v-dialog v-model="labelDialog.show" max-width="420" persistent>
        <v-card>
          <v-card-title class="text-h6">{{ labelDialog.isNew ? '新建收藏夹' : '编辑收藏夹' }}</v-card-title>
          <v-card-text>
            <v-text-field
              v-model="labelDialog.name"
              label="收藏夹名称"
              maxlength="30"
              density="compact"
              hint="1-30 个字符"
              persistent-hint
            />
            <div class="d-flex align-center mt-4">
              <v-switch v-model="labelDialog.isPublic" color="success" hide-details density="compact" />
              <span class="ml-3 text-body-2">公开该收藏夹（公开后其他人在 TA 的用户首页可以看到）</span>
            </div>
            <div v-if="!labelDialog.isNew && labelDialog.isDefault" class="text-caption text-medium-emphasis mt-3">
              默认收藏夹不可删除，未指定收藏夹的稿件都会收藏到这里
            </div>
          </v-card-text>
          <v-card-actions>
            <v-spacer />
            <v-btn color="grey" variant="text" :disabled="labelDialog.saving" @click="labelDialog.show = false">
              取消
            </v-btn>
            <v-btn color="primary" variant="elevated" :loading="labelDialog.saving" @click="saveLabel">保存</v-btn>
          </v-card-actions>
        </v-card>
      </v-dialog>

      <!-- 删除收藏夹确认 -->
      <ConfirmDialog
        v-model="deleteDialog"
        title="确认删除"
        confirm-text="删除"
        :loading="deleteDialogSaving"
        persistent
        :text="`确定删除收藏夹「${deletingLabel ? deletingLabel.name : ''}」吗？其中的收藏将移入默认收藏夹。`"
        @confirm="doDeleteLabel"
      />

      <!-- 消息提示 -->
      <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top">
        {{ snackbar.text }}
        <template v-slot:actions>
          <v-btn variant="text" @click="snackbar.show = false">关闭</v-btn>
        </template>
      </v-snackbar>
    </div>
  </v-container>
</template>
  
<script>
import NotLoginCard from '@/components/card/NotLoginCard.vue'
import FavoriteCard from '@/components/card/FavoriteCard.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { useUserStore } from '@/stores/userStore'
import Pagination from '@/components/common/Pagination.vue'

export default {
  components: {
    NotLoginCard,
    FavoriteCard,
    ConfirmDialog,
    Pagination,
  },
  data() {
    return {
      userInfo: useUserStore(),
      page: 1,
      size: 24,
      videoList: [],
      length: 0,
      loading: false,
      totalItems: 0,
      labels: [],
      selectedLabelId: null,
      labelDialog: {
        show: false,
        isNew: true,
        id: null,
        name: '',
        isPublic: false,
        isDefault: false,
        saving: false,
      },
      deleteDialog: false,
      deleteDialogSaving: false,
      deletingLabel: null,
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
    }
  },
  computed: {
    currentLabelName() {
      const label = this.labels.find((item) => item.id === this.selectedLabelId)
      return label ? label.name : ''
    },
  },
  created() {
    if (this.$route.query.page === undefined) {
      this.page = 1
    } else {
      this.page = parseInt(this.$route.query.page)
    }
    if (this.$route.query.labelId !== undefined) {
      this.selectedLabelId = parseInt(this.$route.query.labelId)
    }
    if (this.userInfo.userData != null) {
      this.getLabelList()
      this.getFavoriteList()
    }
  },
  methods: {
    showMessage(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
    getLabelList() {
      this.httpGet('/favorites/label/list', (json) => {
        if (json.status === 200) {
          this.labels = json.data || []
        }
      })
    },
    async getFavoriteList() {
      this.loading = true
      let url = `/favorites/user/list?limit=${this.size}&page=${this.page}`
      if (this.selectedLabelId != null) {
        url += `&labelId=${this.selectedLabelId}`
      }
      try {
        await this.httpGet(url, (json) => {
          this.videoList = json.data.list
          this.page = json.data.currPage
          this.length = json.data.totalPage
          this.totalItems = json.data.totalCount || 0
        })
      } finally {
        this.loading = false
      }
    },
    selectLabel(labelId) {
      this.selectedLabelId = labelId
      this.page = 1
      this.pushQuery()
      this.getFavoriteList()
    },
    pageChange(value) {
      this.page = value
      this.pushQuery()
      this.getFavoriteList()
      window.scrollTo({
        top: 0,
        behavior: 'smooth',
      })
    },
    pushQuery() {
      const query = { page: this.page }
      if (this.selectedLabelId != null) {
        query.labelId = this.selectedLabelId
      }
      this.$router.push({ query })
    },
    labelNameOf(labelId) {
      if (labelId == null) {
        return ''
      }
      const label = this.labels.find((item) => item.id === labelId)
      return label ? label.name : ''
    },
    openCreateLabel() {
      this.labelDialog.isNew = true
      this.labelDialog.id = null
      this.labelDialog.name = ''
      this.labelDialog.isPublic = false
      this.labelDialog.isDefault = false
      this.labelDialog.show = true
    },
    openEditLabel(label) {
      this.labelDialog.isNew = false
      this.labelDialog.id = label.id
      this.labelDialog.name = label.name
      this.labelDialog.isPublic = label.isPublic === 1
      this.labelDialog.isDefault = label.isDefault === 1
      this.labelDialog.show = true
    },
    saveLabel() {
      const name = this.labelDialog.name.trim()
      if (!name) {
        this.showMessage('收藏夹名称不能为空', 'error')
        return
      }
      const url = this.labelDialog.isNew ? '/favorites/label/save' : '/favorites/label/update'
      const body = { name, isPublic: this.labelDialog.isPublic ? 1 : 0 }
      if (!this.labelDialog.isNew) {
        body.id = this.labelDialog.id
      }
      this.labelDialog.saving = true
      this.httpPost(
        url,
        body,
        (json) => {
          this.labelDialog.saving = false
          if (json.status === 200) {
            this.labelDialog.show = false
            this.showMessage(this.labelDialog.isNew ? '收藏夹已创建' : '已保存')
            this.getLabelList()
            if (!this.labelDialog.isNew) {
              this.getFavoriteList()
            }
          } else {
            this.showMessage(json.message || '保存失败', 'error')
          }
        },
        () => {
          this.labelDialog.saving = false
          this.showMessage('保存失败', 'error')
        }
      )
    },
    confirmDeleteLabel(label) {
      this.deletingLabel = label
      this.deleteDialog = true
    },
    doDeleteLabel() {
      if (!this.deletingLabel) {
        return
      }
      this.deleteDialogSaving = true
      this.httpPost(
        '/favorites/label/delete',
        { id: this.deletingLabel.id },
        (json) => {
          this.deleteDialogSaving = false
          this.deleteDialog = false
          if (json.status === 200) {
            this.showMessage('收藏夹已删除，其中的收藏已移入默认收藏夹')
            if (this.selectedLabelId === this.deletingLabel.id) {
              this.selectedLabelId = null
              this.page = 1
              this.pushQuery()
            }
            this.getLabelList()
            this.getFavoriteList()
          } else {
            this.showMessage(json.message || '删除失败', 'error')
          }
        },
        () => {
          this.deleteDialogSaving = false
          this.showMessage('删除失败', 'error')
        }
      )
    },
    handleFavoriteRemoved(favoriteId) {
      this.videoList = this.videoList.filter((item) => item.favorite.id !== favoriteId)
      if (this.videoList.length === 0 && this.page > 1) {
        this.page--
        this.getFavoriteList()
      }
      this.getLabelList()
    },
  },
}
</script>
  
<style scoped>
.v-pagination {
  background: transparent;
}

/* 侧栏跟随滚动吸顶（桌面端），与首页侧边导航一致的无边框风格 */
@media (min-width: 960px) {
  .fav-panel {
    position: sticky;
    top: calc(var(--app-header-height) + 16px);
    max-height: calc(100vh - var(--app-header-height) - 32px);
    overflow-y: auto;
  }
}

.fav-panel__list {
  background: transparent;
}

/* 图标与标题同一行，间距仅 6px，标题吃掉剩余宽度 */
.fav-label-title {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
  min-width: 0;
}

.fav-panel__list :deep(.v-list-item-title) {
  min-width: 0;
  overflow: hidden;
}

.fav-panel__list :deep(.v-list-item) {
  padding-inline: 10px;
}

.fav-panel__list :deep(.v-list-item-append) {
  margin-inline-start: 4px;
  flex-shrink: 0;
}

.fav-panel__count {
  color: rgba(var(--v-theme-on-surface), var(--v-medium-emphasis-opacity));
  margin-inline-end: 2px;
}

.fav-panel__menu-btn {
  opacity: 0.35;
  min-width: 24px;
  width: 24px;
  transition: opacity 0.15s ease-in-out;
}

.v-list-item:hover .fav-panel__menu-btn,
.fav-panel__menu-btn:focus-visible {
  opacity: 1;
}
</style>
