import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAppStore = defineStore('app', () => {
  const collapsed = ref(false)
  const currentMerchant = ref(null)

  const merchantId = computed(() => currentMerchant.value?.id || '')

  const toggleCollapsed = () => {
    collapsed.value = !collapsed.value
  }

  const setCurrentMerchant = (merchant) => {
    currentMerchant.value = merchant
  }

  return {
    collapsed,
    currentMerchant,
    merchantId,
    toggleCollapsed,
    setCurrentMerchant
  }
})
