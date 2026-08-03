<script>
export default {
  onLaunch() {
    console.log('App Launch')
    // H5 环境：处理根路径访问，自动重定向到 index 页面并保留参数
    this.handleRootRedirect()
  },
  onShow() {
    console.log('App Show')
  },
  onHide() {
    console.log('App Hide')
  },
  methods: {
    handleRootRedirect() {
      // 无论何时启动，只要 URL 中携带了 code/deviceNo/q 参数，就自动跳转
      const fullUrl = window.location.href
      const codeMatch = fullUrl.match(/[?&](?:code|deviceNo|q)=([^&#]+)/)
      if (!codeMatch) return

      const code = decodeURIComponent(codeMatch[1])
      const targetUrl = '#/pages/index/index?code=' + code
      
      // 如果已经在目标页面就不要再跳转了
      const currentHash = window.location.hash
      if (currentHash && currentHash.startsWith('#/pages/index/index')) return

      console.log('[APP] 根路径重定向:', targetUrl)
      // 使用完整的 href 重新设置以确保路由正确触发
      const base = fullUrl.split('#')[0]
      window.location.href = base + targetUrl
    }
  }
}
</script>

<style lang="scss">
@import 'uview-plus/index.scss';

page {
  background-color: #f5f6fa;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}

.container {
  padding: 24rpx;
}
</style>
