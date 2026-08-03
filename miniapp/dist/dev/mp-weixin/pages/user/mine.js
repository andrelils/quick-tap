"use strict";
const common_vendor = require("../../common/vendor.js");
const store_user = require("../../store/user.js");
const _sfc_main = {
  __name: "mine",
  setup(__props, { expose: __expose }) {
    __expose();
    const userStore = store_user.useUserStore();
    const stats = common_vendor.ref({
      totalScans: 0,
      totalPromotions: 0,
      coupons: 0
    });
    const headerPadding = common_vendor.ref(88);
    function initNavBar() {
      const sysInfo = common_vendor.index.getSystemInfoSync();
      const statusBarHeight = sysInfo.statusBarHeight || 20;
      try {
        const menuRect = common_vendor.index.getMenuButtonBoundingClientRect();
        const topPadding = menuRect.bottom + 8;
        headerPadding.value = Math.max(topPadding, statusBarHeight + 52);
      } catch (e) {
        headerPadding.value = statusBarHeight + 44;
      }
    }
    common_vendor.onLoad(() => {
      initNavBar();
    });
    common_vendor.onMounted(() => {
      if (userStore.isLoggedIn) {
        loadStats();
      }
    });
    common_vendor.onShow(() => {
      if (userStore.isLoggedIn) {
        loadStats();
      }
    });
    const loadStats = async () => {
    };
    const goToLogin = () => {
      common_vendor.wx$1.login({
        success: async (res) => {
          if (res.code) {
            try {
              await userStore.loginByWechat(res.code);
              loadStats();
            } catch (e) {
              common_vendor.index.showToast({
                title: "登录失败",
                icon: "none"
              });
            }
          }
        }
      });
    };
    const goToRegisterBind = () => {
      common_vendor.index.navigateTo({
        url: "/pages/user/register-bind"
      });
    };
    const goToMyDevices = () => {
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    };
    const goToMyCoupons = () => {
      common_vendor.index.navigateTo({
        url: "/pages/coupon/list"
      });
    };
    const goToPromotionHistory = () => {
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    };
    const goToScanHistory = () => {
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    };
    const goToSettings = () => {
      common_vendor.index.showToast({
        title: "功能开发中",
        icon: "none"
      });
    };
    const goToAbout = () => {
      common_vendor.index.showModal({
        title: "关于我们",
        content: "碰一碰好评卡系统 v1.0.0\n\nNFC智能推广，一键好评，助力商家成长",
        showCancel: false
      });
    };
    const handleLogout = () => {
      common_vendor.index.showModal({
        title: "提示",
        content: "确定要退出登录吗？",
        success: (res) => {
          if (res.confirm) {
            userStore.logout();
            common_vendor.index.showToast({
              title: "已退出登录",
              icon: "success"
            });
          }
        }
      });
    };
    const __returned__ = { userStore, stats, headerPadding, initNavBar, loadStats, goToLogin, goToRegisterBind, goToMyDevices, goToMyCoupons, goToPromotionHistory, goToScanHistory, goToSettings, goToAbout, handleLogout, ref: common_vendor.ref, onMounted: common_vendor.onMounted, get onShow() {
      return common_vendor.onShow;
    }, get onLoad() {
      return common_vendor.onLoad;
    }, get useUserStore() {
      return store_user.useUserStore;
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
  var _a, _b, _c, _d, _e, _f, _g, _h;
  return common_vendor.e({
    a: $setup.userStore.isLoggedIn
  }, $setup.userStore.isLoggedIn ? common_vendor.e({
    b: ((_a = $setup.userStore.userInfo) == null ? void 0 : _a.avatar) || "/static/avatar.png",
    c: common_vendor.t(((_b = $setup.userStore.userInfo) == null ? void 0 : _b.nickname) || "微信用户"),
    d: (_c = $setup.userStore.userInfo) == null ? void 0 : _c.phone
  }, ((_d = $setup.userStore.userInfo) == null ? void 0 : _d.phone) ? {
    e: common_vendor.t($setup.userStore.userInfo.phone)
  } : {}, {
    f: (_e = $setup.userStore.userInfo) == null ? void 0 : _e.phone
  }, ((_f = $setup.userStore.userInfo) == null ? void 0 : _f.phone) ? {
    g: common_vendor.p({
      name: "checkmark-circle",
      size: "20",
      color: "#52c41a"
    })
  } : {}) : {
    h: common_vendor.p({
      name: "account",
      color: "#fff",
      size: "48"
    }),
    i: common_vendor.p({
      name: "arrow-right",
      size: "24",
      color: "rgba(255,255,255,0.6)"
    }),
    j: common_vendor.o($setup.goToLogin)
  }, {
    k: $setup.headerPadding + "px",
    l: $setup.userStore.isLoggedIn
  }, $setup.userStore.isLoggedIn ? {
    m: common_vendor.t($setup.stats.totalScans || 0),
    n: common_vendor.t($setup.stats.totalPromotions || 0),
    o: common_vendor.t($setup.stats.coupons || 0)
  } : {}, {
    p: !((_g = $setup.userStore.userInfo) == null ? void 0 : _g.phone)
  }, !((_h = $setup.userStore.userInfo) == null ? void 0 : _h.phone) ? {
    q: common_vendor.p({
      name: "link",
      color: "#1677ff",
      size: "28"
    }),
    r: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    s: common_vendor.o($setup.goToRegisterBind)
  } : {}, {
    t: common_vendor.p({
      name: "scan",
      color: "#52c41a",
      size: "28"
    }),
    v: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    w: common_vendor.o($setup.goToMyDevices),
    x: common_vendor.p({
      name: "coupon",
      color: "#faad14",
      size: "28"
    }),
    y: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    z: $setup.stats.coupons > 0
  }, $setup.stats.coupons > 0 ? {
    A: common_vendor.t($setup.stats.coupons)
  } : {}, {
    B: common_vendor.o($setup.goToMyCoupons),
    C: common_vendor.p({
      name: "chart-pie",
      color: "#722ed1",
      size: "28"
    }),
    D: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    E: common_vendor.o($setup.goToPromotionHistory),
    F: common_vendor.p({
      name: "clock",
      color: "#13c2c2",
      size: "28"
    }),
    G: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    H: common_vendor.o($setup.goToScanHistory),
    I: common_vendor.p({
      name: "setting",
      color: "#8c8c8c",
      size: "28"
    }),
    J: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    K: common_vendor.o($setup.goToSettings),
    L: common_vendor.p({
      name: "info-circle",
      color: "#8c8c8c",
      size: "28"
    }),
    M: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    N: common_vendor.o($setup.goToAbout),
    O: $setup.userStore.isLoggedIn
  }, $setup.userStore.isLoggedIn ? {
    P: common_vendor.o($setup.handleLogout)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-10732b5c"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/user/mine.vue"]]);
wx.createPage(MiniProgramPage);
