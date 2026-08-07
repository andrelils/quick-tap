<template>
  <div class="dashboard-page">
    <div class="page-header">
      <div class="page-title">数据概览</div>
      <div class="page-desc">实时掌握系统运营数据</div>
      <a-select
        v-if="userStore.isAdmin"
        v-model:value="selectedMerchantId"
        placeholder="选择商家（全部）"
        style="width: 200px; margin-top: 12px"
        allow-clear
        show-search
        :filter-option="filterMerchantOption"
        @change="handleMerchantChange"
      >
        <a-select-option v-for="m in merchantList" :key="m.id" :value="String(m.id)">{{ m.name }}</a-select-option>
      </a-select>
    </div>
    
    <a-row :gutter="[16, 16]" class="stats-cards">
      <a-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card blue">
          <div class="stat-icon">
            <UserOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalUsers || 0 }}</div>
            <div class="stat-label">总用户数</div>
          </div>
          <div class="stat-trend up">
            <ArrowUpOutlined />
            <span>{{ overview.userGrowth || '12%' }}</span>
          </div>
        </div>
      </a-col>
      
      <a-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card green">
          <div class="stat-icon">
            <ShopOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalMerchants || 0 }}</div>
            <div class="stat-label">入驻商家</div>
          </div>
          <div class="stat-trend up">
            <ArrowUpOutlined />
            <span>{{ overview.merchantGrowth || '8%' }}</span>
          </div>
        </div>
      </a-col>
      
      <a-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card orange">
          <div class="stat-icon">
            <QrcodeOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ overview.totalScans || 0 }}</div>
            <div class="stat-label">总扫描次数</div>
          </div>
          <div class="stat-trend up">
            <ArrowUpOutlined />
            <span>{{ overview.scanGrowth || '25%' }}</span>
          </div>
        </div>
      </a-col>
      
      <a-col :xs="12" :sm="12" :md="6" :lg="6">
        <div class="stat-card purple">
          <div class="stat-icon">
            <WalletOutlined />
          </div>
          <div class="stat-content">
            <div class="stat-value">¥{{ overview.monthRevenue || 0 }}</div>
            <div class="stat-label">本月营收</div>
          </div>
          <div class="stat-trend up">
            <ArrowUpOutlined />
            <span>{{ overview.revenueGrowth || '18%' }}</span>
          </div>
        </div>
      </a-col>
    </a-row>
    
    <a-row :gutter="[16, 16]" class="chart-section">
      <a-col :xs="24" :sm="24" :md="16" :lg="16">
        <div class="card-wrapper chart-card">
          <div class="card-header">
            <span class="card-title">数据趋势</span>
            <a-radio-group v-model:value="trendType" size="small">
              <a-radio-button value="user">用户</a-radio-button>
              <a-radio-button value="scan">扫描</a-radio-button>
              <a-radio-button value="revenue">营收</a-radio-button>
            </a-radio-group>
          </div>
          <div class="chart-container">
            <v-chart class="chart" :option="trendChartOption" autoresize />
          </div>
        </div>
      </a-col>
      
      <a-col :xs="24" :sm="24" :md="8" :lg="8">
        <div class="card-wrapper chart-card">
          <div class="card-header">
            <span class="card-title">推广平台分布</span>
          </div>
          <div class="chart-container">
            <v-chart class="chart" :option="platformChartOption" autoresize />
          </div>
        </div>
      </a-col>
    </a-row>
    
    <a-row :gutter="[16, 16]" class="bottom-section">
      <a-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="card-wrapper list-card">
          <div class="card-header">
            <span class="card-title">AI 创作统计</span>
            <a type="link" size="small" @click="$router.push('/ai/generate')">查看更多</a>
          </div>
          <a-table
            :columns="aiColumns"
            :data-source="aiStats"
            :pagination="false"
            size="small"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'type'">
                <a-tag :color="record.color">{{ record.typeName }}</a-tag>
              </template>
              <template v-else-if="column.key === 'status'">
                <a-badge :status="record.status === 'success' ? 'success' : 'processing'" :text="record.statusText" />
              </template>
            </template>
          </a-table>
        </div>
      </a-col>
      
      <a-col :xs="24" :sm="24" :md="12" :lg="12">
        <div class="card-wrapper list-card">
          <div class="card-header">
            <span class="card-title">热门商家 TOP5</span>
            <a type="link" size="small" @click="$router.push('/merchant/list')">查看更多</a>
          </div>
          <div class="merchant-ranking">
            <div 
              v-for="(item, index) in topMerchants" 
              :key="item.id"
              class="rank-item"
            >
              <div class="rank-num" :class="'rank-' + (index + 1)">{{ index + 1 }}</div>
              <div class="merchant-avatar">
                <img :src="item.logo || '/vite.svg'" :alt="item.name" />
              </div>
              <div class="merchant-info">
                <div class="merchant-name">{{ item.name }}</div>
                <div class="merchant-stats">
                  <span>{{ item.scanCount }} 次扫描</span>
                </div>
              </div>
              <div class="merchant-value">
                <span>{{ item.promotionCount }}次推广</span>
              </div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import * as echarts from 'echarts'
