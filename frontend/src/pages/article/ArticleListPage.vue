<template>
  <div class="list-page">
    <a-card title="My Articles" :bordered="false" class="list-card">
      <!-- Search Form -->
      <a-form layout="inline" class="search-form" @submit.prevent="handleSearch">
        <a-form-item label="Topic">
          <a-input
            v-model:value="searchParams.topic"
            placeholder="Search by topic"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="Title">
          <a-input
            v-model:value="searchParams.mainTitle"
            placeholder="Search by title"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="Status">
          <a-select
            v-model:value="searchParams.status"
            placeholder="All Status"
            allow-clear
            style="width: 140px"
          >
            <a-select-option value="PENDING">Pending</a-select-option>
            <a-select-option value="PROCESSING">Processing</a-select-option>
            <a-select-option value="COMPLETED">Completed</a-select-option>
            <a-select-option value="FAILED">Failed</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item>
          <a-button type="primary" html-type="submit">
            <SearchOutlined />
            Search
          </a-button>
          <a-button style="margin-left: 8px" @click="handleReset">
            Reset
          </a-button>
        </a-form-item>
      </a-form>

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
            <a-tag :color="ARTICLE_STATUS_COLOR[record.status] || 'default'">
              {{ ARTICLE_STATUS_TEXT[record.status] || record.status }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="viewArticle(record)">
                <EyeOutlined />
                View
              </a-button>
              <a-popconfirm
                title="Are you sure to delete this article?"
                ok-text="Yes"
                cancel-text="No"
                @confirm="deleteArticle(record)"
              >
                <a-button type="link" danger size="small">
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
import { SearchOutlined, EyeOutlined, DeleteOutlined } from '@ant-design/icons-vue'
import { listArticleByPage, deleteArticle as deleteArticleApi } from '@/api/articleController'
import { ARTICLE_STATUS_TEXT, ARTICLE_STATUS_COLOR } from '@/constants/article'
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
  showTotal: (total: number) => `Total ${total} articles`,
})

// ==================== Columns ====================
const columns: TableColumnsType<API.ArticleVO> = [
  {
    title: 'ID',
    dataIndex: 'id',
    key: 'id',
    width: 80,
  },
  {
    title: 'Topic',
    dataIndex: 'topic',
    key: 'topic',
    ellipsis: true,
  },
  {
    title: 'Title',
    dataIndex: 'mainTitle',
    key: 'mainTitle',
    ellipsis: true,
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
    width: 160,
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

.list-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.search-form {
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.search-form .ant-form-item {
  margin-bottom: 16px;
}
</style>
