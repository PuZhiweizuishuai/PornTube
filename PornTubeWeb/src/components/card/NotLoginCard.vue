<!-- 未登录卡片 -->
<template>
  <v-container class="fill-height">
    <v-row justify="center" align="center">
      <v-col cols="12" sm="8" md="6" lg="5">
        <v-card class="mx-auto py-4 rounded-lg" elevation="3" border>
          <v-card-item>
            <div class="text-center">
              <v-icon
                icon="mdi-account-lock-outline"
                size="x-large"
                color="primary"
                class="mb-4"
              ></v-icon>
              <v-card-title class="text-center text-h5 mb-2">需要登录</v-card-title>
              <v-card-text class="text-body-1 mb-2"> 你还没有登录，请登录后查看此内容 </v-card-text>
            </div>
          </v-card-item>

          <v-card-actions class="justify-center">
            <v-btn
              color="primary"
              size="large"
              variant="elevated"
              rounded="lg"
              prepend-icon="mdi-login"
              @click="navigateToLogin"
            >
              登录
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
export default {
  name: 'NotLoginCard',
  methods: {
    navigateToLogin() {
      this.$router.push('/login')
    },
  },
}
</script>

<style scoped>
.v-card {
  transition: transform 0.3s;
}
.v-card:hover {
  transform: translateY(-5px);
}
</style>
  