import {
  UserOutlined,
  ShopOutlined,
  QrcodeOutlined,
  WalletOutlined,
  ArrowUpOutlined
} from '@ant-design/icons-vue'
import { getOverview, getTrend, getTopMerchants, getAiStats } from '@/api/statistics'
import { getAiConfig } from '@/api/ai'
import { getMerchantList } from '@/api/merchant'
import { getPlatformList } from '@/api/marketing'
import { useAppStore } from '@/store/app'
import { useUserStore } from '@/store/user'

const trendType = ref('user')

const appStore = useAppStore()
const userStore = useUserStore()

const loading = ref(false)

const selectedMerchantId = ref(userStore.currentMerchantId || undefined)
const merchantList = ref([])

const filterMerchantOption = (input, option) => {
  return option.children?.[0]?.children?.toLowerCase().includes(input.toLowerCase())
}

const handleMerchantChange = (value) => {
  if (value) {
    const merchant = merchantList.value.find(m => String(m.id) === String(value))
    appStore.setCurrentMerchant(merchant || null)
    userStore.setCurrentMerchantId(value)
  } else {
    appStore.setCurrentMerchant(null)
    userStore.setCurrentMerchantId('')
  }
  selectedMerchantId.value = value
  loadAllData()
}

const loadMerchantList = async () => {
  try {
    const res = await getMerchantList({ page: 1, pageSize: 100 })
    merchantList.value = res.list || []
    const persistedId = userStore.currentMerchantId
    if (persistedId && !appStore.currentMerchant) {
      const merchant = merchantList.value.find(m => String(m.id) === String(persistedId))
      if (merchant) {
        appStore.setCurrentMerchant(merchant)
      }
    }
  } catch (e) { console.error(e) }
}

const getMerchantIdParam = () => {
  const merchantId = appStore.merchantId || userStore.currentMerchantId
  return merchantId ? { merchantId } : {}
}

const overview = reactive({
  totalUsers: 0,
  totalMerchants: 0,
  totalScans: 0,
  monthRevenue: 0,
  userGrowth: '0%',
  merchantGrowth: '0%',
  scanGrowth: '0%',
  revenueGrowth: '0%'
})

const aiColumns = [
  { title: '类型', dataIndex: 'type', key: 'type' },
  { title: '今日', dataIndex: 'today', key: 'today' },
  { title: '本周', dataIndex: 'week', key: 'week' },
  { title: '本月', dataIndex: 'month', key: 'month' },
  { title: '状态', dataIndex: 'status', key: 'status' }
]

const aiStats = ref([])

const topMerchants = ref([])

const trendDataMap = ref({
  user: [],
  scan: [],
  revenue: []
})

const trendCategories = ref([])

const platformData = ref([])

