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

        <!-- Agent Execution Log Panel -->
        <a-card v-if="article.taskId" :bordered="false" class="log-card">
          <div class="log-header" @click="toggleLogPanel">
            <div class="log-header-left">
              <ExperimentOutlined class="log-header-icon" />
              <span class="log-header-title">Execution Log</span>
              <a-tag
                v-if="logStats"
                :color="logStats.failedCount && logStats.failedCount > 0 ? 'error' : 'success'"
                class="log-status-tag"
              >
                {{ logStats.failedCount && logStats.failedCount > 0 ? 'failed' : 'success' }}
              </a-tag>
            </div>
            <div class="log-header-right">
              <component :is="logExpanded ? UpOutlined : DownOutlined" class="log-toggle-icon" />
            </div>
          </div>

          <div v-show="logExpanded" class="log-body">
            <a-spin v-if="logLoading" size="small" tip="Loading logs..." class="log-spinner" />
            <template v-else>
              <!-- Stats Row -->
              <div v-if="agentLogs.length" class="log-stats">
                <div class="log-stat-item">
                  <div class="log-stat-label">Total Duration</div>
                  <div class="log-stat-value">{{ formatDuration(totalDuration) }}</div>
                </div>
                <div class="log-stat-item">
                  <div class="log-stat-label">Agent Count</div>
                  <div class="log-stat-value">{{ agentLogs.length }}</div>
                </div>
                <div class="log-stat-item">
                  <div class="log-stat-label">Avg Duration</div>
                  <div class="log-stat-value">{{ formatDuration(logStats?.averageDurationMs) }}</div>
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
                      <div v-if="index !== agentLogs.length - 1" class="timeline-line"></div>
                    </div>
                  </div>
                  <div class="timeline-content">
                    <div class="timeline-row">
                      <span class="timeline-name">{{ log.agentName || 'Unknown Agent' }}</span>
                      <span class="timeline-duration">{{ formatDuration(log.durationMs) }}</span>
                    </div>
                    <div class="timeline-time">{{ formatTime(log.startTime || '') }}</div>
                  </div>
                </div>
              </div>

              <a-empty v-else description="No execution logs available" class="log-empty" />
            </template>
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
  DownOutlined,
  UpOutlined,
  ProfileOutlined,
  ExperimentOutlined,
  CloseCircleOutlined,
  LoadingOutlined,
} from '@ant-design/icons-vue'
import { getArticleById, getArticleByTaskId } from '@/api/articleController'
import { listAgentLogByPage, getExecutionStats } from '@/api/agentLogController'
import { ARTICLE_STATUS_TEXT, ARTICLE_STATUS_COLOR } from '@/constants/article'
import { markdownToHtml, downloadArticleAsMarkdown } from '@/utils/article'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const error = ref<string | null>(null)
const article = ref<API.ArticleVO | null>(null)

const logExpanded = ref(false)
const logLoading = ref(false)
const agentLogs = ref<API.AgentLogVO[]>([])
const logStats = ref<API.AgentExecutionStatsVO | null>(null)

const renderedContent = computed(() => {
  const md = article.value?.fullContent || article.value?.content || ''
  return markdownToHtml(md)
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

/* Agent Execution Log Panel */
.log-card {
  background: #fff;
  border-radius: 8px;
  margin-bottom: 16px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 4px 0;
}

.log-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.log-header-icon {
  font-size: 18px;
  color: #595959;
}

.log-header-title {
  font-size: 15px;
  font-weight: 600;
  color: #262626;
}

.log-status-tag {
  font-size: 12px;
  line-height: 18px;
  height: 22px;
  padding: 0 8px;
}

.log-toggle-icon {
  font-size: 14px;
  color: #8c8c8c;
  transition: color 0.3s;
}

.log-header:hover .log-toggle-icon {
  color: #262626;
}

.log-body {
  margin-top: 16px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.log-spinner {
  display: flex;
  justify-content: center;
  padding: 24px 0;
}

/* Stats Row */
.log-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.log-stat-item {
  text-align: center;
}

.log-stat-label {
  font-size: 13px;
  color: #8c8c8c;
  margin-bottom: 6px;
}

.log-stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #52c41a;
}

/* Timeline */
.log-timeline {
  padding: 8px 4px;
}

.timeline-item {
  display: flex;
  gap: 16px;
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
  background: #f6ffed;
  color: #52c41a;
  border: 2px solid #52c41a;
}

.timeline-dot.dot-error {
  background: #fff2f0;
  color: #ff4d4f;
  border-color: #ff4d4f;
}

.timeline-dot.dot-running {
  background: #e6f7ff;
  color: #1890ff;
  border-color: #1890ff;
}

.timeline-line {
  flex: 1;
  width: 2px;
  background: #e8e8e8;
  margin: 4px 0;
  min-height: 32px;
}

.timeline-content {
  flex: 1;
  padding-bottom: 20px;
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
  color: #262626;
}

.timeline-duration {
  font-size: 14px;
  font-weight: 500;
  color: #52c41a;
}

.timeline-time {
  font-size: 12px;
  color: #8c8c8c;
}

.timeline-last .timeline-content {
  padding-bottom: 0;
}

.log-empty {
  padding: 24px 0;
}

/* Responsive */
@media (max-width: 600px) {
  .log-stats {
    grid-template-columns: 1fr;
    gap: 12px;
  }
}
</style>
