"use strict";
const utils_request = require("../utils/request.js");
const getPromotionPlatforms = (merchantId) => {
  return utils_request.request.get("/promotion/platforms", { merchantId });
};
const getPromotionPlatformDetail = (id) => {
  return utils_request.request.get(`/promotion/platform/${id}`);
};
const logPromotionClick = (data) => {
  return utils_request.request.post("/promotion/log", data);
};
exports.getPromotionPlatformDetail = getPromotionPlatformDetail;
exports.getPromotionPlatforms = getPromotionPlatforms;
exports.logPromotionClick = logPromotionClick;
