<template>
  <div class="create-page">
    <a-row :gutter="24" class="main-row">
      <!-- Left: Progress Steps -->
      <a-col :xs="24" :lg="4" class="side-col">
        <div class="side-section surface-card">
          <h3 class="side-title">Creation flow</h3>
          <p class="side-subtitle">Multi-agent pipeline</p>
          <div class="steps-list">
            <div
              v-for="(step, index) in CREATION_STEPS"
              :key="step.key"
              class="step-item"
              :class="{
                'step-active': creationStore.currentStepIndex === index,
                'step-completed': creationStore.currentStepIndex > index,
              }"
            >
              <div class="step-marker">
                <span v-if="creationStore.currentStepIndex > index" class="step-check">
                  <CheckOutlined />
                </span>
                <span v-else class="step-number">{{ index + 1 }}</span>
              </div>
              <div class="step-body">
                <div class="step-name">{{ step.title }}</div>
                <div class="step-desc">{{ step.description }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-col>

      <!-- Center: Input or Preview -->
      <a-col :xs="24" :lg="14" class="center-col">
        <!-- Input Panel -->
        <div v-if="showInputPanel" class="center-panel surface-card input-panel">
          <h1 class="center-title">Create an article</h1>
          <p class="center-subtitle">
            Enter a topic and let the agents generate a complete article with outline, content, and images.
          </p>
          <a-textarea
            v-model:value="topicInput"
            :rows="4"
            placeholder="e.g., The Future of Artificial Intelligence in Healthcare"
            :disabled="!creationStore.canStart"
            class="topic-textarea"
            @pressEnter="handleStart"
          />
          <div class="char-count">{{ topicInput.length }} / 500</div>

          <!-- Article Style -->
          <div class="option-section">
            <div class="option-header">
              <span class="option-title">Article style</span>
              <span class="option-hint">Default if none selected</span>
            </div>
            <div class="option-group">
              <label
                v-for="style in articleStyles"
                :key="style.value"
                class="option-pill"
                :class="{ 'option-pill-active': selectedStyle === style.value }"
              >
                <input
                  v-model="selectedStyle"
                  type="radio"
                  name="article-style"
                  :value="style.value"
                  class="option-input"
                />
                {{ style.label }}
              </label>
            </div>
          </div>

          <!-- Image Method -->
          <div class="option-section">
            <div class="option-header">
              <span class="option-title">Image method</span>
              <span class="option-hint">All methods if none selected</span>
            </div>
            <div class="option-group">
              <label
                v-for="method in imageMethods"
                :key="method.value"
                class="option-pill"
                :class="{ 'option-pill-active': selectedMethods.includes(method.value) }"
              >
                <input
                  v-model="selectedMethods"
                  type="checkbox"
                  :value="method.value"
                  class="option-input"
                />
                {{ method.label }}
              </label>
            </div>
          </div>

          <a-button
            type="primary"
            size="large"
            class="start-btn"
            :loading="creationStore.isCreating"
            :disabled="!topicInput.trim() || !creationStore.canStart"
            @click="handleStart"
          >
            <RocketOutlined />
            Start creation
          </a-button>

          <a-alert
            v-if="creationStore.isFailed && creationStore.error"
            type="error"
            :message="creationStore.error"
            show-icon
            closable
            class="error-alert"
            @close="creationStore.reset()"
          />

          <a-result
            v-if="creationStore.isCompleted"
            status="success"
            title="Article created successfully"
            class="success-result"
          >
            <template #extra>
              <a-button type="primary" @click="goToArticle">
                View article
              </a-button>
              <a-button @click="handleReset">
                Create another
              </a-button>
            </template>
          </a-result>
        </div>

        <!-- Preview Panel -->
        <div v-else class="center-panel surface-card preview-panel">
          <div class="preview-header">
            <span class="topic-tag">Topic: {{ topicInput }}</span>
            <span
              v-if="creationStore.isConnected"
              class="status-badge processing"
            >Streaming</span>
            <span
              v-else-if="creationStore.isCreating"
              class="status-badge processing"
            >Creating</span>
            <span
              v-else-if="creationStore.isCompleted"
              class="status-badge completed"
            >Completed</span>
            <span
              v-else-if="creationStore.isFailed"
              class="status-badge failed"
            >Failed</span>
          </div>

          <a-tabs v-model:activeKey="activeTabKey" class="preview-tabs">
            <a-tab-pane key="title" tab="Title">
              <div class="content-preview">
                <div v-if="creationStore.articleTitle" class="markdown-body">
                  <h2>Generated title</h2>
                  <h3>{{ creationStore.articleTitle }}</h3>
                  <p v-if="creationStore.articleSubTitle">{{ creationStore.articleSubTitle }}</p>
                </div>
                <div v-else class="empty-placeholder">
                  <a-spin v-if="creationStore.currentStepIndex >= 0 && !creationStore.articleTitle" />
                  <p v-else>Title will appear here once generation starts.</p>
                </div>
              </div>
            </a-tab-pane>

            <a-tab-pane key="outline" tab="Outline">
              <div class="content-preview">
                <div
                  v-if="creationStore.outlineDisplay"
                  class="markdown-body"
                  v-html="renderOutline"
                />
                <div v-else class="empty-placeholder">
                  <a-spin v-if="creationStore.currentStepIndex >= 1 && !creationStore.outlineDisplay" />
                  <p v-else>Outline will appear here once generation starts.</p>
                </div>
              </div>
            </a-tab-pane>

            <a-tab-pane key="content" tab="Content">
              <div class="content-preview">
                <div
                  v-if="creationStore.contentDisplay"
                  class="markdown-body"
                  v-html="renderContent"
                />
                <div v-else class="empty-placeholder">
                  <a-spin v-if="creationStore.currentStepIndex >= 2 && !creationStore.contentDisplay" />
                  <p v-else>Content will appear here after outline is complete.</p>
                </div>
              </div>
            </a-tab-pane>
          </a-tabs>

          <div v-if="creationStore.isCompleted || creationStore.isFailed" class="bottom-actions">
            <a-button v-if="creationStore.isCompleted" type="primary" @click="goToArticle">
              View full article
            </a-button>
            <a-button @click="handleReset">
              {{ creationStore.isCompleted ? 'Create another' : 'Try again' }}
            </a-button>
          </div>
        </div>
      </a-col>

      <!-- Right: Sidebar -->
      <a-col :xs="24" :lg="6" class="side-col">
        <!-- Quota Card -->
        <div class="side-card surface-card">
          <h4 class="card-title">
            <CrownOutlined />
            Creation quota
          </h4>
          <div class="quota-body">
            <span v-if="loginUserStore.isAdmin" class="status-badge completed">Admin</span>
            <span class="quota-text">{{ loginUserStore.isAdmin ? 'Unlimited' : '10 / 10 remaining' }}</span>
          </div>
        </div>

        <!-- Hot Topics -->
        <div class="side-card surface-card">
          <h4 class="card-title">
            <FireOutlined />
            Hot topics
          </h4>
          <div class="topic-tags">
            <button
              v-for="topic in hotTopics"
              :key="topic"
              class="topic-tag-item press-scale"
              @click="selectTopic(topic)"
            >
              {{ topic }}
            </button>
          </div>
        </div>

        <!-- Writing Tips -->
        <div class="side-card surface-card">
          <h4 class="card-title">
            <StarOutlined />
            Writing tips
          </h4>
          <div class="tips-list">
            <div v-for="(tip, idx) in writingTips" :key="idx" class="tip-item">
              <span class="tip-num">{{ idx + 1 }}</span>
              <div class="tip-body">
                <div class="tip-name">{{ tip.name }}</div>
                <div class="tip-desc">{{ tip.desc }}</div>
              </div>
            </div>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckOutlined,
  RocketOutlined,
  CrownOutlined,
  FireOutlined,
  StarOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useArticleCreationStore } from '@/stores/articleCreation'
