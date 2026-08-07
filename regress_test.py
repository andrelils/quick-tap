#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""QuickTap 后端全量 API 回归测试 (localhost:8222, /api)"""
import base64
import json
import os
import random
import sys
import time
import traceback

import requests
import pymysql

BASE = "http://localhost:8222"
P = "regress_test_"
ADMIN_USER = "Dmy510"
ADMIN_PASS = "Dmy510510510"

DB_CFG = dict(host="154.8.138.48", user="andre_sql", password="lls_970401", database="andre_sql",
              charset="utf8mb4", cursorclass=pymysql.cursors.DictCursor)

results = []          # (module, name, ok, detail)
failures = []         # (module, method, path, body_summary, resp_message)

TIMEOUT = 40

def now_ts():
    return time.strftime("%H:%M:%S")

def log(msg):
    print("[%s] %s" % (now_ts(), msg), flush=True)

def db_exec(sql, args=None):
    conn = pymysql.connect(**DB_CFG)
    try:
        with conn.cursor() as cur:
            cur.execute(sql, args)
        conn.commit()
    finally:
        conn.close()

def db_query(sql, args=None):
    conn = pymysql.connect(**DB_CFG)
    try:
        with conn.cursor() as cur:
            cur.execute(sql, args)
            return cur.fetchall()
    finally:
        conn.close()

class TestCtx:
    def __init__(self):
        self.admin_token = None
        self.user_token = None
        self.user_id = None
        self.merchant_id = None
        self.device_ids = []
        self.created_admin_ids = []
        self.created_user_ids = []
        self.created_plan_ids = []
        self.created_coupon_ids = []
        self.created_platform_ids = []
        self.created_config_ids = []
        self.created_perm_ids = []
        self.created_corpus_ids = []
        self.created_cat_ids = []
        self.created_qrcode_ids = []
        self.created_order_ids = []
        self.uploaded_urls = []
        self.test_admin_tokens = []

ctx = TestCtx()

def call(method, path, token=None, json_body=None, params=None, files=None, timeout=TIMEOUT,
         skip_json=False):
    headers = {}
    if token:
        headers["Authorization"] = "Bearer " + token
    url = BASE + path
    try:
        r = requests.request(method, url, headers=headers, json=json_body, params=params,
                             files=files, timeout=timeout)
    except Exception as e:
        return False, {"__err__": str(e)}, 0
    status = r.status_code
    try:
        body = r.json() if not skip_json else {"__raw__": r.text[:500]}
    except Exception:
        body = {"__raw__": r.text[:500]}
    return True, body, status

def is_ok(body):
    if "__err__" in body:
        return False
    if "__raw__" in body:
        return False
    return str(body.get("code")) == "1000"

def rec(module, name, ok, resp, extra=""):
    detail = ""
    if ok:
        detail = "OK"
    else:
        msg = ""
        if isinstance(resp, dict):
            msg = resp.get("message") or resp.get("__raw__") or resp.get("__err__") or json.dumps(resp, ensure_ascii=False)[:300]
        detail = msg
        failures.append((module, name, extra, msg))
    results.append((module, name, ok, detail))
    flag = "PASS" if ok else "FAIL"
    log("  [%s] %-70s %s" % (flag, name, detail if not ok else ""))

def ok_or(module, name, resp, extra=""):
    rec(module, name, is_ok(resp), resp, extra)
    return resp

def data_of(resp):
    return resp.get("data") if isinstance(resp, dict) else None

def rand_suffix(n=6):
    return "%d%06d" % (int(time.time()) % 100000, random.randint(0, 999999))

def admin_login():
    _, body, st = call("POST", "/api/admin/auth/login",
                       json_body={"username": ADMIN_USER, "password": ADMIN_PASS})
    if not is_ok(body):
        log("FATAL admin login failed: %s" % json.dumps(body, ensure_ascii=False))
        sys.exit(1)
    ctx.admin_token = data_of(body)["token"]
    log("admin login OK, role=%s" % data_of(body).get("role"))

def register_user():
    suffix = rand_suffix()
    uname = ("regress_u" + suffix)[:20]   # @Size(max=20)
    phone = "13" + str(random.randint(100000000, 999999999))[0:9]
    _, body, st = call("POST", "/api/user/register",
                       json_body={"username": uname, "password": "regress123",
                                  "nickname": "regress_nick", "phone": phone})
    if is_ok(body):
        d = data_of(body)
        ctx.user_token = d.get("token")
        ctx.user_id = d.get("userId")
        ctx.created_user_ids.append(ctx.user_id)
    return body

def db_register_user_fallback():
    """C端注册接口不可用时，直接插一条 user 记录供后续模块使用（openid 登录方案）"""
    if ctx.user_id:
        return ctx.user_id
    suffix = rand_suffix()
    phone = "13" + str(random.randint(100000000, 999999999))[0:9]
    openid = P + "openid_" + suffix
    nickname = P + "nick_" + suffix
    db_exec("INSERT INTO user (openid, nickname, phone, status, created_at, updated_at) "
            "VALUES (%s, %s, %s, 1, NOW(), NOW())", (openid, nickname, phone))
    rows = db_query("SELECT id FROM user WHERE openid = %s ORDER BY id DESC LIMIT 1", (openid,))
    if rows:
        uid = rows[0]["id"]
        ctx.user_id = uid
        ctx.created_user_ids.append(uid)
        log("  DB fallback user id=%s (注册接口不可用)" % uid)
    return ctx.user_id

