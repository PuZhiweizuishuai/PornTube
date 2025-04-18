import { useUserStore } from '../stores/userStore';

export default {
  install: (app) => {
    // 检查未授权访问的函数
    const checkUnauthorized = function(json, context) {
      if (json && json.status === 0 && json.message === "登录过期或未登录") {
        const userStore = useUserStore();
        userStore.deleteUserData();
        
        // 使用Vue实例中的router导航
        if (context && context.$router) {
          context.$router.push('/login');
        } else {
          // 备用方案，直接使用window.location
          window.location.href = '/login';
        }
        return true;
      }
      return false;
    };
    
    app.config.globalProperties.httpGet = function (url, cb) {
      fetch(`${this.SERVER_API_URL}${url}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          //'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'GET',
        credentials: 'include',
      })
        .then((response) => response.json())
        .then((json) => {
          if (!checkUnauthorized(json, this)) {
            cb(json);
          }
        })
        .catch((error) => {
          console.error('HTTP Get Error:', error)
          return null
        })
    }

    app.config.globalProperties.httpPost = function (url, data, cb) {
      fetch(`${this.SERVER_API_URL}${url}`, {
        headers: {
          'Content-Type': 'application/json; charset=UTF-8',
          //'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify(data),
      })
        .then((response) => response.json())
        .then((json) => {
          if (!checkUnauthorized(json, this)) {
            cb(json);
          }
        })
        .catch((error) => {
          console.error('HTTP Post Error:', error)
          return null
        })
    }

    app.config.globalProperties.uploadFiles = function (url, form, cb) {
      fetch(`${this.SERVER_API_URL}${url}`, {
        headers: {
          //'X-XSRF-TOKEN': this.$cookies.get('XSRF-TOKEN')
        },
        method: 'POST',
        credentials: 'include',
        body: form,
      })
        .then((response) => response.json())
        .then((json) => {
          if (!checkUnauthorized(json, this)) {
            cb(json);
          }
        })
        .catch((error) => {
          console.error('Upload Files Error:', error)
          return null
        })
    }
  },
}
