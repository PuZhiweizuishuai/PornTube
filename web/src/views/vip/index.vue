<template>
  <v-container fill-height>
    <v-row v-if="this.$store.state.userInfo == null || checkPower.checkVip(this.$store.state.userInfo) === false" justify="center" align="center">
      <v-col>
        <v-card
          class="mx-auto"
          max-width="500"
        >
          <v-row justify="center">
            <v-col cols="10" style="text-align: center">
              <h3>开启VIP，享受无限观看次数。获取专属VIP标记！</h3>
            </v-col>
          </v-row>
          <v-row justify="center">
            <v-col cols="10" style="text-align: center">
              <v-btn x-large outlined color="indigo" @click="()=>this.$router.push('/vip/pay')">开启</v-btn>
            </v-col>
          </v-row>

        </v-card>
      </v-col>
    </v-row>
    <v-row v-else justify="center" align="center">
      <v-col>
        <v-card
          class="mx-auto"
          max-width="400"
        >
          <v-img
            class="white--text align-end"
            height="200px"
            src="https://cdn.vuetifyjs.com/images/cards/docks.jpg"
          >
            <v-card-title>你好，尊贵的VIP:  {{ this.$store.state.userInfo.username }}</v-card-title>
          </v-img>

          <v-card-text class="text--primary">
            <div>VIP到期时间： <strong>{{ TimeUtil.renderTime(this.$store.state.userInfo.userRoleEntity.vipStopTime) }}</strong></div>
            <div>剩余观看次数：<strong> ∞ </strong></div>
          </v-card-text>

          <v-card-actions>
            <v-btn
              color="orange"
              text
              @click="()=>this.$router.push('/vip/pay')"
            >
              续费
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>

  </v-container>
</template>

<script>
import checkPower from '@/utils/check-power.vue'
import TimeUtil from '@/utils/time-util.vue'
export default {
  name: 'VIP',
  data() {
    return {
      checkPower,
      TimeUtil
    }
  }
}
</script>

<style>

</style>
