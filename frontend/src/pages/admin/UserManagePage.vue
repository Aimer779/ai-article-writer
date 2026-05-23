<template>
  <div class="user-manage-page">
    <a-card title="User Management">
      <a-form class="search-form" layout="inline" :model="searchParams" @finish="loadData">
        <a-form-item label="Account" name="userAccount">
          <a-input
            v-model:value="searchParams.userAccount"
            allow-clear
            placeholder="Search account"
          />
        </a-form-item>
        <a-form-item label="Name" name="userName">
          <a-input
            v-model:value="searchParams.userName"
            allow-clear
            placeholder="Search name"
          />
        </a-form-item>
        <a-form-item label="Role" name="userRole">
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
            <a-tag :color="record.userRole === USER_ROLE.ADMIN ? 'red' : 'blue'">
              {{ USER_ROLE_TEXT[record.userRole as UserRole] || record.userRole }}
            </a-tag>
          </template>
          <template v-if="column.key === 'action'">
            <a-space>
              <a-popconfirm
                title="Are you sure you want to delete this user?"
                @confirm="handleDelete(record)"
              >
                <a-button type="link" size="small" danger>Delete</a-button>
              </a-popconfirm>
            </a-space>
          </template>
        </template>
      </a-table>
    </a-card>
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
  { title: 'Created At', dataIndex: 'createTime', key: 'createTime', width: 220 },
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
    message.success('User deleted successfully')
    loadData()
  } else {
    message.error(response.data.message || 'Delete failed')
  }
}
</script>

<style scoped>
.user-manage-page {
  padding: 24px;
}

.search-form {
  margin-bottom: 16px;
}
</style>
