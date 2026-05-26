<template>
  <a-layout class="basic-layout">
    <!-- Top navigation bar -->
    <GlobalHeader />
    <!-- Main content area -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>

    <!-- Footer copyright info -->
    <GlobalFooter />
  </a-layout>
</template>

<script setup lang="ts">
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
</script>

<style scoped>
.basic-layout {
  background: none;
  min-height: 100vh;
}

.main-content {
  flex: 1;
  width: 100%;
  padding: 0;
  background: none;
  margin: 0;
}
</style>
