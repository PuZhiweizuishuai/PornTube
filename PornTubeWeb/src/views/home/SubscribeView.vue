<template>
  <v-container>
    <NotLoginCard v-if="userInfo.userData == null"></NotLoginCard>
    <div v-else>
      <v-row align="center" class="mb-4">
        <v-col>
          <h2 class="text-h4 font-weight-bold">
            <v-icon icon="mdi-youtube-subscription" color="red" class="mr-2"></v-icon>
            订阅
          </h2>
          <p class="text-body-2 text-grey">你关注的那些博主</p>
        </v-col>
      </v-row>
    </div>
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