import { useLoginUserStore } from '@/stores'
import { CREATION_STEPS } from '@/constants/article'
import { markdownToHtml } from '@/utils/article'

const router = useRouter()
const creationStore = useArticleCreationStore()
const loginUserStore = useLoginUserStore()

const topicInput = ref('')
const activeTabKey = ref('title')
const titlePreviewReady = ref(false)
let titlePreviewTimer: number | null = null

const selectedStyle = ref('default')
const selectedMethods = ref<string[]>([])

const articleStyles = [
  { label: 'Default', value: 'default' },
  { label: 'Tech', value: 'tech' },
  { label: 'Emotional', value: 'emotional' },
  { label: 'Educational', value: 'educational' },
  { label: 'Humorous', value: 'humorous' },
]

const imageMethods = [
  { label: 'Pexels', value: 'pexels' },
  { label: 'AI Image', value: 'nano_banana' },
  { label: 'Mermaid', value: 'mermaid' },
  { label: 'Iconify', value: 'iconify' },
  { label: 'Meme', value: 'meme' },
  { label: 'SVG', value: 'svg' },
]

watch(
  () => creationStore.articleTitle,
  (title) => {
    if (titlePreviewTimer !== null) {
      window.clearTimeout(titlePreviewTimer)
      titlePreviewTimer = null
    }

    titlePreviewReady.value = false
    if (title) {
      activeTabKey.value = 'title'
      titlePreviewTimer = window.setTimeout(() => {
        titlePreviewReady.value = true
        titlePreviewTimer = null
      }, 1200)
    }
  },
)

