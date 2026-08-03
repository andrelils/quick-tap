"use strict";
const common_vendor = require("../../common/vendor.js");
const store_user = require("../../store/user.js");
const store_app = require("../../store/app.js");
const api_merchant = require("../../api/merchant.js");
const MERCHANT_ID_DEMO = 1;
const _sfc_main = {
  __name: "index",
  setup(__props, { expose: __expose }) {
    __expose();
    const userStore = store_user.useUserStore();
    const appStore = store_app.useAppStore();
    const merchantInfo = common_vendor.ref(null);
    const checking = common_vendor.ref(false);
    let hasCheckedOnLoad = false;
    const headerPadding = common_vendor.ref(88);
    const safeAreaTop = common_vendor.ref(44);
    function initNavBar() {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      const statusBarHeight = sysInfo.statusBarHeight || 20;
      safeAreaTop.value = statusBarHeight;
      try {
        const menuRect = common_vendor.index.getMenuButtonBoundingClientRect();
        const topPadding = menuRect.bottom + 8;
        headerPadding.value = Math.max(topPadding, statusBarHeight + 52);
      } catch (e) {
        headerPadding.value = statusBarHeight + 44;
      }
    }
    common_vendor.onLoad((options) => {
      initNavBar();
      console.log("[首页] onLoad options:", JSON.stringify(options));
      const params = resolveEntryParams(options);
      console.log("[首页] 解析后参数:", JSON.stringify(params));
      if (params.code) {
        hasCheckedOnLoad = true;
        handleEntryCode(params.code, params.type);
      } else if (options && options.merchantId) {
        loadMerchantById(options.merchantId);
      } else if (appStore.currentMerchant) {
        merchantInfo.value = appStore.currentMerchant;
      } else {
        console.log("[首页] 无入口参数，自动加载演示商家");
        loadMockMerchant();
      }
    });
    common_vendor.onShow(() => {
      if (hasCheckedOnLoad) {
        hasCheckedOnLoad = false;
        return;
      }
      if (appStore.currentMerchant) {
        merchantInfo.value = appStore.currentMerchant;
      }
    });
    function resolveEntryParams(options) {
      if (!options) return {};
      let code = options.code || options.deviceNo || options.q || options.d || options.device || "";
      let type = options.type || (options.q ? "qrcode" : "nfc");
      if (!code && options.scene) {
        const scene = decodeURIComponent(options.scene);
        const sceneMatch = scene.match(/(?:code|deviceNo|d|device|q)=([^&]+)/);
        if (sceneMatch) {
          code = sceneMatch[1];
          type = scene.includes("type=qrcode") ? "qrcode" : "nfc";
        } else if (scene.trim()) {
          code = scene.trim();
          type = "nfc";
        }
      }
      if (options.q && !code) {
        code = options.q;
      }
      if (options.q && options.q.startsWith("http")) {
        try {
          const decoded = decodeURIComponent(options.q);
          const urlMatch = decoded.match(/[?&](q|code|deviceNo|d|device)=([^&]+)/);
          if (urlMatch) {
            code = urlMatch[2];
            type = urlMatch[1] === "q" ? "qrcode" : "nfc";
          } else {
            code = decoded;
          }
        } catch {
          code = options.q;
        }
      }
      return { code, type };
    }
    async function loadMerchantById(merchantId) {
      checking.value = true;
      try {
        const res = await api_merchant.checkMerchantBind({ code: `mock_${merchantId}`, type: "mock" });
        if (res && res.bound && res.merchantId) {
          appStore.setCurrentMerchant(res.merchant);
          merchantInfo.value = res.merchant;
        } else {
          mockMerchant();
        }
      } catch (e) {
        console.error("[首页] 加载商家失败:", e);
        mockMerchant();
      } finally {
        checking.value = false;
      }
    }
    function mockMerchant() {
      const mock = {
        id: MERCHANT_ID_DEMO,
        name: "碰一碰演示商家",
        logo: "",
        bannerImages: [
          "/static/banner/banner1.jpg",
          "/static/banner/banner2.jpg",
          "/static/banner/banner3.jpg"
        ],
        address: "北京市朝阳区建国路88号",
        description: "这是一个演示商家页面，展示NFC智能推广系统的完整功能",
        bossWechat: "demo_boss_wechat",
        businessHours: "09:00-22:00",
        wifiName: "DemoShop-WiFi",
        wifiPassword: "demo1234",
        contactPhone: "400-888-8888",
        shopImages: [
          "/static/banner/banner1.jpg",
          "/static/banner/banner2.jpg",
          "/static/banner/banner3.jpg"
        ]
      };
      merchantInfo.value = mock;
      appStore.setCurrentMerchant(mock);
    }
    function loadMockMerchant() {
      mockMerchant();
      common_vendor.index.showToast({ title: "已加载演示商家", icon: "success" });
    }
    async function handleEntryCode(code, type) {
      checking.value = true;
      try {
        const res = await api_merchant.checkMerchantBind({ code, type });
        if (res && res.bound && res.merchantId) {
          appStore.setCurrentMerchant(res.merchant);
          merchantInfo.value = res.merchant;
          common_vendor.index.reLaunch({
            url: `/pages/merchant/detail?merchantId=${res.merchantId}`
          });
        } else {
          common_vendor.index.reLaunch({
            url: `/pages/user/register-bind?code=${encodeURIComponent(code)}&type=${type}`
          });
        }
      } catch (e) {
        console.error("[首页] 检查绑定失败:", e);
        common_vendor.index.showToast({
          title: "设备识别失败，请重试",
          icon: "none"
        });
      } finally {
        checking.value = false;
      }
    }
    const handleScan = () => {
      common_vendor.wx$1.scanCode({
        success: (res) => {
          parseScanResult(res.result);
        },
        fail: () => {
          common_vendor.index.showToast({
            title: "扫码取消",
            icon: "none"
          });
        }
      });
    };
    const parseScanResult = async (result) => {
      if (!result) {
        common_vendor.index.showToast({ title: "扫码结果无效", icon: "none" });
        return;
      }
      let code = "";
      let type = "qrcode";
      if (result.startsWith("http://") || result.startsWith("https://")) {
        try {
          const url = new URL(result);
          const q = url.searchParams.get("q");
          const d = url.searchParams.get("d") || url.searchParams.get("device") || url.searchParams.get("deviceNo");
          if (q) {
            code = q;
            type = "qrcode";
          } else if (d) {
            code = d;
            type = "nfc";
          }
        } catch {
          code = result;
        }
      } else {
        code = result.trim();
        type = "qrcode";
      }
      if (!code) {
        common_vendor.index.showToast({ title: "无效的二维码", icon: "none" });
        return;
      }
      checking.value = true;
      try {
        const res = await api_merchant.checkMerchantBind({ code, type });
        if (res && res.bound && res.merchantId) {
          appStore.setCurrentMerchant(res.merchant);
          common_vendor.index.reLaunch({
            url: `/pages/merchant/detail?merchantId=${res.merchantId}`
          });
        } else {
          common_vendor.index.reLaunch({
            url: `/pages/user/register-bind?code=${encodeURIComponent(code)}&type=${type}`
          });
        }
      } catch (e) {
        console.error("[扫码] 检查绑定失败:", e);
        common_vendor.index.showToast({ title: "设备识别失败，请重试", icon: "none" });
      } finally {
        checking.value = false;
      }
    };
    const goToWifi = () => {
      common_vendor.index.navigateTo({
        url: `/pages/wifi/index?merchantId=${merchantInfo.value.id}`
      });
    };
    const goToCoupon = () => {
      common_vendor.index.navigateTo({
        url: `/pages/coupon/list?merchantId=${merchantInfo.value.id}`
      });
    };
    const goToRegister = () => {
      common_vendor.index.navigateTo({
        url: "/pages/user/register-bind"
      });
    };
    const goToMine = () => {
      common_vendor.index.switchTab({
        url: "/pages/user/mine"
      });
    };
    const goToMerchantDetail = () => {
      common_vendor.index.navigateTo({
        url: `/pages/merchant/detail?merchantId=${merchantInfo.value.id}`
      });
    };
    const __returned__ = { userStore, appStore, MERCHANT_ID_DEMO, merchantInfo, checking, get hasCheckedOnLoad() {
      return hasCheckedOnLoad;
    }, set hasCheckedOnLoad(v) {
      hasCheckedOnLoad = v;
    }, headerPadding, safeAreaTop, initNavBar, resolveEntryParams, loadMerchantById, mockMerchant, loadMockMerchant, handleEntryCode, handleScan, parseScanResult, goToWifi, goToCoupon, goToRegister, goToMine, goToMerchantDetail, ref: common_vendor.ref, get onLoad() {
      return common_vendor.onLoad;
    }, get onShow() {
      return common_vendor.onShow;
    }, get useUserStore() {
      return store_user.useUserStore;
    }, get useAppStore() {
      return store_app.useAppStore;
    }, get checkMerchantBind() {
      return api_merchant.checkMerchantBind;
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
  return common_vendor.e({
    a: common_vendor.p({
      name: "thumb-up-fill",
      color: "#fff",
      size: "32"
    }),
    b: $setup.headerPadding + "px",
    c: common_vendor.p({
      name: "scan",
      color: "#1677ff",
      size: "48"
    }),
    d: common_vendor.o($setup.handleScan),
    e: $setup.merchantInfo
  }, $setup.merchantInfo ? {
    f: common_vendor.p({
      name: "wifi",
      color: "#fff",
      size: "28"
    }),
    g: common_vendor.o($setup.goToWifi)
  } : {}, {
    h: $setup.merchantInfo
  }, $setup.merchantInfo ? {
    i: common_vendor.p({
      name: "coupon",
      color: "#fff",
      size: "28"
    }),
    j: common_vendor.o($setup.goToCoupon)
  } : {}, {
    k: !$setup.userStore.isLoggedIn
  }, !$setup.userStore.isLoggedIn ? {
    l: common_vendor.p({
      name: "account",
      color: "#fff",
      size: "28"
    }),
    m: common_vendor.o($setup.goToRegister)
  } : {}, {
    n: common_vendor.p({
      name: "account-fill",
      color: "#fff",
      size: "28"
    }),
    o: common_vendor.o($setup.goToMine),
    p: common_vendor.p({
      name: "star",
      color: "#fff",
      size: "28"
    }),
    q: common_vendor.o($setup.loadMockMerchant),
    r: $setup.merchantInfo
  }, $setup.merchantInfo ? common_vendor.e({
    s: $setup.merchantInfo.logo || "/static/logo.png",
    t: common_vendor.t($setup.merchantInfo.name),
    v: common_vendor.p({
      name: "map-marker",
      size: "20",
      color: "#999"
    }),
    w: common_vendor.t($setup.merchantInfo.address || "暂无地址"),
    x: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    y: $setup.merchantInfo.description
  }, $setup.merchantInfo.description ? {
    z: common_vendor.t($setup.merchantInfo.description)
  } : {}, {
    A: common_vendor.o($setup.goToMerchantDetail)
  }) : {}, {
    B: !$setup.merchantInfo && !$setup.checking
  }, !$setup.merchantInfo && !$setup.checking ? {
    C: common_vendor.p({
      name: "info-circle",
      size: "64",
      color: "#d9d9d9"
    }),
    D: common_vendor.o($setup.loadMockMerchant)
  } : {}, {
    E: $setup.checking
  }, $setup.checking ? {} : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-83a5a03c"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/index/index.vue"]]);
wx.createPage(MiniProgramPage);
