<template>
  <div class="home-page">
    <!-- Hero -->
    <section class="hero">
      <div class="hero-inner">
        <div class="hero-text">
          <h1 class="hero-title">
            Multi-Agent article generation<br />for teams that write at scale
          </h1>
          <p class="hero-lead">
            From topic research to polished content with images — orchestrated by specialized AI agents, streaming in real time.
          </p>
          <div class="hero-actions">
            <a-button
              type="primary"
              size="large"
              class="hero-cta"
              @click="router.push('/create')"
            >
              <EditOutlined />
              Start creating
            </a-button>
            <a-button
              v-if="loginUserStore.isLoggedIn"
              size="large"
              class="hero-secondary"
              @click="router.push('/articles')"
            >
              <FileTextOutlined />
              View history
            </a-button>
          </div>
        </div>
        <div class="hero-visual">
          <!-- Abstract agent-flow diagram as CSS shapes -->
          <div class="flow-diagram">
            <div class="flow-node">Topic</div>
            <div class="flow-line" />
            <div class="flow-node">Research</div>
            <div class="flow-line" />
            <div class="flow-node">Outline</div>
            <div class="flow-line" />
            <div class="flow-node">Content</div>
            <div class="flow-line" />
            <div class="flow-node active">Images</div>
          </div>
        </div>
      </div>
    </section>

    <!-- Recent Articles -->
    <section v-if="loginUserStore.isLoggedIn" class="content-section">
      <div class="section-header">
        <h2>Recent articles</h2>
        <router-link to="/articles" class="section-link">
          View all
          <RightOutlined />
        </router-link>
      </div>

      <div class="articles-grid">
        <article
          v-for="(item, index) in recentArticles"
          :key="item.id"
          class="article-card press-scale"
          :style="{ animationDelay: `${index * 60}ms` }"
          @click="viewArticle(item)"
        >
          <!-- Cover image -->
          <div class="card-cover">
            <img
              v-if="item.coverImage"
              :src="item.coverImage"
              :alt="item.mainTitle || 'Cover'"
              class="cover-img"
              loading="lazy"
            />
            <div
              v-else
              class="cover-placeholder"
              :style="placeholderStyle(index)"
            >
              <span class="placeholder-letter">
                {{ (item.mainTitle || item.topic || '?').charAt(0).toUpperCase() }}
              </span>
            </div>
          </div>

          <!-- Card body -->
          <div class="card-body">
            <div class="card-top">
              <span
                class="status-badge"
                :class="item.status?.toLowerCase() || 'pending'"
              >
                {{ ARTICLE_STATUS_TEXT[item.status || ''] || item.status }}
              </span>
              <time class="card-date">{{ formatTime(item.createTime) }}</time>
            </div>
            <h3 class="card-title">{{ item.mainTitle || 'Untitled' }}</h3>
            <p class="card-topic">{{ item.topic }}</p>
          </div>
        </article>
      </div>

      <div v-if="!loading && recentArticles.length === 0" class="empty-state">
        <div class="empty-icon">
          <FileTextOutlined />
        </div>
        <h3>No articles yet</h3>
        <p>Start your first article and the Agents will handle the rest.</p>
        <a-button type="primary" @click="router.push('/create')">
          Create article
        </a-button>
      </div>

      <div v-if="loading" class="loading-wrapper">
        <a-spin tip="Loading articles..." />
      </div>
    </section>

  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  EditOutlined,
  FileTextOutlined,
  RightOutlined,
} from '@ant-design/icons-vue'
import { listArticleByPage } from '@/api/articleController'
import { ARTICLE_STATUS_TEXT } from '@/constants/article'
import { useLoginUserStore } from '@/stores'

const router = useRouter()
const loginUserStore = useLoginUserStore()

const loading = ref(false)
const recentArticles = ref<API.ArticleVO[]>([])

function formatTime(timeStr: string | undefined): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
  })
}

function placeholderStyle(index: number) {
  const hues = [45, 25, 55, 35, 65, 15]
  const hue = hues[index % hues.length]
  return {
    background: `linear-gradient(135deg, oklch(92% 0.03 ${hue}), oklch(86% 0.04 ${hue}))`,
  }
}

function viewArticle(item: API.ArticleVO) {
  if (item.id) {
    router.push(`/article/${item.id}`)
  } else if (item.taskId) {
    router.push(`/article/${item.taskId}`)
  }
}

async function loadRecent() {
  if (!loginUserStore.isLoggedIn) return
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
  max-width: 1280px;
  margin: 0 auto;
  padding-bottom: var(--space-7);
}

/* ==================== Hero ==================== */
.hero {
  padding: var(--space-7) var(--space-5);
  border-bottom: 1px solid var(--border);
}

.hero-inner {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: var(--space-6);
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 var(--space-5);
}

