<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <div class="logo">
        <router-link to="/">
          AI Article Writer
        </router-link>
      </div>

      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        @click="handleMenuClick"
      />

      <div class="user-actions">
        <a-space v-if="!loginUserStore.isLoggedIn">
          <a-button type="link" @click="router.push('/user/login')">Login</a-button>
          <a-button type="primary" @click="router.push('/user/register')">Register</a-button>
        </a-space>
        <a-dropdown v-else>
          <a-button>
            {{ displayName }}
          </a-button>
          <template #overlay>
            <a-menu @click="handleUserMenuClick">
              <a-menu-item key="logout">Logout</a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, type MenuProps } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const selectedKeys = ref<string[]>([route.path])

const menuItems = computed<MenuProps['items']>(() => [
  {
    key: '/',
    label: 'Home',
  },
  ...(loginUserStore.isAdmin
    ? [
        {
          key: '/admin/userManage',
          label: 'User Management',
        },
      ]
    : []),
])

const displayName = computed(() => {
  return loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || 'User'
})

watch(
  () => route.path,
  (path) => {
    selectedKeys.value = [path]
  },
)

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push(String(key))
}

const handleUserMenuClick: MenuProps['onClick'] = async ({ key }) => {
  if (key === 'logout') {
    const response = await loginUserStore.logout()
    if (response.data.code === 0) {
      message.success('Logged out')
      router.push('/user/login')
    } else {
      message.error(response.data.message || 'Logout failed')
    }
  }
}
</script>

<style scoped>
.global-header {
  display: flex;
  align-items: center;
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  padding: 0 24px;
}

.header-content {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

.logo {
  font-size: 20px;
  font-weight: bold;
  margin-right: 48px;
}

.logo a {
  color: #1890ff;
  text-decoration: none;
}

.logo a:hover {
  color: #40a9ff;
}

:deep(.ant-menu) {
  flex: 1;
  border: none;
}

.user-actions {
  display: flex;
  align-items: center;
  margin-left: 24px;
}
</style>
