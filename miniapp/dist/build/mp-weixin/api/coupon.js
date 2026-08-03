"use strict";const e=require("../utils/request.js");exports.getCouponList=t=>e.request.get("/coupon/list",{merchantId:t}),exports.getMyCoupons=()=>e.request.get("/coupon/my");
