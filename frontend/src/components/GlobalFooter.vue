<template>
  <a-layout-footer class="global-footer">
    <div class="footer-inner">
      <!-- Brand -->
      <div class="footer-col brand-col">
        <div class="brand">
          <div class="brand-mark">A</div>
          <span class="brand-name">AI Article Writer</span>
        </div>
        <p class="brand-tagline">
          Built for teams that write at scale.
        </p>
      </div>

      <!-- Product -->
      <div class="footer-col">
        <h4 class="col-title">Product</h4>
        <nav class="col-links">
          <router-link to="/">Home</router-link>
          <router-link to="/create">Create</router-link>
          <router-link v-if="loginUserStore.isLoggedIn" to="/articles">
            History
          </router-link>
          <router-link to="/vip">VIP</router-link>
        </nav>
      </div>

      <!-- Resources -->
      <div class="footer-col">
        <h4 class="col-title">Resources</h4>
        <nav class="col-links">
          <a href="#" @click.prevent>Documentation</a>
          <a href="#" @click.prevent>API</a>
          <a href="#" @click.prevent>Community</a>
          <a href="#" @click.prevent>Demos</a>
        </nav>
      </div>

      <!-- Newsletter -->
      <div class="footer-col newsletter-col">
        <h4 class="col-title">Newsletter</h4>
        <p class="newsletter-desc">
          Get the latest updates on AI writing.
        </p>
        <div class="newsletter-form">
          <input
            v-model="email"
            type="email"
            placeholder="your@email.com"
            class="newsletter-input"
            @keyup.enter="handleSubscribe"
          />
          <button class="newsletter-btn" @click="handleSubscribe">
            <ArrowRightOutlined />
          </button>
        </div>
      </div>
    </div>

    <!-- Bottom bar -->
    <div class="footer-bottom">
      <p>© {{ currentYear }} AI Article Writer. All rights reserved.</p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { ArrowRightOutlined } from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores'
import { message } from 'ant-design-vue'

const currentYear = new Date().getFullYear()
const loginUserStore = useLoginUserStore()
const email = ref('')

function handleSubscribe() {
  if (!email.value || !email.value.includes('@')) {
    message.warning('Please enter a valid email address')
    return
  }
  message.success('Thanks for subscribing!')
  email.value = ''
}
</script>

<style scoped>
.global-footer {
  background: var(--canvas) !important;
  border-top: 1px solid var(--border);
  padding: var(--space-7) var(--space-5) var(--space-5);
}

.footer-inner {
  max-width: 1200px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1.25fr;
  gap: var(--space-6);
}

/* Brand column */
.brand-col {
  padding-right: var(--space-5);
}

.brand {
  display: flex;
  align-items: center;
  gap: var(--space-2);
  margin-bottom: var(--space-3);
}

.brand-mark {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--accent);
  color: #fff;
  font-size: 13px;
  font-weight: 700;
  border-radius: var(--radius-md);
}

.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: var(--ink);
  letter-spacing: -0.01em;
}

.brand-tagline {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-muted);
  max-width: 240px;
}

/* Column titles — Letta-style */
.col-title {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-muted);
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin: 0 0 var(--space-3);
}

/* Links */
.col-links {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.col-links a {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-secondary);
  text-decoration: none;
  transition: color 0.15s ease;
  width: fit-content;
}

.col-links a:hover {
  color: var(--accent);
}

/* Newsletter */
.newsletter-col {
  min-width: 0;
}

.newsletter-desc {
  margin: 0 0 var(--space-3);
  font-size: 13px;
  line-height: 1.5;
  color: var(--text-muted);
}

.newsletter-form {
  display: flex;
  gap: var(--space-1);
}

.newsletter-input {
  flex: 1;
  min-width: 0;
  height: 36px;
  padding: 0 var(--space-3);
  font-size: 13px;
  font-family: var(--font-sans);
  color: var(--ink);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  outline: none;
  transition: border-color 0.15s ease, box-shadow 0.15s ease;
}

.newsletter-input::placeholder {
  color: var(--text-muted);
}

.newsletter-input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px var(--accent-subtle);
}

.newsletter-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  background: var(--ink);
  color: #fff;
  border: none;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background 0.15s ease;
}

.newsletter-btn:hover {
  background: var(--accent);
}

/* Bottom bar */
.footer-bottom {
  max-width: 1200px;
  margin: var(--space-6) auto 0;
  padding-top: var(--space-5);
  border-top: 1px solid var(--border);
}

.footer-bottom p {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}

/* Responsive */
@media (max-width: 1024px) {
  .footer-inner {
    grid-template-columns: repeat(2, 1fr);
  }

  .brand-col {
    grid-column: 1 / -1;
    padding-right: 0;
  }

  .brand-tagline {
    max-width: 100%;
  }
}

@media (max-width: 640px) {
  .global-footer {
    padding: var(--space-6) var(--space-4) var(--space-4);
  }

  .footer-inner {
    grid-template-columns: 1fr;
    gap: var(--space-5);
  }

  .footer-bottom {
    margin-top: var(--space-5);
    padding-top: var(--space-4);
    text-align: center;
  }
}
</style>
