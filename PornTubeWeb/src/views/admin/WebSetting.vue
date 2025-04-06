<template>
  <v-container fill-height>
    <v-row justify="center" align="center">
      <v-col>
        <v-card class="mx-auto" outlined>
          <v-row justify="center" style="padding-top: 12px; padding-bottom: 12px">
            <v-col cols="10">
              <h3>网页设置</h3>
            </v-col>
          </v-row>
          <!-- <v-divider /> -->
          <v-row justify="center">
            <v-col cols="10">
              <v-text-field v-model="setting.name" label="网页名" />
            </v-col>
          </v-row>
          <v-row justify="center">
            <v-col cols="10">
              <v-textarea v-model="setting.webDescribe" label="简介" />
            </v-col>
          </v-row>
          <v-row justify="center">
            <v-col cols="10">
              <v-text-field v-model="setting.logoUrl" label="LOGO URL" />
            </v-col>
          </v-row>
          <v-row justify="center">
            <v-col cols="10">
              <v-switch
                :false-value="0"
                :true-value="1"
                color="primary"
                v-model="setting.openInvitationRegister"
                label="是否开启邀请码注册"
              />
            </v-col>
          </v-row>
          <v-row justify="center">
            <v-col cols="10">
              <v-switch
                color="primary"
                :false-value="0"
                :true-value="1"
                v-model="setting.openNoVipLimit"
                label="非vip观看次数限制"
              />
            </v-col>
          </v-row>
          <v-row v-if="setting.openNoVipLimit" justify="center">
            <v-col cols="10">
              <v-text-field v-model="setting.noVipViewCount" label="非VIP观看次数" type="number" />
            </v-col>
          </v-row>
          <v-row v-if="setting.openNoVipLimit" justify="center">
            <v-col cols="10">
              <v-switch
                color="primary"
                :false-value="0"
                :true-value="1"
                v-model="setting.openUploadVideoAddViewCount"
                label="开启上传视频增加观看次数"
              />
            </v-col>
          </v-row>

          <v-row justify="center">
            <v-col cols="10">
              <v-switch
                :false-value="0"
                :true-value="1"
                color="primary"
                v-model="setting.openExamine"
                label="开启审核"
              />
            </v-col>
          </v-row>
          <v-row justify="center">
            <v-col cols="10">
              <v-text-field
                v-model="setting.homeMaxVideoCount"
                label="首页最大显示数量"
                type="number"
              />
            </v-col>
          </v-row>
          <v-row justify="center">
            <v-col cols="10" />
          </v-row>
          <v-row justify="center" style="padding-top: 12px; padding-bottom: 24px">
            <v-col cols="10">
              <v-btn color="primary" block @click="save">保存</v-btn>
            </v-col>
          </v-row>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>
  
  <script>
export default {
  data() {
    return {
      setting: {},
    }
  },
  created() {
    this.getSetting()
  },
  methods: {
    getSetting() {
      this.httpGet('/web/info', (json) => {
        this.setting = json.data
      })
    },
    save() {
      this.httpPost('/admin/setting/save', this.setting, (json) => {})
    },
  },
}
</script>
  
  <style>
</style>
  