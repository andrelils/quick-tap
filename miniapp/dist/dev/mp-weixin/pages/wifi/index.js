"use strict";
const common_vendor = require("../../common/vendor.js");
const api_merchant = require("../../api/merchant.js");
const store_app = require("../../store/app.js");
const _sfc_main = {
  __name: "index",
  setup(__props, { expose: __expose }) {
    __expose();
    const appStore = store_app.useAppStore();
    const wifiInfo = common_vendor.ref(null);
    const showPassword = common_vendor.ref(false);
    common_vendor.onLoad((options) => {
      const { merchantId } = options;
      if (merchantId) {
        loadWifiInfo(merchantId);
      }
    });
    const loadWifiInfo = async (merchantId) => {
      try {
        const res = await api_merchant.getMerchantWifi(merchantId);
        if (res) {
          wifiInfo.value = res;
        } else {
          mockWifiInfo();
        }
      } catch (e) {
        console.error("加载WiFi信息失败", e);
        mockWifiInfo();
      }
    };
    const mockWifiInfo = () => {
      const merchant = appStore.currentMerchant;
      wifiInfo.value = {
        ssid: (merchant == null ? void 0 : merchant.wifiName) || "DemoShop-WiFi",
        password: (merchant == null ? void 0 : merchant.wifiPassword) || "demo1234",
        encryption: "WPA2"
      };
    };
    const togglePassword = () => {
      showPassword.value = !showPassword.value;
    };
    const copyText = (text) => {
      common_vendor.index.setClipboardData({
        data: text,
        success: () => {
          common_vendor.index.showToast({
            title: "已复制",
            icon: "success"
          });
        }
      });
    };
    const copyPassword = () => {
      var _a;
      if ((_a = wifiInfo.value) == null ? void 0 : _a.password) {
        copyText(wifiInfo.value.password);
      }
    };
    const showWifiQr = () => {
      common_vendor.index.showToast({
        title: "二维码生成中...",
        icon: "loading"
      });
    };
    const __returned__ = { appStore, wifiInfo, showPassword, loadWifiInfo, mockWifiInfo, togglePassword, copyText, copyPassword, showWifiQr, ref: common_vendor.ref, onMounted: common_vendor.onMounted, get onLoad() {
      return common_vendor.onLoad;
    }, get getMerchantWifi() {
      return api_merchant.getMerchantWifi;
    }, get useAppStore() {
      return store_app.useAppStore;
    } };
    Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
    return __returned__;
  }
};
if (!Array) {
  const _easycom_u_icon2 = common_vendor.resolveComponent("u-icon");
  _easycom_u_icon2();
}
const _easycom_u_icon = () => "../../node-modules/uview-plus/components/u-icon/u-icon.js";
if (!Math) {
  _easycom_u_icon();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  var _a;
  return common_vendor.e({
    a: common_vendor.p({
      name: "wifi",
      color: "#fff",
      size: "64"
    }),
    b: common_vendor.t(((_a = $setup.wifiInfo) == null ? void 0 : _a.ssid) || "WiFi连接"),
    c: $setup.wifiInfo
  }, $setup.wifiInfo ? {
    d: common_vendor.p({
      name: "scan",
      color: "#1677ff",
      size: "80"
    })
  } : {}, {
    e: $setup.wifiInfo
  }, $setup.wifiInfo ? {
    f: common_vendor.t($setup.wifiInfo.ssid),
    g: common_vendor.p({
      name: "copy",
      size: "24",
      color: "#1677ff"
    }),
    h: common_vendor.o(($event) => $setup.copyText($setup.wifiInfo.ssid)),
    i: common_vendor.t($setup.showPassword ? $setup.wifiInfo.password : "********"),
    j: common_vendor.p({
      name: $setup.showPassword ? "eye" : "eye-fill",
      size: "24",
      color: "#1677ff"
    }),
    k: common_vendor.o($setup.togglePassword),
    l: common_vendor.t($setup.wifiInfo.encryption || "WPA")
  } : {}, {
    m: common_vendor.p({
      name: "copy",
      color: "#fff",
      size: "28"
    }),
    n: common_vendor.o($setup.copyPassword),
    o: common_vendor.p({
      name: "scan",
      color: "#1677ff",
      size: "28"
    }),
    p: common_vendor.o($setup.showWifiQr),
    q: common_vendor.p({
      name: "info-circle",
      size: "28",
      color: "#1677ff"
    })
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-1bdb54f9"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/wifi/index.vue"]]);
wx.createPage(MiniProgramPage);
