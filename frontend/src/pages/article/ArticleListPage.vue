<template>
  <div class="list-page">
    <!-- Page Header -->
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">History</h1>
        <p class="page-subtitle">Manage all your created articles</p>
      </div>
      <a-button type="primary" class="create-btn" @click="router.push('/create')">
        <PlusOutlined />
        Create new article
      </a-button>
    </div>

    <div class="surface-card list-card">
      <!-- Search Bar -->
      <div class="search-bar">
        <div class="search-inputs">
          <a-input
            v-model:value="searchParams.topic"
            placeholder="Search by topic"
            allow-clear
            class="search-input"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
          <a-input
            v-model:value="searchParams.mainTitle"
            placeholder="Search by title"
            allow-clear
            class="search-input"
          >
            <template #prefix>
              <SearchOutlined />
            </template>
          </a-input>
          <a-select
            v-model:value="searchParams.status"
            placeholder="All status"
            allow-clear
            class="status-select"
          >
            <a-select-option value="PENDING">Pending</a-select-option>
            <a-select-option value="PROCESSING">Processing</a-select-option>
            <a-select-option value="COMPLETED">Completed</a-select-option>
            <a-select-option value="FAILED">Failed</a-select-option>
          </a-select>
          <a-button type="primary" @click="handleSearch">
            Search
          </a-button>
          <a-button @click="handleReset">
            Reset
          </a-button>
        </div>
        <span class="total-count">Total <strong>{{ pagination.total }}</strong> articles</span>
      </div>

      <!-- Table -->
      <a-table
        :columns="columns"
        :data-source="articleList"
        :pagination="pagination"
        :loading="loading"
        row-key="id"
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'status'">
            <span class="status-badge" :class="`status-${record.status?.toLowerCase()}`">
              <span class="dot" />
              {{ ARTICLE_STATUS_TEXT[record.status] || record.status }}
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="16">
              <button class="action-btn action-view press-scale" @click="viewArticle(record)">
                <EyeOutlined />
                View
              </button>
              <button class="action-btn action-export press-scale" @click="exportArticle(record)">
                <DownloadOutlined />
                Export
              </button>
              <a-popconfirm
                title="Delete this article?"
                ok-text="Delete"
                cancel-text="Cancel"
                @confirm="deleteArticle(record)"
              >
                <button class="action-btn action-delete press-scale">
                  <DeleteOutlined />
                  Delete
                </button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  SearchOutlined,
  EyeOutlined,
  DeleteOutlined,
  DownloadOutlined,
  PlusOutlined,
} from '@ant-design/icons-vue'
import { listArticleByPage, deleteArticle as deleteArticleApi, getArticleById } from '@/api/articleController'
import { ARTICLE_STATUS_TEXT } from '@/constants/article'
import { downloadArticleAsMarkdown } from '@/utils/article'
import type { TablePaginationConfig } from 'ant-design-vue'
import type { TableColumnsType } from 'ant-design-vue'

const router = useRouter()

// ==================== State ====================
const loading = ref(false)
const articleList = ref<API.ArticleVO[]>([])

const searchParams = reactive<API.ArticleQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const pagination = reactive<TablePaginationConfig>({
  current: 1,
  pageSize: 10,
  total: 0,
  showSizeChanger: true,
})

// ==================== Columns ====================
const columns: TableColumnsType<API.ArticleVO> = [
  {
    title: 'Topic',
    dataIndex: 'topic',
    key: 'topic',
    ellipsis: true,
    width: 200,
  },
  {
    title: 'Title',
    dataIndex: 'mainTitle',
    key: 'mainTitle',
    ellipsis: true,
    width: 280,
  },
  {
    title: 'Status',
    dataIndex: 'status',
    key: 'status',
    width: 120,
  },
  {
    title: 'Created',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: 'Completed',
    dataIndex: 'completedTime',
    key: 'completedTime',
    width: 180,
  },
  {
    title: 'Action',
    key: 'action',
    width: 240,
    fixed: 'right',
  },
]

