<template>
  <div class="user-manage-page">
    <div class="page-header">
      <div class="header-left">
        <h1 class="page-title">User management</h1>
        <p class="page-subtitle">Manage accounts and roles</p>
      </div>
    </div>

    <div class="surface-card table-card">
      <a-form class="search-form" layout="inline" :model="searchParams" @finish="loadData">
        <a-form-item name="userAccount">
          <a-input
            v-model:value="searchParams.userAccount"
            allow-clear
            placeholder="Search account"
          />
        </a-form-item>
        <a-form-item name="userName">
          <a-input
            v-model:value="searchParams.userName"
            allow-clear
            placeholder="Search name"
          />
        </a-form-item>
        <a-form-item name="userRole">
          <a-select
            v-model:value="searchParams.userRole"
            allow-clear
            placeholder="Select role"
            style="width: 140px"
          >
            <a-select-option :value="USER_ROLE.USER">User</a-select-option>
            <a-select-option :value="USER_ROLE.ADMIN">Admin</a-select-option>
          </a-select>
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
        @change="handleTableChange"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'userRole'">
            <span
              class="role-badge"
              :class="record.userRole === USER_ROLE.ADMIN ? 'admin' : 'user'"
            >
              {{ USER_ROLE_TEXT[record.userRole as UserRole] || record.userRole }}
            </span>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-popconfirm
                title="Delete this user?"
                ok-text="Delete"
                cancel-text="Cancel"
                @confirm="handleDelete(record)"
              >
                <button class="action-delete press-scale">Delete</button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, reactive, ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import type { TablePaginationConfig } from 'ant-design-vue'
import { deleteUser, listUserByPage } from '@/api/userController'
import { USER_ROLE, USER_ROLE_TEXT, type UserRole } from '@/constants/user'

const loading = ref(false)
const data = ref<API.LoginUserVO[]>([])

const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

const total = ref(0)

const pagination = computed<TablePaginationConfig>(() => ({
  current: searchParams.pageNum,
  pageSize: searchParams.pageSize,
  total: total.value,
  showSizeChanger: true,
  showTotal: (value) => `Total ${value} users`,
}))

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id', width: 90 },
  { title: 'Account', dataIndex: 'userAccount', key: 'userAccount' },
  { title: 'Name', dataIndex: 'userName', key: 'userName' },
  { title: 'Role', dataIndex: 'userRole', key: 'userRole', width: 120 },
  { title: 'Created', dataIndex: 'createTime', key: 'createTime', width: 220 },
  { title: 'Action', key: 'action', width: 120 },
]

const loadData = async () => {
  loading.value = true
  try {
    const response = await listUserByPage(searchParams)
    const result = response.data
    if (result.code === 0 && result.data) {
      data.value = result.data.records || []
      total.value = result.data.totalRow ?? 0
    } else {
      message.error(result.message || 'Failed to load users')
    }
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

const handleTableChange = (page: TablePaginationConfig) => {
  searchParams.pageNum = page.current || 1
  searchParams.pageSize = page.pageSize || 10
  loadData()
}

const handleReset = () => {
  searchParams.userAccount = undefined
  searchParams.userName = undefined
  searchParams.userRole = undefined
  searchParams.pageNum = 1
  loadData()
}

const handleDelete = async (record: API.LoginUserVO) => {
  if (!record.id) {
    return
  }
  const response = await deleteUser({ id: record.id })
  if (response.data.code === 0 && response.data.data) {
    message.success('User deleted')
    loadData()
  } else {
    message.error(response.data.message || 'Delete failed')
  }
}
</script>

<style scoped>
.user-manage-page {
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
}

.role-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.01em;
}

.role-badge.admin {
  background: var(--error-subtle);
  color: var(--error);
}

.role-badge.user {
  background: var(--accent-subtle);
  color: var(--accent);
}

.action-delete {
  font-size: 14px;
  font-weight: 500;
  color: var(--error);
  background: none;
  border: none;
  cursor: pointer;
  font-family: var(--font-sans);
  transition: color 0.15s ease;
}

.action-delete:hover {
  color: oklch(45% 0.18 25);
}
</style>
