"use strict";
Object.defineProperty(exports, Symbol.toStringTag, { value: "Module" });
const common_vendor = require("./common/vendor.js");
if (!Math) {
  "./pages/index/index.js";
  "./pages/merchant/detail.js";
  "./pages/user/register-bind.js";
  "./pages/user/mine.js";
  "./pages/promotion/jump.js";
  "./pages/wifi/index.js";
  "./pages/coupon/list.js";
  "./pages/scan/scan.js";
}
const _sfc_main = {
  onLaunch() {
    console.log("App Launch");
  },
  onShow() {
    console.log("App Show");
  },
  onHide() {
    console.log("App Hide");
  }
};
const App = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/App.vue"]]);
function createApp() {
  const app = common_vendor.createSSRApp(App);
  const pinia = common_vendor.createPinia();
  app.use(pinia);
  app.use(common_vendor.uviewPlus);
  return { app };
}
createApp().app.mount("#app");
exports.createApp = createApp;
