"use strict";
const utils_request = require("../utils/request.js");
const wechatLogin = (code) => {
  return utils_request.request.post("/user/auth/wechat-mini", { code });
};
const getReferrerList = () => {
  return utils_request.request.get("/user/referrer/list");
};
const registerBind = (data) => {
  return utils_request.request.post("/user/register-bind", data);
};
const getUserInfo = () => {
  return utils_request.request.get("/user/info");
};
const sendSmsCode = (phone) => {
  return utils_request.request.post("/user/send-sms", { phone });
};
exports.getReferrerList = getReferrerList;
exports.getUserInfo = getUserInfo;
exports.registerBind = registerBind;
exports.sendSmsCode = sendSmsCode;
exports.wechatLogin = wechatLogin;
