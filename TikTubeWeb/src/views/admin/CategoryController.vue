<template>
  <v-container class="pa-4" fluid>
    <!-- 页面标题和操作栏 -->
    <v-sheet class="mb-6 pa-3 rounded-lg" color="background" elevation="1">
      <div class="d-flex justify-space-between align-center flex-wrap">
        <div>
          <h1 class="text-h4 text-md-h3 font-weight-bold text-primary mb-1">分类管理</h1>
          <p class="text-body-2 text-medium-emphasis">管理视频和内容的分类层级</p>
        </div>
        <v-btn
          color="primary"
          variant="flat"
          prepend-icon="mdi-plus"
          @click="openDialog('add', null)"
          class="text-capitalize"
        >
          添加一级分类
        </v-btn>
      </div>
    </v-sheet>

    <!-- 加载中状态 -->
    <v-sheet v-if="loading" class="d-flex justify-center align-center my-10" height="300">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </v-sheet>

    <!-- 内容区域 -->
    <v-row v-else>
      <!-- 左侧：分类卡片概览 -->
      <v-col cols="12" lg="4" order="2" order-lg="1">
        <v-sheet class="pa-3 mb-6 rounded-lg" color="background" elevation="1">
          <h2 class="text-h6 font-weight-bold mb-3">分类概览</h2>

          <div v-if="rootCategories.length > 0" class="category-cards">
            <v-hover
              v-for="category in rootCategories"
              :key="category.id"
              v-slot="{ isHovering, props }"
            >
              <v-card
                v-bind="props"
                :elevation="isHovering ? 4 : 1"
                class="mb-3 overflow-hidden transition-swing"
                :class="{ 'border-primary': isHovering }"
              >
                <v-card-item class="pa-3">
                  <template v-slot:prepend>
                    <v-avatar color="primary" variant="tonal" class="mr-3">
                      <v-icon>mdi-folder</v-icon>
                    </v-avatar>
                  </template>

                  <v-card-title class="px-0 py-1 text-h6">{{ category.name }}</v-card-title>
                  <v-card-subtitle class="px-0 text-medium-emphasis"
                    >ID: {{ category.id }} · 排序值: {{ category.sort }}</v-card-subtitle
                  >
                </v-card-item>

                <v-card-text class="pb-0">
                  <p class="text-body-2 text-truncate-2">{{ category.describes || '暂无描述' }}</p>
                </v-card-text>

                <v-divider class="mt-3"></v-divider>

                <v-card-actions>
                  <v-chip size="small" color="primary-lighten-4" variant="tonal" class="mr-auto">
                    {{ category.children?.length || 0 }}个子分类
                  </v-chip>

                  <v-btn
                    icon="mdi-plus-circle-outline"
                    size="small"
                    color="primary"
                    variant="text"
                    @click.stop="openDialog('addSub', category)"
                    v-tooltip="'添加子分类'"
                  ></v-btn>
                  <v-btn
                    icon="mdi-pencil-outline"
                    size="small"
                    color="info"
                    variant="text"
                    @click.stop="openDialog('edit', category)"
                    v-tooltip="'编辑分类'"
                  ></v-btn>
                  <v-btn
                    icon="mdi-delete-outline"
                    size="small"
                    color="error"
                    variant="text"
                    @click.stop="confirmDelete(category)"
                    v-tooltip="'删除分类'"
                  ></v-btn>
                </v-card-actions>
              </v-card>
            </v-hover>
          </div>

          <v-sheet
            v-else
            class="d-flex flex-column align-center justify-center py-10 rounded"
            color="grey-lighten-4"
          >
            <v-icon size="x-large" color="grey" class="mb-3">mdi-folder-outline</v-icon>
            <p class="text-body-1 text-medium-emphasis">暂无分类数据</p>
          </v-sheet>
        </v-sheet>
      </v-col>

      <!-- 右侧：详细分类树 -->
      <v-col cols="12" lg="8" order="1" order-lg="2">
        <v-sheet class="pa-3 rounded-lg" color="background" elevation="1">
          <h2 class="text-h6 font-weight-bold mb-3">分类详情</h2>

          <v-expansion-panels
            v-if="rootCategories.length > 0"
            variant="accordion"
            class="category-panel"
          >
            <v-expansion-panel
              v-for="category in rootCategories"
              :key="category.id"
              class="mb-2"
              rounded="lg"
            >
              <v-expansion-panel-title>
                <v-row no-gutters class="align-center">
                  <v-col cols="1" class="text-subtitle-2 font-weight-medium d-none d-sm-flex">{{
                    category.id
                  }}</v-col>
                  <v-col cols="12" sm="3" md="4" class="text-subtitle-1 font-weight-bold">{{
                    category.name
                  }}</v-col>
                  <v-col
                    cols="12"
                    sm="6"
                    md="6"
                    class="text-body-2 text-truncate d-none d-sm-flex"
                    >{{ category.describes }}</v-col
                  >
                  <v-col cols="auto" sm="1" class="text-center ml-auto">
                    <v-chip size="small" color="primary" variant="tonal">{{
                      category.sort
                    }}</v-chip>
                  </v-col>
                </v-row>
              </v-expansion-panel-title>

              <v-expansion-panel-text>
                <!-- 子分类标题栏 -->
                <div class="d-flex justify-space-between align-center mb-4">
                  <h3 class="text-subtitle-1 font-weight-bold">
                    <v-icon start size="small">mdi-folder-open-outline</v-icon>
                    子分类列表
                  </h3>
                  <v-btn
                    size="small"
                    color="primary"
                    variant="tonal"
                    prepend-icon="mdi-plus"
                    @click.stop="openDialog('addSub', category)"
                    class="text-capitalize"
                  >
                    添加子分类
                  </v-btn>
                </div>

                <!-- 子分类表头 -->
                <v-sheet color="grey-lighten-4" class="mb-2 rounded-t pa-2 d-none d-sm-block">
                  <v-row no-gutters align="center">
                    <v-col cols="1" class="text-caption font-weight-bold">ID</v-col>
                    <v-col cols="3" class="text-caption font-weight-bold">名称</v-col>
                    <v-col cols="6" class="text-caption font-weight-bold">描述</v-col>
                    <v-col cols="1" class="text-caption font-weight-bold text-center">排序值</v-col>
                    <v-col cols="1" class="text-caption font-weight-bold text-center">操作</v-col>
                  </v-row>
                </v-sheet>

                <!-- 子分类列表 -->
                <template v-if="category.children && category.children.length > 0">
                  <v-hover
                    v-for="subItem in category.children"
                    :key="subItem.id"
                    v-slot="{ isHovering, props }"
                  >
                    <v-sheet
                      v-bind="props"
                      class="mb-2 pa-2 rounded-lg"
                      :color="isHovering ? 'grey-lighten-3' : 'background'"
                      :elevation="isHovering ? 2 : 0"
                    >
                      <div class="d-block d-sm-none pa-2">
                        <div class="d-flex justify-space-between align-center">
                          <div class="text-subtitle-1 font-weight-medium">{{ subItem.name }}</div>
                          <v-chip size="small" color="primary" variant="tonal">{{
                            subItem.sort
                          }}</v-chip>
                        </div>
                        <div class="text-body-2 text-medium-emphasis mb-2">
                          {{ subItem.describes }}
                        </div>
                        <div class="d-flex justify-end">
                          <v-btn
                            icon="mdi-pencil-outline"
                            size="small"
                            color="info"
                            variant="text"
                            @click.stop="openDialog('edit', subItem)"
                          ></v-btn>
                          <v-btn
                            icon="mdi-delete-outline"
                            size="small"
                            color="error"
                            variant="text"
                            @click.stop="confirmDelete(subItem)"
                          ></v-btn>
                        </div>
                      </div>

                      <v-row no-gutters align="center" class="d-none d-sm-flex">
                        <v-col cols="1" class="text-body-2">{{ subItem.id }}</v-col>
                        <v-col cols="3" class="text-body-2 font-weight-medium">{{
                          subItem.name
                        }}</v-col>
                        <v-col cols="6" class="text-body-2 text-truncate">{{
                          subItem.describes
                        }}</v-col>
                        <v-col cols="1" class="text-body-2 text-center">
                          <v-chip size="x-small" color="primary" variant="flat">{{
                            subItem.sort
                          }}</v-chip>
                        </v-col>
                        <v-col cols="1" class="d-flex justify-center">
                          <v-btn
                            icon="mdi-pencil-outline"
                            size="x-small"
                            color="info"
                            variant="text"
                            density="comfortable"
                            @click.stop="openDialog('edit', subItem)"
                            v-tooltip="'编辑子分类'"
                          ></v-btn>
                          <v-btn
                            icon="mdi-delete-outline"
                            size="x-small"
                            color="error"
                            variant="text"
                            density="comfortable"
                            @click.stop="confirmDelete(subItem)"
                            v-tooltip="'删除子分类'"
                          ></v-btn>
                        </v-col>
                      </v-row>
                    </v-sheet>
                  </v-hover>
                </template>

                <!-- 无子分类显示 -->
                <v-sheet
                  v-else
                  color="grey-lighten-4"
                  class="d-flex justify-center align-center pa-6 rounded-lg"
                >
                  <div class="text-center">
                    <v-icon size="large" color="grey-lighten-1" class="mb-2"
                      >mdi-folder-open-outline</v-icon
                    >
                    <p class="text-body-2 text-medium-emphasis">暂无子分类，点击上方按钮添加</p>
                  </div>
                </v-sheet>
              </v-expansion-panel-text>
            </v-expansion-panel>
          </v-expansion-panels>

          <!-- 无数据时显示 -->
          <v-sheet
            v-else
            class="d-flex flex-column align-center justify-center py-16 rounded"
            color="grey-lighten-4"
          >
            <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-folder-outline</v-icon>
            <h3 class="text-h6 text-medium-emphasis mb-2">暂无分类数据</h3>
            <p class="text-body-2 text-medium-emphasis mb-4">点击右上角按钮添加一级分类</p>
            <v-btn
              color="primary"
              variant="tonal"
              prepend-icon="mdi-plus"
              @click="openDialog('add', null)"
            >
              添加一级分类
            </v-btn>
          </v-sheet>
        </v-sheet>
      </v-col>
    </v-row>

    <!-- 分类编辑对话框 -->
    <v-dialog v-model="dialog" max-width="560px" persistent>
      <v-card class="rounded-lg">
        <v-toolbar color="primary" class="text-white rounded-t-lg">
          <v-toolbar-title>{{ dialogTitle }}</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="closeDialog"></v-btn>
        </v-toolbar>

        <v-card-text class="pa-4 pt-6">
          <v-form ref="form" v-model="valid">
            <v-row>
              <v-col cols="12">
                <v-text-field
                  v-model="editedItem.name"
                  label="分类名称"
                  variant="outlined"
                  density="comfortable"
                  hide-details="auto"
                  required
                  :rules="[(v) => !!v || '分类名称不能为空']"
                ></v-text-field>
              </v-col>

              <v-col cols="12" sm="6">
                <v-select
                  v-if="dialogMode === 'addSub'"
                  v-model="editedItem.fatherId"
                  :items="rootCategoriesOptions"
                  item-title="name"
                  item-value="id"
                  label="父级分类"
                  variant="outlined"
                  density="comfortable"
                  hide-details="auto"
                  disabled
                ></v-select>
                <v-select
                  v-else
                  v-model="editedItem.type"
                  :items="[
                    { title: '一级分类', value: 1 },
                    { title: '二级分类', value: 2 },
                  ]"
                  item-title="title"
                  item-value="value"
                  label="分类级别"
                  variant="outlined"
                  density="comfortable"
                  hide-details="auto"
                  disabled
                ></v-select>
              </v-col>

              <v-col cols="12" sm="6">
                <v-text-field
                  v-model.number="editedItem.sort"
                  label="排序值"
                  type="number"
                  variant="outlined"
                  density="comfortable"
                  hide-details="auto"
                  hint="数值越大越靠前"
                  persistent-hint
                  required
                  :rules="[(v) => v !== undefined || '排序值不能为空']"
                ></v-text-field>
              </v-col>

              <v-col cols="12">
                <v-textarea
                  v-model="editedItem.describes"
                  label="分类描述"
                  variant="outlined"
                  rows="3"
                  hint="简要描述该分类的内容和范围"
                  persistent-hint
                  hide-details="auto"
                ></v-textarea>
              </v-col>
            </v-row>
          </v-form>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" color="grey-darken-1" @click="closeDialog">取消</v-btn>
          <v-btn variant="elevated" color="primary" @click="saveCategory" :disabled="!valid">
            {{ editedItem.id ? '保存修改' : '创建分类' }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 删除确认对话框 -->
    <v-dialog v-model="deleteDialog" max-width="460px" persistent>
      <v-card class="rounded-lg">
        <v-toolbar color="error" class="text-white rounded-t-lg">
          <v-toolbar-title>确认删除</v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" @click="deleteDialog = false"></v-btn>
        </v-toolbar>

        <v-card-text class="pa-4 pt-6">
          <div class="d-flex align-center mb-4">
            <v-avatar color="error-lighten-4" class="mr-3">
              <v-icon color="error">mdi-alert-circle</v-icon>
            </v-avatar>
            <div>
              <div class="text-h6 mb-1">删除分类 "{{ deleteItem?.name }}"</div>
              <div class="text-body-2">此操作不可恢复，请确认</div>
            </div>
          </div>

          <v-alert
            v-if="deleteItem?.type === 1"
            type="warning"
            variant="tonal"
            border="start"
            density="compact"
            class="mb-3"
          >
            <template v-slot:prepend>
              <v-icon size="small">mdi-information-outline</v-icon>
            </template>
            <strong>注意：</strong>删除一级分类将同时删除其下所有二级分类！
          </v-alert>

          <p class="text-body-2 text-medium-emphasis">
            删除后，该分类下的内容将失去关联，可能需要重新分类。
          </p>
        </v-card-text>

        <v-divider></v-divider>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn variant="text" color="grey-darken-1" @click="deleteDialog = false">取消</v-btn>
          <v-btn variant="elevated" color="error" @click="deleteCategory">
            <v-icon start>mdi-delete</v-icon>
            确认删除
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 提示消息 -->
    <v-snackbar v-model="snackbar" :color="snackbarColor" timeout="3000" location="top">
      {{ snackbarText }}
      <template #action="{ attrs }">
        <v-btn text v-bind="attrs" @click="snackbar = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  name: 'CategoryController',
  data() {
    return {
      // 表格数据
      categories: [],
      rootCategories: [],
      loading: false,

      // 编辑对话框
      dialog: false,
      dialogMode: '', // 'add', 'addSub', 'edit'
      valid: true,
      editedItem: {
        id: null,
        name: '',
        type: 1,
        fatherId: 0,
        describes: '',
        icon: null,
        sort: 0,
      },
      defaultItem: {
        id: null,
        name: '',
        type: 1,
        fatherId: 0,
        describes: '',
        icon: null,
        sort: 0,
      },

      // 删除对话框
      deleteDialog: false,
      deleteItem: null,

      // 提示消息
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
    }
  },

  computed: {
    dialogTitle() {
      const titles = {
        add: '添加一级分类',
        addSub: '添加二级分类',
        edit: '编辑分类',
      }
      return titles[this.dialogMode] || '分类信息'
    },

    rootCategoriesOptions() {
      // 确保rootCategories是数组
      if (!Array.isArray(this.rootCategories)) {
        return []
      }

      // 只处理有效的分类对象
      return this.rootCategories
        .filter((cat) => cat && typeof cat === 'object' && cat.id)
        .map((cat) => ({
          id: cat.id,
          name: cat.name,
        }))
    },
  },

  created() {
    this.fetchCategories()
  },

  methods: {
    // 获取分类数据
    fetchCategories() {
      this.loading = true
      this.httpGet('/category/list', (json) => {
        this.loading = false
        if (json.status === 200) {
          // 确保json.data是数组，如果不是数组则尝试处理
          if (Array.isArray(json.data)) {
            this.categories = json.data
            this.rootCategories = json.data.filter((cat) => cat.type === 1)
          } else if (json.data && typeof json.data === 'object') {
            // 如果data不是数组而是对象，尝试获取其中的数组字段
            if (Array.isArray(json.data.list)) {
              this.categories = json.data.list
              this.rootCategories = json.data.list.filter((cat) => cat.type === 1)
            } else {
              // 尝试将对象转为数组
              const dataArray = Object.values(json.data).filter(
                (item) => item && typeof item === 'object'
              )
              this.categories = dataArray
              this.rootCategories = dataArray.filter((cat) => cat && cat.type === 1)
            }
          } else {
            this.categories = []
            this.rootCategories = []
            this.showMessage('获取分类数据格式异常', 'error')
          }
        } else {
          this.showMessage('获取分类数据失败: ' + json.message, 'error')
        }
      })
    },

    // 打开编辑对话框
    openDialog(mode, item) {
      this.dialogMode = mode

      if (mode === 'add') {
        // 添加一级分类
        this.editedItem = Object.assign({}, this.defaultItem)
        this.editedItem.type = 1
        this.editedItem.fatherId = 0
      } else if (mode === 'addSub') {
        // 添加二级分类
        this.editedItem = Object.assign({}, this.defaultItem)
        this.editedItem.type = 2
        this.editedItem.fatherId = item.id
      } else if (mode === 'edit') {
        // 编辑分类
        this.editedItem = Object.assign({}, item)
      }

      this.dialog = true
    },

    // 关闭编辑对话框
    closeDialog() {
      this.dialog = false
      this.$nextTick(() => {
        this.editedItem = Object.assign({}, this.defaultItem)
      })
    },

    // 保存分类
    saveCategory() {
      if (!this.$refs.form.validate()) return

      const isNew = !this.editedItem.id
      const url = isNew ? '/admin/category/save' : '/admin/category/update'

      this.httpPost(url, this.editedItem, (json) => {
        if (json.status === 200) {
          this.showMessage(json.data.info, 'success')
          this.closeDialog()
          this.fetchCategories()
        } else {
          this.showMessage((isNew ? '添加' : '更新') + '分类失败: ' + json.message, 'error')
        }
      })
    },

    // 确认删除对话框
    confirmDelete(item) {
      this.deleteItem = item
      this.deleteDialog = true
    },

    // 删除分类
    deleteCategory() {
      if (!this.deleteItem) return

      this.httpPost('/admin/category/delete', this.deleteItem, (json) => {
        if (json.status === 200) {
          this.showMessage(json.data.info, 'success')
          this.fetchCategories()
        }
        this.deleteDialog = false
        this.deleteItem = null
      })
    },

    // 显示提示消息
    showMessage(text, color = 'success') {
      this.snackbarText = text
      this.snackbarColor = color
      this.snackbar = true
    },
  },
}
</script>

<style scoped>
.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.text-truncate-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.border-primary {
  border: 2px solid var(--v-primary-base) !important;
  transition: all 0.3s ease;
}

.category-panel .v-expansion-panel {
  overflow: hidden;
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.5, 1);
}

.category-cards {
  max-height: 600px;
  overflow-y: auto;
  padding-right: 4px;
}

.category-cards::-webkit-scrollbar {
  width: 6px;
}

.category-cards::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.category-cards::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 10px;
}

.category-cards::-webkit-scrollbar-thumb:hover {
  background: #999;
}

/* 响应式调整 */
@media (max-width: 600px) {
  .category-panel .v-expansion-panel-title {
    padding: 12px;
  }
}
</style>