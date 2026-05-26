<template>
  <div class="home-page">
    <!-- Hero Section -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">Welcome to AI Article Writer</h1>
        <p class="hero-subtitle">
          Generate high-quality articles with AI assistance. From topic to polished content with images, fully automated.
        </p>
        <div class="hero-actions">
          <a-button type="primary" size="large" @click="router.push('/create')">
            <EditOutlined />
            Start Creating
          </a-button>
          <a-button size="large" @click="router.push('/articles')">
            <FileTextOutlined />
            History
          </a-button>
        </div>
      </div>
    </div>

    <!-- Recent Articles -->
    <div class="recent-section">
      <div class="section-header">
        <h2>Recent Articles</h2>
        <a-button type="link" @click="router.push('/articles')">
          View All
          <RightOutlined />
        </a-button>
      </div>

      <a-row :gutter="[16, 16]">
        <a-col
          v-for="item in recentArticles"
          :key="item.id"
          :xs="24"
          :sm="12"
          :md="8"
        >
          <a-card
            hoverable
            class="article-card"
            @click="viewArticle(item)"
          >
            <div class="card-status">
              <a-tag :color="ARTICLE_STATUS_COLOR[item.status || ''] || 'default'">
                {{ ARTICLE_STATUS_TEXT[item.status || ''] || item.status }}
              </a-tag>
            </div>
            <h3 class="card-title">{{ item.mainTitle || 'Untitled' }}</h3>
            <p class="card-topic">{{ item.topic }}</p>
            <div class="card-meta">
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </a-card>
        </a-col>
      </a-row>

      <a-empty
        v-if="!loading && recentArticles.length === 0"
        description="No articles yet. Start creating your first article!"
      />

      <div v-if="loading" class="loading-wrapper">
        <a-spin tip="Loading..." />
      </div>
    </div>

    <!-- Feature Highlights -->
    <div class="features-section">
      <h2 class="features-title">How It Works</h2>
      <a-row :gutter="[24, 24]">
        <a-col :xs="24" :sm="12" :md="8">
          <div class="feature-item">
            <div class="feature-icon">
              <EditOutlined />
            </div>
            <h3>Enter a Topic</h3>
            <p>Simply input any topic you're interested in writing about.</p>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <div class="feature-item">
            <div class="feature-icon">
              <ThunderboltOutlined />
            </div>
            <h3>AI Writes in Real-time</h3>
            <p>Watch as AI generates outline, content, and images with live progress updates.</p>
          </div>
        </a-col>
        <a-col :xs="24" :sm="12" :md="8">
          <div class="feature-item">
            <div class="feature-icon">
              <DownloadOutlined />
            </div>
            <h3>Export & Manage</h3>
            <p>Download your article as Markdown and manage all creations in one place.</p>
          </div>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  EditOutlined,
  FileTextOutlined,
  RightOutlined,
  ThunderboltOutlined,
  DownloadOutlined,
} from '@ant-design/icons-vue'
import { listArticleByPage } from '@/api/articleController'
import { ARTICLE_STATUS_TEXT, ARTICLE_STATUS_COLOR } from '@/constants/article'

const router = useRouter()

const loading = ref(false)
const recentArticles = ref<API.ArticleVO[]>([])

function formatTime(timeStr: string | undefined): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleDateString()
}

function viewArticle(item: API.ArticleVO) {
  if (item.id) {
    router.push(`/article/${item.id}`)
  } else if (item.taskId) {
    router.push(`/article/${item.taskId}`)
  }
}

async function loadRecent() {
  loading.value = true
  try {
    const res = await listArticleByPage({
      pageNum: 1,
      pageSize: 6,
      sortField: 'createTime',
      sortOrder: 'descend',
    })
    if (res.data.code === 0 && res.data.data) {
      recentArticles.value = res.data.data.records || []
    }
  } catch {
    // Silently fail for homepage
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadRecent()
})
</script>

<style scoped>
.home-page {
  padding-bottom: 48px;
}

/* Hero */
.hero-section {
  background: linear-gradient(135deg, #1890ff 0%, #36cfc9 100%);
  color: #fff;
  padding: 80px 24px;
  text-align: center;
}

.hero-content {
  max-width: 720px;
  margin: 0 auto;
}

.hero-title {
  font-size: 40px;
  font-weight: 700;
  margin-bottom: 16px;
  color: #fff;
}

.hero-subtitle {
  font-size: 18px;
  line-height: 1.6;
  margin-bottom: 32px;
  opacity: 0.95;
}

.hero-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

.hero-actions .ant-btn {
  min-width: 160px;
  height: 48px;
  font-size: 16px;
}

.hero-actions .ant-btn-primary {
  background: #fff;
  color: #1890ff;
  border-color: #fff;
}

.hero-actions .ant-btn-primary:hover {
  background: #f0f5ff;
  color: #40a9ff;
  border-color: #f0f5ff;
}

.hero-actions .ant-btn:not(.ant-btn-primary) {
  background: transparent;
  color: #fff;
  border-color: rgba(255, 255, 255, 0.6);
}

.hero-actions .ant-btn:not(.ant-btn-primary):hover {
  border-color: #fff;
  color: #fff;
}

/* Recent Section */
.recent-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.article-card {
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}

.article-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.card-status {
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-topic {
  font-size: 13px;
  color: #595959;
  margin-bottom: 12px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.card-meta {
  font-size: 12px;
  color: #8c8c8c;
}

.loading-wrapper {
  text-align: center;
  padding: 48px 0;
}

/* Features */
.features-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 48px 24px;
}

.features-title {
  text-align: center;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 40px;
}

.feature-item {
  text-align: center;
  padding: 24px;
}

.feature-icon {
  font-size: 48px;
  color: #1890ff;
  margin-bottom: 16px;
}

.feature-item h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #262626;
}

.feature-item p {
  color: #595959;
  line-height: 1.6;
}
</style>
