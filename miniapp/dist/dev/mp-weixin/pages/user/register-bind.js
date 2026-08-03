"use strict";
const common_vendor = require("../../common/vendor.js");
const api_user = require("../../api/user.js");
const store_user = require("../../store/user.js");
const _sfc_main = {
  __name: "register-bind",
  setup(__props, { expose: __expose }) {
    __expose();
    const userStore = store_user.useUserStore();
    const form = common_vendor.ref({
      phone: "",
      smsCode: ""
    });
    const deviceCode = common_vendor.ref("");
    const deviceType = common_vendor.ref("nfc");
    const loading = common_vendor.ref(false);
    const countdown = common_vendor.ref(0);
    const referrerList = common_vendor.ref([]);
    const selectedReferrer = common_vendor.ref(null);
    const showReferrerPicker = common_vendor.ref(false);
    let timer = null;
    const canSubmit = common_vendor.computed(() => {
      return form.value.phone && form.value.smsCode && form.value.phone.length === 11;
    });
    common_vendor.onLoad((options) => {
      if (options.code) {
        deviceCode.value = decodeURIComponent(options.code);
      }
      if (options.type) {
        deviceType.value = options.type;
      }
      ensureLogin();
      loadReferrerList();
    });
    const ensureLogin = async () => {
      if (userStore.isLoggedIn) return;
      try {
        const { code } = await common_vendor.index.login({ provider: "weixin" });
        if (code) {
          await userStore.loginByWechat(code);
        }
      } catch (e) {
        console.error("静默登录失败", e);
      }
    };
    const loadReferrerList = async () => {
      try {
        const res = await api_user.getReferrerList();
        referrerList.value = Array.isArray(res) ? res : (res == null ? void 0 : res.list) || [];
        if (referrerList.value.length > 0 && !selectedReferrer.value) {
          selectedReferrer.value = referrerList.value[0];
        }
      } catch (e) {
        console.error("获取推荐人列表失败", e);
        referrerList.value = [];
      }
    };
    const selectReferrer = (item) => {
      selectedReferrer.value = item;
      showReferrerPicker.value = false;
    };
    const roleText = (role) => {
      if (role === "super_admin") return "超级管理员";
      if (role === "admin") return "管理员";
      return "";
    };
    const sendSmsCode = () => {
      if (countdown.value > 0) return;
      if (!form.value.phone) {
        common_vendor.index.showToast({ title: "请输入手机号", icon: "none" });
        return;
      }
      if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
        common_vendor.index.showToast({ title: "手机号格式不正确", icon: "none" });
        return;
      }
      countdown.value = 60;
      timer = setInterval(() => {
        countdown.value--;
        if (countdown.value <= 0) {
          clearInterval(timer);
        }
      }, 1e3);
      api_user.sendSmsCode(form.value.phone).then(() => {
        common_vendor.index.showToast({ title: "验证码已发送", icon: "success" });
      }).catch(() => {
        clearInterval(timer);
        countdown.value = 0;
      });
    };
    const handleSubmit = async () => {
      var _a;
      if (!canSubmit.value || loading.value) return;
      if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
        common_vendor.index.showToast({ title: "手机号格式不正确", icon: "none" });
        return;
      }
      if (!userStore.isLoggedIn) {
        await ensureLogin();
        if (!userStore.isLoggedIn) {
          common_vendor.index.showToast({ title: "请先授权登录", icon: "none" });
          return;
        }
      }
      loading.value = true;
      try {
        const payload = {
          phone: form.value.phone,
          smsCode: form.value.smsCode,
          code: deviceCode.value,
          type: deviceType.value,
          referrerCode: ((_a = selectedReferrer.value) == null ? void 0 : _a.userCode) || ""
        };
        const res = await api_user.registerBind(payload);
        if (res) {
          if (res.userInfo) {
            userStore.setUserInfo(res.userInfo);
          }
        }
        common_vendor.index.showToast({ title: "注册绑定成功", icon: "success" });
        setTimeout(() => {
          if (deviceCode.value) {
            common_vendor.index.redirectTo({
              url: "/pages/index/index"
            });
          } else {
            common_vendor.index.navigateBack();
          }
        }, 1500);
      } catch (e) {
        console.error("注册绑定失败", e);
      } finally {
        loading.value = false;
      }
    };
    const __returned__ = { userStore, form, deviceCode, deviceType, loading, countdown, referrerList, selectedReferrer, showReferrerPicker, get timer() {
      return timer;
    }, set timer(v) {
      timer = v;
    }, canSubmit, ensureLogin, loadReferrerList, selectReferrer, roleText, sendSmsCode, handleSubmit, ref: common_vendor.ref, computed: common_vendor.computed, get onLoad() {
      return common_vendor.onLoad;
    }, get registerBind() {
      return api_user.registerBind;
    }, get sendSms() {
      return api_user.sendSmsCode;
    }, get getReferrerList() {
      return api_user.getReferrerList;
    }, get useUserStore() {
      return store_user.useUserStore;
    } };
    Object.defineProperty(__returned__, "__isScriptSetup", { enumerable: false, value: true });
    return __returned__;
  }
};
if (!Array) {
  const _easycom_u_icon2 = common_vendor.resolveComponent("u-icon");
  const _easycom_u_input2 = common_vendor.resolveComponent("u-input");
  (_easycom_u_icon2 + _easycom_u_input2)();
}
const _easycom_u_icon = () => "../../node-modules/uview-plus/components/u-icon/u-icon.js";
const _easycom_u_input = () => "../../node-modules/uview-plus/components/u-input/u-input.js";
if (!Math) {
  (_easycom_u_icon + _easycom_u_input)();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: $setup.deviceCode
  }, $setup.deviceCode ? {
    b: common_vendor.p({
      name: $setup.deviceType === "qrcode" ? "scan" : "wifi",
      size: "36",
      color: "#1677ff"
    }),
    c: common_vendor.t($setup.deviceType === "qrcode" ? "二维码" : "NFC标签"),
    d: common_vendor.t($setup.deviceCode)
  } : {}, {
    e: common_vendor.o(($event) => $setup.form.phone = $event),
    f: common_vendor.p({
      placeholder: "请输入手机号",
      type: "number",
      maxlength: "11",
      border: "none",
      modelValue: $setup.form.phone
    }),
    g: common_vendor.o(($event) => $setup.form.smsCode = $event),
    h: common_vendor.p({
      placeholder: "请输入验证码",
      type: "number",
      maxlength: "6",
      border: "none",
      modelValue: $setup.form.smsCode
    }),
    i: common_vendor.t($setup.countdown > 0 ? `${$setup.countdown}s` : "获取验证码"),
    j: $setup.countdown > 0 ? 1 : "",
    k: common_vendor.o($setup.sendSmsCode),
    l: $setup.selectedReferrer
  }, $setup.selectedReferrer ? {
    m: $setup.selectedReferrer.avatar || "/static/logo.png",
    n: common_vendor.t($setup.selectedReferrer.nickname),
    o: common_vendor.t($setup.selectedReferrer.userCode)
  } : {}, {
    p: common_vendor.p({
      name: "arrow-right",
      size: "20",
      color: "#ccc"
    }),
    q: common_vendor.o(($event) => $setup.showReferrerPicker = true),
    r: common_vendor.p({
      name: "info-circle",
      size: "28",
      color: "#1677ff"
    }),
    s: common_vendor.t($setup.loading ? "提交中..." : "立即注册绑定"),
    t: !$setup.canSubmit || $setup.loading ? 1 : "",
    v: common_vendor.o($setup.handleSubmit),
    w: $setup.loading,
    x: $setup.showReferrerPicker
  }, $setup.showReferrerPicker ? common_vendor.e({
    y: common_vendor.p({
      name: "close",
      size: "24",
      color: "#999"
    }),
    z: common_vendor.o(($event) => $setup.showReferrerPicker = false),
    A: common_vendor.f($setup.referrerList, (item, k0, i0) => {
      return common_vendor.e({
        a: item.avatar || "/static/logo.png",
        b: common_vendor.t(item.nickname),
        c: common_vendor.t($setup.roleText(item.role)),
        d: common_vendor.n(item.role),
        e: common_vendor.t(item.userCode),
        f: $setup.selectedReferrer && $setup.selectedReferrer.id === item.id
      }, $setup.selectedReferrer && $setup.selectedReferrer.id === item.id ? {
        g: "941cc867-6-" + i0,
        h: common_vendor.p({
          name: "checkmark-circle-fill",
          size: "28",
          color: "#1677ff"
        })
      } : {}, {
        i: item.id,
        j: $setup.selectedReferrer && $setup.selectedReferrer.id === item.id ? 1 : "",
        k: common_vendor.o(($event) => $setup.selectReferrer(item), item.id)
      });
    }),
    B: $setup.referrerList.length === 0
  }, $setup.referrerList.length === 0 ? {} : {}, {
    C: common_vendor.o(() => {
    }),
    D: common_vendor.o(($event) => $setup.showReferrerPicker = false)
  }) : {});
}
const MiniProgramPage = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-941cc867"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/src/pages/user/register-bind.vue"]]);
wx.createPage(MiniProgramPage);
