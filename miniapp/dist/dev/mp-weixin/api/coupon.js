"use strict";
const utils_request = require("../utils/request.js");
const getCouponList = (merchantId) => {
  return utils_request.request.get(`/coupon/list`, { merchantId });
};
const claimCoupon = (couponId) => {
  return utils_request.request.post("/coupon/claim", { couponId });
};
const getMyCoupons = () => {
  return utils_request.request.get("/coupon/my");
};
exports.claimCoupon = claimCoupon;
exports.getCouponList = getCouponList;
exports.getMyCoupons = getMyCoupons;
