<template>
  <v-container>
    <v-card class="mx-auto w-100 mb-4" elevation="2" rounded="lg">
      <v-toolbar color="red-darken-3" dark>
        <v-toolbar-title>网站基本设置</v-toolbar-title>
      </v-toolbar>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="setting.name"
              label="网站名称"
              variant="outlined"
              prepend-inner-icon="mdi-web"
              hint="设置网站的名称"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="setting.baseUrl"
              label="网站URL"
              variant="outlined"
              prepend-inner-icon="mdi-web"
              hint="设置网站的URL地址,发送邮件模板时需要，格式为 https://example.com"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="setting.logoUrl"
              label="网站Logo URL"
              variant="outlined"
              prepend-inner-icon="mdi-image"
              hint="设置网站Logo的URL地址"
              persistent-hint
            />
          </v-col>
          <v-col cols="12">
            <v-textarea
              v-model="setting.webDescribe"
              label="网站简介"
              variant="outlined"
              prepend-inner-icon="mdi-text-box"
              hint="简短描述您的网站"
              persistent-hint
              rows="3"
              auto-grow
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="setting.homeMaxVideoCount"
              label="每页大视频显示数量"
              type="number"
              variant="outlined"
              prepend-inner-icon="mdi-video"
              hint="设置每页最多显示多少个视频"
              persistent-hint
            />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-card class="mx-auto mb-4" elevation="2">
      <v-toolbar color="red-darken-3" dark>
        <v-toolbar-title>用户权限设置</v-toolbar-title>
      </v-toolbar>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openInvitationRegister"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="开启邀请码注册"
              hint="开启后用户注册需要邀请码"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openNoVipLimit"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="非VIP用户观看次数限制"
              hint="开启后非VIP用户每日观看次数受限（TODO：该功能因实现不稳定，暂时移除，该配置暂不生效）"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6" v-if="setting.openNoVipLimit === 1">
            <v-text-field
              v-model="setting.noVipViewCount"
              label="非VIP用户每日观看次数"
              type="number"
              variant="outlined"
              prepend-inner-icon="mdi-eye"
              hint="设置非VIP用户每日最大观看次数"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6" v-if="setting.openNoVipLimit === 1">
            <v-switch
              v-model="setting.openUploadVideoAddViewCount"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="上传视频增加观看次数"
              hint="开启后用户上传视频可增加每日观看次数"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openExamine"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="开启内容审核"
              hint="开启后视频、文章、图片需要审核才能发布"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openCommentExam"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="开启评论审核"
              hint="开启后用户提交的评论需要审核才能发布"
              persistent-hint
            />
          </v-col>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openDanmakuExam"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="开启弹幕审核"
              hint="开启后用户发布的弹幕需要审核才能发布"
              persistent-hint
            />
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <v-card class="mx-auto mb-4" elevation="2">
      <v-toolbar color="red-darken-3" dark>
        <v-toolbar-title>投稿权限设置</v-toolbar-title>
      </v-toolbar>
      <v-card-text>
        <v-alert
          type="info"
          variant="tonal"
          density="compact"
          class="mb-4"
          text="这些开关只作用于「普通用户」，VIP 用户与管理员不受限制。总开关关闭后普通用户完全只读：不能投稿、不能上传视频与封面等素材，也不能修改、删除自己已发布的内容，适合把系统作为演示环境部署到公网。"
        />
        <v-row>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openNormalUserPost"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="允许普通用户投稿（总开关）"
              hint="关闭后普通用户不能投稿，也不能修改、删除自己已发布的内容"
              persistent-hint
            />
          </v-col>
          <template v-if="setting.openNormalUserPost">
            <v-col cols="12" md="6">
              <v-switch
                v-model="setting.openNormalUserPostVideo"
                :false-value="false"
                :true-value="true"
                color="red-darken-3"
                label="允许普通用户投稿视频"
                hint="包含发布视频、上传视频文件与视频封面"
                persistent-hint
              />
            </v-col>
            <v-col cols="12" md="6">
              <v-switch
                v-model="setting.openNormalUserPostArticle"
                :false-value="false"
                :true-value="true"
                color="red-darken-3"
                label="允许普通用户投稿文章"
                hint="包含发布文章、上传文章封面与正文内的图片素材"
                persistent-hint
              />
            </v-col>
          </template>
        </v-row>
      </v-card-text>
    </v-card>

    <v-card class="mx-auto mb-4" elevation="2">
      <v-toolbar color="red-darken-3" dark>
        <v-toolbar-title>邮箱设置</v-toolbar-title>
      </v-toolbar>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openEmail"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="启用邮箱功能"
              hint="开启后系统将验证用户注册邮箱，并可通过邮箱找回密码，使用邮箱发布部分消息！"
              persistent-hint
            />
          </v-col>
        </v-row>
        <v-expand-transition>
          <div v-if="setting.openEmail">
            <v-divider class="my-4"></v-divider>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.mailConfig.host"
                  label="邮箱服务器地址"
                  variant="outlined"
                  prepend-inner-icon="mdi-server"
                  hint="如: smtp.example.com"
                  persistent-hint
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.mailConfig.port"
                  label="邮箱服务器端口"
                  type="number"
                  variant="outlined"
                  prepend-inner-icon="mdi-numeric"
                  hint="如: 465, 587等"
                  persistent-hint
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.mailConfig.protocol"
                  label="邮箱协议"
                  variant="outlined"
                  prepend-inner-icon="mdi-protocol"
                  hint="如: smtp"
                  persistent-hint
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.mailConfig.username"
                  label="邮箱账号"
                  variant="outlined"
                  prepend-inner-icon="mdi-account"
                  hint="邮箱登录账号"
                  persistent-hint
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.mailConfig.password"
                  label="邮箱密码"
                  variant="outlined"
                  prepend-inner-icon="mdi-lock"
                  hint="邮箱登录密码或授权码"
                  persistent-hint
                  type="password"
                />
              </v-col>
            </v-row>
          </div>
        </v-expand-transition>
      </v-card-text>
    </v-card>

        <!-- Redis配置卡片 -->
        <!-- <v-card class="mx-auto mb-4" elevation="2">
      <v-toolbar color="red-darken-3" dark>
        <v-toolbar-title>Redis设置</v-toolbar-title>
      </v-toolbar>
      <v-card-text>
        <v-row>
          <v-col cols="12" md="6">
            <v-switch
              v-model="setting.openRedis"
              :false-value="false"
              :true-value="true"
              color="red-darken-3"
              label="启用Redis功能"
              hint="开启后系统将使用Redis进行缓存，提高系统性能"
              persistent-hint
            />
          </v-col>
        </v-row>
        <v-expand-transition>
          <div v-if="setting.openRedis">
            <v-divider class="my-4"></v-divider>
            <v-row>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.redisConfig.host"
                  label="Redis服务器地址"
                  variant="outlined"
                  prepend-inner-icon="mdi-server"
                  hint="如: localhost, 127.0.0.1"
                  persistent-hint
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.redisConfig.port"
                  label="Redis服务器端口"
                  type="number"
                  variant="outlined"
                  prepend-inner-icon="mdi-numeric"
                  hint="如: 6379"
                  persistent-hint
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.redisConfig.password"
                  label="Redis密码"
                  variant="outlined"
                  prepend-inner-icon="mdi-lock"
                  hint="Redis服务器密码，如果没有可以留空"
                  persistent-hint
                  type="password"
                />
              </v-col>
              <v-col cols="12" md="6">
                <v-text-field
                  v-model="setting.redisConfig.database"
                  label="Redis数据库索引"
                  type="number"
                  variant="outlined"
                  prepend-inner-icon="mdi-database"
                  hint="Redis数据库索引，默认为0"
                  persistent-hint
                />
              </v-col>
            </v-row>
          </div>
        </v-expand-transition>
      </v-card-text>
    </v-card> -->

    <v-card-actions class="justify-center mb-6">
      <v-btn
        color="red-darken-3"
        size="large"
        variant="elevated"
        prepend-icon="mdi-content-save"
        @click="save"
        :loading="loading"
        min-width="200"
      >
        保存设置
      </v-btn>
    </v-card-actions>

    <v-snackbar
      v-model="snackbar"
      :color="snackbarColor"
      timeout="3000"
      location="top"
    >
      {{ snackbarText }}
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  data() {
    return {
      setting: {
        mailConfig: {},
        //redisConfig: {} // 添加redisConfig初始化
      },
      loading: false,
      snackbar: false,
      snackbarText: '',
      snackbarColor: 'success'
    }
  },
  created() {
    this.getSetting()
  },
  methods: {
    getSetting() {
      this.loading = true
      this.httpGet('/admin/setting/info', (json) => {
        this.setting = json.data
        // 确保mailConfig存在
        if (!this.setting.mailConfig) {
          this.setting.mailConfig = {}
        }
        // 确保redisConfig存在
        if (!this.setting.redisConfig) {
          this.setting.redisConfig = {}
        }
        this.loading = false
      })
    },
    save() {
      this.loading = true
      this.httpPost('/admin/setting/save', this.setting, (json) => {
        this.showSuccess('设置保存成功')
        this.loading = false
        console.log(json)
      })
    },
    showSuccess(message) {
      this.snackbarText = message
      this.snackbarColor = 'success'
      this.snackbar = true
    },
    showError(message) {
      this.snackbarText = message
      this.snackbarColor = 'error'
      this.snackbar = true
    }
  }
}
</script>

<style>
.v-card {
  border-radius: 8px;
}
.v-toolbar {
  border-top-left-radius: 8px;
  border-top-right-radius: 8px;
}
</style>
  