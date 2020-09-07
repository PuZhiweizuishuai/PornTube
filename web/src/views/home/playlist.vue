<template>
  <v-container fill-height>
    <NoLoginShow v-if="this.$store.state.userInfo == null" />
  </v-container>
</template>

<script>
import NoLoginShow from '@/components/no-login-show.vue'
export default {
  name: 'PlayList',
  components: {
    NoLoginShow
  }
}
</script>

<style>

</style>
