"use strict";
const common_vendor = require("../common/vendor.js");
const utils_mock = require("./mock.js");
const BASE_URL = "http://154.8.138.48:3000/api";
typeof common_vendor.wx$1 !== "undefined" && common_vendor.wx$1.getSystemInfoSync;
const request$1 = (options) => {
  return new Promise((resolve, reject) => {
    const token = common_vendor.index.getStorageSync("token") || "";
    {
      utils_mock.tryMockResponse(options.url, options.method || "GET", options.data).then((mockRes) => {
        if (mockRes !== null && mockRes !== void 0) {
          setTimeout(() => resolve(mockRes), 80);
          return;
        }
        doRealRequest(options, token, resolve, reject);
      }).catch(() => {
        doRealRequest(options, token, resolve, reject);
      });
    }
  });
};
function doRealRequest(options, token, resolve, reject) {
  common_vendor.index.request({
    url: BASE_URL + options.url,
    method: options.method || "GET",
    data: options.data || {},
    header: {
      "Content-Type": "application/json",
      "Authorization": token ? `Bearer ${token}` : "",
      ...options.header
    },
    success: (res) => {
      if (res.statusCode === 200) {
        const data = res.data;
        if (data.code === 0) {
          resolve(data.data);
        } else if (data.code === 401) {
          common_vendor.index.removeStorageSync("token");
          common_vendor.index.removeStorageSync("userInfo");
          common_vendor.index.showToast({ title: "请先登录", icon: "none" });
          reject(data);
        } else {
          common_vendor.index.showToast({ title: data.message || "请求失败", icon: "none" });
          reject(data);
        }
      } else {
        utils_mock.tryMockResponse(options.url, options.method || "GET", options.data).then((mockRes) => {
          if (mockRes !== null && mockRes !== void 0) {
            resolve(mockRes);
          } else {
            reject(res);
          }
        });
      }
    },
    fail: (err) => {
      utils_mock.tryMockResponse(options.url, options.method || "GET", options.data).then((mockRes) => {
        if (mockRes !== null && mockRes !== void 0) {
          resolve(mockRes);
        } else {
          reject(err);
        }
      });
    }
  });
}
const request = {
  get: (url, data) => request$1({ url, method: "GET", data }),
  post: (url, data) => request$1({ url, method: "POST", data }),
  put: (url, data) => request$1({ url, method: "PUT", data }),
  delete: (url, data) => request$1({ url, method: "DELETE", data })
};
exports.request = request;
