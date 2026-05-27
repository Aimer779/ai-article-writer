<template>
  <div class="data-page">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h1 class="page-title">Data analysis</h1>
        <p class="page-subtitle">System operation overview</p>
      </div>
      <a-button class="refresh-btn" @click="handleRefresh">
        <ReloadOutlined />
        Refresh
      </a-button>
    </div>

    <!-- Stat Cards -->
    <div class="stats-grid">
      <div class="stat-card surface-card">
        <div class="stat-icon" style="background: var(--success-subtle); color: var(--success);">
          <EditOutlined />
        </div>
        <div class="stat-body">
          <div class="stat-label">Today's creation</div>
          <div class="stat-value text-tabular">{{ stats.today }}</div>
        </div>
      </div>
      <div class="stat-card surface-card">
        <div class="stat-icon" style="background: var(--accent-subtle); color: var(--accent);">
          <BarChartOutlined />
        </div>
        <div class="stat-body">
          <div class="stat-label">Weekly creation</div>
          <div class="stat-value text-tabular">{{ stats.week }}</div>
        </div>
      </div>
      <div class="stat-card surface-card">
        <div class="stat-icon" style="background: oklch(96% 0.02 285); color: oklch(55% 0.14 285);">
          <RiseOutlined />
        </div>
        <div class="stat-body">
          <div class="stat-label">Monthly creation</div>
          <div class="stat-value text-tabular">{{ stats.month }}</div>
        </div>
      </div>
      <div class="stat-card surface-card">
        <div class="stat-icon" style="background: var(--warning-subtle); color: oklch(55% 0.1 85);">
          <CheckCircleOutlined />
        </div>
        <div class="stat-body">
          <div class="stat-label">Success rate</div>
          <div class="stat-value text-tabular">{{ stats.successRate }}%</div>
        </div>
      </div>
    </div>

    <!-- Charts -->
    <div class="charts-grid">
      <div class="chart-card surface-card">
        <div class="chart-header">
          <LineChartOutlined style="color: var(--accent);" />
          <span class="chart-title">Creation trend</span>
        </div>
        <div ref="trendChartRef" class="chart-body" />
      </div>
      <div class="chart-card surface-card">
        <div class="chart-header">
          <ThunderboltOutlined style="color: var(--accent);" />
          <span class="chart-title">Performance stats</span>
        </div>
        <div class="performance-body">
          <div class="performance-item">
            <div class="performance-label">Average duration</div>
            <div class="performance-value text-tabular">{{ stats.avgDuration }}s</div>
          </div>
          <div class="performance-divider" />
          <div class="performance-item">
            <div class="performance-label">Total creation</div>
            <div class="performance-value text-tabular">{{ stats.total }}</div>
          </div>
        </div>
      </div>
      <div class="chart-card surface-card">
        <div class="chart-header">
          <TeamOutlined style="color: var(--accent);" />
          <span class="chart-title">User analysis</span>
        </div>
        <div ref="userChartRef" class="chart-body" />
      </div>
      <div class="chart-card surface-card">
        <div class="chart-header">
          <PieChartOutlined style="color: var(--accent);" />
          <span class="chart-title">Quota usage</span>
        </div>
        <div ref="quotaChartRef" class="chart-body" />
      </div>
    </div>
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

// TODO: Replace with real API calls to aggregate article/agent stats
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

// Design system hex equivalents for ECharts (canvas does not support CSS vars)
const PALETTE = {
  accent: '#c2783f',
  accentLight: '#d99a6a',
  success: '#4a9b5e',
  error: '#c44d3a',
  gold: '#d4a03c',
  neutral: '#d4d0cc',
  border: '#d4d0cc',
  textSecondary: '#7a7268',
  surface: '#ffffff',
}

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: ['Today', 'Weekly', 'Monthly', 'Total'],
      axisLine: { lineStyle: { color: PALETTE.border } },
      axisLabel: { color: PALETTE.textSecondary, fontFamily: 'Manrope, sans-serif' },
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: PALETTE.border } },
      axisLabel: { color: PALETTE.textSecondary, fontFamily: 'Manrope, sans-serif' },
    },
    series: [
      {
        data: [stats.value.today, stats.value.week, stats.value.month, stats.value.total],
        type: 'bar',
        barWidth: '40%',
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: PALETTE.accentLight },
            { offset: 1, color: PALETTE.accent },
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
      textStyle: { color: PALETTE.textSecondary, fontFamily: 'Manrope, sans-serif' },
    },
    series: [
      {
        name: 'User type',
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 4, borderColor: PALETTE.surface, borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: false } },
        data: [
          { value: 10, name: 'VIP member', itemStyle: { color: PALETTE.gold } },
          { value: 20, name: 'Active user', itemStyle: { color: PALETTE.accent } },
          { value: 44, name: 'Other user', itemStyle: { color: PALETTE.neutral } },
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
        itemStyle: { borderRadius: 4, borderColor: PALETTE.surface, borderWidth: 2 },
        label: { show: true, position: 'outside', color: PALETTE.textSecondary, fontFamily: 'Manrope, sans-serif' },
        labelLine: { lineStyle: { color: PALETTE.border } },
        data: [
          { value: 0, name: 'Used', itemStyle: { color: PALETTE.error } },
          { value: 30, name: 'Remaining', itemStyle: { color: PALETTE.success } },
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
  // TODO: Fetch real aggregated stats from backend
  const hide = message.success('Data refreshed', 3)

  // Fallback: force close if the auto-dismiss timer fails
  setTimeout(() => {
    hide?.()
  }, 4000)
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
  max-width: 1280px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
  gap: var(--space-3);
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--ink);
  margin: 0 0 4px;
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.refresh-btn {
  font-weight: 500;
}

/* Stat Cards */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-4);
  margin-bottom: var(--space-4);
}

@media (max-width: 1024px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  padding: var(--space-4);
  display: flex;
  align-items: center;
  gap: var(--space-3);
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;
}

.stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 4px;
  text-transform: uppercase;
  letter-spacing: 0.01em;
  font-weight: 500;
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--ink);
  line-height: 1.2;
  letter-spacing: -0.022em;
}

/* Charts */
.charts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-4);
}

@media (max-width: 1024px) {
  .charts-grid {
    grid-template-columns: 1fr;
  }
}

.chart-card {
  padding: var(--space-4);
}

.chart-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: var(--space-3);
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
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
  padding: 0 var(--space-2);
}

.performance-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-4) 0;
}

.performance-label {
  font-size: 14px;
  color: var(--text-secondary);
}

.performance-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--accent);
  letter-spacing: -0.018em;
}

.performance-divider {
  height: 1px;
  background: var(--border);
}
</style>
