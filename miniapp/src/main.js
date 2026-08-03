import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import uviewPlus from 'uview-plus'
import UIcon from 'uview-plus/components/u-icon/u-icon.vue'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)
  const pinia = createPinia()

  app.use(pinia)
  app.use(uviewPlus)

  // 修复 uview-plus UInput 组件内部引用 up-icon 未注册的问题
  app.component('up-icon', UIcon)

  return { app }
}
