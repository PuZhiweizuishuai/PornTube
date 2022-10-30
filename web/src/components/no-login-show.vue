<template>
  <v-row justify="center" align="center" style="margin-top: 12px;">
    <v-col cols="12">
      <v-card
        class="mx-auto"
        max-width="500"
      >
        <v-row justify="center">
          <v-col cols="10" style="text-align: center">
            你还没有登录，请登录后查看
          </v-col>
        </v-row>
        <v-row justify="center">
          <v-col cols="10" style="text-align: center">
            <v-btn x-large outlined color="indigo" @click="()=>this.$router.push('/login')">登录</v-btn>
          </v-col>
        </v-row>

      </v-card>
    </v-col>
  </v-row>
</template>

<script>
export default {
  name: 'NoLoginShow'

}
</script>

<style>

</style>
