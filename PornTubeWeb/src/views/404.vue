<template>
  <v-container fluid class="pa-0 fill-height d-flex align-center">
    <v-row justify="center" align="center">
      <v-col cols="12" sm="10" md="8" lg="6" xl="4">
        <v-card class="mx-auto pa-4" elevation="0" rounded="lg">
          <v-card-item class="text-center">
            <v-card-title class="text-h4 font-weight-bold my-4">
              <span class="text-red">4</span>
              <span class="text-red">0</span>
              <span class="text-red">4</span>
            </v-card-title>
            <v-card-subtitle class="text-h6 my-3"> 此页面不可用 </v-card-subtitle>
          </v-card-item>

          <v-card-text class="text-center">
            <v-img
              src="/images/error.gif"
              alt="404 "
              class="mx-auto my-4"
              max-width="300"
              contain
            ></v-img>

            <p class="text-body-1 my-3">尝试访问的页面不存在或已被删除。</p>

            <v-divider class="my-5"></v-divider>

            <div class="d-flex flex-column flex-sm-row justify-center gap-3 mt-5">
              <v-btn
                color="red"
                variant="elevated"
                prepend-icon="mdi-home"
                size="large"
                rounded="lg"
                @click="$router.push('/')"
              >
                返回首页
              </v-btn>
              <span>&nbsp;&nbsp;&nbsp;&nbsp;</span>
              <v-btn
                variant="outlined"
                color="default"
                prepend-icon="mdi-refresh"
                size="large"
                rounded="lg"
                @click="goBack"
              >
                返回上一页
              </v-btn>
            </div>

            <div class="mt-6 text-body-2 text-medium-emphasis">
              如果您认为这是一个错误，请
              <a href="#" class="text-decoration-none text-red">联系我们</a>
            </div>
          </v-card-text>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: 'NotFoundPage',
  methods: {
    goBack() {
      window.history.length > 1 ? this.$router.go(-1) : this.$router.push('/')
    },
  },
}
</script>

<style>
.text-red {
  color: #ff0000 !important;
}

.v-card {
  background-color: transparent !important;
}
</style>