"use strict";
const common_vendor = require("../../../../common/vendor.js");
const _sfc_main = {
  name: "u-input",
  mixins: [common_vendor.mpMixin, common_vendor.mixin, common_vendor.props$1],
  data() {
    return {
      // 清除操作
      clearInput: false,
      // 输入框的值
      innerValue: "",
      // 是否处于获得焦点状态
      focused: false,
      // value是否第一次变化，在watch中，由于加入immediate属性，会在第一次触发，此时不应该认为value发生了变化
      firstChange: true,
      // value绑定值的变化是由内部还是外部引起的
      changeFromInner: false,
      // 记录blur时原生输入框的值，用于识别blur阶段的外部格式化回流
      blurValue: null,
      // 过滤处理方法
      innerFormatter: (value) => value,
      showPassword: false
    };
  },
  created() {
    if (this.formatter) {
      this.innerFormatter = this.formatter;
    }
  },
  watch: {
    modelValue: {
      immediate: true,
      handler(newVal, oldVal) {
        const isBlurFormattedValue = this.blurValue !== null && newVal !== this.blurValue && newVal !== this.innerValue;
        if (this.changeFromInner && !isBlurFormattedValue || this.innerValue === newVal) {
          this.changeFromInner = false;
          this.blurValue = null;
          return;
        }
        this.innerValue = newVal;
        this.blurValue = null;
        if (this.firstChange === false && this.changeFromInner === false) {
          this.valueChange(this.innerValue, true);
        } else {
          if (!this.firstChange) common_vendor.formValidate(this, "change");
        }
        this.firstChange = false;
        this.changeFromInner = false;
      }
    }
  },
  computed: {
    placeholderStyleInner() {
      var _a, _b;
      if (this.placeholderStyle) {
        return this.placeholderStyle;
      }
      return `color: ${this.upThemeVar("--up-tips-color", ((_b = (_a = this.$u) == null ? void 0 : _a.color) == null ? void 0 : _b.tipsColor) || "#909399")}`;
    },
    // 是否密码
    isPassword() {
      let ret = false;
      if (this.password) {
        ret = true;
      } else if (this.type == "password") {
        ret = true;
      } else {
        ret = false;
      }
      if (this.showPassword) {
        ret = false;
      }
      return ret;
    },
    // 是否显示清除控件
    isShowClear() {
      const { clearable, readonly, focused, innerValue, onlyClearableOnFocused } = this;
      if (!clearable || readonly) {
        return false;
      }
      if (onlyClearableOnFocused) {
        return !!focused && innerValue !== "";
      } else {
        return innerValue !== "";
      }
    },
    // 组件的类名
    inputClass() {
      let classes = [], { border, disabled, shape } = this;
      border === "surround" && (classes = classes.concat(["u-input--radius"]));
      classes.push(`u-input--${shape}`);
      border === "bottom" && (classes = classes.concat([
        "u-input--no-radius"
      ]));
      return classes.join(" ");
    },
    inputBorderColor() {
      const lightBorder = this.upThemeVar("--up-border-color", "#dadbde");
      return this.upThemeVar(
        "--up-input-border-color",
        this.upThemeIsDark ? "rgba(255, 255, 255, 0.08)" : lightBorder
      );
    },
    // 组件的样式
    wrapperStyle() {
      var _a, _b;
      const style = {};
      if (this.disabled) {
        style.backgroundColor = this.disabledColor || this.upThemeVar("--up-bg-color", ((_b = (_a = this.$u) == null ? void 0 : _a.color) == null ? void 0 : _b.bgColor) || "#f3f4f6");
      }
      if (this.border === "surround") {
        style.borderWidth = "0.5px";
        style.borderStyle = "solid";
        style.borderColor = this.inputBorderColor;
      }
      if (this.border === "bottom") {
        style.borderBottomWidth = "0.5px";
        style.borderBottomStyle = "solid";
        style.borderBottomColor = this.inputBorderColor;
      }
      if (this.border === "none") {
        style.padding = "0";
      } else {
        style.paddingTop = "6px";
        style.paddingBottom = "6px";
        style.paddingLeft = "9px";
        style.paddingRight = "9px";
      }
      return common_vendor.deepMerge(style, common_vendor.addStyle(this.customStyle));
    },
    // 输入框的样式
    inputStyle() {
      var _a, _b;
      const style = {
        color: this.color || this.upThemeVar("--up-main-color", ((_b = (_a = this.$u) == null ? void 0 : _a.color) == null ? void 0 : _b.mainColor) || "#303133"),
        fontSize: common_vendor.addUnit(this.fontSize),
        textAlign: this.inputAlign
      };
      return style;
    }
  },
  emits: ["update:modelValue", "focus", "blur", "change", "confirm", "clear", "keyboardheightchange", "nicknamereview"],
  methods: {
    // 在微信小程序中，不支持将函数当做props参数，故只能通过ref形式调用
    setFormatter(e) {
      this.innerFormatter = e;
    },
    // 当键盘输入时，触发input事件
    onInput(e) {
      let { value = "" } = e.detail || {};
      this.innerValue = value;
      this.$nextTick(() => {
        let formatValue = this.innerFormatter(value);
        this.innerValue = formatValue;
        this.valueChange(formatValue);
      });
    },
    // 输入框失去焦点时触发
    onBlur(event) {
      var _a;
      this.blurValue = (_a = event == null ? void 0 : event.detail) == null ? void 0 : _a.value;
      this.$emit("blur", event.detail.value);
      common_vendor.sleep(150).then(() => {
        this.focused = false;
      });
      common_vendor.formValidate(this, "blur");
    },
    // 输入框聚焦时触发
    onFocus(event) {
      this.focused = true;
      this.$emit("focus");
    },
    doFocus() {
      this.$refs["input-native"].focus();
    },
    doBlur() {
      this.$refs["input-native"].blur();
    },
    // 点击完成按钮时触发
    onConfirm(event) {
      const detail = event && event.detail ? event.detail : {};
      const confirmValue = typeof detail.value !== "undefined" ? detail.value : this.innerValue;
      this.$emit("confirm", confirmValue);
    },
    // 键盘高度发生变化的时候触发此事件
    // 兼容性：微信小程序2.7.0+、App 3.1.0+
    onkeyboardheightchange(event) {
      this.$emit("keyboardheightchange", event);
    },
    onnicknamereview(event) {
      this.$emit("nicknamereview", event);
    },
    // 内容发生变化，进行处理
    valueChange(value, isOut = false) {
      if (this.clearInput) {
        this.innerValue = "";
        this.clearInput = false;
      }
      this.$nextTick(() => {
        if (!isOut || this.clearInput) {
          this.changeFromInner = true;
          this.$emit("update:modelValue", value);
          this.$emit("change", value);
        }
        common_vendor.formValidate(this, "change");
      });
    },
    // 点击清除控件
    onClear() {
      this.clearInput = true;
      this.innerValue = "";
      this.$nextTick(() => {
        this.valueChange("");
        this.$emit("clear");
      });
    },
    /**
     * 在安卓nvue上，事件无法冒泡
     * 在某些时间，我们希望监听u-from-item的点击事件，此时会导致点击u-form-item内的u-input后
     * 无法触发u-form-item的点击事件，这里通过手动调用u-form-item的方法进行触发
     */
    clickHandler() {
      if (this.disabled || this.readonly) {
        common_vendor.index.hideKeyboard();
      }
    }
  }
};
if (!Array) {
  const _component_up_icon = common_vendor.resolveComponent("up-icon");
  _component_up_icon();
}
function _sfc_render(_ctx, _cache, $props, $setup, $data, $options) {
  return common_vendor.e({
    a: _ctx.prefixIcon || _ctx.$slots.prefix
  }, _ctx.prefixIcon || _ctx.$slots.prefix ? {
    b: common_vendor.p({
      name: _ctx.prefixIcon,
      size: "18",
      customStyle: _ctx.prefixIconStyle
    })
  } : {}, {
    c: common_vendor.s($options.inputStyle),
    d: $data.showPassword && "password" == _ctx.type ? "text" : _ctx.type,
    e: _ctx.focus,
    f: _ctx.cursor,
    g: $data.innerValue,
    h: _ctx.autoBlur,
    i: _ctx.disabled || _ctx.readonly,
    j: _ctx.maxlength,
    k: _ctx.placeholder,
    l: $options.placeholderStyleInner,
    m: _ctx.placeholderClass,
    n: _ctx.confirmType,
    o: _ctx.confirmHold,
    p: _ctx.holdKeyboard,
    q: _ctx.cursorColor,
    r: _ctx.cursorSpacing,
    s: _ctx.adjustPosition,
    t: _ctx.selectionEnd,
    v: _ctx.selectionStart,
    w: $options.isPassword,
    x: _ctx.ignoreCompositionEvent,
    y: common_vendor.o((...args) => $options.onInput && $options.onInput(...args)),
    z: common_vendor.o((...args) => $options.onBlur && $options.onBlur(...args)),
    A: common_vendor.o((...args) => $options.onFocus && $options.onFocus(...args)),
    B: common_vendor.o((...args) => $options.onConfirm && $options.onConfirm(...args)),
    C: common_vendor.o((...args) => $options.onkeyboardheightchange && $options.onkeyboardheightchange(...args)),
    D: common_vendor.o((...args) => $options.onnicknamereview && $options.onnicknamereview(...args)),
    E: common_vendor.o((...args) => $options.clickHandler && $options.clickHandler(...args)),
    F: $options.isShowClear
  }, $options.isShowClear ? {
    G: common_vendor.p({
      name: "close",
      size: "11",
      color: "#ffffff",
      customStyle: "line-height: 12px"
    }),
    H: common_vendor.o((...args) => $options.onClear && $options.onClear(...args))
  } : {}, {
    I: (_ctx.type == "password" || _ctx.password) && _ctx.passwordVisibilityToggle
  }, (_ctx.type == "password" || _ctx.password) && _ctx.passwordVisibilityToggle ? {
    J: common_vendor.o(($event) => $data.showPassword = !$data.showPassword),
    K: common_vendor.p({
      name: $data.showPassword ? "eye-off" : "eye-fill",
      size: "18"
    })
  } : {}, {
    L: _ctx.suffixIcon || _ctx.$slots.suffix
  }, _ctx.suffixIcon || _ctx.$slots.suffix ? {
    M: common_vendor.p({
      name: _ctx.suffixIcon,
      size: "18",
      customStyle: _ctx.suffixIconStyle
    })
  } : {}, {
    N: common_vendor.n($options.inputClass),
    O: common_vendor.s($options.wrapperStyle)
  });
}
const Component = /* @__PURE__ */ common_vendor._export_sfc(_sfc_main, [["render", _sfc_render], ["__scopeId", "data-v-5904192e"], ["__file", "/Users/liliusheng/Desktop/andre/quick-tap/miniapp/node_modules/uview-plus/components/u-input/u-input.vue"]]);
wx.createComponent(Component);
