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
      <nav class="nav-wrapper">
        <router-link
          v-for="item in menuItems"
          :key="item.key"
          :to="item.key"
          :class="['nav-tab', { active: route.path === item.key }]"
        >
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- Right: User Actions -->
      <div class="user-actions">
        <a-space v-if="!loginUserStore.isLoggedIn">
          <a-button type="link" @click="router.push('/user/login')">Login</a-button>
          <a-button type="primary" @click="router.push('/user/register')">Register</a-button>
        </a-space>
        <a-space v-else>
          <router-link
            v-if="!isVip"
            to="/vip"
            class="upgrade-vip-btn"
          >
            <CrownOutlined />
            <span>Upgrade VIP</span>
          </router-link>
          <router-link
            v-else
            to="/vip"
            class="vip-badge"
          >
            <CrownOutlined />
            <span>VIP</span>
          </router-link>
          <a-dropdown>
            <a-button class="user-btn">
              <UserOutlined />
              {{ displayName }}
              <DownOutlined />
            </a-button>
            <template #overlay>
              <a-menu @click="handleUserMenuClick">
                <a-menu-item v-if="isVip" key="vip">
                  <CrownOutlined />
                  VIP Benefits
                </a-menu-item>
                <a-menu-divider v-if="isVip" />
                <a-menu-item key="logout">
                  <LogoutOutlined />
                  Logout
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </a-space>
      </div>
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  HomeOutlined,
  EditOutlined,
  UnorderedListOutlined,
  SettingOutlined,
  BarChartOutlined,
  UserOutlined,
  DownOutlined,
  LogoutOutlined,
  CrownOutlined,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores'
import { USER_ROLE_VIP } from '@/constants/user'

const router = useRouter()
const route = useRoute()
const loginUserStore = useLoginUserStore()

const isVip = computed(() => loginUserStore.loginUser.userRole === USER_ROLE_VIP || loginUserStore.loginUser.vip === true)

const menuItems = computed(() => {
  const items = [
    { key: '/', label: 'Home', icon: HomeOutlined },
  ]

  if (loginUserStore.isLoggedIn) {
    items.push(
      { key: '/create', label: 'Create', icon: EditOutlined },
      { key: '/articles', label: 'History', icon: UnorderedListOutlined },
    )
  }

  if (loginUserStore.isAdmin) {
    items.push(
      { key: '/admin/userManage', label: 'Management', icon: SettingOutlined },
      { key: '/admin/dataAnalysis', label: 'Data', icon: BarChartOutlined },
    )
  }

  return items
})

const displayName = computed(() => {
  return loginUserStore.loginUser.userName || loginUserStore.loginUser.userAccount || 'User'
})

const handleUserMenuClick = async ({ key }: { key: string }) => {
  if (key === 'logout') {
    const response = await loginUserStore.logout()
    if (response.data.code === 0) {
      message.success('Logged out')
      router.push('/user/login')
    } else {
      message.error(response.data.message || 'Logout failed')
    }
  } else if (key === 'vip') {
    router.push('/vip')
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
  width: 100%;
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
.nav-wrapper {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
}

.nav-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 16px;
  line-height: 64px;
  color: rgba(0, 0, 0, 0.65);
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
  border-bottom: 2px solid transparent;
  white-space: nowrap;
}

.nav-tab:hover {
  color: #1890ff;
}

.nav-tab.active {
  color: #1890ff;
  border-bottom-color: #1890ff;
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

.upgrade-vip-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 100%);
  color: #c41d7f;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.3s;
}

.upgrade-vip-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(196, 29, 127, 0.2);
}

.vip-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  color: #874d00;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
}

.vip-badge:hover {
  opacity: 0.9;
}
</style>
