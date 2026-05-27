<template>
  <a-layout-footer class="global-footer">
    <div class="footer-inner">
      <p>© {{ currentYear }} AI Article Writer. Built for teams that write at scale.</p>
      <nav class="footer-links">
        <router-link to="/">Home</router-link>
        <router-link v-if="loginUserStore.isLoggedIn" to="/articles">History</router-link>
        <router-link to="/vip">VIP</router-link>
      </nav>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
import { useLoginUserStore } from '@/stores'

const currentYear = new Date().getFullYear()
const loginUserStore = useLoginUserStore()
</script>

<style scoped>
.global-footer {
  background: var(--canvas) !important;
  border-top: 1px solid var(--border);
  padding: var(--space-5) var(--space-5) var(--space-6);
}

.footer-inner {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: var(--space-4);
  flex-wrap: wrap;
}

.footer-inner p {
  margin: 0;
  color: var(--text-muted);
  font-size: 13px;
}

.footer-links {
  display: flex;
  gap: var(--space-4);
}

.footer-links a {
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: color 0.15s ease;
}

.footer-links a:hover {
  color: var(--accent);
}

@media (max-width: 640px) {
  .footer-inner {
    flex-direction: column;
    text-align: center;
  }
}
</style>
