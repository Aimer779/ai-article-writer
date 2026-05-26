<template>
  <div class="data-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Data Analysis</h1>
        <p class="page-subtitle">System operation overview</p>
      </div>
      <a-button class="refresh-btn" @click="handleRefresh">
        <ReloadOutlined />
        Refresh Data
      </a-button>
    </div>

    <!-- Stat Cards -->
    <a-row :gutter="16" class="stat-row">
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #dcfce7; color: #22c55e;">
            <EditOutlined />
          </div>
          <div class="stat-body">
            <div class="stat-label">Today's Creation</div>
            <div class="stat-value">{{ stats.today }}</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #dbeafe; color: #3b82f6;">
            <BarChartOutlined />
          </div>
          <div class="stat-body">
            <div class="stat-label">Weekly Creation</div>
            <div class="stat-value">{{ stats.week }}</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #f3e8ff; color: #a855f7;">
            <RiseOutlined />
          </div>
          <div class="stat-body">
            <div class="stat-label">Monthly Creation</div>
            <div class="stat-value">{{ stats.month }}</div>
          </div>
        </div>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <div class="stat-card">
          <div class="stat-icon" style="background: #fef9c3; color: #eab308;">
            <CheckCircleOutlined />
          </div>
          <div class="stat-body">
            <div class="stat-label">Success Rate</div>
            <div class="stat-value">{{ stats.successRate }}%</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <!-- Charts -->
    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <LineChartOutlined style="color: #22c55e;" />
            <span class="chart-title">Creation Trend</span>
          </div>
          <div ref="trendChartRef" class="chart-body" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <ThunderboltOutlined style="color: #22c55e;" />
            <span class="chart-title">Performance Stats</span>
          </div>
          <div class="performance-body">
            <div class="performance-item">
              <div class="performance-label">Average Duration</div>
              <div class="performance-value">{{ stats.avgDuration }}s</div>
            </div>
            <div class="performance-divider" />
            <div class="performance-item">
              <div class="performance-label">Total Creation</div>
              <div class="performance-value">{{ stats.total }}</div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="16" class="chart-row">
      <a-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <TeamOutlined style="color: #22c55e;" />
            <span class="chart-title">User Analysis</span>
          </div>
          <div ref="userChartRef" class="chart-body" />
        </div>
      </a-col>
      <a-col :xs="24" :lg="12">
        <div class="chart-card">
          <div class="chart-header">
            <PieChartOutlined style="color: #22c55e;" />
            <span class="chart-title">Quota Usage</span>
          </div>
          <div ref="quotaChartRef" class="chart-body" />
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  ReloadOutlined,
  EditOutlined,
  BarChartOutlined,
  RiseOutlined,
  CheckCircleOutlined,
  LineChartOutlined,
  ThunderboltOutlined,
  TeamOutlined,
  PieChartOutlined,
} from '@ant-design/icons-vue'
import * as echarts from 'echarts'

const trendChartRef = ref<HTMLDivElement | null>(null)
const userChartRef = ref<HTMLDivElement | null>(null)
const quotaChartRef = ref<HTMLDivElement | null>(null)

let trendChart: echarts.ECharts | null = null
let userChart: echarts.ECharts | null = null
let quotaChart: echarts.ECharts | null = null

const stats = ref({
  today: 11,
  week: 18,
  month: 74,
  total: 74,
  successRate: 52.7,
  avgDuration: 100.7,
})

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['Today', 'Weekly', 'Monthly', 'Total'],
      axisLine: { lineStyle: { color: '#e2e8f0' } },
      axisLabel: { color: '#64748b' },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: '#f1f5f9' } },
      axisLabel: { color: '#64748b' },
    },
    series: [
      {
        data: [stats.value.today, stats.value.week, stats.value.month, stats.value.total],
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#86efac' },
            { offset: 1, color: '#22c55e' },
          ]),
          borderRadius: [4, 4, 0, 0],
        },
      },
    ],
  })
}

function initUserChart() {
  if (!userChartRef.value) return
  userChart = echarts.init(userChartRef.value)
  userChart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: { color: '#475569' },
    },
    series: [
      {
        name: 'User Type',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: false } },
        data: [
          { value: 10, name: 'VIP Member', itemStyle: { color: '#86efac' } },
          { value: 20, name: 'Active User', itemStyle: { color: '#60a5fa' } },
          { value: 44, name: 'Other User', itemStyle: { color: '#94a3b8' } },
        ],
      },
    ],
  })
}

function initQuotaChart() {
  if (!quotaChartRef.value) return
  quotaChart = echarts.init(quotaChartRef.value)
  quotaChart.setOption({
    tooltip: { trigger: 'item' },
    series: [
      {
        name: 'Quota',
        type: 'pie',
        radius: '65%',
        center: ['45%', '50%'],
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: true, position: 'outside', color: '#475569' },
        labelLine: { lineStyle: { color: '#cbd5e1' } },
        data: [
          { value: 0, name: 'Used', itemStyle: { color: '#ef4444' } },
          { value: 30, name: 'Remaining', itemStyle: { color: '#22c55e' } },
        ],
      },
    ],
  })
}

function handleResize() {
  trendChart?.resize()
  userChart?.resize()
  quotaChart?.resize()
}

async function handleRefresh() {
  message.success('Data refreshed')
}

onMounted(async () => {
  await nextTick()
  initTrendChart()
  initUserChart()
  initQuotaChart()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  userChart?.dispose()
  quotaChart?.dispose()
})
</script>

<style scoped>
.data-page {
  padding: 24px;
  background: #f8fafc;
  min-height: calc(100vh - 64px);
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  margin: 0 0 4px;
}

.page-subtitle {
  font-size: 14px;
  color: #64748b;
  margin: 0;
}

.refresh-btn {
  border-radius: 8px;
  height: 40px;
  font-size: 14px;
}

/* Stat Cards */
.stat-row {
  margin-bottom: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
}

.stat-label {
  font-size: 14px;
  color: #64748b;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: #0f172a;
  line-height: 1.2;
}

/* Chart Cards */
.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  margin-bottom: 16px;
}

.chart-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 15px;
  font-weight: 600;
  color: #1f1f1f;
}

.chart-body {
  width: 100%;
  height: 280px;
}

/* Performance Stats */
.performance-body {
  display: flex;
  flex-direction: column;
  justify-content: center;
  height: 280px;
  padding: 0 16px;
}

.performance-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px 0;
}

.performance-label {
  font-size: 14px;
  color: #475569;
}

.performance-value {
  font-size: 24px;
  font-weight: 700;
  color: #22c55e;
}

.performance-divider {
  height: 1px;
  background: #f1f5f9;
}

@media (max-width: 992px) {
  .page-header {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
