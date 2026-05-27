<template>
  <a-layout-header class="global-header">
    <div class="header-inner">
      <!-- Left: Logo -->
      <router-link to="/" class="logo">
        <span class="logo-mark">A</span>
        <span class="logo-text">AI Article Writer</span>
      </router-link>

      <!-- Center: Navigation -->
      <nav class="nav-wrapper">
        <router-link
          v-for="item in menuItems"
          :key="item.key"
          :to="item.key"
          :class="['nav-tab', { active: isActive(item.key) }]"
        >
          <component :is="item.icon" />
          <span>{{ item.label }}</span>
        </router-link>
      </nav>

      <!-- Right: User Actions -->
      <div class="user-actions">
        <template v-if="!loginUserStore.isLoggedIn">
          <a-button
            type="link"
            class="login-btn"
            @click="router.push('/user/login')"
          >
            Log in
          </a-button>
          <a-button
            type="primary"
            class="register-btn"
            @click="router.push('/user/register')"
          >
            Get started
          </a-button>
        </template>
        <template v-else>
          <router-link
            v-if="!isVip"
            to="/vip"
            class="upgrade-badge press-scale"
          >
            <CrownOutlined />
            <span>Upgrade</span>
          </router-link>
          <router-link
            v-else
            to="/vip"
            class="vip-badge press-scale"
          >
            <CrownOutlined />
            <span>VIP</span>
          </router-link>

          <a-dropdown placement="bottomRight">
            <button class="user-menu-btn press-scale">
              <UserOutlined />
              <span class="user-name">{{ displayName }}</span>
              <DownOutlined class="caret" />
            </button>
            <template #overlay>
              <a-menu @click="handleUserMenuClick">
                <a-menu-item v-if="isVip" key="vip">
                  <CrownOutlined />
                  VIP benefits
                </a-menu-item>
                <a-menu-divider v-if="isVip" />
                <a-menu-item key="logout">
                  <LogoutOutlined />
                  Log out
                </a-menu-item>
              </a-menu>
            </template>
          </a-dropdown>
        </template>
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

const isActive = (path: string) => {
  if (path === '/') {
    return route.path === '/'
  }
  return route.path.startsWith(path)
}

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
  position: sticky;
  top: 0;
  z-index: 100;
  height: 60px;
  line-height: 60px;
  background: var(--surface) !important;
  border-bottom: 1px solid var(--border);
  padding: 0;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 var(--space-5);
  height: 100%;
}

@media (max-width: 1024px) {
  .header-inner {
    padding: 0 var(--space-4);
  }
}

/* Logo */
.logo {
  display: inline-flex;
  align-items: center;
  gap: var(--space-2);
  text-decoration: none;
  flex-shrink: 0;
}

.logo-mark {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: var(--radius-md);
  background: var(--accent);
  color: #fff;
  font-weight: 700;
  font-size: 14px;
  line-height: 1;
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.01em;
}

.logo:hover .logo-text {
  color: var(--ink);
}

/* Navigation */
.nav-wrapper {
  display: flex;
  align-items: center;
  gap: var(--space-1);
  flex: 1;
  justify-content: center;
}

.nav-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 0 var(--space-3);
  height: 36px;
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: color 0.15s ease, background-color 0.15s ease;
  white-space: nowrap;
}

.nav-tab:hover {
  color: var(--ink);
  background: var(--canvas);
}

.nav-tab.active {
  color: var(--accent);
  background: var(--accent-subtle);
}

/* User Actions */
.user-actions {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  flex-shrink: 0;
}

.login-btn {
  font-weight: 500;
  color: var(--text-secondary) !important;
}

.login-btn:hover {
  color: var(--ink) !important;
  background: var(--canvas) !important;
}

.upgrade-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 1px 14px;
  border-radius: 6px;
  border: 1px solid var(--accent);
  background: transparent;
  color: var(--accent);
  font-size: 12px;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.2s ease;
}

.upgrade-badge:hover {
  background: var(--accent);
  color: #fff;
}

.vip-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 1px 14px;
  border-radius: 6px;
  background: linear-gradient(135deg, #FDE68A, #D4AF37);
  color: #78350F;
  font-size: 12px;
  font-weight: 700;
  text-decoration: none;
  border: none;
  box-shadow: 0 1px 4px rgba(212, 175, 55, 0.25);
  overflow: hidden;
  transition: all 0.2s ease;
}

.vip-badge::after {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 50%;
  height: 100%;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.35),
    transparent
  );
  transform: skewX(-20deg);
  animation: vip-shimmer 3s infinite;
  pointer-events: none;
}

@keyframes vip-shimmer {
  0% {
    left: -100%;
  }
  20% {
    left: 200%;
  }
  100% {
    left: 200%;
  }
}

.vip-badge:hover {
  box-shadow: 0 3px 10px rgba(212, 175, 55, 0.4);
  transform: translateY(-1px);
}

.user-menu-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  height: 36px;
  padding: 0 var(--space-3);
  border-radius: var(--radius-md);
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--ink);
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.user-menu-btn:hover {
  border-color: var(--border-strong);
  background: var(--canvas);
}

.user-name {
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.caret {
  font-size: 11px;
  color: var(--text-muted);
}

/* Mobile: hide center nav on small screens */
@media (max-width: 768px) {
  .nav-wrapper {
    display: none;
  }

  .logo-text {
    display: none;
  }
}
</style>
