<template>
  <v-container fluid class="pa-6">
    <v-card elevation="2" rounded="lg" class="mb-6">
      <v-toolbar color="primary" density="comfortable">
        <v-toolbar-title class="text-h5 font-weight-bold text-white">
          <v-icon class="mr-2">mdi-account-group</v-icon>
          用户列表
        </v-toolbar-title>
        <v-spacer></v-spacer>
        <v-text-field
          v-model="search"
          prepend-inner-icon="mdi-magnify"
          label="搜索用户"
          single-line
          hide-details
          density="compact"
          bg-color="grey-lighten-4"
          variant="solo"
          class="mr-4 rounded-pill"
          style="max-width: 300px"
        ></v-text-field>
        <v-btn
          color="white"
          prepend-icon="mdi-account-plus"
          variant="elevated"
          @click="openAddUserDialog"
          rounded="pill"
          class="mr-2"
        >
          新增用户
        </v-btn>
      </v-toolbar>

      <v-card-text class="pa-4">
        <v-data-table
          :headers="headers"
          :items="users"
          :search="search"
          :loading="loading"
          :items-per-page="pageSize"
          class="elevation-0 rounded-lg"
          hover
        >
          <template #[`item.id`]="{ item }">
            <v-chip
              size="small"
              :to="`/user/${item.id}`"
              class="font-weight-medium text-primary text-subtitle-2"
              rounded="lg"
            >
              #{{ item.id }}
            </v-chip>
          </template>

          <template #[`item.username`]="{ item }">
            <div class="d-flex align-center">
              <v-avatar size="36" class="mr-3 elevation-2">
                <v-img :src="item.avatarUrl" alt="用户头像"></v-img>
              </v-avatar>
              <div>
                <v-btn
                  :to="`/user/${item.id}`"
                  variant="text"
                  color="primary"
                  density="comfortable"
                  class="px-0 font-weight-bold text-subtitle-1"
                >
                  {{ item.username }}
                </v-btn>
                <div class="text-caption text-grey" v-if="item.introduction">
                  {{
                    item.introduction.length > 20
                      ? item.introduction.slice(0, 20) + '...'
                      : item.introduction
                  }}
                </div>
              </div>
            </div>
          </template>

          <template #[`item.createTime`]="{ item }">
            <div class="d-flex flex-column">
              <span class="text-subtitle-2">{{ formatDate(item.createTime) }}</span>
              <span class="text-caption text-grey">
                {{ getTimeAgo(item.createTime) }}
              </span>
            </div>
          </template>

          <template #[`item.userRoleEntity.role`]="{ item }">
            <v-chip
              :color="getRoleColor(item.userRoleEntity.role)"
              size="small"
              class="text-white font-weight-medium"
              variant="elevated"
              @click="openRoleDialog(item)"
              rounded="pill"
            >
              {{ getRoleName(item.userRoleEntity.role) }}
            </v-chip>
          </template>

          <template #[`item.vipTime`]="{ item }">
            <div v-if="item.userRoleEntity.role === 'ROLE_VIP'" class="d-flex flex-column">
              <div class="d-flex align-center">
                <v-icon size="small" color="amber-darken-2" class="mr-1">mdi-calendar-start</v-icon>
                <span class="text-caption">{{ formatDate(item.userRoleEntity.vipStartTime) }}</span>
              </div>
              <div class="d-flex align-center mt-1">
                <v-icon size="small" color="amber-darken-2" class="mr-1">mdi-calendar-end</v-icon>
                <span class="text-caption">{{ formatDate(item.userRoleEntity.vipStopTime) }}</span>
              </div>
            </div>
            <div v-else>
              <v-icon color="grey-lighten-1">mdi-minus</v-icon>
            </div>
          </template>

          <template #[`item.actions`]="{ item }">
            <div class="d-flex">
              <v-tooltip location="top" text="设置角色">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    color="primary"
                    variant="text"
                    @click="openRoleDialog(item)"
                    class="mr-2"
                  >
                    <v-icon>mdi-shield-account</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>

              <v-tooltip location="top" text="重置密码">
                <template #activator="{ props }">
                  <v-btn
                    v-bind="props"
                    icon
                    size="small"
                    color="orange"
                    variant="text"
                    @click="openResetPasswordDialog(item)"
                  >
                    <v-icon>mdi-lock-reset</v-icon>
                  </v-btn>
                </template>
              </v-tooltip>
            </div>
          </template>

          <template #no-data>
            <div class="d-flex flex-column align-center py-6">
              <v-icon size="x-large" color="grey-lighten-1" class="mb-4">mdi-account-search</v-icon>
              <span class="text-body-1 text-medium-emphasis">没有找到用户数据</span>
              <v-btn color="primary" class="mt-4" @click="fetchUsers" prepend-icon="mdi-refresh">
                刷新数据
              </v-btn>
            </div>
          </template>

          <template #bottom>
            <div class="d-flex justify-space-between align-center pa-4">
              <div class="text-caption text-medium-emphasis">共 {{ totalCount }} 名用户</div>
              <v-pagination
                v-model="page"
                :length="totalPage"
                @update:model-value="fetchUsers"
                :total-visible="7"
                rounded="lg"
                density="comfortable"
              ></v-pagination>
            </div>
          </template>

          <template #loading>
            <div class="d-flex align-center justify-center pa-4">
              <v-progress-circular indeterminate color="primary"></v-progress-circular>
              <span class="ml-4 text-medium-emphasis">加载中，请稍候...</span>
            </div>
          </template>
        </v-data-table>
      </v-card-text>
    </v-card>

    <!-- 角色设置弹窗 -->
    <v-dialog v-model="roleDialog" max-width="500" transition="dialog-bottom-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-shield-account</v-icon>
            设置用户角色
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="roleDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="pt-6">
          <div class="d-flex align-center mb-6">
            <v-avatar size="60" class="mr-4 elevation-2">
              <v-img :src="selectedUser ? selectedUser.avatarUrl : ''" alt="用户头像"></v-img>
            </v-avatar>
            <div>
              <div class="text-h6 font-weight-bold">
                {{ selectedUser ? selectedUser.username : '' }}
              </div>
              <div class="text-caption text-medium-emphasis">
                ID: {{ selectedUser ? selectedUser.id : '' }}
              </div>
              <div class="d-flex align-center mt-1">
                <v-icon size="small" color="primary" class="mr-1">mdi-email-outline</v-icon>
                <span class="text-caption">{{ selectedUser ? selectedUser.mail : '' }}</span>
              </div>
            </div>
          </div>

          <v-divider class="mb-4"></v-divider>

          <div class="font-weight-medium mb-3">选择用户角色</div>
          <v-radio-group v-model="selectedRole" class="mb-6">
            <v-row>
              <v-col cols="4">
                <v-radio value="ROLE_USER">
                  <template #label>
                    <div class="d-flex align-center">
                      <v-icon color="blue" class="mr-1">mdi-account</v-icon>
                      <span>普通用户</span>
                    </div>
                  </template>
                </v-radio>
              </v-col>
              <v-col cols="4">
                <v-radio value="ROLE_VIP">
                  <template #label>
                    <div class="d-flex align-center">
                      <v-icon color="amber-darken-2" class="mr-1">mdi-crown</v-icon>
                      <span>VIP用户</span>
                    </div>
                  </template>
                </v-radio>
              </v-col>
              <v-col cols="4">
                <v-radio value="ROLE_ADMIN">
                  <template #label>
                    <div class="d-flex align-center">
                      <v-icon color="red" class="mr-1">mdi-shield-account</v-icon>
                      <span>管理员</span>
                    </div>
                  </template>
                </v-radio>
              </v-col>
            </v-row>
          </v-radio-group>

          <v-expand-transition>
            <div v-if="selectedRole !== 'ROLE_USER'" class="date-pickers-container">
              <div class="font-weight-medium mb-3">
                <v-icon color="primary" class="mr-1">mdi-calendar-clock</v-icon>
                设置有效期
              </div>
              <v-row>
                <v-col cols="6">
                  <v-menu
                    v-model="startDateMenu"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    min-width="auto"
                  >
                    <template #activator="{ props }">
                      <v-text-field
                        v-model="startDate"
                        label="开始日期"
                        readonly
                        v-bind="props"
                        prepend-inner-icon="mdi-calendar-start"
                        variant="outlined"
                        density="comfortable"
                        hide-details
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="startDate"
                      @update:model-value="startDateMenu = false"
                      color="primary"
                    ></v-date-picker>
                  </v-menu>
                </v-col>

                <v-col cols="6">
                  <v-menu
                    v-model="endDateMenu"
                    :close-on-content-click="false"
                    transition="scale-transition"
                    min-width="auto"
                  >
                    <template #activator="{ props }">
                      <v-text-field
                        v-model="endDate"
                        label="结束日期"
                        readonly
                        v-bind="props"
                        prepend-inner-icon="mdi-calendar-end"
                        variant="outlined"
                        density="comfortable"
                        hide-details
                      ></v-text-field>
                    </template>
                    <v-date-picker
                      v-model="endDate"
                      @update:model-value="endDateMenu = false"
                      color="primary"
                    ></v-date-picker>
                  </v-menu>
                </v-col>
              </v-row>
            </div>
          </v-expand-transition>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="roleDialog = false" class="mr-2"> 取消 </v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            @click="updateUserRole"
            :loading="updateRoleLoading"
            rounded="lg"
            prepend-icon="mdi-content-save"
          >
            保存
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 重置密码弹窗 -->
    <v-dialog v-model="resetPasswordDialog" max-width="460" transition="dialog-top-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="orange" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-lock-reset</v-icon>
            重置用户密码
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="resetPasswordDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <div class="d-flex align-center mb-4">
            <v-avatar size="42" class="mr-3 elevation-1">
              <v-img :src="selectedUser ? selectedUser.avatarUrl : ''" alt="用户头像"></v-img>
            </v-avatar>
            <div class="text-h6 font-weight-medium">
              {{ selectedUser ? selectedUser.username : '' }}
            </div>
          </div>

          <v-alert
            v-if="!newPassword"
            color="warning"
            icon="mdi-alert"
            border="start"
            elevation="1"
            class="mb-6"
          >
            您确定要重置此用户的密码吗？此操作将生成一个新的随机密码，请确保将新密码安全地传达给用户。
          </v-alert>

          <v-expand-transition>
            <div v-if="newPassword" class="mt-4">
              <v-alert
                color="success"
                icon="mdi-check-circle"
                border="start"
                elevation="2"
                density="compact"
                class="mb-4"
              >
                密码已成功重置！
              </v-alert>

              <v-card variant="outlined" class="pa-4 password-display">
                <div class="text-subtitle-2 mb-2">新密码</div>
                <div class="d-flex align-center">
                  <code class="text-h6 font-weight-bold password-text">{{ newPassword }}</code>
                  <v-spacer></v-spacer>
                  <v-btn
                    icon
                    color="primary"
                    size="small"
                    variant="text"
                    @click="copyToClipboard(newPassword)"
                    class="ml-2"
                  >
                    <v-icon>mdi-content-copy</v-icon>
                  </v-btn>
                </div>
              </v-card>

              <div class="text-caption text-medium-emphasis mt-2">
                <v-icon size="small" class="mr-1">mdi-information</v-icon>
                请保存此密码，关闭窗口后将无法再次查看
              </div>
            </div>
          </v-expand-transition>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn
            v-if="newPassword"
            color="primary"
            variant="text"
            @click="resetPasswordDialog = false"
            class="mr-2"
          >
            关闭
          </v-btn>
          <v-btn
            v-if="!newPassword"
            color="grey"
            variant="text"
            @click="resetPasswordDialog = false"
            class="mr-2"
          >
            取消
          </v-btn>
          <v-btn
            v-if="!newPassword"
            color="warning"
            variant="elevated"
            @click="resetPassword"
            :loading="resetLoading"
            rounded="lg"
            prepend-icon="mdi-lock-reset"
          >
            重置密码
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 新增用户弹窗 -->
    <v-dialog v-model="addUserDialog" max-width="600" transition="dialog-bottom-transition">
      <v-card elevation="4" rounded="lg">
        <v-toolbar color="primary" density="comfortable">
          <v-toolbar-title class="text-h6 text-white d-flex align-center">
            <v-icon class="mr-2">mdi-account-plus</v-icon>
            新增用户
          </v-toolbar-title>
          <v-spacer></v-spacer>
          <v-btn icon @click="addUserDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-toolbar>

        <v-card-text class="py-6">
          <v-form ref="addUserForm" @submit.prevent="addUser">
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newUser.mail"
                  label="邮箱"
                  placeholder="请输入邮箱"
                  :rules="[
                    (v) => !!v || '邮箱不能为空',
                    (v) => /^.+@.+\..+$/.test(v) || '邮箱格式错误',
                  ]"
                  type="email"
                  density="comfortable"
                  variant="outlined"
                  prepend-inner-icon="mdi-email-outline"
                  clearable
                  required
                />
              </v-col>

              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newUser.phone"
                  label="手机号"
                  placeholder="请输入手机号（选填）"
                  density="comfortable"
                  variant="outlined"
                  prepend-inner-icon="mdi-phone-outline"
                  clearable
                />
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newUser.username"
                  label="昵称"
                  placeholder="用户昵称"
                  :rules="[(v) => !!v || '昵称不能为空']"
                  density="comfortable"
                  variant="outlined"
                  prepend-inner-icon="mdi-account-outline"
                  clearable
                  required
                />
              </v-col>

              <v-col cols="12" md="6">
                <v-text-field
                  v-model="newUser.password"
                  label="密码"
                  placeholder="请设置密码"
                  :rules="[
                    (v) => !!v || '密码不能为空',
                    (v) => (v && v.length >= 6) || '密码长度不能小于6个字符',
                  ]"
                  density="comfortable"
                  variant="outlined"
                  prepend-inner-icon="mdi-lock-outline"
                  :append-inner-icon="showPassword ? 'mdi-eye-off' : 'mdi-eye'"
                  @click:append-inner="showPassword = !showPassword"
                  :type="showPassword ? 'text' : 'password'"
                  clearable
                  required
                />
              </v-col>
            </v-row>

            <v-row class="mt-2">
              <v-col cols="12">
                <div class="d-flex align-center mb-3">
                  <div class="text-subtitle-1 font-weight-medium">用户角色</div>
                  <v-spacer></v-spacer>
                  <v-chip-group
                    v-model="newUser.role"
                    mandatory
                    selected-class="bg-primary text-white"
                  >
                    <v-chip value="ROLE_USER" filter variant="outlined" class="mr-2">
                      <v-icon start>mdi-account</v-icon>
                      普通用户
                    </v-chip>
                    <v-chip value="ROLE_VIP" filter variant="outlined" class="mr-2">
                      <v-icon start>mdi-crown</v-icon>
                      VIP用户
                    </v-chip>
                    <v-chip value="ROLE_ADMIN" filter variant="outlined">
                      <v-icon start>mdi-shield-account</v-icon>
                      管理员
                    </v-chip>
                  </v-chip-group>
                </div>
              </v-col>
            </v-row>

            <v-expand-transition>
              <div v-if="newUser.role !== 'ROLE_USER'" class="date-pickers-container mt-4">
                <div class="font-weight-medium mb-3">
                  <v-icon color="primary" class="mr-1">mdi-calendar-clock</v-icon>
                  设置角色有效期
                </div>
                <v-row>
                  <v-col cols="6">
                    <v-menu
                      v-model="newUserStartDateMenu"
                      :close-on-content-click="false"
                      transition="scale-transition"
                      min-width="auto"
                    >
                      <template #activator="{ props }">
                        <v-text-field
                          v-model="newUser.startDate"
                          label="开始日期"
                          readonly
                          v-bind="props"
                          prepend-inner-icon="mdi-calendar-start"
                          variant="outlined"
                          density="comfortable"
                          hide-details
                        ></v-text-field>
                      </template>
                      <v-date-picker
                        v-model="newUser.startDate"
                        @update:model-value="newUserStartDateMenu = false"
                        color="primary"
                      ></v-date-picker>
                    </v-menu>
                  </v-col>

                  <v-col cols="6">
                    <v-menu
                      v-model="newUserEndDateMenu"
                      :close-on-content-click="false"
                      transition="scale-transition"
                      min-width="auto"
                    >
                      <template #activator="{ props }">
                        <v-text-field
                          v-model="newUser.endDate"
                          label="结束日期"
                          readonly
                          v-bind="props"
                          prepend-inner-icon="mdi-calendar-end"
                          variant="outlined"
                          density="comfortable"
                          hide-details
                        ></v-text-field>
                      </template>
                      <v-date-picker
                        v-model="newUser.endDate"
                        @update:model-value="newUserEndDateMenu = false"
                        color="primary"
                      ></v-date-picker>
                    </v-menu>
                  </v-col>
                </v-row>
              </div>
            </v-expand-transition>
          </v-form>
        </v-card-text>

        <v-card-actions class="pa-4">
          <v-spacer></v-spacer>
          <v-btn color="grey" variant="text" @click="addUserDialog = false" class="mr-2">
            取消
          </v-btn>
          <v-btn
            color="primary"
            variant="elevated"
            @click="addUser"
            :loading="addUserLoading"
            rounded="lg"
            prepend-icon="mdi-account-plus"
          >
            创建用户
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <!-- 提示消息 -->
    <v-snackbar
      v-model="snackbar"
      :color="snackbarColor"
      :timeout="3000"
      location="top"
      rounded="pill"
    >
      <div class="d-flex align-center">
        <v-icon class="mr-2">{{
          snackbarColor === 'success'
            ? 'mdi-check-circle'
            : snackbarColor === 'error'
            ? 'mdi-alert-circle'
            : 'mdi-information'
        }}</v-icon>
        {{ snackbarText }}
      </div>
      <template #actions>
        <v-btn size="small" icon variant="text" @click="snackbar = false">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  data() {
    return {
      users: [],
      loading: false,
      search: '',
      page: 1,
      pageSize: 10,
      totalPage: 1,
      totalCount: 0,
      headers: [
        { title: 'ID', key: 'id', align: 'start', sortable: true, width: '80px' },
        { title: '用户名', key: 'username', align: 'start', sortable: true, width: '220px' },
        { title: '邮箱', key: 'mail', align: 'start', sortable: true },
        { title: '电话', key: 'phone', align: 'start', sortable: true },
        { title: '注册时间', key: 'createTime', align: 'start', sortable: true },
        { title: '投稿数量', key: 'submitCount', align: 'center', sortable: true, width: '100px' },
        { title: '粉丝数量', key: 'fansCount', align: 'center', sortable: true, width: '100px' },
        { title: '用户角色', key: 'userRoleEntity.role', align: 'center', sortable: true },
        { title: 'VIP期限', key: 'vipTime', align: 'center' },
        { title: '操作', key: 'actions', align: 'center', sortable: false, width: '100px' },
      ],

      // 角色设置相关
      roleDialog: false,
      selectedUser: null,
      selectedRole: 'ROLE_USER',
      startDate: null,
      endDate: null,
      startDateMenu: false,
      endDateMenu: false,
      updateRoleLoading: false,

      // 重置密码相关
      resetPasswordDialog: false,
      newPassword: '',
      resetLoading: false,

      // 新增用户相关
      addUserDialog: false,
      addUserLoading: false,
      showPassword: false,
      newUser: {
        mail: '',
        phone: '',
        username: '',
        password: '',
        role: 'ROLE_USER',
        startDate: null,
        endDate: null,
      },
      newUserStartDateMenu: false,
      newUserEndDateMenu: false,

      // 提示消息
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success',
    }
  },
  created() {
    this.fetchUsers()
    this.initNewUserDates()
  },
  methods: {
    fetchUsers() {
      this.loading = true
      this.httpGet(`/admin/user/list?limit=${this.pageSize}&page=${this.page}`, (json) => {
        if (json && json.data) {
          this.users = json.data.list
          this.totalCount = json.data.totalCount
          this.totalPage = json.data.totalPage
          this.pageSize = json.data.pageSize
        }
        this.loading = false
      })
    },
    formatDate(timestamp) {
      if (!timestamp) return '-'
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
        date.getDate()
      ).padStart(2, '0')}`
    },
    getTimeAgo(timestamp) {
      if (!timestamp) return ''
      const now = new Date()
      const date = new Date(timestamp)
      const diffInSeconds = Math.floor((now - date) / 1000)

      if (diffInSeconds < 60) {
        return `${diffInSeconds}秒前`
      } else if (diffInSeconds < 3600) {
        return `${Math.floor(diffInSeconds / 60)}分钟前`
      } else if (diffInSeconds < 86400) {
        return `${Math.floor(diffInSeconds / 3600)}小时前`
      } else if (diffInSeconds < 2592000) {
        return `${Math.floor(diffInSeconds / 86400)}天前`
      } else if (diffInSeconds < 31536000) {
        return `${Math.floor(diffInSeconds / 2592000)}个月前`
      } else {
        return `${Math.floor(diffInSeconds / 31536000)}年前`
      }
    },
    getRoleColor(role) {
      switch (role) {
        case 'ROLE_ADMIN':
          return 'red'
        case 'ROLE_VIP':
          return 'amber-darken-2'
        default:
          return 'blue'
      }
    },
    getRoleName(role) {
      switch (role) {
        case 'ROLE_ADMIN':
          return '管理员'
        case 'ROLE_VIP':
          return 'VIP用户'
        default:
          return '普通用户'
      }
    },
    openRoleDialog(user) {
      this.selectedUser = user
      this.selectedRole = user.userRoleEntity ? user.userRoleEntity.role : 'ROLE_USER'

      // 设置时间
      if (user.userRoleEntity && user.userRoleEntity.vipStartTime) {
        this.startDate = this.formatDateForPicker(user.userRoleEntity.vipStartTime)
      } else {
        this.startDate = this.formatDateForPicker(new Date())
      }

      if (user.userRoleEntity && user.userRoleEntity.vipStopTime) {
        this.endDate = this.formatDateForPicker(user.userRoleEntity.vipStopTime)
      } else {
        // 默认结束日期为当前日期加一年
        const oneYearLater = new Date()
        oneYearLater.setFullYear(oneYearLater.getFullYear() + 1)
        this.endDate = this.formatDateForPicker(oneYearLater)
      }

      this.roleDialog = true
    },
    formatDateForPicker(timestamp) {
      const date = new Date(timestamp)
      return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(
        date.getDate()
      ).padStart(2, '0')}`
    },
    updateUserRole() {
      if (!this.selectedUser) return

      // 构建更新数据
      const roleData = {
        userid: this.selectedUser.id,
        role: this.selectedRole,
      }

      if (this.selectedRole !== 'ROLE_USER') {
        if (!this.startDate || !this.endDate) {
          this.showSnackbar('请设置开始和结束日期', 'error')
          return
        }

        roleData.vipStartTime = new Date(this.startDate).getTime()
        roleData.vipStopTime = new Date(this.endDate).getTime()

        if (roleData.vipStartTime >= roleData.vipStopTime) {
          this.showSnackbar('结束日期必须晚于开始日期', 'error')
          return
        }
      }

      // 发送更新请求
      this.updateRoleLoading = true
      this.httpPost('/admin/user/update/role', roleData, (json) => {
        if (json.data != null) {
          this.showSnackbar('用户角色更新成功')
          this.fetchUsers() // 刷新列表
        } else {
          this.showSnackbar('用户角色更新失败', 'error')
        }
        this.updateRoleLoading = false
        this.roleDialog = false
      })
    },
    openResetPasswordDialog(user) {
      this.selectedUser = user
      this.newPassword = ''
      this.resetPasswordDialog = true
    },
    resetPassword() {
      if (!this.selectedUser) return

      this.resetLoading = true
      this.httpPost('/admin/user/update/pwd', { id: this.selectedUser.id }, (json) => {
        if (json.data != '') {
          this.newPassword = json.data
          this.showSnackbar('密码重置成功', 'success')
        } else {
          this.showSnackbar('密码重置失败', 'error')
        }
        this.resetLoading = false
      })
    },
    copyToClipboard(text) {
      navigator.clipboard.writeText(text).then(
        () => {
          this.showSnackbar('密码已复制到剪贴板', 'success')
        },
        () => {
          this.showSnackbar('复制失败，请手动复制', 'error')
        }
      )
    },
    openAddUserDialog() {
      this.initNewUserDates()
      this.addUserDialog = true
    },
    initNewUserDates() {
      const now = new Date()
      const oneYearLater = new Date()
      oneYearLater.setFullYear(now.getFullYear() + 1)

      this.newUser = {
        mail: '',
        phone: '',
        username: '',
        password: '',
        role: 'ROLE_USER',
        startDate: this.formatDateForPicker(now),
        endDate: this.formatDateForPicker(oneYearLater),
      }
    },
    addUser() {
      // 验证表单
      if (!this.newUser.mail || !this.newUser.username || !this.newUser.password) {
        this.showSnackbar('请填写必填信息', 'error')
        return
      }

      if (!/^.+@.+\..+$/.test(this.newUser.mail)) {
        this.showSnackbar('邮箱格式错误', 'error')
        return
      }

      if (this.newUser.password.length < 6) {
        this.showSnackbar('密码长度不能小于6个字符', 'error')
        return
      }

      if (this.newUser.phone && !/^[1][3-9][0-9]{9}$/.test(this.newUser.phone)) {
        this.showSnackbar('手机号码格式错误', 'error')
        return
      }

      if (this.newUser.role !== 'ROLE_USER') {
        if (!this.newUser.startDate || !this.newUser.endDate) {
          this.showSnackbar('请设置角色有效期', 'error')
          return
        }

        const startTime = new Date(this.newUser.startDate).getTime()
        const endTime = new Date(this.newUser.endDate).getTime()

        if (startTime >= endTime) {
          this.showSnackbar('结束日期必须晚于开始日期', 'error')
          return
        }
      }

      // 构建请求数据
      const userData = {
        mail: this.newUser.mail,
        phone: this.newUser.phone || '',
        username: this.newUser.username,
        password: this.newUser.password,
        role: this.newUser.role,
      }

      if (this.newUser.role !== 'ROLE_USER') {
        userData.vipStartTime = new Date(this.newUser.startDate).getTime()
        userData.vipStopTime = new Date(this.newUser.endDate).getTime()
      }

      //this.addUserLoading = true

      this.httpPost('/admin/user/add', userData, (json) => {
        if (json.status == 200) {
          this.showSnackbar('用户创建成功')
          this.fetchUsers() // 刷新列表
          this.addUserDialog = false
        } else {
          this.showSnackbar(json.message, 'error')
        }
      })
    },
    showSnackbar(text, color = 'success') {
      this.snackbarText = text
      this.snackbarColor = color
      this.snackbar = true
    },
  },
}
</script>

<style>
.v-data-table {
  border-radius: 8px;
  overflow: hidden;
}

.v-data-table-header th {
  font-weight: bold !important;
  background-color: #f5f5f5 !important;
}

.v-avatar img {
  object-fit: cover;
}

.date-pickers-container {
  padding: 16px;
  background-color: #f5f5f5;
  border-radius: 8px;
}

.password-display {
  background-color: #f8f9fa;
}

.password-text {
  font-family: monospace;
  letter-spacing: 1px;
}
</style>