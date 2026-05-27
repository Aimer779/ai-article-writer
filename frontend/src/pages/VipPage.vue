<template>
  <div class="vip-page">
    <div class="vip-container">
      <!-- Page Header -->
      <div class="page-header">
        <div class="header-badge">
          <CrownOutlined />
          <span>VIP Exclusive</span>
        </div>
        <h1 class="page-title">Upgrade to Lifetime VIP</h1>
        <p class="page-subtitle">Unlock all premium features, unlimited creation quota, valid for life</p>
      </div>

      <!-- Main Section: Left-Right Layout -->
      <div class="main-section">
        <!-- Left: Pricing Card -->
        <div class="pricing-card">
          <div class="pricing-badge">Limited Offer</div>
          <div class="pricing-header">
            <div class="plan-icon">
              <CrownOutlined />
            </div>
            <h2 class="plan-name">Lifetime Membership</h2>
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

          <div class="pricing-divider"></div>
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
            @click="handlePurchase"
            class="purchase-btn"
          >
            <template #icon>
              <ThunderboltOutlined />
            </template>
            {{ isVip ? 'You are already a lifetime member' : 'Upgrade Now' }}
          </a-button>
          <div class="security-notice">
            <SafetyOutlined />
            <span>Secure Payment · 7-Day Money-Back Guarantee</span>
          </div>
        </div>

        <!-- Right: Member Privileges -->
        <div class="features-section">
          <h3 class="section-title">
            <StarOutlined />
            Member Privileges
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
      <div class="faq-section">
        <h3 class="section-title">
          <QuestionCircleOutlined />
          Frequently Asked Questions
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
    title: 'Fast Creation',
    desc: 'Priority AI queue, article generation speed boosted by 50%',
  },
  {
    icon: PictureOutlined,
    title: 'Smart Images',
    desc: 'Auto-generate HD covers, Mermaid charts, and SVG illustrations',
  },
  {
    icon: FileTextOutlined,
    title: 'Unlimited Quota',
    desc: 'No daily quota limits, write as much as you want',
  },
  {
    icon: CustomerServiceOutlined,
    title: 'VIP Support',
    desc: '7x24 dedicated support, issues resolved first',
  },
  {
    icon: SyncOutlined,
    title: 'Lifetime Updates',
    desc: 'All new features free forever, pay once and enjoy for life',
  },
  {
    icon: CrownOutlined,
    title: 'Exclusive Badge',
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
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8ec 100%);
  padding: 40px 24px;
}

.vip-container {
  max-width: 1200px;
  margin: 0 auto;
}

/* Page Header */
.page-header {
  text-align: center;
  margin-bottom: 48px;
}

.header-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  color: #874d00;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 16px;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.page-subtitle {
  font-size: 16px;
  color: #666;
  max-width: 480px;
  margin: 0 auto;
}

/* Main Section */
.main-section {
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 32px;
  margin-bottom: 48px;
}

/* Pricing Card */
.pricing-card {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  position: relative;
  overflow: hidden;
}

.pricing-badge {
  position: absolute;
  top: 0;
  right: 0;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  color: #fff;
  padding: 6px 16px;
  font-size: 12px;
  font-weight: 500;
  border-bottom-left-radius: 12px;
}

.pricing-header {
  text-align: center;
  margin-bottom: 24px;
}

.plan-icon {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: #fff;
  margin: 0 auto 16px;
}

.plan-name {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 16px;
}

.price-display {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 2px;
  margin-bottom: 8px;
}

.currency {
  font-size: 24px;
  font-weight: 600;
  color: #ff4d4f;
}

.price {
  font-size: 48px;
  font-weight: 700;
  color: #ff4d4f;
  line-height: 1;
}

.period {
  font-size: 14px;
  color: #999;
}

.original-price {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 14px;
}

.original-label {
  color: #999;
}

.original-value {
  color: #999;
  text-decoration: line-through;
}

.pricing-divider {
  height: 1px;
  background: #eee;
  margin: 24px 0;
}

.pricing-features {
  margin-bottom: 24px;
}

.pricing-feature {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 0;
  font-size: 14px;
  color: #444;
}

.feature-check {
  color: #52c41a;
  font-size: 16px;
  flex-shrink: 0;
}

.purchase-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  border: none;
  color: #874d00;
}

.purchase-btn:hover {
  opacity: 0.9;
}

.purchase-btn:disabled {
  background: #f0f0f0;
  color: #999;
}

.security-notice {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 16px;
  font-size: 12px;
  color: #999;
}

/* Features Section */
.features-section {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 24px;
}

.features-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.feature-card {
  padding: 20px;
  border-radius: 12px;
  background: #f8f9fa;
  transition: all 0.3s;
}

.feature-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.feature-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #fff;
  margin-bottom: 12px;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.feature-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

/* FAQ Section */
.faq-section {
  background: #fff;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
}

.faq-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.faq-item {
  border: 1px solid #eee;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s;
}

.faq-item:hover {
  border-color: #ffd700;
}

.faq-item.active {
  border-color: #ffd700;
  background: #fffbe6;
}

.faq-question {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 500;
  color: #1a1a2e;
}

.faq-q {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  color: #fff;
  flex-shrink: 0;
}

.faq-text {
  flex: 1;
}

.faq-arrow {
  font-size: 12px;
  color: #999;
  transition: transform 0.3s;
}

.faq-item.active .faq-arrow {
  transform: rotate(180deg);
}

.faq-answer {
  padding: 0 20px 16px 54px;
  font-size: 14px;
  color: #666;
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
