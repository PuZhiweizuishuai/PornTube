import Vue from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import vuetify from './plugins/vuetify'
import VueCookies from 'vue-cookies'
import HttpFetch from '@/utils/fetch.js'
Vue.use(VueCookies)
Vue.use(HttpFetch)
Vue.config.productionTip = false

Vue.prototype.SERVER_API_URL = '/api'

new Vue({
  router,
  store,
  vuetify,
  render: h => h(App)
}).$mount('#app')