const loadOverview = async () => {
  try {
    const res = await getOverview(getMerchantIdParam())
    const data = res || {}
    Object.assign(overview, {
      totalUsers: data.totalUsers ?? 0,
      totalMerchants: data.totalMerchants ?? 0,
      totalScans: data.totalScans ?? 0,
      monthRevenue: data.monthRevenue ?? 0,
      userGrowth: data.userGrowth ?? '0%',
      merchantGrowth: data.merchantGrowth ?? '0%',
      scanGrowth: data.scanGrowth ?? '0%',
      revenueGrowth: data.revenueGrowth ?? '0%'
    })
    if (data.platforms && Array.isArray(data.platforms)) {
      platformData.value = data.platforms
    }
  } catch (e) {
    console.error('加载概览数据失败', e)
  }
}

const loadPlatforms = async () => {
  try {
    const res = await getPlatformList({ current: 1, pageSize: 100, ...getMerchantIdParam() })
    const list = res?.list || res || []
    platformData.value = (Array.isArray(list) ? list : [])
      .filter(p => p && p.name)
      .map(p => ({
        name: p.name,
        value: p.count || p.scanCount || p.usageCount || 0
      }))
  } catch (e) {
    console.error('加载平台数据失败', e)
  }
}

const loadTrend = async () => {
  try {
    const params = getMerchantIdParam()
    const [userRes, scanRes, revenueRes] = await Promise.all([
      getTrend({ ...params, type: 'user' }),
      getTrend({ ...params, type: 'scan' }),
      getTrend({ ...params, type: 'revenue' })
    ])
    const data = userRes || {}
    trendCategories.value = data.categories || data.labels || []
    trendDataMap.value = {
      user: userRes?.data || [],
      scan: scanRes?.data || [],
      revenue: revenueRes?.data || []
    }
  } catch (e) {
    console.error('加载趋势数据失败', e)
  }
}

const loadAiStats = async () => {
  try {
    const res = await getAiStats(getMerchantIdParam())
    const data = (res && res.generationsByType) || res || {}
    const typeConfig = [
      { key: 'text', label: '文字', color: 'blue' },
      { key: 'image', label: '图片', color: 'purple' },
      { key: 'video', label: '视频', color: 'gold' }
    ]
    aiStats.value = typeConfig.map(t => {
      const s = data[t.key] || {}
      return {
        type: t.key,
        typeName: t.label,
        color: t.color,
        today: s.today || 0,
        week: s.week || 0,
        month: s.month || 0,
        status: 'success',
        statusText: '正常'
      }
    })
  } catch (e) {
    console.error('加载 AI 统计失败', e)
    aiStats.value = []
  }
}

const loadTopMerchants = async () => {
  try {
    const params = { ...getMerchantIdParam(), limit: 5 }
    const res = await getTopMerchants(params)
    const list = Array.isArray(res) ? res : []
    topMerchants.value = list.map(m => ({
      id: m.id,
      name: m.name || m.shopName,
      logo: m.logo || '',
      scanCount: m.scanCount || 0,
      promotionCount: m.promotionCount || 0
    }))
  } catch (e) {
    console.error('加载热门商家失败', e)
    topMerchants.value = []
  }
}

const trendChartOption = computed(() => {
  const dataMap = {
    user: trendDataMap.value.user || [],
    scan: trendDataMap.value.scan || [],
    revenue: trendDataMap.value.revenue || []
  }
  const titleMap = {
    user: '新增用户',
    scan: '扫描次数',
    revenue: '营收（元）'
  }
  const categories = trendCategories.value.length
    ? trendCategories.value
    : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月']
  
  return {
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: categories,
      axisLine: { lineStyle: { color: '#f0f0f0' } },
      axisLabel: { color: '#8c8c8c' }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#f0f0f0', type: 'dashed' } },
      axisLabel: { color: '#8c8c8c' }
    },
    series: [
      {
        name: titleMap[trendType.value],
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        data: dataMap[trendType.value],
        lineStyle: {
          width: 2,
          color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [
            { offset: 0, color: '#1677ff' },
            { offset: 1, color: '#4096ff' }
          ])
        },
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(22, 119, 255, 0.2)' },
            { offset: 1, color: 'rgba(22, 119, 255, 0)' }
          ])
        },
        itemStyle: { color: '#1677ff' }
      }
    ]
  }
})

