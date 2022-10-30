const path = require('path')

function resolve(dir) {
  return path.join(__dirname, dir)
}

const name = 'PornHub'

module.exports = {
  productionSourceMap: false,
  // options...
  devServer: {
    port: 8000,
    proxy: {
      '/api': {
        target: 'http://123.56.161.54:8081',
        changeOrigin: true,
        ws: true,
        pathRewrite: {
          '^/api': '/api'
        }
      }

    }
  },
  configureWebpack: {
    // provide the app's title in webpack's name field, so that
    // it can be accessed in index.html to inject the correct title.
    name: name,
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  }
}
