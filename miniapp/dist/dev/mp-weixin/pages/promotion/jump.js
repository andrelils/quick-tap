"use strict";
const common_vendor = require("../../common/vendor.js");
const api_promotion = require("../../api/promotion.js");
const _sfc_main = {
  __name: "jump",
  setup(__props, { expose: __expose }) {
    __expose();
    const platformId = common_vendor.ref(null);
    const merchantId = common_vendor.ref(null);
    const platformName = common_vendor.ref("推广平台");
    const platformColor = common_vendor.ref("#1677ff");
    const platformDesc = common_vendor.ref("");
    const jumpMode = common_vendor.ref("scheme");
    const schemeUrl = common_vendor.ref("");
    const webUrl = common_vendor.ref("");
    const fallbackUrl = common_vendor.ref("");
    const miniprogramAppid = common_vendor.ref("");
    const miniprogramPath = common_vendor.ref("");
    const currentLink = common_vendor.computed(() => {
      return webUrl.value || fallbackUrl.value || schemeUrl.value || "";
    });
    common_vendor.onLoad((options) => {
      const { id, platformId: pid, merchantId: mid, type, name, url } = options;
      merchantId.value = mid;
      if (type === "coupon" && url) {
        platformName.value = decodeURIComponent(name || "领券中心");
        platformDesc.value = "正在跳转到领券页面...";
        jumpMode.value = "webview";
        webUrl.value = decodeURIComponent(url);
        fallbackUrl.value = webUrl.value;
        return;
      }
      platformId.value = id || pid;
      if (platformId.value) {
        loadPlatformInfo();
        recordClick();
      }
    });
    const loadPlatformInfo = async () => {
      try {
        const res = await api_promotion.getPromotionPlatformDetail(platformId.value);
        if (res) {
          platformName.value = res.name || "推广平台";
          platformColor.value = res.color || "#1677ff";
          platformDesc.value = res.description || "";
          jumpMode.value = res.jumpMode || "scheme";
          schemeUrl.value = res.schemeUrl || "";
          webUrl.value = res.webUrl || "";
          fallbackUrl.value = res.fallbackUrl || "";
          miniprogramAppid.value = res.miniprogramAppid || "";
          miniprogramPath.value = res.miniprogramPath || "";
        }
      } catch (e) {
        console.error("加载平台信息失败", e);
        common_vendor.index.showToast({
          title: "加载平台信息失败",
          icon: "none"
        });
      }
    };
    const recordClick = async () => {
      try {
        await api_promotion.logPromotionClick({
          platformId: platformId.value,
          merchantId: merchantId.value
        });
      } catch (e) {
        console.error("记录点击失败", e);
      }
    };
    const handleSchemeJump = () => {
      if (schemeUrl.value) {
        common_vendor.index.showModal({
          title: "提示",
          content: '小程序内无法直接唤起APP，请点击"复制链接"后在浏览器中打开',
          showCancel: true,
          confirmText: "复制链接",
          success: (res) => {
            if (res.confirm) {
              copyLink();
            }
          }
        });
      } else {
        common_vendor.index.showToast({
          title: "跳转链接未配置",
          icon: "none"
        });
      }
    };
    const openWebUrl = () => {
      const url = webUrl.value || fallbackUrl.value;
      if (!url) {
        common_vendor.index.showToast({
          title: "链接未配置",
          icon: "none"
        });
        return;
      }
      common_vendor.index.showModal({
        title: "提示",
        content: '小程序内无法直接打开外部链接，请点击"复制链接"后在浏览器中打开',
        showCancel: true,
        confirmText: "复制链接",
        success: (res) => {
          if (res.confirm) {
            copyLink();
          }
        }
      });
    };
    const navigateToMiniProgram = () => {
      if (miniprogramAppid.value) {
        common_vendor.wx$1.navigateToMiniProgram({
          appId: miniprogramAppid.value,
          path: miniprogramPath.value,
          success: () => {
            console.log("跳转小程序成功");
          },
          fail: (err) => {
            console.error("跳转小程序失败", err);
            common_vendor.index.showToast({
              title: "跳转失败，请复制链接",
              icon: "none"
            });
          }
        });
      } else {
        copyLink();
      }
    };
    const copyLink = () => {
      const link = currentLink.value;
      if (!link) {
        common_vendor.index.showToast({
          title: "暂无可复制链接",
          icon: "none"
        });
        return;
      }
      common_vendor.index.setClipboardData({
        data: link,
        success: () => {
          common_vendor.index.showToast({
            title: "链接已复制",
            icon: "success"
          });
        }
      });
    };
    const __returned__ = { platformId, merchantId, platformName, platformColor, platformDesc, jumpMode, schemeUrl, webUrl, fallbackUrl, miniprogramAppid, miniprogramPath, currentLink, loadPlatformInfo, recordClick, handleSchemeJump, openWebUrl, navigateToMiniProgram, copyLink, ref: common_vendor.ref, computed: common_vendor.computed, get onLoad() {
      return common_vendor.onLoad;
    }, get getPromotionPlatformDetail() {
      return api_promotion.getPromotionPlatformDetail;
    }, get logPromotionClick() {
      return api_promotion.logPromotionClick;
    } };
    Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
    return __returned__;
  }
};
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: common_vendor.t($setup.platformName.charAt(0)),
    b: $setup.platformColor,
    c: common_vendor.t($setup.platformName),
    d: $setup.platformDesc
  }, $setup.platformDesc ? {
    e: common_vendor.t($setup.platformDesc)
  } : {
    f: common_vendor.t($setup.platformName)
  }, {
    g: $setup.jumpMode === "scheme" && $setup.schemeUrl
  }, $setup.jumpMode === "scheme" && $setup.schemeUrl ? {
    h: common_vendor.t($setup.platformName),
    i: common_vendor.o($setup.handleSchemeJump)
  } : {}, {
    j: ($setup.jumpMode === "webview" || $setup.jumpMode === "scheme") && $setup.webUrl
  }, ($setup.jumpMode === "webview" || $setup.jumpMode === "scheme") && $setup.webUrl ? {
    k: common_vendor.o($setup.openWebUrl)
  } : {}, {
    l: $setup.jumpMode === "miniprogram" && $setup.miniprogramAppid
  }, $setup.jumpMode === "miniprogram" && $setup.miniprogramAppid ? {
    m: common_vendor.t($setup.platformName),
    n: common_vendor.o($setup.navigateToMiniProgram)
  } : {}, {
    o: $setup.jumpMode === "copy" || $setup.fallbackUrl
  }, $setup.jumpMode === "copy" || $setup.fallbackUrl ? {
    p: common_vendor.o($setup.copyLink)
  } : {}, {
    q: $setup.currentLink
  }, $setup.currentLink ? {
    r: common_vendor.t($setup.currentLink)
  } : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-2d310800"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/promotion/jump.vue"]]);
wx.createPage(MiniProgramPage);
