import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const currentMerchant = ref(null)
  const currentDevice = ref(null)

  const setCurrentMerchant = (merchant) => {
    currentMerchant.value = merchant
  }

  const setCurrentDevice = (device) => {
    currentDevice.value = device
  }

  const clearCurrent = () => {
    currentMerchant.value = null
    currentDevice.value = null
  }

  return {
    currentMerchant,
    currentDevice,
    setCurrentMerchant,
    setCurrentDevice,
    clearCurrent
  }
})
