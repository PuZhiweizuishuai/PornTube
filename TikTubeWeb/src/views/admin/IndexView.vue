<template>
  <v-container fluid>
    <v-card class="mx-auto w-100 mb-4" elevation="2" rounded="lg">
      <v-toolbar color="green-lighten-1">
        <v-toolbar-title class="text-h5 font-weight-medium">
          <v-icon color="primary" class="mr-2">mdi-information</v-icon>
          数据管理中心
        </v-toolbar-title>
        <v-spacer></v-spacer>
      </v-toolbar>
      <v-card-text>
        <v-row>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="primary"
              prepend-icon="mdi-trending-up"
              variant="outlined"
              block
              @click="refreshPopular"
            >
              手动刷新时下流行
            </v-btn>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="error"
              prepend-icon="mdi-delete-sweep"
              variant="outlined"
              block
              @click="deleteTempFile"
            >
              手动清理临时文件
            </v-btn>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="info"
              prepend-icon="mdi-database-sync"
              variant="outlined"
              block
              @click="syscData"
            >
              立即同步缓存
            </v-btn>
          </v-col>
          <v-col cols="12" sm="6" md="3">
            <v-btn
              color="success"
              prepend-icon="mdi-advertisements"
              variant="outlined"
              block
              @click="syscAds"
            >
              立即同步广告数据
            </v-btn>
          </v-col>
        </v-row>
      </v-card-text>
    </v-card>

    <!-- 操作结果提示 -->
    <v-snackbar v-model="snackbar.show" :color="snackbar.color" :timeout="3000" location="top">
      {{ snackbar.text }}
      <template v-slot:actions>
        <v-btn variant="text" color="white" @click="snackbar.show = false">关闭</v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>
  
<script>
export default {
  data() {
    return {
      snackbar: {
        show: false,
        text: '',
        color: 'success',
      },
    }
  },

  methods: {
    refreshPopular() {
      this.httpPost('/admin/system/refresh/hot', {}, (json) => {
        if (json.status === 200) {
          this.showSnackbar('热门数据刷新成功', 'success')
        } else {
          this.showSnackbar('热门数据刷新失败', 'error')
        }
      })
    },
    deleteTempFile() {
      this.httpPost('/admin/system/file/delete', {}, (json) => {
        if (json.status === 200) {
          this.showSnackbar('临时文件清理成功', 'success')
        } else {
          this.showSnackbar('临时文件清理失败', 'error')
        }
      })
    },
    syscData() {
      this.httpPost('/admin/system/data/sync', {}, (json) => {
        if (json.status === 200) {
          this.showSnackbar('缓存同步成功', 'success')
        } else {
          this.showSnackbar('缓存同步失败', 'error')
        }
      })
    },
    syscAds() {
      this.httpPost('/admin/system/ads/sync', {}, (json) => {
        if (json.status === 200) {
          this.showSnackbar('广告数据同步成功', 'success')
        } else {
          this.showSnackbar('广告数据同步失败', 'error')
        }
      })
    },

    // 显示操作结果提示
    showSnackbar(text, color = 'success') {
      this.snackbar.text = text
      this.snackbar.color = color
      this.snackbar.show = true
    },
  },
}
</script>
