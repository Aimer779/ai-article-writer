<template>
  <a-layout-footer class="global-footer">
    <div class="footer-content">
      <p>© {{ currentYear }} AI Article Writer. All rights reserved.</p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
const currentYear = new Date().getFullYear()
</script>

<style scoped>
.global-footer {
  text-align: center;
  background: #f0f2f5;
  padding: 24px 50px;
}

.footer-content p {
  margin: 0;
  color: #666;
  font-size: 14px;
}
</style>
