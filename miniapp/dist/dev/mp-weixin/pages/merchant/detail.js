"use strict";
const common_vendor = require("../../common/vendor.js");
const store_app = require("../../store/app.js");
const api_merchant = require("../../api/merchant.js");
const api_promotion = require("../../api/promotion.js");
const api_coupon = require("../../api/coupon.js");
const _sfc_main = {
  __name: "detail",
  setup(__props, { expose: __expose }) {
    __expose();
    const appStore = store_app.useAppStore();
    const merchantInfo = common_vendor.ref(null);
    const platforms = common_vendor.ref([]);
    const coupons = common_vendor.ref([]);
    const bannerImages = common_vendor.ref([]);
    const currentBanner = common_vendor.ref(0);
    const claimedCouponIds = common_vendor.ref([]);
    const shopImages = common_vendor.computed(() => {
      var _a;
      return ((_a = merchantInfo.value) == null ? void 0 : _a.shopImages) || [];
    });
    common_vendor.onLoad((options) => {
      const { merchantId, deviceId } = options;
      if (merchantId) {
        if (appStore.currentMerchant && String(appStore.currentMerchant.id) === String(merchantId)) {
          merchantInfo.value = appStore.currentMerchant;
          bannerImages.value = appStore.currentMerchant.bannerImages || [];
        }
        loadAll(merchantId);
      }
    });
    const loadAll = async (merchantId) => {
      await Promise.all([
        loadMerchantInfo(merchantId),
        loadPlatforms(merchantId),
        loadCoupons(merchantId)
      ]);
    };
    const loadMerchantInfo = async (merchantId) => {
      try {
        const res = await api_merchant.getMerchantInfo(merchantId);
        if (res) {
          merchantInfo.value = res;
          appStore.setCurrentMerchant(res);
          bannerImages.value = (res == null ? void 0 : res.bannerImages) || [];
        }
      } catch (e) {
        console.error("加载商家信息失败", e);
        if (!merchantInfo.value) {
          mockMerchantInfo(merchantId);
        }
      }
    };
    const mockMerchantInfo = (merchantId) => {
      const mock = {
        id: Number(merchantId) || 1,
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
      bannerImages.value = mock.bannerImages;
      appStore.setCurrentMerchant(mock);
    };
    const loadPlatforms = async (merchantId) => {
      try {
        const res = await api_promotion.getPromotionPlatforms(merchantId);
        if (Array.isArray(res)) {
          platforms.value = res;
        } else if (res && Array.isArray(res.platforms)) {
          platforms.value = res.platforms;
        } else {
          platforms.value = [];
        }
      } catch (e) {
        console.error("加载推广平台失败", e);
        platforms.value = [];
      }
    };
    const loadCoupons = async (merchantId) => {
      try {
        const res = await api_coupon.getCouponList(merchantId);
        coupons.value = Array.isArray(res) ? res : (res == null ? void 0 : res.list) || [];
      } catch (e) {
        console.error("加载优惠券失败", e);
        coupons.value = [];
      }
    };
    const isCouponClaimed = (coupon) => {
      return claimedCouponIds.value.includes(coupon.id);
    };
    const onBannerChange = (e) => {
      currentBanner.value = e.detail.current;
    };
    const previewBanner = (idx) => {
      if (bannerImages.value.length === 0) return;
      common_vendor.index.previewImage({
        urls: bannerImages.value,
        current: idx
      });
    };
    const previewShopImage = (idx) => {
      if (shopImages.value.length === 0) return;
      common_vendor.index.previewImage({
        urls: shopImages.value,
        current: idx
      });
    };
    const handlePromotion = (platform) => {
      var _a;
      common_vendor.index.navigateTo({
        url: `/pages/promotion/jump?id=${platform.id}&merchantId=${(_a = merchantInfo.value) == null ? void 0 : _a.id}`
      });
    };
    const handleAddWechat = () => {
      var _a;
      const wechat = (_a = merchantInfo.value) == null ? void 0 : _a.bossWechat;
      if (!wechat) {
        common_vendor.index.showToast({ title: "老板未配置微信", icon: "none" });
        return;
      }
      common_vendor.index.setClipboardData({
        data: wechat,
        success: () => {
          common_vendor.index.showModal({
            title: "加老板微信",
            content: `老板微信号已复制：${wechat}
请打开微信搜索添加`,
            confirmText: "去添加",
            showCancel: false
          });
        }
      });
    };
    const goToWifi = () => {
      var _a;
      common_vendor.index.navigateTo({
        url: `/pages/wifi/index?merchantId=${(_a = merchantInfo.value) == null ? void 0 : _a.id}`
      });
    };
    const handleClaimCoupon = (coupon) => {
      var _a;
      if (isCouponClaimed(coupon)) return;
      const url = coupon.thirdPartyUrl || coupon.linkUrl || coupon.externalUrl;
      if (!url) {
        common_vendor.index.showToast({ title: "暂无可领取链接", icon: "none" });
        return;
      }
      common_vendor.index.navigateTo({
        url: `/pages/promotion/jump?type=coupon&name=${encodeURIComponent(coupon.title || coupon.name)}&url=${encodeURIComponent(url)}&merchantId=${(_a = merchantInfo.value) == null ? void 0 : _a.id}`
      });
    };
    const callPhone = () => {
      var _a;
      if ((_a = merchantInfo.value) == null ? void 0 : _a.contactPhone) {
        common_vendor.index.makePhoneCall({
          phoneNumber: merchantInfo.value.contactPhone
        });
      }
    };
    const formatDate = (date) => {
      if (!date) return "";
      const d = new Date(date);
      if (isNaN(d.getTime())) return "";
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    };
    const __returned__ = { appStore, merchantInfo, platforms, coupons, bannerImages, currentBanner, claimedCouponIds, shopImages, loadAll, loadMerchantInfo, mockMerchantInfo, loadPlatforms, loadCoupons, isCouponClaimed, onBannerChange, previewBanner, previewShopImage, handlePromotion, handleAddWechat, goToWifi, handleClaimCoupon, callPhone, formatDate, ref: common_vendor.ref, computed: common_vendor.computed, get onLoad() {
      return common_vendor.onLoad;
    }, get useAppStore() {
      return store_app.useAppStore;
    }, get getMerchantInfo() {
      return api_merchant.getMerchantInfo;
    }, get getPromotionPlatforms() {
      return api_promotion.getPromotionPlatforms;
    }, get getCouponList() {
      return api_coupon.getCouponList;
    }, get claimCoupon() {
      return api_coupon.claimCoupon;
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
    a: $setup.bannerImages.length > 0
  }, $setup.bannerImages.length > 0 ? {
    b: common_vendor.f($setup.bannerImages, (img, idx, i0) => {
      return {
        a: img,
        b: common_vendor.o(($event) => $setup.previewBanner(idx), idx),
        c: idx
      };
    }),
    c: $setup.bannerImages.length > 1,
    d: common_vendor.o($setup.onBannerChange)
  } : {
    e: ((_a = $setup.merchantInfo) == null ? void 0 : _a.logo) || "/static/logo.png"
  }, {
    f: $setup.merchantInfo
  }, $setup.merchantInfo ? {} : {}, {
    g: $setup.merchantInfo
  }, $setup.merchantInfo ? common_vendor.e({
    h: $setup.merchantInfo.logo || "/static/logo.png",
    i: common_vendor.t($setup.merchantInfo.name),
    j: common_vendor.p({
      name: "map-marker",
      size: "20",
      color: "rgba(255,255,255,0.85)"
    }),
    k: common_vendor.t($setup.merchantInfo.address || "暂无地址"),
    l: $setup.merchantInfo.businessHours
  }, $setup.merchantInfo.businessHours ? {
    m: common_vendor.p({
      name: "clock",
      size: "20",
      color: "rgba(255,255,255,0.85)"
    }),
    n: common_vendor.t($setup.merchantInfo.businessHours)
  } : {}) : {}, {
    o: $setup.coupons.length > 0
  }, $setup.coupons.length > 0 ? {
    p: common_vendor.t($setup.coupons.length)
  } : {}, {
    q: $setup.coupons.length > 0
  }, $setup.coupons.length > 0 ? {
    r: common_vendor.f($setup.coupons, (coupon, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(coupon.amount || coupon.value || 0),
        b: coupon.minAmount > 0
      }, coupon.minAmount > 0 ? {
        c: common_vendor.t(coupon.minAmount)
      } : {}, {
        d: common_vendor.t(coupon.title || coupon.name),
        e: coupon.startTime
      }, coupon.startTime ? {
        f: common_vendor.t($setup.formatDate(coupon.startTime)),
        g: common_vendor.t($setup.formatDate(coupon.endTime))
      } : {}, {
        h: common_vendor.t($setup.isCouponClaimed(coupon) ? "去使用" : "去领取"),
        i: $setup.isCouponClaimed(coupon) ? 1 : "",
        j: common_vendor.o(($event) => $setup.handleClaimCoupon(coupon), coupon.id),
        k: coupon.id
      });
    })
  } : {
    s: common_vendor.p({
      name: "coupon",
      size: "56",
      color: "#d9d9d9"
    })
  }, {
    t: common_vendor.f($setup.platforms, (platform, k0, i0) => {
      return {
        a: common_vendor.t((platform.name || "?").charAt(0)),
        b: platform.color || "#1677ff",
        c: common_vendor.t(platform.name),
        d: "p-" + platform.id,
        e: common_vendor.o(($event) => $setup.handlePromotion(platform), "p-" + platform.id)
      };
    }),
    v: $setup.merchantInfo && $setup.merchantInfo.bossWechat
  }, $setup.merchantInfo && $setup.merchantInfo.bossWechat ? {
    w: common_vendor.o($setup.handleAddWechat)
  } : {}, {
    x: $setup.merchantInfo && $setup.merchantInfo.wifiName
  }, $setup.merchantInfo && $setup.merchantInfo.wifiName ? {
    y: common_vendor.p({
      name: "wifi",
      color: "#fff",
      size: "32"
    }),
    z: common_vendor.o($setup.goToWifi)
  } : {}, {
    A: $setup.merchantInfo && ($setup.merchantInfo.description || $setup.shopImages.length > 0 || $setup.merchantInfo.contactPhone)
  }, $setup.merchantInfo && ($setup.merchantInfo.description || $setup.shopImages.length > 0 || $setup.merchantInfo.contactPhone) ? common_vendor.e({
    B: $setup.merchantInfo.description
  }, $setup.merchantInfo.description ? {
    C: common_vendor.t($setup.merchantInfo.description)
  } : {}, {
    D: $setup.shopImages.length > 0
  }, $setup.shopImages.length > 0 ? {
    E: common_vendor.f($setup.shopImages, (img, idx, i0) => {
      return {
        a: img,
        b: idx,
        c: common_vendor.o(($event) => $setup.previewShopImage(idx), idx)
      };
    })
  } : {}, {
    F: $setup.merchantInfo.contactPhone
  }, $setup.merchantInfo.contactPhone ? {
    G: common_vendor.p({
      name: "phone",
      size: "24",
      color: "#1677ff"
    }),
    H: common_vendor.t($setup.merchantInfo.contactPhone),
    I: common_vendor.o($setup.callPhone),
    J: common_vendor.p({
      name: "phone-fill",
      size: "24",
      color: "#52c41a"
    }),
    K: common_vendor.o($setup.callPhone)
  } : {}) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-e1764325"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/merchant/detail.vue"]]);
wx.createPage(MiniProgramPage);
