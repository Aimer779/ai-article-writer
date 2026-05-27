<template>
  <div class="vip-page">
    <div class="vip-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-badge">
          <CrownOutlined />
          <span>VIP exclusive</span>
        </div>
        <h1 class="page-title">Upgrade to lifetime VIP</h1>
        <p class="page-subtitle">Unlock all premium features, unlimited creation quota, valid for life</p>
      </div>

      <!-- Main Section -->
      <div class="main-section">
        <!-- Left: Pricing Card -->
        <div class="pricing-card surface-elevated">
          <div class="pricing-badge">Limited offer</div>
          <div class="pricing-header">
            <div class="plan-icon">
              <CrownOutlined />
            </div>
            <h2 class="plan-name">Lifetime membership</h2>
            <div class="price-display">
              <span class="currency">$</span>
              <span class="price">199</span>
              <span class="period">/lifetime</span>
            </div>
            <div class="original-price">
              <span class="original-label">Original</span>
              <span class="original-value">$299</span>
            </div>
          </div>

          <div class="pricing-divider" />
          <div class="pricing-features">
            <div v-for="(item, index) in pricingFeatures" :key="index" class="pricing-feature">
              <CheckCircleOutlined class="feature-check" />
              <span>{{ item }}</span>
            </div>
          </div>

          <a-button
            type="primary"
            size="large"
            :loading="purchasing"
            :disabled="isVip"
            class="purchase-btn"
            @click="handlePurchase"
          >
            <ThunderboltOutlined />
            {{ isVip ? 'You are already a lifetime member' : 'Upgrade now' }}
          </a-button>
          <div class="security-notice">
            <SafetyOutlined />
            <span>Secure payment · 7-day money-back guarantee</span>
          </div>
        </div>

        <!-- Right: Member Privileges -->
        <div class="features-section surface-card">
          <h3 class="section-title">
            <StarOutlined />
            Member privileges
          </h3>
          <div class="features-grid">
            <div v-for="(feature, index) in memberPrivileges" :key="index" class="feature-card">
              <div class="feature-icon">
                <component :is="feature.icon" />
              </div>
              <h4 class="feature-title">{{ feature.title }}</h4>
              <p class="feature-desc">{{ feature.desc }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- FAQ Section -->
      <div class="faq-section surface-card">
        <h3 class="section-title">
          <QuestionCircleOutlined />
          Frequently asked questions
        </h3>
        <div class="faq-list">
          <div
            v-for="(faq, index) in faqList"
            :key="index"
            class="faq-item"
            :class="{ active: activeFaq === index }"
            @click="toggleFaq(index)"
          >
            <div class="faq-question">
              <span class="faq-q">Q</span>
              <span class="faq-text">{{ faq.question }}</span>
              <DownOutlined class="faq-arrow" />
            </div>
            <div v-show="activeFaq === index" class="faq-answer">
              {{ faq.answer }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  CrownOutlined,
  CheckCircleOutlined,
  ThunderboltOutlined,
  SafetyOutlined,
  StarOutlined,
  QuestionCircleOutlined,
  DownOutlined,
  RocketOutlined,
  PictureOutlined,
  FileTextOutlined,
  CustomerServiceOutlined,
  SyncOutlined,
} from '@ant-design/icons-vue'
import { useLoginUserStore } from '@/stores'
import { USER_ROLE_VIP } from '@/constants/user'
import { createCheckoutSession } from '@/api/paymentController'

const loginUserStore = useLoginUserStore()

const isVip = computed(() => loginUserStore.loginUser.userRole === USER_ROLE_VIP || loginUserStore.loginUser.vip === true)

const purchasing = ref(false)
const activeFaq = ref<number | null>(null)

const pricingFeatures = [
  'Unlimited article creation',
  'Priority access to latest AI models',
  'Auto-generate HD cover images',
  'Mermaid charts and SVG illustrations',
  'Dedicated customer support',
  'Lifetime free updates',
]

const memberPrivileges = [
  {
    icon: RocketOutlined,
    title: 'Fast creation',
    desc: 'Priority AI queue, article generation speed boosted by 50%',
  },
  {
    icon: PictureOutlined,
    title: 'Smart images',
    desc: 'Auto-generate HD covers, Mermaid charts, and SVG illustrations',
  },
  {
    icon: FileTextOutlined,
    title: 'Unlimited quota',
    desc: 'No daily quota limits, write as much as you want',
  },
  {
    icon: CustomerServiceOutlined,
    title: 'VIP support',
    desc: '7x24 dedicated support, issues resolved first',
  },
  {
    icon: SyncOutlined,
    title: 'Lifetime updates',
    desc: 'All new features free forever, pay once and enjoy for life',
  },
  {
    icon: CrownOutlined,
    title: 'Exclusive badge',
    desc: 'VIP exclusive badge across the site, showing your premium status',
  },
]

const faqList = [
  {
    question: 'Is the lifetime membership valid forever?',
    answer: 'Yes, the lifetime membership is a one-time payment with permanent validity. No renewal is needed, and all benefits are enjoyed permanently.',
  },
  {
    question: 'What payment methods are supported?',
    answer: 'We support Stripe payments, including major credit and debit cards such as Visa, MasterCard, and American Express.',
  },
  {
    question: 'Can I get a refund after purchase?',
    answer: 'We offer a 7-day money-back guarantee. If you are not satisfied within 7 days of purchase, contact customer service for a full refund.',
  },
  {
    question: 'Will member benefits be updated?',
    answer: 'Yes. Lifetime members will automatically enjoy all new premium features added in the future at no extra cost.',
  },
]

const toggleFaq = (index: number) => {
  activeFaq.value = activeFaq.value === index ? null : index
}

const handlePurchase = async () => {
  if (isVip.value) {
    message.info('You are already a lifetime member')
    return
  }
  purchasing.value = true
  try {
    const response = await createCheckoutSession({ productType: 'vip' })
    if (response.data.code === 0 && response.data.data?.checkoutUrl) {
      window.location.href = response.data.data.checkoutUrl
    } else {
      message.error(response.data.message || 'Failed to create checkout session')
    }
  } catch (error) {
    message.error('Request failed, please try again later')
  } finally {
    purchasing.value = false
  }
}
</script>

<style scoped>
.vip-page {
  padding: var(--space-6) var(--space-5);
}

.vip-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: var(--space-6);
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: oklch(95% 0.03 85);
  color: oklch(55% 0.1 85);
  border-radius: var(--radius-pill);
  font-size: 13px;
  font-weight: 600;
  margin-bottom: var(--space-3);
  border: 1px solid oklch(88% 0.04 85);
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: var(--space-2);
  letter-spacing: -0.022em;
}