# =====================================================================
# Module 1: 商家 MerchantController
# =====================================================================
def test_merchant():
    M = "1.商家"
    log("== %s ==" % M)
    suffix = rand_suffix()
    body = {"name": P + "merchant_" + suffix, "contactName": "regress_tester",
            "contactPhone": "13800000001", "contactEmail": "regress@test.com",
            "wifiName": "regress_wifi", "wifiPassword": "12345678", "address": "regress_addr"}
    _, resp, _ = call("POST", "/api/merchant", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/merchant 创建", resp, "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        # 兼容 create DTO 无 address 字段的情况
        body.pop("address", None)
        _, resp2, _ = call("POST", "/api/merchant", token=ctx.admin_token, json_body=body)
        r = ok_or(M, "POST /api/merchant 创建(去address重试)", resp2,
                  "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        return None
    mid = data_of(r).get("id")
    ctx.merchant_id = mid
    log("  merchant id=%s" % mid)

    _, resp, _ = call("GET", "/api/merchant/list", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/merchant/list", resp)

    _, resp, _ = call("GET", "/api/merchant/%s" % mid, token=ctx.admin_token)
    ok_or(M, "GET /api/merchant/{id}", resp)

    upd = {"name": P + "merchant_upd_" + suffix, "contactName": "regress_tester2",
           "contactPhone": "13800000002", "address": "regress_addr2",
           "bannerImages": "[]", "shopImages": "[]", "wifiName": "upd_wifi",
           "wifiPassword": "87654321", "businessHours": "09:00-22:00"}
    _, resp, _ = call("PUT", "/api/merchant/%s" % mid, token=ctx.admin_token, json_body=upd)
    ok_or(M, "PUT /api/merchant/{id} 更新", resp, "body=" + json.dumps(upd, ensure_ascii=False))

    _, resp, _ = call("PUT", "/api/merchant/%s/approve" % mid, token=ctx.admin_token)
    ok_or(M, "PUT /api/merchant/{id}/approve", resp)

    _, resp, _ = call("PUT", "/api/merchant/%s/disable" % mid, token=ctx.admin_token)
    ok_or(M, "PUT /api/merchant/{id}/disable", resp)

    _, resp, _ = call("PUT", "/api/merchant/%s/enable" % mid, token=ctx.admin_token)
    ok_or(M, "PUT /api/merchant/{id}/enable", resp)

    _, resp, _ = call("PUT", "/api/merchant/%s/reject" % mid, token=ctx.admin_token)
    ok_or(M, "PUT /api/merchant/{id}/reject", resp)

    _, resp, _ = call("PUT", "/api/merchant/%s/approve" % mid, token=ctx.admin_token)
    ok_or(M, "PUT /api/merchant/{id}/approve(重新审核通过)", resp)

    # 按审核状态列表
    _, resp, _ = call("GET", "/api/merchant/audit-status/1", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/merchant/audit-status/1", resp)
    return mid

def test_merchant_delete(mid):
    M = "1.商家"
    if not mid:
        return
    _, resp, _ = call("DELETE", "/api/merchant/%s" % mid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/merchant/{id}", resp)

# =====================================================================
# Module 2: 额度 MerchantQuotaController
# =====================================================================
def test_quota(mid):
    M = "2.额度"
    log("== %s ==" % M)
    _, resp, _ = call("GET", "/api/admin/merchant-quota/list", token=ctx.admin_token,
                      params={"page": 1, "pageSize": 10})
    ok_or(M, "GET /api/admin/merchant-quota/list", resp)

    _, resp, _ = call("GET", "/api/admin/merchant-quota/all", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/merchant-quota/all", resp)

    _, resp, _ = call("GET", "/api/merchant/merchant-quota/usage", token=ctx.admin_token,
                      params={"merchantId": mid})
    ok_or(M, "GET /api/merchant/merchant-quota/usage", resp)

    _, resp, _ = call("GET", "/api/merchant/merchant-quota/details", token=ctx.admin_token,
                      params={"merchantId": mid})
    ok_or(M, "GET /api/merchant/merchant-quota/details", resp)

    _, resp, _ = call("GET", "/api/merchant/merchant-quota/check/text", token=ctx.admin_token,
                      params={"merchantId": mid})
    ok_or(M, "GET /api/merchant/merchant-quota/check/text", resp)

    adj = {"storageLimit": 2048, "textQuota": 1000, "imageQuota": 500, "videoQuota": 100}
    _, resp, _ = call("POST", "/api/admin/merchant-quota/%s/adjust" % mid,
                      token=ctx.admin_token, json_body=adj)
    ok_or(M, "POST /api/admin/merchant-quota/{id}/adjust", resp, "body=" + json.dumps(adj, ensure_ascii=False))

    _, resp, _ = call("POST", "/api/admin/merchant-quota/%s/reset" % mid, token=ctx.admin_token)
    ok_or(M, "POST /api/admin/merchant-quota/{id}/reset", resp)

# =====================================================================
# Module 3: 设备 DeviceController
# =====================================================================
def test_device(mid):
    M = "3.设备"
    log("== %s ==" % M)
    suffix = rand_suffix()
    body = {"deviceNo": P + "dev_" + suffix, "name": P + "device_" + suffix,
            "merchantId": mid, "type": "qrcode", "systemCode": P + "sys_" + suffix,
            "url": "http://example.com/regress", "status": 1}
    _, resp, _ = call("POST", "/api/device", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/device 创建", resp, "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        return None
    did = data_of(r).get("id")
    ctx.device_ids.append(did)
    log("  device id=%s" % did)

    _, resp, _ = call("GET", "/api/device/list", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/device/list", resp)

    _, resp, _ = call("GET", "/api/device/%s" % did, token=ctx.admin_token)
    ok_or(M, "GET /api/device/{id}", resp)

    _, resp, _ = call("GET", "/api/device/merchant/%s" % mid, token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/device/merchant/{merchantId}", resp)

    upd = {"deviceNo": P + "dev_upd_" + suffix, "name": P + "device_upd_" + suffix,
           "type": "qrcode", "status": 1}
    _, resp, _ = call("PUT", "/api/device/%s" % did, token=ctx.admin_token, json_body=upd)
    ok_or(M, "PUT /api/device/{id} 更新", resp, "body=" + json.dumps(upd, ensure_ascii=False))

    _, resp, _ = call("PUT", "/api/device/%s/disable" % did, token=ctx.admin_token)
    ok_or(M, "PUT /api/device/{id}/disable", resp)

    _, resp, _ = call("PUT", "/api/device/%s/enable" % did, token=ctx.admin_token)
    ok_or(M, "PUT /api/device/{id}/enable", resp)

    _, resp, _ = call("DELETE", "/api/device/%s" % did, token=ctx.admin_token)
    ok_or(M, "DELETE /api/device/{id}", resp)
    return did

# =====================================================================
# Module 4: 二维码 QrCodeController
# =====================================================================
def test_qrcode(mid):
    M = "4.二维码"
    log("== %s ==" % M)
    # 需要一台设备
    suffix = rand_suffix()
    body = {"deviceNo": P + "qrdev_" + suffix, "name": P + "qrdev_" + suffix,
            "merchantId": mid, "type": "qrcode", "systemCode": P + "qrsys_" + suffix}
    _, resp, _ = call("POST", "/api/device", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/device (二维码测试前置)", resp)
    if not is_ok(r):
        return
    devid = data_of(r).get("id")
    ctx.device_ids.append(devid)

    gen = {"deviceId": devid, "qrData": P + "qr_" + suffix, "type": "STANDARD"}
    _, resp, _ = call("POST", "/api/admin/qrcode/generate", token=ctx.admin_token,
                      params={"merchantId": mid}, json_body=gen)
    r = ok_or(M, "POST /api/admin/qrcode/generate", resp, "body=" + json.dumps(gen, ensure_ascii=False))
    if not is_ok(r):
        # 删除前置设备
        call("DELETE", "/api/device/%s" % devid, token=ctx.admin_token)
        return
    qid = data_of(r).get("id")
    ctx.created_qrcode_ids.append(qid)
    log("  qrcode id=%s" % qid)

    _, resp, _ = call("GET", "/api/admin/qrcode/list", token=ctx.admin_token,
                      params={"merchantId": mid})
    ok_or(M, "GET /api/admin/qrcode/list", resp)

    _, resp, _ = call("GET", "/api/admin/qrcode/%s" % qid, token=ctx.admin_token)
    ok_or(M, "GET /api/admin/qrcode/{id}", resp)

    _, resp, _ = call("DELETE", "/api/admin/qrcode/%s" % qid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/admin/qrcode/{id}", resp)

    call("DELETE", "/api/device/%s" % devid, token=ctx.admin_token)

# =====================================================================
# Module 5: AI 生成 + 语料
# =====================================================================
def test_ai_corpus(mid):
    M = "5.AI生成+语料"
    log("== %s ==" % M)
    _, resp, _ = call("GET", "/api/admin/ai/generate/history", token=ctx.admin_token,
                      params={"merchantId": mid, "pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/admin/ai/generate/history(分页)", resp)

    _, resp, _ = call("GET", "/api/admin/ai-generate/statistics", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/ai-generate/statistics", resp)

    gen = {"merchantId": mid, "prompt": "regress_test_prompt_文本生成"}
    _, resp, _ = call("POST", "/api/admin/ai/generate/text", token=ctx.admin_token, json_body=gen,
                      timeout=90)
    ok_or(M, "POST /api/admin/ai/generate/text(可能因key/额度失败)", resp,
          "body=" + json.dumps(gen, ensure_ascii=False))

    # ---- 语料分类 ----
    cat_body = {"name": P + "cat_" + rand_suffix(), "description": "regress", "sortOrder": 1}
    _, resp, _ = call("POST", "/api/merchant/corpus/categories", token=ctx.admin_token,
                      params={"merchantId": mid}, json_body=cat_body)
    r = ok_or(M, "POST /api/merchant/corpus/categories 创建", resp,
              "body=" + json.dumps(cat_body, ensure_ascii=False))
    if is_ok(r):
        catid = data_of(r).get("id")
        ctx.created_cat_ids.append(catid)
        _, resp, _ = call("GET", "/api/merchant/corpus/categories", token=ctx.admin_token,
                          params={"merchantId": mid})
        ok_or(M, "GET /api/merchant/corpus/categories", resp)
        _, resp, _ = call("GET", "/api/merchant/corpus/categories/%s" % catid,
                          token=ctx.admin_token, params={"merchantId": mid})
        ok_or(M, "GET /api/merchant/corpus/categories/{id}", resp)
        upd = {"name": P + "cat_upd", "description": "regress2", "sortOrder": 2, "enabled": True}
        _, resp, _ = call("PUT", "/api/merchant/corpus/categories/%s" % catid,
                          token=ctx.admin_token, params={"merchantId": mid}, json_body=upd)
        ok_or(M, "PUT /api/merchant/corpus/categories/{id}", resp,
              "body=" + json.dumps(upd, ensure_ascii=False))
        _, resp, _ = call("DELETE", "/api/merchant/corpus/categories/%s" % catid,
                          token=ctx.admin_token, params={"merchantId": mid})
        ok_or(M, "DELETE /api/merchant/corpus/categories/{id}", resp)

    # ---- 语料 ----
    cor_body = {"title": P + "corpus_" + rand_suffix(), "content": "regress content",
                "category": "regress_cat", "type": "text", "tags": "[]", "status": 1}
    _, resp, _ = call("POST", "/api/merchant/corpus", token=ctx.admin_token,
                      params={"merchantId": mid}, json_body=cor_body)
    r = ok_or(M, "POST /api/merchant/corpus 创建", resp,
              "body=" + json.dumps(cor_body, ensure_ascii=False))
    if is_ok(r):
        cid = data_of(r).get("corpusId")
        ent_id = data_of(r).get("id")
        ctx.created_corpus_ids.append(cid)
        _, resp, _ = call("GET", "/api/merchant/corpus", token=ctx.admin_token,
                          params={"merchantId": mid, "pageNum": 1, "pageSize": 10})
        ok_or(M, "GET /api/merchant/corpus", resp)
        _, resp, _ = call("GET", "/api/merchant/corpus/%s" % cid, token=ctx.admin_token)
        ok_or(M, "GET /api/merchant/corpus/{corpusId}", resp)
        upd = {"id": ent_id, "title": P + "corpus_upd", "content": "regress content upd",
               "type": "text", "status": 1}
        _, resp, _ = call("PUT", "/api/merchant/corpus/%s" % cid, token=ctx.admin_token, json_body=upd)
        ok_or(M, "PUT /api/merchant/corpus/{corpusId}", resp,
              "body=" + json.dumps(upd, ensure_ascii=False))
        _, resp, _ = call("DELETE", "/api/merchant/corpus/%s" % cid, token=ctx.admin_token)
        ok_or(M, "DELETE /api/merchant/corpus/{corpusId}", resp)

    _, resp, _ = call("GET", "/api/merchant/corpus/storage", token=ctx.admin_token,
                      params={"merchantId": mid})
    ok_or(M, "GET /api/merchant/corpus/storage", resp)
    _, resp, _ = call("GET", "/api/merchant/corpus/search", token=ctx.admin_token,
                      params={"keyword": "regress", "pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/merchant/corpus/search", resp)
    _, resp, _ = call("GET", "/api/admin/corpus", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/admin/corpus", resp)

# =====================================================================
# Module 6: AI 配置
# =====================================================================
def test_ai_config(mid):
    M = "6.AI配置"
    log("== %s ==" % M)
    _, resp, _ = call("GET", "/api/merchant/ai-config", token=ctx.admin_token,
                      params={"merchantId": mid})
    ok_or(M, "GET /api/merchant/ai-config", resp)

    cfg = {"textModel": "regress-text-model", "imageModel": "regress-image-model",
           "videoModel": "regress-video-model", "apiKey": "", "apiSecret": "", "enabled": True}
    _, resp, _ = call("PUT", "/api/merchant/ai-config", token=ctx.admin_token,
                      params={"merchantId": mid}, json_body=cfg)
    ok_or(M, "PUT /api/merchant/ai-config", resp, "body=" + json.dumps(cfg, ensure_ascii=False))

    _, resp, _ = call("GET", "/api/admin/ai-config", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/ai-config(全局)", resp)

    _, resp, _ = call("GET", "/api/admin/ai-config/%s" % mid, token=ctx.admin_token)
    ok_or(M, "GET /api/admin/ai-config/{merchantId}", resp)

    _, resp, _ = call("GET", "/api/admin/ai-config/list", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/admin/ai-config/list", resp)

    _, resp, _ = call("GET", "/api/admin/ai-config/overview", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/admin/ai-config/overview", resp)

    _, resp, _ = call("DELETE", "/api/admin/ai-config/%s" % mid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/admin/ai-config/{merchantId}(清理测试配置)", resp)

# =====================================================================
# Module 7: 优惠券
# =====================================================================
def test_coupon(mid):
    M = "7.优惠券"
    log("== %s ==" % M)
    suffix = rand_suffix()
    body = {"merchantId": mid, "title": P + "coupon_" + suffix, "type": "cash",
            "amount": 5.0, "minAmount": 10.0, "totalCount": 100,
            "startTime": "2026-08-01T00:00:00", "endTime": "2026-12-31T23:59:59",
            "link": "https://example.com/regress", "description": "regress"}
    _, resp, _ = call("POST", "/api/coupon", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/coupon 创建", resp, "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        return None
    cid = data_of(r).get("id")
    ctx.created_coupon_ids.append(cid)
    log("  coupon id=%s" % cid)

    _, resp, _ = call("GET", "/api/coupon/list", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/coupon/list", resp)

    _, resp, _ = call("GET", "/api/coupon/%s" % cid, token=ctx.admin_token)
    ok_or(M, "GET /api/coupon/{id}", resp)

    upd = dict(body)
    upd["title"] = P + "coupon_upd_" + suffix
    _, resp, _ = call("PUT", "/api/coupon/%s" % cid, token=ctx.admin_token, json_body=upd)
    ok_or(M, "PUT /api/coupon/{id}", resp, "body=" + json.dumps(upd, ensure_ascii=False))

    _, resp, _ = call("PUT", "/api/coupon/%s/disable" % cid, token=ctx.admin_token)
    ok_or(M, "PUT /api/coupon/{id}/disable", resp)

    _, resp, _ = call("PUT", "/api/coupon/%s/enable" % cid, token=ctx.admin_token)
    ok_or(M, "PUT /api/coupon/{id}/enable", resp)

    _, resp, _ = call("DELETE", "/api/coupon/%s" % cid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/coupon/{id}", resp)
    return cid

# =====================================================================
# Module 8: 推广平台 + 推广配置
# =====================================================================
def test_promotion(mid):
    M = "8.推广平台+配置"
    log("== %s ==" % M)
    suffix = rand_suffix()
    body = {"code": P + "platform_" + suffix, "name": P + "platform_" + suffix,
            "description": "regress", "jumpMode": "webview",
            "webUrlTemplate": "https://example.com/{code}", "sortOrder": 1,
            "requiredParams": "[]", "optionalParams": "[]", "color": "#FF0000"}
    _, resp, _ = call("POST", "/api/promotion/platforms", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/promotion/platforms 创建", resp,
              "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        return None
    pid = data_of(r).get("id")
    ctx.created_platform_ids.append(pid)
    log("  platform id=%s" % pid)

    _, resp, _ = call("GET", "/api/promotion/platforms", params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/promotion/platforms(公开)", resp)

    _, resp, _ = call("GET", "/api/promotion/platforms/%s" % pid)
    ok_or(M, "GET /api/promotion/platforms/{id}", resp)

    upd = {"name": P + "platform_upd_" + suffix, "description": "regress2",
           "enabled": True, "sortOrder": 2}
    _, resp, _ = call("PUT", "/api/promotion/platforms/%s" % pid, token=ctx.admin_token, json_body=upd)
    ok_or(M, "PUT /api/promotion/platforms/{id}", resp, "body=" + json.dumps(upd, ensure_ascii=False))

    cfg_body = {"type": "platform", "platformId": pid, "sort": 1,
                "params": json.dumps({"code": "ABC"}), "customName": "regress_cfg"}
    _, resp, _ = call("POST", "/api/promotion-config/merchant/%s" % mid,
                      token=ctx.admin_token, json_body=cfg_body)
    r = ok_or(M, "POST /api/promotion-config/merchant/{merchantId}", resp,
              "body=" + json.dumps(cfg_body, ensure_ascii=False))
    if is_ok(r):
        cfgid = data_of(r).get("id")
        ctx.created_config_ids.append(cfgid)
        _, resp, _ = call("GET", "/api/promotion-config/merchant/%s/active" % mid,
                          token=ctx.admin_token)
        ok_or(M, "GET /api/promotion-config/merchant/{merchantId}/active", resp)
        _, resp, _ = call("GET", "/api/promotion-config/%s" % cfgid, token=ctx.admin_token)
        ok_or(M, "GET /api/promotion-config/{configId}", resp)
        _, resp, _ = call("PUT", "/api/promotion-config/%s" % cfgid, token=ctx.admin_token,
                          json_body={"customName": "regress_cfg_upd", "sort": 2, "status": 1})
        ok_or(M, "PUT /api/promotion-config/{configId}", resp)
        _, resp, _ = call("DELETE", "/api/promotion-config/%s" % cfgid, token=ctx.admin_token)
        ok_or(M, "DELETE /api/promotion-config/{configId}", resp)

    # 小程序推广列表(公开) 需商家启用的配置 -> 配置已删，返回空
    _, resp, _ = call("GET", "/api/miniapp/promotion/platforms", params={"merchantId": mid})
    ok_or(M, "GET /api/miniapp/promotion/platforms(公开)", resp)

    _, resp, _ = call("DELETE", "/api/promotion/platforms/%s" % pid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/promotion/platforms/{id}", resp)
    return pid

# =====================================================================
# Module 9: 套餐
# =====================================================================
def test_plan():
    M = "9.套餐"
    log("== %s ==" % M)
    suffix = rand_suffix()
    body = {"name": P + "plan_" + suffix, "level": "basic", "price": 99.0,
            "durationMonths": 1, "deviceCount": 10, "textQuota": 100,
            "imageQuota": 50, "videoQuota": 10, "storageLimit": 1024}
    _, resp, _ = call("POST", "/api/plan", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/plan 创建", resp, "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        return None
    planid = data_of(r).get("id")
    ctx.created_plan_ids.append(planid)
    log("  plan id=%s" % planid)

    _, resp, _ = call("GET", "/api/plan/list")
    ok_or(M, "GET /api/plan/list(公开)", resp)

    _, resp, _ = call("GET", "/api/plan/%s" % planid)
    ok_or(M, "GET /api/plan/{id}", resp)

    _, resp, _ = call("GET", "/api/plan/all")
    ok_or(M, "GET /api/plan/all", resp)

    upd = dict(body)
    upd["name"] = P + "plan_upd_" + suffix
    _, resp, _ = call("PUT", "/api/plan/%s" % planid, token=ctx.admin_token, json_body=upd)
    ok_or(M, "PUT /api/plan/{id}", resp, "body=" + json.dumps(upd, ensure_ascii=False))

    _, resp, _ = call("PUT", "/api/plan/%s/disable" % planid, token=ctx.admin_token)
    ok_or(M, "PUT /api/plan/{id}/disable", resp)
    _, resp, _ = call("PUT", "/api/plan/%s/enable" % planid, token=ctx.admin_token)
    ok_or(M, "PUT /api/plan/{id}/enable", resp)
    _, resp, _ = call("PUT", "/api/plan/%s/recommend" % planid, token=ctx.admin_token)
    ok_or(M, "PUT /api/plan/{id}/recommend", resp)
    _, resp, _ = call("PUT", "/api/plan/%s/unrecommend" % planid, token=ctx.admin_token)
    ok_or(M, "PUT /api/plan/{id}/unrecommend", resp)

    _, resp, _ = call("DELETE", "/api/plan/%s" % planid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/plan/{id}", resp)
    return planid

# =====================================================================
# Module 10: 订单
# =====================================================================
def test_order(mid):
    M = "10.订单"
    log("== %s ==" % M)
    # 自建套餐供订单引用
    suffix = rand_suffix()
    plan_body = {"name": P + "oplan_" + suffix, "level": "pro", "price": 199.0,
                 "durationMonths": 3, "deviceCount": 20, "textQuota": 500,
                 "imageQuota": 200, "videoQuota": 50, "storageLimit": 2048}
    _, resp, _ = call("POST", "/api/plan", token=ctx.admin_token, json_body=plan_body)
    r = ok_or(M, "POST /api/plan (订单前置)", resp)
    if not is_ok(r):
        return
    planid = data_of(r).get("id")
    ctx.created_plan_ids.append(planid)

    body = {"merchantId": mid, "planId": planid, "amount": 199.0}
    _, resp, _ = call("POST", "/api/order", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/order 创建", resp, "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        call("DELETE", "/api/plan/%s" % planid, token=ctx.admin_token)
        return
    oid = data_of(r).get("id")
    ctx.created_order_ids.append(oid)
    log("  order id=%s" % oid)

    _, resp, _ = call("GET", "/api/order/list", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/order/list", resp)

    _, resp, _ = call("GET", "/api/order/%s" % oid, token=ctx.admin_token)
    ok_or(M, "GET /api/order/{id}", resp)

    _, resp, _ = call("PUT", "/api/order/%s/pay" % oid, token=ctx.admin_token)
    ok_or(M, "PUT /api/order/{id}/pay", resp)

    _, resp, _ = call("PUT", "/api/order/%s/refund" % oid, token=ctx.admin_token,
                      json_body={"reason": "regress_refund"})
    ok_or(M, "PUT /api/order/{id}/refund", resp)

    _, resp, _ = call("DELETE", "/api/order/%s" % oid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/order/{id}", resp)

    call("DELETE", "/api/plan/%s" % planid, token=ctx.admin_token)

# =====================================================================
# Module 11: 用户管理 (C端 + 管理端)
# =====================================================================
def test_user():
    M = "11.用户管理"
    log("== %s ==" % M)
    body = register_user()
    r = ok_or(M, "POST /api/user/register", body,
              "body={username: regress_u_xxx, password: regress123}")
    if is_ok(body):
        _, resp, _ = call("GET", "/api/user/info", token=ctx.user_token)
        ok_or(M, "GET /api/user/info(C端)", resp)
        upd = {"username": data_of(body).get("username"), "password": "regress123",
               "nickname": "regress_nick_upd", "phone": data_of(body).get("phone")}
        _, resp, _ = call("PUT", "/api/user/info", token=ctx.user_token, json_body=upd)
        ok_or(M, "PUT /api/user/info(C端)", resp, "body=" + json.dumps(upd, ensure_ascii=False))
        # C端登录
        _, resp, _ = call("POST", "/api/user/login",
                          json_body={"username": data_of(body).get("username"), "password": "regress123"})
        ok_or(M, "POST /api/user/login", resp)
    else:
        # 注册接口真实故障（user 表缺 username 列），用 DB 直插用户供后续模块使用
        db_register_user_fallback()
        log("  [SKIP] GET/PUT /api/user/info 与 /api/user/login 因注册接口500无法验证")

    # 管理端用户管理
    suffix = rand_suffix()
    au_body = {"username": P + "adminuser_" + suffix, "password": "regress123",
               "nickname": "regress_au", "role": "admin", "status": 1,
               "phone": "13700000001", "email": "au@regress.com"}
    _, resp, _ = call("POST", "/api/admin/user", token=ctx.admin_token, json_body=au_body)
    r = ok_or(M, "POST /api/admin/user 创建", resp,
              "body=" + json.dumps(au_body, ensure_ascii=False))
    if not is_ok(r):
        return
    auid = data_of(r).get("id")
    ctx.created_admin_ids.append(auid)
    log("  admin-user id=%s" % auid)

    _, resp, _ = call("GET", "/api/admin/user/list", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10, "keyword": P})
    ok_or(M, "GET /api/admin/user/list", resp)

    upd = {"nickname": "regress_au_upd", "email": "au2@regress.com", "phone": "13700000002"}
    _, resp, _ = call("PUT", "/api/admin/user/%s" % auid, token=ctx.admin_token, json_body=upd)
    ok_or(M, "PUT /api/admin/user/{id}", resp, "body=" + json.dumps(upd, ensure_ascii=False))

    _, resp, _ = call("PUT", "/api/admin/user/%s/status" % auid, token=ctx.admin_token,
                      params={"status": 0})
    ok_or(M, "PUT /api/admin/user/{id}/status?status=0", resp)
    _, resp, _ = call("PUT", "/api/admin/user/%s/status" % auid, token=ctx.admin_token,
                      params={"status": 1})
    ok_or(M, "PUT /api/admin/user/{id}/status?status=1", resp)

    _, resp, _ = call("PUT", "/api/admin/user/%s/reset-password" % auid, token=ctx.admin_token,
                      json_body={"password": "regress456"})
    ok_or(M, "PUT /api/admin/user/{id}/reset-password", resp)

    _, resp, _ = call("DELETE", "/api/admin/user/%s" % auid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/admin/user/{id}", resp)

# =====================================================================
# Module 12: 管理员 AdminController
# =====================================================================
def test_admin():
    M = "12.管理员"
    log("== %s ==" % M)
    _, resp, _ = call("GET", "/api/admin/list", token=ctx.admin_token,
                      params={"pageNum": 1, "pageSize": 10})
    ok_or(M, "GET /api/admin/list", resp)

    suffix = rand_suffix()
    body = {"username": P + "admin_" + suffix, "password": "regress123",
            "nickname": "regress_admin", "role": "admin", "status": 1,
            "phone": "13600000001", "email": "adm@regress.com"}
    _, resp, _ = call("POST", "/api/admin", token=ctx.admin_token, json_body=body)
    r = ok_or(M, "POST /api/admin 创建", resp, "body=" + json.dumps(body, ensure_ascii=False))
    if not is_ok(r):
        return
    aid = data_of(r).get("id")
    ctx.created_admin_ids.append(aid)
    log("  admin id=%s" % aid)

    _, resp, _ = call("GET", "/api/admin/%s" % aid, token=ctx.admin_token)
    ok_or(M, "GET /api/admin/{id}", resp)

    upd = {"nickname": "regress_admin_upd", "email": "adm2@regress.com", "phone": "13600000002"}
    _, resp, _ = call("PUT", "/api/admin/%s" % aid, token=ctx.admin_token, json_body=upd)
    ok_or(M, "PUT /api/admin/{id}", resp, "body=" + json.dumps(upd, ensure_ascii=False))

    _, resp, _ = call("PUT", "/api/admin/%s/disable" % aid, token=ctx.admin_token)
    ok_or(M, "PUT /api/admin/{id}/disable", resp)
    _, resp, _ = call("PUT", "/api/admin/%s/enable" % aid, token=ctx.admin_token)
    ok_or(M, "PUT /api/admin/{id}/enable", resp)
    _, resp, _ = call("PUT", "/api/admin/%s/reset-password" % aid, token=ctx.admin_token,
                      json_body={"password": "regress789"})
    ok_or(M, "PUT /api/admin/{id}/reset-password", resp)

    # updateInfoBySelf: 用测试管理员自身 token 调 PUT /api/admin/user/info，不改 Dmy510
    _, login_body, _ = call("POST", "/api/admin/auth/login",
                            json_body={"username": body["username"], "password": "regress789"})
    if is_ok(login_body):
        tk = data_of(login_body).get("token")
        ctx.test_admin_tokens.append(tk)
        _, resp, _ = call("PUT", "/api/admin/user/info", token=tk,
                          json_body={"nickname": "regress_self_upd", "email": "self@regress.com"})
        ok_or(M, "PUT /api/admin/user/info(updateInfoBySelf)", resp)
        _, resp, _ = call("GET", "/api/admin/user/info", token=tk)
        ok_or(M, "GET /api/admin/user/info(当前用户信息)", resp)
    else:
        ok_or(M, "POST /api/admin/auth/login(测试管理员)", login_body)

    _, resp, _ = call("DELETE", "/api/admin/%s" % aid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/admin/{id}", resp)

# =====================================================================
# Module 13: 角色 + 权限
# =====================================================================
def test_role_permission():
    M = "13.角色+权限"
    log("== %s ==" % M)
    _, resp, _ = call("GET", "/api/admin/roles", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/roles", resp)
    _, resp, _ = call("GET", "/api/admin/roles/permissions", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/roles/permissions", resp)
    _, resp, _ = call("GET", "/api/admin/roles/matrix", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/roles/matrix", resp)
    _, resp, _ = call("GET", "/api/admin/roles/admins", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/roles/admins", resp)
    _, resp, _ = call("GET", "/api/admin/roles/admin", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/roles/{roleId}", resp)

    # 建临时管理员做角色分配
    suffix = rand_suffix()
    _, resp, _ = call("POST", "/api/admin", token=ctx.admin_token,
                      json_body={"username": P + "roleadmin_" + suffix, "password": "regress123",
                                 "role": "admin", "status": 1})
    if is_ok(resp):
        raid = data_of(resp).get("id")
        ctx.created_admin_ids.append(raid)
        _, resp2, _ = call("POST", "/api/admin/roles/assign", token=ctx.admin_token,
                           params={"adminId": raid, "roleId": "admin"})
        ok_or(M, "POST /api/admin/roles/assign", resp2, "params=adminId=%s&roleId=admin" % raid)
        _, resp3, _ = call("GET", "/api/admin/roles/%s/permissions" % raid, token=ctx.admin_token)
        ok_or(M, "GET /api/admin/roles/{userId}/permissions", resp3)
        _, resp4, _ = call("GET", "/api/admin/roles/%s/check-permission" % raid, token=ctx.admin_token,
                           params={"permission": "merchant.view"})
        ok_or(M, "GET /api/admin/roles/{userId}/check-permission", resp4)
        call("DELETE", "/api/admin/%s" % raid, token=ctx.admin_token)

    # ---- /api/v1/permissions ----
    _, resp, _ = call("GET", "/api/v1/permissions", token=ctx.admin_token)
    ok_or(M, "GET /api/v1/permissions", resp)
    _, resp, _ = call("GET", "/api/v1/permissions/resources", token=ctx.admin_token)
    ok_or(M, "GET /api/v1/permissions/resources", resp)
    _, resp, _ = call("GET", "/api/v1/permissions/resource/merchant", token=ctx.admin_token)
    ok_or(M, "GET /api/v1/permissions/resource/{resource}", resp)

    perm_code = P + "perm_" + rand_suffix()
    _, resp, _ = call("POST", "/api/v1/permissions", token=ctx.admin_token,
                      params={"code": perm_code, "resource": "regress", "action": "read",
                              "description": "regress perm"})
    r = ok_or(M, "POST /api/v1/permissions", resp,
              "params=code=%s&resource=regress&action=read" % perm_code)
    if not is_ok(r):
        return
    perm_id = data_of(r).get("id")
    ctx.created_perm_ids.append(perm_id)
    log("  permission id=%s code=%s" % (perm_id, perm_code))

    _, resp, _ = call("PUT", "/api/v1/permissions/%s" % perm_id, token=ctx.admin_token,
                      params={"description": "regress perm updated"})
    ok_or(M, "PUT /api/v1/permissions/{id}", resp)

    _, resp, _ = call("GET", "/api/v1/permissions/%s" % perm_code, token=ctx.admin_token)
    ok_or(M, "GET /api/v1/permissions/{code}", resp)

    _, resp, _ = call("POST", "/api/v1/permissions/roles/admin/permissions/%s" % perm_code,
                      token=ctx.admin_token)
    ok_or(M, "POST /api/v1/permissions/roles/{roleCode}/permissions/{permissionCode}", resp,
          "role=admin perm=%s" % perm_code)

    _, resp, _ = call("GET", "/api/v1/permissions/roles/admin/permissions", token=ctx.admin_token)
    ok_or(M, "GET /api/v1/permissions/roles/{roleCode}/permissions", resp)

    _, resp, _ = call("DELETE", "/api/v1/permissions/roles/admin/permissions/%s" % perm_code,
                      token=ctx.admin_token)
    ok_or(M, "DELETE /api/v1/permissions/roles/{roleCode}/permissions/{permissionCode}", resp)

    _, resp, _ = call("DELETE", "/api/v1/permissions/%s" % perm_id, token=ctx.admin_token)
    ok_or(M, "DELETE /api/v1/permissions/{id}", resp)

# =====================================================================
# Module 14: 系统设置
# =====================================================================
def test_system():
    M = "14.系统设置"
    log("== %s ==" % M)
    _, resp, _ = call("GET", "/api/admin/system/settings", token=ctx.admin_token)
    r = ok_or(M, "GET /api/admin/system/settings", resp)
    if is_ok(r) and data_of(r):
        # 原样回写，不改变现有配置
        _, resp2, _ = call("PUT", "/api/admin/system/settings", token=ctx.admin_token,
                           json_body={"domain": data_of(r)})
        ok_or(M, "PUT /api/admin/system/settings(原样回写)", resp2)

    _, resp, _ = call("GET", "/api/admin/system/admin-merchant-access/list", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/system/admin-merchant-access/list", resp)

# =====================================================================
# Module 15: 统计 + 扫码日志
# =====================================================================
def test_statistics(mid):
    M = "15.统计+扫码日志"
    log("== %s ==" % M)
    _, resp, _ = call("GET", "/api/admin/statistics/overview", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/statistics/overview", resp)
    _, resp, _ = call("GET", "/api/admin/statistics/trend", token=ctx.admin_token,
                      params={"startDate": "2026-07-01", "endDate": "2026-08-07"})
    ok_or(M, "GET /api/admin/statistics/trend", resp)
    _, resp, _ = call("GET", "/api/admin/statistics/top/merchants", token=ctx.admin_token,
                      params={"limit": 5})
    ok_or(M, "GET /api/admin/statistics/top/merchants", resp)
    _, resp, _ = call("GET", "/api/admin/statistics/ai-stats", token=ctx.admin_token)
    ok_or(M, "GET /api/admin/statistics/ai-stats", resp)
    _, resp, _ = call("GET", "/api/admin/statistics/merchant/%s" % mid, token=ctx.admin_token)
    ok_or(M, "GET /api/admin/statistics/merchant/{merchantId}", resp)

    # 扫码日志：造设备 + 用户
    suffix = rand_suffix()
    _, resp, _ = call("POST", "/api/device", token=ctx.admin_token,
                      json_body={"deviceNo": P + "scandev_" + suffix, "name": P + "scandev_" + suffix,
                                 "merchantId": mid, "type": "qrcode"})
    if is_ok(resp):
        devid = data_of(resp).get("id")
        ctx.device_ids.append(devid)
        uid = ctx.user_id
        _, resp, _ = call("POST", "/api/scan-logs/scan", token=ctx.admin_token,
                          params={"userId": uid, "deviceId": devid, "merchantId": mid})
        ok_or(M, "POST /api/scan-logs/scan", resp, "params=userId=%s&deviceId=%s&merchantId=%s" % (uid, devid, mid))
        _, resp, _ = call("GET", "/api/scan-logs/device/%s" % devid, token=ctx.admin_token)
        ok_or(M, "GET /api/scan-logs/device/{deviceId}", resp)
        _, resp, _ = call("GET", "/api/scan-logs/device/%s/count" % devid, token=ctx.admin_token)
        ok_or(M, "GET /api/scan-logs/device/{deviceId}/count", resp)
        _, resp, _ = call("GET", "/api/scan-logs/device/%s/count-by-time" % devid, token=ctx.admin_token,
                          params={"startTime": "2026-01-01T00:00:00", "endTime": "2026-12-31T23:59:59"})
        ok_or(M, "GET /api/scan-logs/device/{deviceId}/count-by-time", resp)
        if uid:
            _, resp, _ = call("GET", "/api/scan-logs/user/%s" % uid, token=ctx.admin_token)
            ok_or(M, "GET /api/scan-logs/user/{userId}", resp)
            _, resp, _ = call("GET", "/api/scan-logs/user/%s/count" % uid, token=ctx.admin_token)
            ok_or(M, "GET /api/scan-logs/user/{userId}/count", resp)
        call("DELETE", "/api/device/%s" % devid, token=ctx.admin_token)
    else:
        ok_or(M, "POST /api/device (扫码日志前置)", resp)

# =====================================================================
# Module 16: 上传下载
# =====================================================================
def test_upload():
    M = "16.上传下载"
    log("== %s ==" % M)
    # 1x1 PNG
    png_b64 = ("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==")
    png_bytes = base64.b64decode(png_b64)
    files = {"file": ("regress_test_1x1.png", png_bytes, "image/png")}
    _, resp, _ = call("POST", "/api/admin/upload/image", token=ctx.admin_token, files=files,
                      timeout=60)
    r = ok_or(M, "POST /api/admin/upload/image", resp)
    if is_ok(r):
        url = data_of(r).get("url")
        ctx.uploaded_urls.append(url)
        fname = data_of(r).get("filename")
        log("  uploaded url=%s" % url)
        try:
            r2 = requests.get(BASE + url, timeout=30)
            ok = r2.status_code == 200
            ok_or(M, "GET %s (下载断言200)" % url,
                  {"code": "1000" if ok else "0", "message": "http_status=%s" % r2.status_code})
        except Exception as e:
            ok_or(M, "GET %s" % url, {"code": "0", "message": str(e)})
        # 尝试删除本地临时文件
        candidates = [
            os.path.join("QuickTap-Server", "uploads", "images", fname),
            os.path.join("uploads", "images", fname),
            os.path.join(os.getcwd(), "uploads", "images", fname),
        ]
        for c in candidates:
            try:
                if os.path.exists(c):
                    os.remove(c)
                    log("  removed temp file: %s" % c)
            except Exception as e:
                log("  remove temp file failed: %s (%s)" % (c, e))
    # 头像上传
    files2 = {"file": ("regress_test_avatar.png", png_bytes, "image/png")}
    _, resp, _ = call("POST", "/api/admin/upload/avatar", token=ctx.admin_token, files=files2,
                      timeout=60)
    r2 = ok_or(M, "POST /api/admin/upload/avatar", resp)
    if is_ok(r2):
        url = data_of(r2).get("url")
        ctx.uploaded_urls.append(url)
        fname = data_of(r2).get("filename")
        candidates = [
            os.path.join("QuickTap-Server", "uploads", "avatars", fname),
            os.path.join("uploads", "avatars", fname),
        ]
        for c in candidates:
            try:
                if os.path.exists(c):
                    os.remove(c)
            except Exception:
                pass

# =====================================================================
# Module 17: 用户设备
# =====================================================================
def test_user_device(mid):
    M = "17.用户设备"
    log("== %s ==" % M)
    db_register_user_fallback()
    suffix = rand_suffix()
    _, resp, _ = call("POST", "/api/device", token=ctx.admin_token,
                      json_body={"deviceNo": P + "uddev_" + suffix, "name": P + "uddev_" + suffix,
                                 "merchantId": mid, "type": "qrcode"})
    r = ok_or(M, "POST /api/device (用户设备前置)", resp)
    if not is_ok(r):
        return
    devid = data_of(r).get("id")
    ctx.device_ids.append(devid)
    uid = ctx.user_id

    _, resp, _ = call("POST", "/api/user-device/bind/%s/%s" % (uid, devid), token=ctx.admin_token)
    ok_or(M, "POST /api/user-device/bind/{userId}/{deviceId}", resp,
          "path=/bind/%s/%s" % (uid, devid))
    _, resp, _ = call("GET", "/api/user-device/user/%s" % uid, token=ctx.admin_token)
    ok_or(M, "GET /api/user-device/user/{userId}", resp)
    _, resp, _ = call("GET", "/api/user-device/device/%s" % devid, token=ctx.admin_token)
    ok_or(M, "GET /api/user-device/device/{deviceId}", resp)
    _, resp, _ = call("GET", "/api/user-device/check/%s/%s" % (uid, devid), token=ctx.admin_token)
    ok_or(M, "GET /api/user-device/check/{userId}/{deviceId}", resp)
    _, resp, _ = call("DELETE", "/api/user-device/unbind/%s/%s" % (uid, devid), token=ctx.admin_token)
    ok_or(M, "DELETE /api/user-device/unbind/{userId}/{deviceId}", resp)
    _, resp, _ = call("DELETE", "/api/user-device/user/%s/all" % uid, token=ctx.admin_token)
    ok_or(M, "DELETE /api/user-device/user/{userId}/all", resp)
    call("DELETE", "/api/device/%s" % devid, token=ctx.admin_token)

# =====================================================================
# Module 18: 小程序 C端公开接口
# =====================================================================
def test_miniapp(mid):
    M = "18.小程序C端"
    log("== %s ==" % M)
    # 查一台测试设备做 check-bind
    rows = db_query("SELECT id, system_code, device_no FROM device WHERE name LIKE %s ORDER BY id DESC LIMIT 1",
                    (P + "%",))
    code = None
    if rows:
        code = rows[0].get("system_code") or rows[0].get("device_no")
    if not code:
        # 造一台
        suffix = rand_suffix()
        _, resp, _ = call("POST", "/api/device", token=ctx.admin_token,
                          json_body={"deviceNo": P + "mini_" + suffix, "name": P + "mini_" + suffix,
                                     "merchantId": mid, "type": "qrcode", "systemCode": P + "minisys_" + suffix})
        if is_ok(resp):
            ctx.device_ids.append(data_of(resp).get("id"))
            code = P + "minisys_" + suffix
    if code:
        _, resp, _ = call("GET", "/api/miniapp/merchant/check-bind", params={"code": code})
        ok_or(M, "GET /api/miniapp/merchant/check-bind", resp, "params=code=%s" % code)
    else:
        ok_or(M, "GET /api/miniapp/merchant/check-bind", {"code": "0", "message": "无测试设备"})

    _, resp, _ = call("GET", "/api/miniapp/merchant/info/%s" % mid)
    ok_or(M, "GET /api/miniapp/merchant/info/{merchantId}", resp)
    _, resp, _ = call("GET", "/api/miniapp/merchant/wifi", params={"merchantId": mid})
    ok_or(M, "GET /api/miniapp/merchant/wifi", resp)
    _, resp, _ = call("GET", "/api/miniapp/merchant/promotion", params={"merchantId": mid})
    ok_or(M, "GET /api/miniapp/merchant/promotion", resp)
    _, resp, _ = call("GET", "/api/miniapp/coupon/list", params={"merchantId": mid})
    ok_or(M, "GET /api/miniapp/coupon/list", resp)
    _, resp, _ = call("GET", "/api/miniapp/promotion/platforms", params={"merchantId": mid})
    ok_or(M, "GET /api/miniapp/promotion/platforms", resp)
    _, resp, _ = call("GET", "/api/miniapp/user/referrer/list")
    ok_or(M, "GET /api/miniapp/user/referrer/list", resp)

# =====================================================================
# DB 清理
# =====================================================================
def db_cleanup():
    log("== DB 清理 ==")
    try:
        mids = [ctx.merchant_id] if ctx.merchant_id else []
        devs = ctx.device_ids
        uids = ctx.created_user_ids
        admins = ctx.created_admin_ids
        plans = ctx.created_plan_ids
        coupons = ctx.created_coupon_ids
        platforms = ctx.created_platform_ids
        perms = ctx.created_perm_ids
        orders = ctx.created_order_ids

        def qlist(ids):
            if not ids:
                return "NULL"
            return ",".join(str(i) for i in ids)

        # 先删引用表
        if uids:
            db_exec("DELETE FROM user_coupon WHERE user_id IN (%s)" % qlist(uids))
            db_exec("DELETE FROM user_device WHERE user_id IN (%s)" % qlist(uids))
            db_exec("DELETE FROM user_merchant WHERE user_id IN (%s)" % qlist(uids))
            db_exec("DELETE FROM scan_log WHERE user_id IN (%s)" % qlist(uids))
        if devs:
            db_exec("DELETE FROM user_device WHERE device_id IN (%s)" % qlist(devs))
            db_exec("DELETE FROM scan_log WHERE device_id IN (%s)" % qlist(devs))
        if coupons:
            db_exec("DELETE FROM user_coupon WHERE coupon_id IN (%s)" % qlist(coupons))
        if perms:
            db_exec("DELETE FROM role_permissions WHERE permission_id IN (%s)" % qlist(perms))
        if platforms:
            db_exec("DELETE FROM merchant_promotion_config WHERE platform_id IN (%s)" % qlist(platforms))
        if mids:
            db_exec("DELETE FROM merchant_promotion_config WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM ai_config WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM ai_generate_record WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM corpus WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM corpus_category WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM scan_log WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM user_merchant WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM qrcode WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM coupon WHERE merchant_id IN (%s)" % qlist(mids))
            db_exec("DELETE FROM order_record WHERE merchant_id IN (%s)" % qlist(mids))
        if plans:
            db_exec("DELETE FROM order_record WHERE plan_id IN (%s)" % qlist(plans))
            db_exec("DELETE FROM plan WHERE id IN (%s)" % qlist(plans))
        if orders:
            db_exec("DELETE FROM order_record WHERE id IN (%s)" % qlist(orders))
        if admins:
            db_exec("DELETE FROM admin WHERE id IN (%s)" % qlist(admins))
        if uids:
            db_exec("DELETE FROM user WHERE id IN (%s)" % qlist(uids))

        # 兜底 LIKE 清理（防漏）
        db_exec("DELETE FROM device WHERE device_no LIKE %s OR name LIKE %s", (P + "%", P + "%"))
        db_exec("DELETE FROM merchant WHERE name LIKE %s", (P + "%",))
        db_exec("DELETE FROM coupon WHERE title LIKE %s", (P + "%",))
        db_exec("DELETE FROM plan WHERE name LIKE %s", (P + "%",))
        # user 表无 username 列，按 nickname 清理（勿用 phone，会误删真实用户）
        db_exec("DELETE FROM user WHERE nickname LIKE %s", (P + "%",))
        db_exec("DELETE FROM admin WHERE username LIKE %s", (P + "%",))
        db_exec("DELETE FROM corpus WHERE title LIKE %s", (P + "%",))
        db_exec("DELETE FROM corpus_category WHERE name LIKE %s", (P + "%",))
        db_exec("DELETE FROM promotion_platform WHERE code LIKE %s OR name LIKE %s", (P + "%", P + "%"))
        db_exec("DELETE FROM permissions WHERE code LIKE %s", (P + "%",))
        db_exec("DELETE FROM qrcode WHERE code LIKE %s", (P + "%",))
        db_exec("DELETE FROM order_record WHERE order_no LIKE %s", (P + "%",))
        log("  DB 清理完成")
    except Exception as e:
        log("  DB 清理异常: %s" % e)
        traceback.print_exc()

# =====================================================================
# 汇总
# =====================================================================
def summary():
    print()
    print("=" * 100)
    print("结果汇总")
    print("=" * 100)
    modules = {}
    for m, name, ok, detail in results:
        modules.setdefault(m, [0, 0])
        modules[m][1] += 1
        if ok:
            modules[m][0] += 1
    for m in sorted(modules, key=lambda x: int(x.split(".")[0])):
        ok, total = modules[m]
        status = "ALL PASS" if ok == total else "HAS FAIL"
        print("  %-28s %d/%d %s" % (m, ok, total, status))
    print()
    if failures:
        print("=" * 100)
        print("失败清单 (%d)" % len(failures))
        print("=" * 100)
        for mod, name, extra, msg in failures:
            print("  [%s] %s" % (mod, name))
            if extra:
                print("      %s" % extra)
            print("      resp: %s" % msg)
    else:
        print("无失败项")
    print("=" * 100)

def main():
    admin_login()
    log("开始回归测试...")
    mid = test_merchant()
    if mid:
        test_quota(mid)
        test_device(mid)
        test_qrcode(mid)
        test_ai_corpus(mid)
        test_ai_config(mid)
        test_coupon(mid)
        test_promotion(mid)
    test_plan()
    if mid:
        test_order(mid)
    test_user()
    test_admin()
    test_role_permission()
    test_system()
    if mid:
        test_statistics(mid)
        test_user_device(mid)
        test_miniapp(mid)
    test_upload()
    if mid:
        test_merchant_delete(mid)
    db_cleanup()
    summary()

if __name__ == "__main__":
    main()
