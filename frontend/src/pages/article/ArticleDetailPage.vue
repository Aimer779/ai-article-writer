<template>
  <div class="detail-page">
    <div class="detail-container">
      <!-- Breadcrumb & Actions -->
      <div class="detail-header">
        <a-breadcrumb>
          <a-breadcrumb-item>
            <router-link to="/articles">History</router-link>
          </a-breadcrumb-item>
          <a-breadcrumb-item>Article detail</a-breadcrumb-item>
        </a-breadcrumb>
        <a-space>
          <a-select
            v-model:value="selectedTheme"
            placeholder="Select theme"
            style="width: 180px"
            :options="themeOptions"
            allow-clear
          />
          <a-button @click="handleCopyWechat">
            Copy for WeChat
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
        <div class="surface-card meta-card">
          <div class="article-meta">
            <h1 class="article-title">{{ article.mainTitle || 'Untitled' }}</h1>
            <p v-if="article.subTitle" class="article-subtitle">{{ article.subTitle }}</p>
            <div class="meta-tags">
              <span class="meta-tag">Topic: {{ article.topic }}</span>
              <span
                class="status-badge"
                :class="article.status?.toLowerCase() || 'pending'"
              >
                {{ ARTICLE_STATUS_TEXT[article.status || ''] || article.status }}
              </span>
              <span v-if="article.taskId" class="meta-tag">Task: {{ article.taskId }}</span>
            </div>
            <div class="meta-times">
              <span v-if="article.createTime">
                <ClockCircleOutlined />
                Created {{ formatTime(article.createTime) }}
              </span>
              <span v-if="article.completedTime">
                <CheckCircleOutlined />
                Completed {{ formatTime(article.completedTime) }}
              </span>
            </div>
          </div>
        </div>

        <!-- Agent Execution Log Panel -->
        <div v-if="article.taskId" class="surface-card log-card">
          <button class="log-header" @click="toggleLogPanel">
            <div class="log-header-left">
              <ExperimentOutlined class="log-header-icon" />
              <span class="log-header-title">Execution log</span>
              <span
                v-if="logStats"
                class="status-badge"
                :class="logStats.failedCount && logStats.failedCount > 0 ? 'failed' : 'completed'"
              >
                {{ logStats.failedCount && logStats.failedCount > 0 ? 'Has failures' : 'All succeeded' }}
              </span>
            </div>
            <div class="log-header-right">
              <component :is="logExpanded ? UpOutlined : DownOutlined" class="log-toggle-icon" />
            </div>
          </button>

          <div v-show="logExpanded" class="log-body">
            <a-spin v-if="logLoading" size="small" tip="Loading logs..." class="log-spinner" />
            <template v-else>
              <!-- Stats Row -->
              <div v-if="agentLogs.length" class="log-stats">
                <div class="log-stat-item">
                  <div class="log-stat-label">Total duration</div>
                  <div class="log-stat-value text-tabular">{{ formatDuration(totalDuration) }}</div>
                </div>
                <div class="log-stat-item">
                  <div class="log-stat-label">Agent count</div>
                  <div class="log-stat-value text-tabular">{{ agentLogs.length }}</div>
                </div>
                <div class="log-stat-item">
                  <div class="log-stat-label">Avg duration</div>
                  <div class="log-stat-value text-tabular">{{ formatDuration(logStats?.averageDurationMs) }}</div>
                </div>
              </div>

              <!-- Timeline -->
              <div v-if="agentLogs.length" class="log-timeline">
                <div
                  v-for="(log, index) in agentLogs"
                  :key="log.id || index"
                  class="timeline-item"
                  :class="{ 'timeline-last': index === agentLogs.length - 1 }"
                >
                  <div class="timeline-left">
                    <div class="timeline-dot-wrapper">
                      <div
                        class="timeline-dot"
                        :class="{
                          'dot-success': log.status === 'success',
                          'dot-error': log.status === 'failed',
                          'dot-running': log.status === 'running',
                        }"
                      >
                        <CheckCircleOutlined v-if="log.status === 'success'" />
                        <CloseCircleOutlined v-else-if="log.status === 'failed'" />
                        <LoadingOutlined v-else-if="log.status === 'running'" />
                        <ProfileOutlined v-else />
                      </div>
                      <div v-if="index !== agentLogs.length - 1" class="timeline-line" />
                    </div>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-row">
                      <span class="timeline-name">{{ log.agentName || 'Unknown agent' }}</span>
                      <span class="timeline-duration text-tabular">{{ formatDuration(log.durationMs) }}</span>
                    </div>
                    <div class="timeline-time">{{ formatTime(log.startTime || '') }}</div>
                  </div>
                </div>
              </div>

              <div v-else class="empty-state">
                <p>No execution logs available</p>
              </div>
            </template>
          </div>
        </div>

        <!-- Cover Image -->
        <div v-if="article.coverImage" class="surface-card cover-card">
          <img :src="article.coverImage" alt="Cover" class="cover-image" />
        </div>

        <!-- Body -->
        <div class="surface-card body-card">
          <div v-if="article.fullContent" class="markdown-body" v-html="renderedContent" />
          <div v-else-if="article.content" class="markdown-body" v-html="renderedContent" />
          <div v-else class="empty-state">
            <p>No content available</p>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  DownloadOutlined,
  ClockCircleOutlined,
  CheckCircleOutlined,
  DownOutlined,
  UpOutlined,
  ProfileOutlined,
  ExperimentOutlined,
  CloseCircleOutlined,
  LoadingOutlined,
} from '@ant-design/icons-vue'
import { getArticleById, getArticleByTaskId } from '@/api/articleController'
import { listAgentLogByPage, getExecutionStats } from '@/api/agentLogController'
import { ARTICLE_STATUS_TEXT } from '@/constants/article'
import { markdownToHtml, downloadArticleAsMarkdown } from '@/utils/article'
import { getStyleList } from '@/constants/themes'
import { copyToWechat } from '@/utils/clipboardExporter'
import 'highlight.js/styles/github.css'

