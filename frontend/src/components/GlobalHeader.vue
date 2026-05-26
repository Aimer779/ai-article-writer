<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <!-- Left: Logo -->
      <div class="logo">
        <router-link to="/">
          AI Article Writer
        </router-link>
      </div>

      <!-- Center: Navigation -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        class="nav-menu"
        @click="handleMenuClick"
      />

      <!-- Right: User Actions -->
      <div class="user-actions">
        <a-space v-if="!loginUserStore.isLoggedIn">
          <a-button type="link" @click="router.push('/user/login')">Login</a-button>
          <a-button type="primary" @click="router.push('/user/register')">Register</a-button>
        </a-space>
        <a-dropdown v-else>
          <a-button class="user-btn">
            <UserOutlined />
            {{ displayName }}
            <DownOutlined />
          </a-button>
          <template #overlay>
            <a-menu @click="handleUserMenuClick">
              <a-menu-item key="logout">
                <LogoutOutlined />
                Logout
              </a-menu-item>
            </a-menu>
          </template>
        </a-dropdown>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed, watch, h } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message, type MenuProps } from 'ant-design-vue'
import {
  HomeOutlined,
  EditOutlined,
  UnorderedListOutlined,
  SettingOutlined,
  UserOutlined,
  DownOutlined,
  LogoutOutlined,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const selectedKeys = ref<string[]>([route.path])

const menuItems = computed<MenuProps['items']>(() => {
  const items: MenuProps['items'] = [
    {
      key: '/',
      label: 'Home',
      icon: () => h(HomeOutlined),
    },
  ]

  if (loginUserStore.isLoggedIn) {
    items.push(
      {
        key: '/create',
        label: 'Create',
        icon: () => h(EditOutlined),
      },
      {
        key: '/articles',
        label: 'My Articles',
        icon: () => h(UnorderedListOutlined),
      },
    )
  }

  if (loginUserStore.isAdmin) {
    items.push({
      key: '/admin/userManage',
      label: 'Management',
      icon: () => h(SettingOutlined),
    })
  }

  return items
})

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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  position: relative;
}

/* Left: Logo */
.logo {
  flex-shrink: 0;
  font-size: 20px;
  font-weight: bold;
}

.logo a {
  color: #1890ff;
  text-decoration: none;
}

.logo a:hover {
  color: #40a9ff;
}

/* Center: Navigation */
.nav-menu {
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  border-bottom: none;
  background: transparent;
}

.nav-menu :deep(.ant-menu-item) {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  line-height: 64px;
}

.nav-menu :deep(.ant-menu-item .anticon) {
  font-size: 16px;
  margin-right: 0;
}

/* Right: User Actions */
.user-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
}

.user-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
</style>
