"use strict";
const common_vendor = require("../common/vendor.js");
const useAppStore = common_vendor.defineStore("app", () => {
  const currentMerchant = common_vendor.ref(null);
  const currentDevice = common_vendor.ref(null);
  const setCurrentMerchant = (merchant) => {
    currentMerchant.value = merchant;
  };
  const setCurrentDevice = (device) => {
    currentDevice.value = device;
  };
  const clearCurrent = () => {
    currentMerchant.value = null;
    currentDevice.value = null;
  };
  return {
    currentMerchant,
    currentDevice,
    setCurrentMerchant,
    setCurrentDevice,
    clearCurrent
  };
});
exports.useAppStore = useAppStore;