.hero-title {
  font-size: clamp(32px, 4vw, 48px);
  line-height: 1.1;
  letter-spacing: -0.022em;
  font-weight: 700;
  color: var(--ink);
  margin-bottom: var(--space-4);
}

.hero-lead {
  font-size: 17px;
  line-height: 1.6;
  color: var(--text-secondary);
  max-width: 480px;
  margin-bottom: var(--space-5);
}

.hero-actions {
  display: flex;
  gap: var(--space-3);
  flex-wrap: wrap;
}

.hero-cta {
  height: 44px;
  padding: 0 var(--space-4);
  font-size: 15px;
  font-weight: 600;
}

.hero-secondary {
  height: 44px;
  padding: 0 var(--space-4);
  font-size: 15px;
  font-weight: 500;
  border-color: var(--border);
  color: var(--ink);
}

.hero-secondary:hover {
  border-color: var(--border-strong);
  background: var(--canvas);
}

/* Flow diagram */
.hero-visual {
  display: flex;
  align-items: center;
  justify-content: center;
}

.flow-diagram {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-2);
  padding: var(--space-5);
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-1);
  width: 320px;
}

.flow-node {
  width: 100%;
  text-align: center;
  padding: 10px 0;
  border-radius: var(--radius-md);
  background: var(--canvas);
  border: 1px solid var(--border);
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  letter-spacing: 0.01em;
}

.flow-node.active {
  background: var(--accent-subtle);
  border-color: var(--accent);
  color: var(--accent);
}

.flow-line {
  width: 1px;
  height: 16px;
  background: var(--border);
}

@media (max-width: 1024px) {
  .hero-inner {
    grid-template-columns: 1fr;
    text-align: center;
  }

  .hero-lead {
    margin-left: auto;
    margin-right: auto;
  }

  .hero-actions {
    justify-content: center;
  }

  .hero-visual {
    display: none;
  }
}

/* ==================== Content Sections ==================== */
.content-section {
  padding: var(--space-6) var(--space-5) 0;
  max-width: 1200px;
  margin: 0 auto;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: var(--space-4);
}

.section-header h2 {
  font-size: 20px;
  font-weight: 600;
}

.section-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  font-weight: 500;
  color: var(--accent);
}

.section-link:hover {
  color: var(--accent-hover);
}

/* Articles Grid */
.articles-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
}

@media (max-width: 1024px) {
  .articles-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .articles-grid {
    grid-template-columns: 1fr;
  }
}

.article-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s cubic-bezier(0.16, 1, 0.3, 1), transform 0.2s cubic-bezier(0.16, 1, 0.3, 1);
  animation: fadeInUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
}

.article-card:hover {
  border-color: var(--border-strong);
  box-shadow: var(--shadow-2);
  transform: translateY(-4px);
}

/* Cover image */
.card-cover {
  position: relative;
  width: 100%;
  height: 140px;
  overflow: hidden;
  background: var(--canvas);
}

.card-cover::after {
  content: '';
  position: absolute;
  inset: 0;
  box-shadow: inset 0 0 0 rgba(0, 0, 0, 0);
  transition: box-shadow 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
  pointer-events: none;
}

.article-card:hover .card-cover::after {
  box-shadow: inset 0 0 30px rgba(0, 0, 0, 0.06);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94), filter 0.4s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.article-card:hover .cover-img {
  transform: scale(1.06);
  filter: brightness(1.03);
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.placeholder-letter {
  font-size: 40px;
  font-weight: 700;
  color: var(--text-muted);
  opacity: 0.5;
  line-height: 1;
}

/* Card body */
.card-body {
  padding: var(--space-4);
}

.card-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
}

.status-badge {
  display: inline-block;
  padding: 3px 8px;
  border-radius: var(--radius-sm);
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 0.01em;
  text-transform: uppercase;
}

.status-badge.completed {
  background: var(--success-subtle);
  color: var(--success);
}

.status-badge.processing {
  background: var(--accent-subtle);
  color: var(--accent);
}

.status-badge.pending {
  background: var(--canvas);
  color: var(--text-muted);
  border: 1px solid var(--border);
}

.status-badge.failed {
  background: var(--error-subtle);
  color: var(--error);
}

.card-date {
  font-size: 12px;
  color: var(--text-muted);
  font-variant-numeric: tabular-nums;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
  margin-bottom: var(--space-1);
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.2s ease;
}

.article-card:hover .card-title {
  color: var(--accent);
}

.card-topic {
  font-size: 13px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* Empty state */
.empty-state {
  text-align: center;
  padding: var(--space-7) var(--space-4);
}

.empty-icon {
  font-size: 48px;
  color: var(--border);
  margin-bottom: var(--space-3);
}

.empty-state h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: var(--space-1);
}

.empty-state p {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.loading-wrapper {
  text-align: center;
  padding: var(--space-6) 0;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
