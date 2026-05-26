<template>
  <div class="detail-page">
    <div class="detail-container">
      <!-- Breadcrumb & Actions -->
      <div class="detail-header">
        <a-breadcrumb>
          <a-breadcrumb-item>
            <router-link to="/articles">History</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>Article Detail</a-breadcrumb-item>
        </a-breadcrumb>
        <a-space>
          <a-button @click="router.push('/articles')">
            <ArrowLeftOutlined />
            Back to List
          </a-button>
          <a-button type="primary" @click="handleExport">
            <DownloadOutlined />
            Export Markdown
          </a-button>
        </a-space>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="loading-wrapper">
        <a-spin size="large" tip="Loading article..." />
      </div>

      <!-- Error -->
      <a-result
        v-else-if="error"
        status="error"
        title="Failed to load article"
        :sub-title="error"
      >
        <template #extra>
          <a-button type="primary" @click="loadArticle">Retry</a-button>
        </template>
      </a-result>

      <!-- Article Content -->
      <template v-else-if="article">
        <!-- Meta Card -->
        <a-card :bordered="false" class="meta-card">
          <div class="article-meta">
            <h1 class="article-title">{{ article.mainTitle || 'Untitled' }}</h1>
            <p v-if="article.subTitle" class="article-subtitle">{{ article.subTitle }}</p>
            <div class="meta-tags">
              <a-tag color="blue">Topic: {{ article.topic }}</a-tag>
              <a-tag :color="ARTICLE_STATUS_COLOR[article.status || ''] || 'default'">
                {{ ARTICLE_STATUS_TEXT[article.status || ''] || article.status }}
              </a-tag>
              <a-tag v-if="article.taskId">Task: {{ article.taskId }}</a-tag>
            </div>
            <div class="meta-times">
              <span v-if="article.createTime">
                <ClockCircleOutlined />
                Created: {{ formatTime(article.createTime) }}
              </span>
              <span v-if="article.completedTime">
                <CheckCircleOutlined />
                Completed: {{ formatTime(article.completedTime) }}
              </span>
            </div>
          </div>
        </a-card>

        <!-- Cover Image -->
        <a-card v-if="article.coverImage" :bordered="false" class="cover-card">
          <img :src="article.coverImage" alt="Cover" class="cover-image" />
        </a-card>

        <!-- Body -->
        <a-card :bordered="false" class="body-card">
          <div v-if="article.fullContent" class="markdown-body" v-html="renderedContent" />
          <div v-else-if="article.content" class="markdown-body" v-html="renderedContent" />
          <a-empty v-else description="No content available" />
        </a-card>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  ArrowLeftOutlined,
  DownloadOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import { getArticleById, getArticleByTaskId } from '@/api/articleController'
import { ARTICLE_STATUS_TEXT, ARTICLE_STATUS_COLOR } from '@/constants/article'
import { markdownToHtml, downloadArticleAsMarkdown } from '@/utils/article'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref<string | null>(null)
const article = ref<API.ArticleVO | null>(null)

const renderedContent = computed(() => {
  const md = article.value?.fullContent || article.value?.content || ''
  return markdownToHtml(md)
})

function formatTime(timeStr: string): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString()
}

async function loadArticle() {
  const param = route.params.id as string
  if (!param) {
    error.value = 'Invalid article identifier'
    return
  }

  loading.value = true
  error.value = null

  try {
    // Try numeric ID first, fallback to taskId
    const isNumeric = /^\d+$/.test(param)
    let res

    if (isNumeric) {
      res = await getArticleById({ id: Number(param) })
    } else {
      res = await getArticleByTaskId({ taskId: param })
    }

    if (res.data.code === 0 && res.data.data) {
      article.value = res.data.data
    } else {
      error.value = res.data.message || 'Article not found'
    }
  } catch (err: any) {
    error.value = err.message || 'Failed to load article'
  } finally {
    loading.value = false
  }
}

function handleExport() {
  if (!article.value) return
  downloadArticleAsMarkdown(article.value)
  message.success('Article exported as Markdown')
}

onMounted(() => {
  loadArticle()
})
</script>

<style scoped>
.detail-page {
  padding: 24px;
  background: #f5f5f5;
  min-height: calc(100vh - 128px);
}

.detail-container {
  max-width: 960px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.meta-card,
.cover-card,
.body-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.article-meta {
  text-align: center;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #1f1f1f;
}

.article-subtitle {
  font-size: 16px;
  color: #595959;
  margin-bottom: 16px;
}

.meta-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 12px;
}

.meta-times {
  display: flex;
  justify-content: center;
  gap: 24px;
  color: #8c8c8c;
  font-size: 13px;
}

.meta-times span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.cover-card {
  padding: 0;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  max-height: 400px;
  object-fit: cover;
  display: block;
}

.body-card {
  padding: 8px 16px 24px;
}

/* Markdown styles */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 24px;
  margin-bottom: 16px;
  font-weight: 600;
  line-height: 1.4;
  color: #262626;
}

.markdown-body :deep(h1) { font-size: 28px; }
.markdown-body :deep(h2) { font-size: 24px; }
.markdown-body :deep(h3) { font-size: 20px; }
.markdown-body :deep(h4) { font-size: 18px; }
.markdown-body :deep(p) {
  margin-bottom: 16px;
  line-height: 1.8;
  color: #434343;
  font-size: 15px;
}

.markdown-body :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Fira Code', monospace;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background: #f5f5f5;
  padding: 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 16px;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #d9d9d9;
  padding-left: 16px;
  margin-left: 0;
  color: #595959;
  font-style: italic;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 24px;
  margin-bottom: 16px;
}

.markdown-body :deep(li) {
  margin-bottom: 6px;
  line-height: 1.8;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 16px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 24px 0;
}

.markdown-body :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: 16px;
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid #e8e8e8;
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: #fafafa;
  font-weight: 600;
}
</style>
