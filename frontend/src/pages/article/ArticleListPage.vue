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
        Create New Article
      </a-button>
    </div>

    <a-card :bordered="false" class="list-card">
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
            placeholder="All Status"
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
            <span class="status-dot" :class="`status-${record.status?.toLowerCase()}`">
              <span class="dot" />
              {{ ARTICLE_STATUS_TEXT[record.status] || record.status }}
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space :size="16">
              <a-button type="link" class="action-btn action-view" @click="viewArticle(record)">
                <EyeOutlined />
                View
              </a-button>
              <a-button type="link" class="action-btn action-export" @click="exportArticle(record)">
                <DownloadOutlined />
                Export
              </a-button>
              <a-popconfirm
                title="Are you sure to delete this article?"
                ok-text="Yes"
                cancel-text="No"
                @confirm="deleteArticle(record)"
              >
                <a-button type="link" class="action-btn action-delete">
                  <DeleteOutlined />
                  Delete
                </a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
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
    title: 'Created At',
    dataIndex: 'createTime',
    key: 'createTime',
    width: 180,
  },
  {
    title: 'Completed At',
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
    downloadArticleAsMarkdown(record.mainTitle, fullContent)
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
  padding: 24px;
}

/* Page Header */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-title {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
  color: #1f1f1f;
}

.page-subtitle {
  font-size: 14px;
  color: #888;
  margin: 0;
}

.create-btn {
  background: #22c55e;
  border-color: #22c55e;
  border-radius: 6px;
  font-weight: 500;
}

.create-btn:hover {
  background: #16a34a;
  border-color: #16a34a;
}

/* Card */
.list-card {
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

/* Search Bar */
.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.search-inputs {
  display: flex;
  align-items: center;
  gap: 12px;
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
  color: #888;
  white-space: nowrap;
}

.total-count strong {
  color: #262626;
  font-weight: 600;
}

/* Status Dot */
.status-dot {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  display: inline-block;
}

.status-completed {
  color: #22c55e;
}
.status-completed .dot {
  background: #22c55e;
}

.status-processing {
  color: #3b82f6;
}
.status-processing .dot {
  background: #3b82f6;
}

.status-pending {
  color: #888;
}
.status-pending .dot {
  background: #888;
}

.status-failed {
  color: #ef4444;
}
.status-failed .dot {
  background: #ef4444;
}

/* Action Buttons */
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  font-size: 14px;
}

.action-view {
  color: #22c55e;
}
.action-view:hover {
  color: #16a34a;
}

.action-export {
  color: #595959;
}
.action-export:hover {
  color: #262626;
}

.action-delete {
  color: #ef4444;
}
.action-delete:hover {
  color: #dc2626;
}
</style>