// ==================== Methods ====================
async function loadData() {
  loading.value = true
  try {
    const res = await listArticleByPage({
      ...searchParams,
      pageNum: pagination.current,
      pageSize: pagination.pageSize,
    })
    if (res.data.code === 0 && res.data.data) {
      const data = res.data.data
      articleList.value = data.records || []
      pagination.total = data.totalRow || 0
    } else {
      message.error(res.data.message || 'Failed to load articles')
    }
  } catch (err: any) {
    message.error(err.message || 'Network error')
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  pagination.current = 1
  loadData()
}

function handleReset() {
  searchParams.topic = undefined
  searchParams.mainTitle = undefined
  searchParams.status = undefined
  pagination.current = 1
  loadData()
}

function handleTableChange(pag: TablePaginationConfig) {
  pagination.current = pag.current ?? 1
  pagination.pageSize = pag.pageSize ?? 10
  loadData()
}

function viewArticle(record: API.ArticleVO) {
  if (record.id) {
    router.push(`/article/${record.id}`)
  } else if (record.taskId) {
    router.push(`/article/${record.taskId}`)
  }
}

async function exportArticle(record: API.ArticleVO) {
  if (!record.id) return
  try {
    let fullContent = record.fullContent
    if (!fullContent) {
      const res = await getArticleById({ id: record.id })
      if (res.data.code === 0 && res.data.data) {
        fullContent = res.data.data.fullContent
      } else {
        message.error(res.data.message || 'Failed to load article content')
        return
      }
    }
    if (!fullContent) {
      message.warning('Article content is empty')
      return
    }
    downloadArticleAsMarkdown({ ...record, fullContent })
    message.success('Article exported as Markdown')
  } catch (err: any) {
    message.error(err.message || 'Network error')
  }
}

async function deleteArticle(record: API.ArticleVO) {
  if (!record.id) return
  try {
    const res = await deleteArticleApi({ id: record.id })
    if (res.data.code === 0 && res.data.data) {
      message.success('Article deleted')
      loadData()
    } else {
      message.error(res.data.message || 'Failed to delete')
    }
  } catch (err: any) {
    message.error(err.message || 'Network error')
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.list-page {
  max-width: 1280px;
  margin: 0 auto;
}

/* Page Header */
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

.create-btn {
  font-weight: 600;
}

/* Card */
.list-card {
  padding: var(--space-4);
}

/* Search Bar */
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
  gap: var(--space-3);
}

.search-inputs {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-wrap: wrap;
}

.search-input {
  width: 200px;
}

.status-select {
  width: 140px;
}

.total-count {
  font-size: 14px;
  color: var(--text-muted);
  white-space: nowrap;
}

.total-count strong {
  color: var(--ink);
  font-weight: 600;
  font-variant-numeric: tabular-nums;
}

/* Status Badge */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.01em;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
}

.dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  display: inline-block;
}

.status-completed {
  background: var(--success-subtle);
  color: var(--success);
}
.status-completed .dot {
  background: var(--success);
}

.status-processing {
  background: var(--accent-subtle);
  color: var(--accent);
}
.status-processing .dot {
  background: var(--accent);
}

.status-pending {
  background: var(--canvas);
  color: var(--text-muted);
  border: 1px solid var(--border);
}
.status-pending .dot {
  background: var(--text-muted);
}

.status-failed {
  background: var(--error-subtle);
  color: var(--error);
}
.status-failed .dot {
  background: var(--error);
}

/* Action Buttons */
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  font-size: 14px;
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-sans);
  font-weight: 500;
  transition: color 0.15s ease;
}

.action-view {
  color: var(--success);
}
.action-view:hover {
  color: oklch(48% 0.15 145);
}

.action-export {
  color: var(--text-secondary);
}
.action-export:hover {
  color: var(--ink);
}

.action-delete {
  color: var(--error);
}
.action-delete:hover {
  color: oklch(45% 0.18 25);
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-3);
  }
}
</style>
