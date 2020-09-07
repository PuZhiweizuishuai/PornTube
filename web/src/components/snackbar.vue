<template>
  <v-snackbar
    v-model="snackbar"
  >
    {{ text }}

    <template v-slot:action="{ attrs }">
      <v-btn
        color="pink"
        text
        v-bind="attrs"
        @click="snackbar = false"
      >
        关闭
      </v-btn>
    </template>
  </v-snackbar>
</template>

<script>
export default {
  name: 'Snackbar',
  props: {
    message: {
      type: String,
      default: ''
    },
    show: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      text: this.message,
      snackbar: false
    }
  }
}
</script>

<style>

</style>
