import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  css: {
    preprocessorOptions: {
      scss: {
        additionalData: `@import "@/styles/variables.scss";@import "uview-plus/theme.scss";@import "uview-plus/libs/css/mixin.scss";`
      }
    }
  }
})
