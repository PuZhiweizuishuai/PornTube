<template>
  <v-container fluid>
    <v-card class="mx-auto w-100" elevation="2" rounded="lg">
      <v-toolbar color="blue">
        <v-toolbar-title class="text-h5 font-weight-medium">
          <v-icon icon="mdi-server" class="mr-2"></v-icon>
          对象存储配置管理
          <v-chip v-if="nowSaveLocation.location === 0" color="info" size="small" class="ml-2">
            当前存储位置：本地存储，剩余可用空间： {{ nowSaveLocation.free }}
          </v-chip>
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <v-btn
          prepend-icon="mdi-plus"
          color="white"
          variant="tonal"
          @click="openDialog(null)"
          class="mr-2"
        >
          新增配置
        </v-btn>
      </v-toolbar>

      <v-alert type="info" variant="tonal" border="start" density="comfortable" class="ma-4">
        同时只能启用一个配置，不建议删除曾经使用过的配置，如果删除可能导致部分使用该配置的文件无法正常访问。
        禁用不表示配置不启用，只表示当前存储位置不是他，不代表之前的存储位置不是。
      </v-alert>

      <!-- 配置列表 -->
      <v-card-text>
        <v-data-table
          :headers="headers"
          :items="ossList"
          :loading="loading"
          hide-default-footer
          mobile-breakpoint="md"
          :hide-default-header="$vuetify.display.smAndDown"
          hover
          class="elevation-0 rounded-lg"
        >
          <!-- 配置名称列 -->
          <template #[`item.configName`]="{ item }">
            <v-chip :color="item.status === 1 ? 'success' : 'grey'" variant="outlined" size="small">
              {{ item.configName }}
            </v-chip>
          </template>

          
          <!-- 访问风格列 -->
          <template #[`item.pathStyleAccess`]="{ item }">
            {{ item.pathStyleAccess === 0 ? 'Path Style' : 'Virtual Hosted Style' }}
          </template>

          <!-- 访问风格列 -->
          <template #[`item.endpoint`]="{ item }" >
            <span class="title-wrap">{{ item.endpoint }}</span>
          </template>

          <!-- 状态列 -->
          <template #[`item.status`]="{ item }">
            <v-chip :color="item.status === 1 ? 'success' : 'grey'" size="small" text-color="white">
              {{ item.status === 1 ? '启用' : '禁用' }}
            </v-chip>
          </template>

          <!-- 操作列 -->
          <template #[`item.actions`]="{ item }">
            <div class="d-flex justify-center">
              <v-tooltip location="top" text="编辑">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    variant="text"
                    size="small"
                    color="primary"
                    @click="openDialog(item)"
                    class="mr-1"
                  >
                    <v-icon>mdi-pencil</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip location="top" text="删除">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    variant="text"
                    size="small"
                    color="error"
                    @click="deleteConfig(item.id)"
                    class="mr-1"
                  >
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip location="top" text="验证">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    variant="text"
                    size="small"
                    color="success"
                    @click="validateConfig(item)"
                  >
                    <v-icon>mdi-check-circle</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>
            </div>
          </template>

          <template #no-data>
            <div class="d-flex flex-column align-center pa-4">
              <v-icon size="large" color="grey-lighten-1" class="mb-2">mdi-emoticon-sad</v-icon>
              <span class="text-body-1 text-grey">暂无配置数据</span>
              <v-btn color="primary" class="mt-4" @click="initialize">刷新数据</v-btn>
            </div>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- 编辑对话框 -->
    <v-dialog v-model="dialog" max-width="800px">
      <v-card>
        <v-card-title class="text-h5 bg-primary text-white">
          <v-icon class="mr-2">{{ editedItem.id ? 'mdi-pencil' : 'mdi-plus' }}</v-icon>
          {{ editedItem.id ? '编辑配置' : '新增配置' }}
          <v-spacer></v-spacer>
          <v-btn icon="mdi-close" variant="text" color="white" @click="dialog = false"></v-btn>
        </v-card-title>

        <v-card-text class="pt-4">
          <v-form ref="form" v-model="valid">
            <v-container>
              <v-row>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editedItem.configName"
                    label="配置名称"
                    :rules="[(v) => !!v || '配置名称不能为空']"
                    required
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-tag"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editedItem.bucketName"
                    label="存储桶名称"
                    :rules="[(v) => !!v || '存储桶名称不能为空']"
                    required
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-folder"
                  ></v-text-field>
                </v-col>
                <v-col cols="12">
                  <v-text-field
                    v-model="editedItem.endpoint"
                    label="端点地址"
                    :rules="[(v) => !!v || '端点地址不能为空']"
                    required
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-web"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editedItem.accessKey"
                    label="访问密钥ID"
                    :rules="[(v) => !!v || '访问密钥ID不能为空']"
                    required
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-key"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editedItem.secretKey"
                    label="访问密钥密码"
                    :rules="[(v) => !!v || '访问密钥密码不能为空']"
                    type="password"
                    required
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-key-variant"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editedItem.region"
                    label="地区"
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-map-marker"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-text-field
                    v-model="editedItem.urlPrefix"
                    label="自定义域名"
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-earth"
                  ></v-text-field>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-select
                    v-model="editedItem.pathStyleAccess"
                    :items="[
                      { title: 'Path Style', value: 0 },
                      { title: 'Virtual Hosted Style', value: 1 },
                    ]"
                    label="访问风格"
                    required
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-format-list-bulleted"
                  ></v-select>
                </v-col>
                <v-col cols="12" sm="6">
                  <v-select
                    v-model="editedItem.status"
                    :items="[
                      { title: '启用', value: 1 },
                      { title: '禁用', value: 0 },
                    ]"
                    label="状态"
                    required
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-toggle-switch"
                  ></v-select>
                </v-col>
                <v-col cols="12">
                  <v-textarea
                    v-model="editedItem.other"
                    label="其它参数配置"
                    rows="3"
                    variant="outlined"
                    density="comfortable"
                    prepend-inner-icon="mdi-cog"
                  ></v-textarea>
                </v-col>
              </v-row>
            </v-container>
          </v-form>
        </v-card-text>

        <v-card-actions class="pa-4 bg-grey-lighten-5">
          <v-spacer></v-spacer>
          <v-btn color="error" variant="text" @click="dialog = false" prepend-icon="mdi-close">
            取消
          </v-btn>
          <v-btn color="primary" variant="text" @click="saveConfig" prepend-icon="mdi-content-save">
            保存
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 消息提示 -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="2000">
      {{ snackbar.text }}
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  name: 'OssView',
  data: () => ({
    loading: false,
    dialog: false,
    valid: false,
    headers: [
      { title: '配置名称', key: 'configName', align: 'start', sortable: false },
      { title: '存储桶', key: 'bucketName', align: 'start', sortable: false },
      { title: '端点', key: 'endpoint', align: 'start', sortable: false },
      { title: '访问风格', key: 'pathStyleAccess', align: 'center', sortable: false },
      { title: '状态', key: 'status', align: 'center', sortable: false },
      { title: '操作', key: 'actions', align: 'center', sortable: false },
    ],
    ossList: [],
    editedItem: {
      id: null,
      configName: '',
      bucketName: '',
      endpoint: '',
      accessKey: '',
      secretKey: '',
      region: '',
      urlPrefix: '',
      pathStyleAccess: 0,
      other: '',
      status: 1,
    },
    defaultItem: {
      id: null,
      configName: '',
      bucketName: '',
      endpoint: '',
      accessKey: '',
      secretKey: '',
      region: '',
      urlPrefix: '',
      pathStyleAccess: 0,
      other: '',
      status: 1,
    },
    snackbar: {
      show: false,
      text: '',
      color: 'success',
    },
    nowSaveLocation: 0,
  }),

  created() {
    this.getNowSaveLocation()
    this.initialize()
  },

  methods: {
    // 初始化数据
    initialize() {
      this.loading = true
      this.httpGet('/admin/oss/list', (json) => {
        if (json.status === 200) {
          this.ossList = json.data
        }
        this.loading = false
      })
    },
    getNowSaveLocation() {
      this.httpGet('/admin/oss/location', (json) => {
        if (json.status === 200) {
          this.nowSaveLocation = json.data
          let availableDisk = json.data.free / 1024 / 1024
          if (availableDisk < 1024) {
            availableDisk = availableDisk.toFixed(2) + 'MB'
          } else {
            availableDisk = (availableDisk / 1024).toFixed(2) + 'GB'
          }
          this.nowSaveLocation.free = availableDisk
        }
      })
    },

    // 打开编辑对话框
    openDialog(item) {
      this.dialog = true
      this.editedItem = item ? { ...item } : { ...this.defaultItem }
    },

    // 保存配置
    async saveConfig() {
      if (!this.$refs.form.validate()) return

      const url = this.editedItem.id ? '/admin/oss/update' : '/admin/oss/save'
      this.httpPost(url, this.editedItem, (json) => {
        if (json.status === 200 && json.data) {
          this.showMessage('操作成功', 'success')
          this.dialog = false
          this.initialize()
        } else {
          this.showMessage(json.message || '操作失败，请检查配置', 'error')
        }
      })
    },

    // 删除配置
    deleteConfig(id) {
      if (confirm('确定要删除该配置吗？')) {
        this.httpPost(`/admin/oss/delete/${id}`, {}, (json) => {
          if (json.status === 200 && json.data) {
            this.showMessage('删除成功', 'success')
            this.initialize()
          } else {
            this.showMessage(json.message || '删除失败', 'error')
          }
        })
      }
    },

    // 验证配置
    validateConfig(item) {
      this.httpPost('/admin/oss/validate', item, (json) => {
        if (json.status === 200) {
          this.showMessage(json.data ? '验证成功' : '验证失败', json.data ? 'success' : 'error')
        } else {
          this.showMessage(json.message || '验证失败', 'error')
        }
      })
    },

    // 显示消息提示
    showMessage(text, color) {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
  },
}
</script>

<style scoped>
.preview-media {
  max-width: 100%;
  max-height: 400px;
  margin: 0 auto;
  display: block;
  border-radius: 4px;
}

</style>