.page-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  max-width: 480px;
  margin: 0 auto;
}

/* Main Section */
.main-section {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: var(--space-5);
  margin-bottom: var(--space-6);
}

/* Pricing Card */
.pricing-card {
  padding: var(--space-5);
  position: relative;
  overflow: hidden;
}

.pricing-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: var(--error);
  color: #fff;
  padding: 5px 14px;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  border-bottom-left-radius: var(--radius-lg);
}

.pricing-header {
  text-align: center;
  margin-bottom: var(--space-4);
}

.plan-icon {
  width: 56px;
  height: 56px;
  background: oklch(95% 0.03 85);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: oklch(55% 0.1 85);
  margin: 0 auto var(--space-3);
  border: 1px solid oklch(88% 0.04 85);
}

.plan-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: var(--space-3);
}

.price-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
  margin-bottom: var(--space-1);
}

.currency {
  font-size: 22px;
  font-weight: 600;
  color: var(--error);
}

.price {
  font-size: 44px;
  font-weight: 700;
  color: var(--error);
  line-height: 1;
  letter-spacing: -0.022em;
}

.period {
  font-size: 14px;
  color: var(--text-muted);
}

.original-price {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
}

.original-label {
  color: var(--text-muted);
}

.original-value {
  color: var(--text-muted);
  text-decoration: line-through;
}

.pricing-divider {
  height: 1px;
  background: var(--border);
  margin: var(--space-4) 0;
}

.pricing-features {
  margin-bottom: var(--space-4);
}

.pricing-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 7px 0;
  font-size: 14px;
  color: var(--text-secondary);
}

.feature-check {
  color: var(--success);
  font-size: 16px;
  flex-shrink: 0;
}

.purchase-btn {
  width: 100%;
  height: 48px;
  font-size: 15px;
  font-weight: 600;
}

.security-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: var(--space-3);
  font-size: 12px;
  color: var(--text-muted);
}

/* Features Section */
.features-section {
  padding: var(--space-5);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: var(--space-4);
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--space-3);
}

.feature-card {
  padding: var(--space-4);
  border-radius: var(--radius-lg);
  background: var(--canvas);
  border: 1px solid var(--border);
  transition: border-color 0.15s ease;
}

.feature-card:hover {
  border-color: var(--border-strong);
}

.feature-icon {
  width: 36px;
  height: 36px;
  background: var(--accent-subtle);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  color: var(--accent);
  margin-bottom: var(--space-2);
}

.feature-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: 4px;
}

.feature-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.5;
}

/* FAQ Section */
.faq-section {
  padding: var(--space-5);
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
}

.faq-item {
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease;
}

.faq-item:hover {
  border-color: var(--border-strong);
}

.faq-item.active {
  border-color: var(--accent);
  background: var(--accent-subtle);
}

.faq-question {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: var(--space-3) var(--space-4);
  font-size: 14px;
  font-weight: 500;
  color: var(--ink);
}

.faq-q {
  width: 22px;
  height: 22px;
  background: var(--accent);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.faq-text {
  flex: 1;
}

.faq-arrow {
  font-size: 12px;
  color: var(--text-muted);
  transition: transform 0.2s ease;
}

.faq-item.active .faq-arrow {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 0 var(--space-4) var(--space-3) 50px;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
}

/* Responsive */
@media (max-width: 900px) {
  .main-section {
    grid-template-columns: 1fr;
  }

  .features-grid {
    grid-template-columns: 1fr;
  }
}
</style>
