module.exports = {
  devServer: {
    port: 10012,
    proxy: {
      '/api': {
        target: 'http://localhost:10022',
        changeOrigin: true
      }
    }
  },
  productionSourceMap: false
}
