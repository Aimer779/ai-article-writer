<template>
  <div class="agent-log-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">Agent logs</h1>
        <p class="page-subtitle">Inspect execution history and failures</p>
      </div>
    </div>

    <div class="surface-card table-card">
      <a-form class="search-form" layout="inline" :model="searchParams" @finish="handleSearch">
        <a-form-item name="timeRange">
          <a-range-picker
            v-model:value="timeRange"
            show-time
            allow-clear
            format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px"
          />
        </a-form-item>
        <a-form-item name="status">
          <a-select
            v-model:value="searchParams.status"
            allow-clear
            placeholder="Status"
            style="width: 140px"
          >
            <a-select-option value="SUCCESS">Success</a-select-option>
            <a-select-option value="FAILED">Failed</a-select-option>
            <a-select-option value="RUNNING">Running</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item name="agentName">
          <a-select
            v-model:value="searchParams.agentName"
            allow-clear
            show-search
            option-filter-prop="label"
            placeholder="Agent name"
            style="width: 220px"
            :options="agentOptions"
          />
        </a-form-item>
        <a-form-item name="taskId">
          <a-input
            v-model:value="searchParams.taskId"
            allow-clear
            placeholder="Task ID"
            style="width: 240px"
          />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">Search</a-button>
            <a-button @click="handleReset">Reset</a-button>
          </a-space>
        </a-form-item>
      </a-form>

      <a-table
        row-key="id"
        :columns="columns"
        :data-source="data"
        :loading="loading"
        :pagination="pagination"
        :scroll="{ x: 1280 }"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <span class="status-badge" :class="normalizeStatus(record.status).toLowerCase()">
              {{ statusText(record.status) }}
            </span>
          </template>
          <template v-else-if="column.key === 'durationMs'">
            <span class="text-tabular">{{ formatDuration(record.durationMs) }}</span>
          </template>
          <template v-else-if="column.key === 'startTime'">
            <span class="text-tabular">{{ formatTime(record.startTime) }}</span>
          </template>
          <template v-else-if="column.key === 'endTime'">
            <span class="text-tabular">{{ formatTime(record.endTime) }}</span>
          </template>
          <template v-else-if="column.key === 'createTime'">
            <span class="text-tabular">{{ formatTime(record.createTime) }}</span>
          </template>
          <template v-else-if="column.key === 'taskId'">
            <span class="task-id">{{ record.taskId || '-' }}</span>
          </template>
          <template v-else-if="column.key === 'errorMessage'">
            <a-tooltip v-if="record.errorMessage" :title="record.errorMessage">
              <span class="error-text">{{ record.errorMessage }}</span>
            </a-tooltip>
            <span v-else class="muted-text">-</span>
          </template>
          <template v-else-if="column.key === 'action'">
            <a-button type="link" class="detail-link" @click="openDetail(record)">Details</a-button>
          </template>
        </template>
      </a-table>
    </div>

    <a-modal
      v-model:open="detailOpen"
      title="Execution log details"
      width="860px"
      :footer="null"
    >
      <div v-if="selectedLog" class="detail-grid">
        <div class="detail-row">
          <span class="detail-label">Task ID</span>
          <span class="detail-value">{{ selectedLog.taskId || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Agent</span>
          <span class="detail-value">{{ selectedLog.agentName || '-' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Status</span>
          <span class="detail-value">{{ statusText(selectedLog.status) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">Prompt</span>
          <span class="detail-value">{{ selectedLog.prompt || '-' }}</span>
        </div>
        <div class="detail-block">
          <div class="detail-label">Input</div>
          <pre>{{ formatJson(selectedLog.inputData) }}</pre>
        </div>
        <div class="detail-block">
          <div class="detail-label">Output</div>
          <pre>{{ formatJson(selectedLog.outputData) }}</pre>
        </div>
        <div v-if="selectedLog.errorMessage" class="detail-block">
          <div class="detail-label">Error</div>
          <pre>{{ selectedLog.errorMessage }}</pre>
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { listAgentLogByPage } from '@/api/agentLogController'

type TimeRangeValue = any[] | null

const loading = ref(false)
const data = ref<API.AgentLogVO[]>([])
const total = ref(0)
const timeRange = ref<TimeRangeValue>(null)
const detailOpen = ref(false)
const selectedLog = ref<API.AgentLogVO | null>(null)

const searchParams = reactive<API.AgentLogQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const pagination = computed<TablePaginationConfig>(() => ({
  current: searchParams.pageNum,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value) => `Total ${value} logs`,
}))

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 90 },
  { title: 'Task ID', dataIndex: 'taskId', key: 'taskId', width: 220 },
  { title: 'Agent', dataIndex: 'agentName', key: 'agentName', width: 240 },
  { title: 'Status', dataIndex: 'status', key: 'status', width: 120 },
  { title: 'Duration', dataIndex: 'durationMs', key: 'durationMs', sorter: true, width: 130 },
  { title: 'Started', dataIndex: 'startTime', key: 'startTime', sorter: true, width: 190 },
  { title: 'Ended', dataIndex: 'endTime', key: 'endTime', width: 190 },
  { title: 'Error', dataIndex: 'errorMessage', key: 'errorMessage', ellipsis: true, width: 260 },
  { title: 'Created', dataIndex: 'createTime', key: 'createTime', sorter: true, width: 190 },
  { title: 'Action', key: 'action', fixed: 'right', width: 100 },
]

const agentOptions = [
  { label: 'Generate titles', value: 'agent1_generate_titles' },
  { label: 'Generate outline', value: 'agent2_generate_outline' },
  { label: 'Generate content', value: 'agent3_generate_content' },
  { label: 'Analyze image requirements', value: 'agent4_analyze_image_requirements' },
  { label: 'Generate images', value: 'agent5_generate_images' },
  { label: 'Assemble full content', value: 'agent6_assemble_full_content' },
]

const normalizeStatus = (status?: string) => (status || '').toUpperCase()

const statusText = (status?: string) => {
  const normalized = normalizeStatus(status)
  if (normalized === 'SUCCESS') return 'Success'
  if (normalized === 'FAILED') return 'Failed'
  if (normalized === 'RUNNING') return 'Running'
  return status || '-'
}

const formatTime = (timeStr?: string) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString()
}

const formatDuration = (durationMs?: number) => {
  if (durationMs === undefined || durationMs === null) return '-'
  if (durationMs < 1000) return `${durationMs}ms`
  return `${(durationMs / 1000).toFixed(1)}s`
}

const formatJson = (value?: string) => {
  if (!value) return '-'
  try {
    return JSON.stringify(JSON.parse(value), null, 2)
  } catch {
    return value
  }
}

const toLocalDateTime = (value: any) => {
  if (!value) return undefined
  if (typeof value.format === 'function') {
    return value.format('YYYY-MM-DDTHH:mm:ss')
  }
  return new Date(value).toISOString().slice(0, 19)
}

const applyTimeRange = () => {
  const [start, end] = timeRange.value || []
  searchParams.startTime = toLocalDateTime(start)
  searchParams.endTime = toLocalDateTime(end)
}

const loadData = async () => {
  loading.value = true
  applyTimeRange()
  try {
    const response = await listAgentLogByPage(searchParams)
    const result = response.data
    if (result.code === 0 && result.data) {
      data.value = result.data.records || []
      total.value = result.data.totalRow ?? 0
    } else {
      message.error(result.message || 'Failed to load logs')
    }
  } catch (err: any) {
    message.error(err.response?.data?.message || 'Failed to load logs')
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  searchParams.pageNum = 1
  loadData()
}

const handleReset = () => {
  timeRange.value = null
  searchParams.taskId = undefined
  searchParams.agentName = undefined
  searchParams.status = undefined
  searchParams.startTime = undefined
  searchParams.endTime = undefined
  searchParams.pageNum = 1
  searchParams.pageSize = 10
  searchParams.sortField = 'createTime'
  searchParams.sortOrder = 'descend'
  loadData()
}

const handleTableChange = (
  page: TablePaginationConfig,
  _filters: Record<string, any>,
  sorter: any,
) => {
  searchParams.pageNum = page.current || 1
  searchParams.pageSize = page.pageSize || 10
  if (sorter?.field && sorter?.order) {
    searchParams.sortField = sorter.field
    searchParams.sortOrder = sorter.order
  } else {
    searchParams.sortField = 'createTime'
    searchParams.sortOrder = 'descend'
  }
  loadData()
}

const openDetail = (record: API.AgentLogVO) => {
  selectedLog.value = record
  detailOpen.value = true
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.agent-log-page {
  max-width: 1280px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  margin: 0;
  color: var(--ink);
}

.page-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  margin: 0;
}

.table-card {
  padding: var(--space-4);
}

.search-form {
  margin-bottom: var(--space-4);
  row-gap: var(--space-3);
}

.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.01em;
}

.status-badge.success {
  background: var(--success-subtle);
  color: var(--success);
}

.status-badge.failed {
  background: var(--error-subtle);
  color: var(--error);
}

.status-badge.running {
  background: var(--accent-subtle);
  color: var(--accent);
}

.task-id {
  font-family: var(--font-mono);
  font-size: 12px;
  color: var(--text-secondary);
}

.error-text {
  color: var(--error);
}

.muted-text {
  color: var(--text-muted);
}

.detail-link {
  padding: 0;
  color: var(--accent);
}

.detail-grid {
  display: grid;
  gap: var(--space-3);
}

.detail-row {
  display: grid;
  grid-template-columns: 120px 1fr;
  gap: var(--space-3);
  align-items: baseline;
}

.detail-label {
  font-size: 12px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.01em;
}

.detail-value {
  color: var(--ink);
  overflow-wrap: anywhere;
}

.detail-block pre {
  margin: 8px 0 0;
  max-height: 260px;
  overflow: auto;
  padding: var(--space-3);
  border-radius: var(--radius-md);
  background: var(--code-bg);
  color: var(--ink);
  font-family: var(--font-mono);
  font-size: 12px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .search-form :deep(.ant-picker),
  .search-form :deep(.ant-input),
  .search-form :deep(.ant-select) {
    width: 100% !important;
  }

  .detail-row {
    grid-template-columns: 1fr;
    gap: 4px;
  }
}
</style>
