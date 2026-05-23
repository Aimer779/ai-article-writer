<template>
  <div class="user-manage-page">
    <a-card title="User Management">
      <a-table :columns="columns" :data-source="data" :loading="loading">
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'action'">
            <a-space>
              <a-button type="link" size="small" @click="handleEdit(record)">Edit</a-button>
              <a-popconfirm
                title="Are you sure delete this user?"
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
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'

const loading = ref(false)

const columns = [
  { title: 'ID', dataIndex: 'id', key: 'id' },
  { title: 'Username', dataIndex: 'username', key: 'username' },
  { title: 'Email', dataIndex: 'email', key: 'email' },
  { title: 'Role', dataIndex: 'role', key: 'role' },
  { title: 'Created At', dataIndex: 'createTime', key: 'createTime' },
  { title: 'Action', key: 'action' },
]

const data = ref([])

onMounted(() => {
  // TODO: Fetch user list from API
  data.value = []
})

const handleEdit = (record: any) => {
  console.log('Edit user:', record)
}

const handleDelete = async (record: any) => {
  // TODO: Call delete API
  console.log('Delete user:', record)
  message.success('User deleted successfully')
}
</script>

<style scoped>
.user-manage-page {
  padding: 24px;
}
</style>
