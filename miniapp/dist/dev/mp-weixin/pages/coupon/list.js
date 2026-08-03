"use strict";
const common_vendor = require("../../common/vendor.js");
const api_coupon = require("../../api/coupon.js");
const _sfc_main = {
  __name: "list",
  setup(__props, { expose: __expose }) {
    __expose();
    const activeTab = common_vendor.ref("available");
    const availableCoupons = common_vendor.ref([]);
    const myCoupons = common_vendor.ref([]);
    const merchantId = common_vendor.ref(null);
    common_vendor.onLoad((options) => {
      if (options.merchantId) {
        merchantId.value = options.merchantId;
        loadAvailableCoupons();
      }
      loadMyCoupons();
    });
    const loadAvailableCoupons = async () => {
      try {
        const res = await api_coupon.getCouponList(merchantId.value);
        availableCoupons.value = res || [];
      } catch (e) {
        console.error("加载优惠券失败", e);
      }
    };
    const loadMyCoupons = async () => {
      try {
        const res = await api_coupon.getMyCoupons();
        myCoupons.value = res || [];
      } catch (e) {
        console.error("加载我的优惠券失败", e);
      }
    };
    const handleClaim = (coupon) => {
      if (coupon.issuedCount >= coupon.totalCount) return;
      const url = coupon.thirdPartyUrl || coupon.linkUrl || coupon.externalUrl;
      if (!url) {
        common_vendor.index.showToast({ title: "暂无可领取链接", icon: "none" });
        return;
      }
      common_vendor.index.navigateTo({
        url: `/pages/promotion/jump?type=coupon&name=${encodeURIComponent(coupon.couponName || coupon.name)}&url=${encodeURIComponent(url)}&merchantId=${merchantId.value}`
      });
    };
    const getCouponValue = (couponId) => {
      const coupon = availableCoupons.value.find((c) => c.id === couponId);
      return (coupon == null ? void 0 : coupon.discountValue) || 0;
    };
    const getCouponName = (couponId) => {
      const coupon = availableCoupons.value.find((c) => c.id === couponId);
      return (coupon == null ? void 0 : coupon.couponName) || "优惠券";
    };
    const formatDate = (date) => {
      if (!date) return "";
      const d = new Date(date);
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, "0")}-${String(d.getDate()).padStart(2, "0")}`;
    };
    const __returned__ = { activeTab, availableCoupons, myCoupons, merchantId, loadAvailableCoupons, loadMyCoupons, handleClaim, getCouponValue, getCouponName, formatDate, ref: common_vendor.ref, computed: common_vendor.computed, onMounted: common_vendor.onMounted, get onLoad() {
      return common_vendor.onLoad;
    }, get getCouponList() {
      return api_coupon.getCouponList;
    }, get claimCoupon() {
      return api_coupon.claimCoupon;
    }, get getMyCoupons() {
      return api_coupon.getMyCoupons;
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
    a: $setup.activeTab === "available" ? 1 : "",
    b: common_vendor.o(($event) => $setup.activeTab = "available"),
    c: $setup.activeTab === "mine" ? 1 : "",
    d: common_vendor.o(($event) => $setup.activeTab = "mine"),
    e: common_vendor.f($setup.availableCoupons, (coupon, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t(coupon.discountValue),
        b: coupon.minAmount > 0
      }, coupon.minAmount > 0 ? {
        c: common_vendor.t(coupon.minAmount)
      } : {}, {
        d: common_vendor.t(coupon.couponName),
        e: common_vendor.t(coupon.description || "欢迎使用"),
        f: common_vendor.t($setup.formatDate(coupon.validEndTime)),
        g: common_vendor.t(coupon.totalCount - coupon.issuedCount),
        h: common_vendor.t(coupon.issuedCount >= coupon.totalCount ? "已领完" : "去领取"),
        i: coupon.issuedCount >= coupon.totalCount ? 1 : "",
        j: common_vendor.o(($event) => $setup.handleClaim(coupon), coupon.id),
        k: coupon.id
      });
    }),
    f: $setup.availableCoupons.length === 0
  }, $setup.availableCoupons.length === 0 ? {
    g: common_vendor.p({
      name: "coupon",
      size: "64",
      color: "#d9d9d9"
    })
  } : {}, {
    h: $setup.activeTab === "available",
    i: common_vendor.f($setup.myCoupons, (record, k0, i0) => {
      return common_vendor.e({
        a: common_vendor.t($setup.getCouponValue(record.couponId)),
        b: common_vendor.t($setup.getCouponName(record.couponId)),
        c: common_vendor.t($setup.getCouponName(record.couponId)),
        d: common_vendor.t(record.couponCode),
        e: common_vendor.t(record.status === 1 ? "已使用" : record.status === 2 ? "已过期" : "未使用"),
        f: common_vendor.t($setup.formatDate(record.createTime)),
        g: record.status !== 0
      }, record.status !== 0 ? {
        h: common_vendor.t(record.status === 1 ? "已使用" : "已过期")
      } : {}, {
        i: record.id,
        j: record.status === 1 ? 1 : "",
        k: record.status === 2 ? 1 : ""
      });
    }),
    j: $setup.myCoupons.length === 0
  }, $setup.myCoupons.length === 0 ? {
    k: common_vendor.p({
      name: "coupon",
      size: "64",
      color: "#d9d9d9"
    })
  } : {}, {
    l: $setup.activeTab === "mine"
  });
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-a79d4467"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/coupon/list.vue"]]);
wx.createPage(MiniProgramPage);