// Auto-switch tabs based on content that is actually available.
watch(
  () => [
    creationStore.currentStepIndex,
    creationStore.outlineBuffer,
    creationStore.contentBuffer,
    titlePreviewReady.value,
  ] as const,
  ([idx, outline, content, canLeaveTitle]) => {
    if (content || idx >= 3) {
      activeTabKey.value = 'content'
    } else if ((outline || idx >= 2) && canLeaveTitle) {
      activeTabKey.value = 'outline'
    } else if (idx >= 0) {
      activeTabKey.value = 'title'
    }
  },
)

const showInputPanel = computed(() => {
  return (
    creationStore.status === 'idle' ||
    creationStore.status === 'failed' ||
    creationStore.status === 'completed'
  )
})

const isTypingOutline = computed(() => {
  return (
    creationStore.status === 'streaming' &&
    creationStore.currentStepIndex === 1 &&
    creationStore.outlineDisplay.length < creationStore.outlineBuffer.length
  )
})

const isTypingContent = computed(() => {
  return (
    creationStore.status === 'streaming' &&
    creationStore.currentStepIndex >= 2 &&
    creationStore.currentStepIndex <= 3 &&
    creationStore.contentDisplay.length < creationStore.contentBuffer.length
  )
})

function appendTypingCursor(html: string): string {
  if (!html) return '<span class="typing-cursor"></span>'
  const lastCloseMatch = html.match(/<\/[a-zA-Z][^>]*>(?![\s\S]*<\/[a-zA-Z][^>]*>)/)
  if (lastCloseMatch && lastCloseMatch.index !== undefined) {
    const idx = lastCloseMatch.index
    return html.slice(0, idx) + '<span class="typing-cursor"></span>' + html.slice(idx)
  }
  return html + '<span class="typing-cursor"></span>'
}

const renderOutline = computed(() => {
  const html = markdownToHtml(creationStore.outlineDisplay)
  return isTypingOutline.value ? appendTypingCursor(html) : html
})

const renderContent = computed(() => {
  const html = markdownToHtml(creationStore.contentDisplay)
  return isTypingContent.value ? appendTypingCursor(html) : html
})

const hotTopics = [
  'How AI Changes the Workplace in 2026',
  'How Programmers Improve Competitiveness',
  'Pros and Cons of Remote Work',
  'How to Cultivate Deep Thinking',
  'New Energy Vehicle Trends',
  'Healthy Diet Guide',
]

