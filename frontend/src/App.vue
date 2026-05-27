<template>
  <a-config-provider :theme="theme">
    <BasicLayout />
  </a-config-provider>
</template>

<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'

const theme = {
  token: {
    colorPrimary: '#c2783f',
    colorSuccess: '#4a9b5e',
    colorWarning: '#d4a03c',
    colorError: '#c44d3a',
    colorTextBase: '#2d2a26',
    colorBgBase: '#faf8f5',
    borderRadius: 6,
    fontFamily: "Manrope, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Noto Sans SC', sans-serif",
  },
}
</script>
