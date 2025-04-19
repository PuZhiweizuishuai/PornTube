<template>
  <v-container>
    <v-row>
      <h1>数据管理中心</h1>
    </v-row>
    <v-row>
      <v-btn @click="refreshPopular">手动刷新时下流行</v-btn>
    </v-row>
    <v-row>
      <v-btn @click="deleteTempFile">手动清理临时文件</v-btn>
    </v-row>
  </v-container>
</template>
  
  <script>
export default {
  data() {
    return {}
  },
  methods: {
    refreshPopular() {
      this.httpPost('/admin/system/refresh/hot', {}, (json) => {
        if (json.status === 200) {
          //
        }
      })
    },
    deleteTempFile() {
      this.httpPost('/admin/system/file/delete', {}, (json) => {
        if (json.status === 200) {
          //
        }
      })
    },
  },
}
</script>
  
  <style>
</style>