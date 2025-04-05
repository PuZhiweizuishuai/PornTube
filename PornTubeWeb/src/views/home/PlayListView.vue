<template>
  <v-container>
    <NotLoginCard v-if="userInfo.userData == null"></NotLoginCard>
    <div v-else>稍后再看开发中</div>
  </v-container>
</template>
  
  <script>
import NotLoginCard from '@/components/card/NotLoginCard.vue'
import { useUserStore } from '@/stores/userStore'
export default {
  components: {
    NotLoginCard,
  },
  data() {
    return {
      userInfo: useUserStore(),
    }
  },
}
</script>
  
  <style>
</style>