const writingTips = [
  { name: 'Hit the pain point', desc: 'Address what users care about most' },
  { name: 'Create suspense', desc: 'Spark curiosity in readers' },
  { name: 'Use numbers', desc: 'Concrete data adds persuasion' },
  { name: 'Tell a story', desc: 'Narratives make content memorable' },
  { name: 'Keep it concise', desc: 'Short paragraphs improve readability' },
  { name: 'Use active voice', desc: 'Direct sentences feel more energetic' },
  { name: 'Add a call to action', desc: 'Guide readers on what to do next' },
]

function selectTopic(topic: string) {
  topicInput.value = topic
}

async function handleStart() {
  const topic = topicInput.value.trim()
  if (!topic) {
    message.warning('Please enter a topic')
    return
  }

  const newTaskId = await creationStore.createTask(topic)
  if (newTaskId) {
    creationStore.connectSSE(newTaskId)
  }
}

function handleReset() {
  const currentTopic = topicInput.value
  creationStore.reset()
  topicInput.value = currentTopic
}

function goToArticle() {
  if (creationStore.taskId) {
    router.push(`/article/${creationStore.taskId}`)
  }
}

onUnmounted(() => {
  if (titlePreviewTimer !== null) {
    window.clearTimeout(titlePreviewTimer)
    titlePreviewTimer = null
  }
  // Keep SSE alive for navigation
})
</script>

<style scoped>
.create-page {
  min-height: calc(100vh - 60px);
  padding: var(--space-5);
}

.main-row {
  margin: 0 auto;
  align-items: flex-start;
}

/* ==================== Side Column ==================== */
.side-col {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.side-section,
.side-card {
  padding: var(--space-4);
}

.side-title {
  font-size: 15px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--ink);
}

.side-subtitle {
  font-size: 12px;
  color: var(--text-muted);
  margin: 0 0 var(--space-4);
}

/* Steps */
.steps-list {
  display: flex;
  flex-direction: column;
  gap: 0;
}

.step-item {
  display: flex;
  gap: 12px;
  padding: 10px 0;
  position: relative;
}

.step-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 15px;
  top: 38px;
  bottom: 0;
  width: 1px;
  background: var(--border);
  transition: background-color 0.3s ease;
}

.step-item.step-completed:not(:last-child)::after {
  background: var(--accent);
}

.step-marker {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: var(--canvas);
  border: 2px solid var(--border);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  z-index: 1;
  transition: background-color 0.3s ease, border-color 0.3s ease, color 0.3s ease;
}

.step-check {
  font-size: 13px;
}

.step-active .step-marker {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
  animation: stepPulse 2s ease-in-out infinite;
}

.step-completed .step-marker {
  background: var(--accent-subtle);
  border-color: var(--accent);
  color: var(--accent);
}

.step-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  line-height: 20px;
  transition: color 0.3s ease;
}

.step-active .step-name {
  color: var(--ink);
  font-weight: 600;
}

.step-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
  line-height: 18px;
}

@keyframes stepPulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.06);
  }
}

/* Side Cards */
.card-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 var(--space-3);
  color: var(--ink);
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title :deep(.anticon) {
  font-size: 15px;
  color: var(--accent);
}

.quota-body {
  display: flex;
  align-items: center;
  gap: var(--space-2);
}

