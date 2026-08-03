<template>
  <a-config-provider :locale="zhCN" :render-empty="renderEmpty">
    <router-view />
  </a-config-provider>
</template>

<script setup>
import zhCN from 'ant-design-vue/es/locale/zh_CN'
import dayjs from 'dayjs'
import { h } from 'vue'
import { Empty } from 'ant-design-vue'
dayjs.locale('zh-cn')

const renderEmpty = () => {
  return h(Empty, {
    image: Empty.PRESENTED_IMAGE_SIMPLE,
    description: '暂无数据'
  })
}
</script>
