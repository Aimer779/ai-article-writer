<template>
  <a-layout-header class="global-header">
    <div class="header-content">
      <!-- Logo -->
      <div class="logo">
        <router-link to="/">
          AI Article Writer
        </router-link>
      </div>

      <!-- Navigation menu -->
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="horizontal"
        :items="menuItems"
        @click="handleMenuClick"
      />
    </div>
  </a-layout-header>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import type { MenuProps } from 'ant-design-vue'

const router = useRouter()
const route = useRoute()

const selectedKeys = ref<string[]>([route.path])

const menuItems = computed<MenuProps['items']>(() => [
  {
    key: '/',
    label: 'Home',
  },
  {
    key: '/about',
    label: 'About',
  },
])

const handleMenuClick: MenuProps['onClick'] = ({ key }) => {
  router.push(key)
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
</style>
