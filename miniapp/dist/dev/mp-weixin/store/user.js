"use strict";
const common_vendor = require("../common/vendor.js");
const api_user = require("../api/user.js");
const useUserStore = common_vendor.defineStore("user", () => {
  const token = common_vendor.ref(common_vendor.index.getStorageSync("token") || "");
  const userInfo = common_vendor.ref(common_vendor.index.getStorageSync("userInfo") || null);
  const isLoggedIn = common_vendor.computed(() => !!token.value);
  const setToken = (newToken) => {
    token.value = newToken;
    common_vendor.index.setStorageSync("token", newToken);
  };
  const setUserInfo = (info) => {
    userInfo.value = info;
    common_vendor.index.setStorageSync("userInfo", info);
  };
  const loginByWechat = async (code) => {
    const res = await api_user.wechatLogin(code);
    if (res.token) {
      setToken(res.token);
      if (res.userInfo) {
        setUserInfo(res.userInfo);
      }
    }
    return res;
  };
  const fetchUserInfo = async () => {
    const res = await api_user.getUserInfo();
    setUserInfo(res);
    return res;
  };
  const logout = () => {
    token.value = "";
    userInfo.value = null;
    common_vendor.index.removeStorageSync("token");
    common_vendor.index.removeStorageSync("userInfo");
  };
  return {
    token,
    userInfo,
    isLoggedIn,
    setToken,
    setUserInfo,
    loginByWechat,
    fetchUserInfo,
    logout
  };
});
exports.useUserStore = useUserStore;
