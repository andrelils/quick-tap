/**
 * Mock 数据拦截器
 * 当后端不可用或不在 request 合法域名列表中时，
 * 自动 fallback 到 mock 数据，避免阻塞小程序开发
 */

// ============== Mock 商家数据 ==============
const MOCK_MERCHANT = {
  id: 1,
  name: '碰一碰演示商家',
  logo: '',
  bannerImages: [
    '/static/banner/banner1.jpg',
    '/static/banner/banner2.jpg',
    '/static/banner/banner3.jpg'
  ],
  address: '北京市朝阳区建国路88号',
  description: '这是一个演示商家页面，展示NFC智能推广系统的完整功能',
  bossWechat: 'demo_boss_wechat',
  businessHours: '09:00-22:00',
  wifiName: 'DemoShop-WiFi',
  wifiPassword: 'demo1234',
  contactPhone: '400-888-8888',
  shopImages: [
    '/static/banner/banner1.jpg',
    '/static/banner/banner2.jpg',
    '/static/banner/banner3.jpg'
  ]
}

const MOCK_COUPONS = [
  {
    id: 101,
    title: '新客立减券',
    name: '新客立减券',
    couponName: '新客立减券',
    amount: 20,
    discountValue: 20,
    value: 20,
    minAmount: 100,
    startTime: '2026-07-01',
    endTime: '2026-12-31',
    validEndTime: '2026-12-31',
    totalCount: 200,
    issuedCount: 50,
    thirdPartyUrl: 'https://www.meituan.com'
  },
  {
    id: 102,
    title: '满减优惠',
    name: '满减优惠',
    couponName: '满减优惠',
    amount: 50,
    discountValue: 50,
    value: 50,
    minAmount: 200,
    startTime: '2026-07-01',
    endTime: '2026-12-31',
    validEndTime: '2026-12-31',
    totalCount: 100,
    issuedCount: 30,
    thirdPartyUrl: 'https://www.dianping.com'
  }
]

const MOCK_PLATFORMS = [
  { id: 1, name: '美团', color: '#ffc107', description: '点评分享', jumpMode: 'webview', webUrl: 'https://www.meituan.com', schemeUrl: 'imeituan://', fallbackUrl: 'https://www.meituan.com' },
  { id: 2, name: '大众点评', color: '#ff4d4f', description: '评价商家', jumpMode: 'webview', webUrl: 'https://www.dianping.com', schemeUrl: 'dianping://', fallbackUrl: 'https://www.dianping.com' },
  { id: 3, name: '抖音', color: '#000000', description: '视频推广', jumpMode: 'webview', webUrl: 'https://www.douyin.com', schemeUrl: 'snssdk1128://', fallbackUrl: 'https://www.douyin.com' }
]

const MOCK_WIFI = {
  ssid: 'DemoShop-WiFi',
  password: 'demo1234',
  encryption: 'WPA2'
}

const MOCK_REFERRERS = [
  { id: 1, nickname: '系统管理员', avatar: '', userCode: 'ADMIN001', role: 'super_admin' },
  { id: 2, nickname: '客服小助手', avatar: '', userCode: 'CS002', role: 'admin' }
]

/**
 * 解析 URL，匹配 mock 路由
 */
function resolveMock(url, method, data = {}) {
  const u = url.split('?')[0]

  // 商家相关
  if (u === '/merchant/check-bind') {
    const codeStr = data?.code || ''
    if (!codeStr || codeStr.startsWith('unbound_')) {
      return { bound: false, merchantId: null }
    }
    return { bound: true, merchantId: 1, merchant: MOCK_MERCHANT }
  }
  if (u.startsWith('/merchant/info/')) {
    return MOCK_MERCHANT
  }
  if (u === '/merchant/promotion' || u === '/promotion/platforms') {
    return MOCK_PLATFORMS
  }
  if (u === '/merchant/wifi') {
    return MOCK_WIFI
  }
  if (u === '/merchant/register' && method === 'POST') {
    return { merchantId: 999, username: data?.username, message: '注册成功(Mock)' }
  }

  // 推广平台
  if (u.startsWith('/promotion/platform/')) {
    const id = Number(u.split('/').pop())
    return MOCK_PLATFORMS.find(p => p.id === id) || MOCK_PLATFORMS[0]
  }

  // 优惠券
  if (u === '/coupon/list') {
    return MOCK_COUPONS
  }
  if (u === '/coupon/my') {
    return [
      { id: 201, couponId: 101, couponCode: 'USED001', status: 1, createTime: '2026-07-01 10:00' },
      { id: 202, couponId: 102, couponCode: 'UNUSED001', status: 0, createTime: '2026-07-20 12:00' }
    ]
  }
  if (u === '/coupon/claim' && method === 'POST') {
    return { success: true, couponCode: 'CLAIM' + Date.now() }
  }

  // 用户相关
  if (u === '/user/referrer/list') {
    return MOCK_REFERRERS
  }
  if (u === '/user/auth/wechat-mini' && method === 'POST') {
    return {
      token: 'mock_token_' + Date.now(),
      userInfo: {
        id: 1,
        nickname: '微信用户',
        avatar: '',
        phone: '',
        userCode: 'MOCK' + Date.now()
      }
    }
  }
  if (u === '/user/info') {
    return { id: 1, nickname: '微信用户', avatar: '', phone: '', userCode: 'MOCK001' }
  }
  if (u === '/user/send-sms' && method === 'POST') {
    return { success: true }
  }
  if (u === '/user/register-bind' && method === 'POST') {
    return {
      success: true,
      userInfo: { id: 1, nickname: '微信用户', avatar: '', phone: data?.phone || '', userCode: 'MOCK' + Date.now() }
    }
  }

  // 推广日志
  if (u === '/promotion/log' && method === 'POST') {
    return { success: true }
  }

  return null
}

export function tryMockResponse(url, method, data) {
  const result = resolveMock(url, method, data)
  if (result !== null) {
    return Promise.resolve(result)
  }
  return null
}

export const MOCK = {
  merchant: MOCK_MERCHANT,
  coupons: MOCK_COUPONS,
  platforms: MOCK_PLATFORMS,
  wifi: MOCK_WIFI,
  referrers: MOCK_REFERRERS
}

export default tryMockResponse
