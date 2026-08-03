"use strict";
const utils_request = require("../utils/request.js");
const checkMerchantBind = (params) => {
  return utils_request.request.get("/merchant/check-bind", params);
};
const getMerchantInfo = (merchantId) => {
  return utils_request.request.get(`/merchant/info/${merchantId}`);
};
const getMerchantWifi = (merchantId) => {
  return utils_request.request.get("/merchant/wifi", { merchantId });
};
exports.checkMerchantBind = checkMerchantBind;
exports.getMerchantInfo = getMerchantInfo;
exports.getMerchantWifi = getMerchantWifi;