const route = useRoute()

const loading = ref(false)
const error = ref<string | null>(null)
const article = ref<API.ArticleVO | null>(null)

const logExpanded = ref(false)
const logLoading = ref(false)
const agentLogs = ref<API.AgentLogVO[]>([])
const logStats = ref<API.AgentExecutionStatsVO | null>(null)

const selectedTheme = ref<string>('')

const themeOptions = computed(() => {
  return getStyleList().map((t) => ({ label: t.name, value: t.key }))
})

const renderedContent = computed(() => {
  const md = article.value?.fullContent || article.value?.content || ''
  return markdownToHtml(md, selectedTheme.value || undefined)
})

function formatTime(timeStr: string): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  return date.toLocaleString()
}

const formatDuration = (ms?: number): string => {
  if (ms === undefined || ms === null) return '-'
  return `${Math.round(ms)}ms`
}

const totalDuration = computed(() => {
  if (!agentLogs.value.length) return 0
  return agentLogs.value.reduce((sum, log) => sum + (log.durationMs || 0), 0)
})

function toggleLogPanel() {
  logExpanded.value = !logExpanded.value
  if (logExpanded.value && article.value?.taskId && agentLogs.value.length === 0) {
    loadAgentLogs()
  }
}

async function loadAgentLogs() {
  if (!article.value?.taskId) return
  logLoading.value = true
  try {
    const [logRes, statsRes] = await Promise.all([
      listAgentLogByPage({
        taskId: article.value.taskId,
        pageNum: 1,
        pageSize: 50,
        sortField: 'createTime',
        sortOrder: 'ascend',
      }),
      getExecutionStats({
        taskId: article.value.taskId,
      }),
    ])

    if (logRes.data.code === 0 && logRes.data.data) {
      agentLogs.value = logRes.data.data.records || []
    }
    if (statsRes.data.code === 0 && statsRes.data.data) {
      logStats.value = statsRes.data.data
    }
  } catch (err: any) {
    console.error('Failed to load agent logs', err)
    message.error(err.response?.data?.message || 'Failed to load execution logs')
  } finally {
    logLoading.value = false
  }
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

async function handleCopyWechat() {
  if (!article.value) return
  const md = article.value.fullContent || article.value.content || ''
  const html = markdownToHtml(md, selectedTheme.value || undefined)
  const ok = await copyToWechat(html, selectedTheme.value || undefined)
  if (ok) {
    message.success('Copied to clipboard in WeChat format')
  } else {
    message.error('Failed to copy')
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
  min-height: calc(100vh - 60px);
}

.detail-container {
  max-width: 960px;
  margin: 0 auto;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-4);
  flex-wrap: wrap;
  gap: var(--space-3);
}

.loading-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.meta-card,
.cover-card,
.body-card,
.log-card {
  margin-bottom: var(--space-4);
  padding: var(--space-5);
}

.article-meta {
  text-align: center;
}

.article-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: var(--space-2);
  color: var(--ink);
  letter-spacing: -0.018em;
  line-height: 1.25;
}

.article-subtitle {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: var(--space-4);
}

.meta-tags {
  display: flex;
  justify-content: center;
  gap: var(--space-2);
  flex-wrap: wrap;
  margin-bottom: var(--space-3);
}

.meta-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  background: var(--canvas);
  border: 1px solid var(--border);
  font-size: 12px;
  color: var(--text-secondary);
  font-weight: 500;
}

.status-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.01em;
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

.meta-times {
  display: flex;
  justify-content: center;
  gap: var(--space-5);
  color: var(--text-muted);
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
  outline: 1px solid rgba(0, 0, 0, 0.08);
  outline-offset: -1px;
}

.body-card {
  padding: var(--space-5) var(--space-5) var(--space-6);
}

