export default {
  install: (app, options) => {
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
          cb(json)
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
          cb(json)
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
          cb(json)
        })
        .catch((error) => {
          console.error('Upload Files Error:', error)
          return null
        })
    }
  },
}