.quota-text {
  font-size: 14px;
  color: var(--text-secondary);
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.topic-tag-item {
  cursor: pointer;
  font-size: 12px;
  padding: 5px 10px;
  border-radius: var(--radius-md);
  background: var(--canvas);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  font-family: var(--font-sans);
  font-weight: 500;
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-tag-item:hover {
  background: var(--accent-subtle);
  border-color: var(--accent);
  color: var(--accent);
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: var(--space-3);
}

.tip-item {
  display: flex;
  gap: 10px;
  align-items: flex-start;
}

.tip-num {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: var(--accent);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tip-name {
  font-size: 13px;
  font-weight: 500;
  color: var(--ink);
}

.tip-desc {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
}

/* ==================== Center Column ==================== */
.center-col {
  display: flex;
  flex-direction: column;
}

.center-panel {
  padding: var(--space-5);
  min-height: 600px;
}

/* Input Panel */
.center-title {
  font-size: 22px;
  font-weight: 700;
  text-align: center;
  margin: 0 0 var(--space-2);
  color: var(--ink);
}

.center-subtitle {
  font-size: 14px;
  color: var(--text-secondary);
  text-align: center;
  margin: 0 0 var(--space-5);
}

.topic-textarea {
  font-size: 15px;
  resize: none;
}

.topic-textarea :deep(.ant-input) {
  border-radius: var(--radius-lg);
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: var(--text-muted);
  margin-top: var(--space-1);
}

.start-btn {
  width: 100%;
  margin-top: var(--space-4);
  height: 48px;
  font-size: 15px;
  font-weight: 600;
}

/* Option Sections */
.option-section {
  margin-top: var(--space-4);
}

.option-header {
  display: flex;
  align-items: baseline;
  gap: var(--space-2);
  margin-bottom: var(--space-2);
}

.option-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--ink);
}

.option-hint {
  font-size: 12px;
  color: var(--text-muted);
}

.option-group {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-2);
}

.option-pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: var(--radius-pill);
  border: 1px solid var(--border);
  background: var(--surface);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: border-color 0.15s ease, background-color 0.15s ease, color 0.15s ease;
  user-select: none;
}

.option-pill:hover {
  border-color: var(--border-strong);
  color: var(--ink);
}

.option-pill-active {
  background: var(--accent-subtle);
  border-color: var(--accent);
  color: var(--accent);
  font-weight: 500;
}

.option-input {
  position: absolute;
  opacity: 0;
  width: 0;
  height: 0;
}

.error-alert {
  margin-top: var(--space-4);
}

.success-result {
  margin-top: var(--space-4);
}

/* Preview Panel */
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-3);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border);
  flex-wrap: wrap;
  gap: var(--space-2);
}

.topic-tag {
  font-size: 13px;
  color: var(--text-secondary);
  font-weight: 500;
}

.status-badge {
  display: inline-block;
  padding: 3px 10px;
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

.status-badge.failed {
  background: var(--error-subtle);
  color: var(--error);
}

.preview-tabs :deep(.ant-tabs-nav) {
  margin-bottom: var(--space-3);
}

.content-preview {
  min-height: 420px;
  position: relative;
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: var(--text-muted);
}

.markdown-body :deep(.typing-cursor) {
  display: inline-block;
  width: 2px;
  height: 1.2em;
  background: var(--accent);
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: blink 1s step-end infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

.bottom-actions {
  display: flex;
  justify-content: center;
  gap: var(--space-3);
  margin-top: var(--space-5);
  padding-top: var(--space-4);
  border-top: 1px solid var(--border);
}

/* Markdown body styles */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: var(--space-4);
  margin-bottom: var(--space-3);
  font-weight: 600;
  line-height: 1.4;
  color: var(--ink);
}

.markdown-body :deep(h1) { font-size: 22px; }
.markdown-body :deep(h2) { font-size: 18px; }
.markdown-body :deep(h3) { font-size: 16px; }
.markdown-body :deep(h4) { font-size: 15px; }

.markdown-body :deep(p) {
  margin-bottom: var(--space-3);
  line-height: 1.75;
  color: var(--text-secondary);
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
  padding: 12px 16px;
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
  padding-left: 12px;
  margin-left: 0;
  color: var(--text-secondary);
  font-style: italic;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 20px;
  margin-bottom: var(--space-3);
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
  line-height: 1.75;
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
  margin: var(--space-4) 0;
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
  padding: 8px 12px;
  border: 1px solid var(--border);
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

/* ==================== Responsive ==================== */
@media (max-width: 992px) {
  .side-col {
    margin-top: var(--space-4);
  }

  .center-panel {
    padding: var(--space-4);
    min-height: auto;
  }
}
</style>
