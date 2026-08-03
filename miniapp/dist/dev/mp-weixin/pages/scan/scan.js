"use strict";
const common_vendor = require("../../common/vendor.js");
const _sfc_main = {
  __name: "scan",
  setup(__props, { expose: __expose }) {
    __expose();
    const handleScan = () => {
      common_vendor.index.showToast({
        title: "扫描功能开发中",
        icon: "none"
      });
    };
    const __returned__ = { handleScan, ref: common_vendor.ref };
    Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
    return __returned__;
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return {
    a: common_vendor.o($setup.handleScan)
  };
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-933ff6b6"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/scan/scan.vue"]]);
wx.createPage(MiniProgramPage);
