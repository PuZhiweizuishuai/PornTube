<template>
  <v-container fill-height>
    <v-row no-gutters justify="center" align="center">
      <v-col
        v-for="item in items"
        :key="item.id"
        v-resize="onResize"
        :cols="cols"
        style="margin-top: 48px;"
      >
        <v-card
          class="mx-auto"
          max-width="344"
        >
          <v-card-text>
            <div> 时长： </div>
            <p class="display-1 text--primary">
              {{ item.time }}
            </p>

            <div class="text--primary">
              <strong> ￥： {{ item.money }} </strong>
            </div>
          </v-card-text>
          <v-card-actions>
            <v-btn
              text
              color="deep-purple accent-4"
              @click="dialog = true"
            >
              支付
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
    <v-dialog
      v-model="dialog"
      max-width="290"
    >
      <v-card>
        <v-card-title class="headline">支付接口尚未完成</v-card-title>

        <v-card-text>
          如有需要，请联系网站管理
        </v-card-text>

        <v-card-actions>
          <v-spacer />

          <v-btn
            color="green darken-1"
            text
            @click="dialog = false"
          >
            取消
          </v-btn>

          <v-btn
            color="green darken-1"
            text
            @click="dialog = false"
          >
            同意
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script>
export default {
  data() {
    return {
      dialog: false,
      cols: 5,
      items: [
        { id: 0, money: 10, time: '一个月' },
        { id: 1, money: 25, time: '三个月' },
        { id: 2, money: 40, time: '半年' },
        { id: 3, money: 60, time: '一年' }
      ],
      windowSize: {

      }
    }
  },
  created() {
    this.windowSize = { x: window.innerWidth, y: window.innerHeight }
  },
  methods: {
    onResize() {
      this.windowSize = { x: window.innerWidth, y: window.innerHeight }
      if (this.windowSize.x < 700) {
        this.cols = 12
      } else {
        this.cols = 5
      }
    }
  }
}
</script>

<style>

</style>