/* Markdown styles */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: var(--space-5);
  margin-bottom: var(--space-3);
  font-weight: 600;
  line-height: 1.4;
  color: var(--ink);
  text-wrap: balance;
}

.markdown-body :deep(h1) { font-size: 26px; }
.markdown-body :deep(h2) { font-size: 22px; }
.markdown-body :deep(h3) { font-size: 18px; }
.markdown-body :deep(h4) { font-size: 16px; }

.markdown-body :deep(p) {
  margin-bottom: var(--space-3);
  line-height: 1.8;
  color: var(--text-secondary);
  text-wrap: pretty;
}

.markdown-body :deep(code) {
  background: var(--code-bg);
  padding: 2px 6px;
  border-radius: var(--radius-sm);
  font-family: var(--font-mono);
  font-size: 0.9em;
  color: var(--ink);
}

.markdown-body :deep(pre) {
  background: var(--code-bg);
  padding: var(--space-3) var(--space-4);
  border-radius: var(--radius-md);
  overflow-x: auto;
  margin-bottom: var(--space-3);
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 3px solid var(--border-strong);
  padding-left: 16px;
  margin-left: 0;
  color: var(--text-secondary);
  font-style: italic;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 24px;
  margin-bottom: var(--space-3);
}

.markdown-body :deep(li) {
  margin-bottom: 6px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: var(--radius-md);
  margin: var(--space-3) 0;
  outline: 1px solid rgba(0, 0, 0, 0.08);
  outline-offset: -1px;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid var(--border);
  margin: var(--space-5) 0;
}

.markdown-body :deep(a) {
  color: var(--accent);
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

.markdown-body :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin-bottom: var(--space-3);
}

.markdown-body :deep(th),
.markdown-body :deep(td) {
  border: 1px solid var(--border);
  padding: 8px 12px;
  text-align: left;
}

.markdown-body :deep(th) {
  background: var(--surface-elevated);
  font-weight: 600;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.01em;
  color: var(--text-secondary);
}

/* Agent Execution Log Panel */
.log-card {
  padding: 0;
  overflow: hidden;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: var(--space-4) var(--space-5);
  background: none;
  border: none;
  width: 100%;
  font-family: var(--font-sans);
  font-size: 14px;
  color: var(--ink);
}

.log-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-header-icon {
  font-size: 18px;
  color: var(--text-secondary);
}

.log-header-title {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
}

.log-toggle-icon {
  font-size: 14px;
  color: var(--text-muted);
  transition: color 0.15s ease;
}

.log-header:hover .log-toggle-icon {
  color: var(--ink);
}

.log-body {
  padding: 0 var(--space-5) var(--space-4);
  border-top: 1px solid var(--border);
}

.log-spinner {
  display: flex;
  justify-content: center;
  padding: var(--space-5) 0;
}

/* Stats Row */
.log-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: var(--space-4);
  margin: var(--space-4) 0 var(--space-5);
  padding: var(--space-4);
  background: var(--canvas);
  border-radius: var(--radius-lg);
  border: 1px solid var(--border);
}

.log-stat-item {
  text-align: center;
}

.log-stat-label {
  font-size: 12px;
  color: var(--text-muted);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.01em;
  font-weight: 500;
}

.log-stat-value {
  font-size: 20px;
  font-weight: 700;
  color: var(--success);
  letter-spacing: -0.018em;
}

/* Timeline */
.log-timeline {
  padding: var(--space-2) var(--space-1);
}

.timeline-item {
  display: flex;
  gap: var(--space-4);
}

.timeline-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 28px;
  flex-shrink: 0;
}

.timeline-dot-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  min-height: 60px;
}

.timeline-dot {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  flex-shrink: 0;
  background: var(--success-subtle);
  color: var(--success);
  border: 2px solid var(--success);
}

.timeline-dot.dot-error {
  background: var(--error-subtle);
  color: var(--error);
  border-color: var(--error);
}

.timeline-dot.dot-running {
  background: var(--accent-subtle);
  color: var(--accent);
  border-color: var(--accent);
}

.timeline-line {
  flex: 1;
  width: 2px;
  background: var(--border);
  margin: 4px 0;
  min-height: 32px;
}

.timeline-content {
  flex: 1;
  padding-bottom: var(--space-4);
  min-height: 60px;
}

.timeline-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.timeline-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
}

.timeline-duration {
  font-size: 13px;
  font-weight: 500;
  color: var(--success);
}

.timeline-time {
  font-size: 12px;
  color: var(--text-muted);
}

.timeline-last .timeline-content {
  padding-bottom: 0;
}

.empty-state {
  text-align: center;
  padding: var(--space-6) 0;
  color: var(--text-muted);
  font-size: 14px;
}

/* Responsive */
@media (max-width: 600px) {
  .log-stats {
    grid-template-columns: 1fr;
    gap: var(--space-3);
  }

  .article-title {
    font-size: 22px;
  }

  .meta-times {
    flex-direction: column;
    gap: var(--space-1);
  }
}
</style>