const platformChartOption = computed(() => {
  const colors = ['#fe2c55', '#ff2442', '#ffd100', '#ff6600', '#bfbfbf', '#722ed1']
  const data = platformData.value.length
    ? platformData.value
    : [{ value: 0, name: '暂无数据' }]
  
  return {
    tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
    legend: {
      orient: 'horizontal',
      bottom: '0%',
      left: 'center',
      itemWidth: 12,
      itemHeight: 12,
      textStyle: { fontSize: 12, color: '#8c8c8c' }
    },
    series: [
      {
        name: '推广平台',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['50%', '40%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: false } },
        labelLine: { show: false },
        data,
        color: colors
      }
    ]
  }
})

const loadAllData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadOverview(),
      loadTrend(),
      loadAiStats(),
      loadTopMerchants(),
      loadPlatforms()
    ])
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  if (userStore.isAdmin) {
    await loadMerchantList()
  }
  await loadAllData()
})

// 监听全局商家切换，自动重新加载数据
watch(() => appStore.merchantId, () => {
  loadAllData()
})
</script>

<style lang="scss" scoped>
.dashboard-page {
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
  
  .page-title {
    font-size: 24px;
    font-weight: 600;
    color: #1f1f1f;
    margin-bottom: 8px;
  }
  
  .page-desc {
    font-size: 14px;
    color: #8c8c8c;
  }
}

.stats-cards {
  margin-bottom: 24px;
}

.stat-card {
  background: $bg-card;
  border-radius: $border-radius;
  padding: 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  box-shadow: $shadow-sm;
  transition: all 0.3s ease;
  
  &:hover {
    box-shadow: $shadow-md;
    transform: translateY(-2px);
  }
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #fff;
  flex-shrink: 0;
}

.stat-card.blue .stat-icon {
  background: linear-gradient(135deg, #1677ff 0%, #4096ff 100%);
}

.stat-card.green .stat-icon {
  background: linear-gradient(135deg, #52c41a 0%, #73d13d 100%);
}

.stat-card.orange .stat-icon {
  background: linear-gradient(135deg, #faad14 0%, #ffc53d 100%);
}

.stat-card.purple .stat-icon {
  background: linear-gradient(135deg, #722ed1 0%, #9254de 100%);
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f1f1f;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #8c8c8c;
}

.stat-trend {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  
  &.up {
    color: #52c41a;
  }
  
  &.down {
    color: #ff4d4f;
  }
}

.chart-section {
  margin-bottom: 24px;
}

.card-wrapper {
  background: $bg-card;
  border-radius: $border-radius;
  box-shadow: $shadow-sm;
  transition: box-shadow 0.3s;
  
  &:hover {
    box-shadow: $shadow-md;
  }
}

.chart-card {
  padding: 20px 24px;
  height: 360px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: $text-color;
}

.chart-container {
  flex: 1;
  min-height: 0;
}

.chart {
  width: 100%;
  height: 100%;
}

.bottom-section {
  margin-bottom: 24px;
}

.list-card {
  padding: 20px 24px;
}

.merchant-ranking {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f5f5f5;
  
  &:last-child {
    border-bottom: none;
    padding-bottom: 0;
  }
}

.rank-num {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  background: #f5f5f5;
  color: #8c8c8c;
  flex-shrink: 0;
}

.rank-1 {
  background: #ff4d4f;
  color: #fff;
}

.rank-2 {
  background: #faad14;
  color: #fff;
}

.rank-3 {
  background: #fa8c16;
  color: #fff;
}

.merchant-avatar {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  background: #f5f5f5;
  flex-shrink: 0;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.merchant-info {
  flex: 1;
  min-width: 0;
}

.merchant-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f1f1f;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.merchant-stats {
  font-size: 12px;
  color: #8c8c8c;
}

.merchant-value {
  font-size: 13px;
  color: #1677ff;
  font-weight: 500;
  flex-shrink: 0;
}
</style>
