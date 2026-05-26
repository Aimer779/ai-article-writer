<template>
  <div class="create-page">
    <a-row :gutter="24" class="main-row">
      <!-- Left: Progress Steps -->
      <a-col :xs="24" :lg="4" class="side-col">
        <div class="side-section">
          <h3 class="side-title">Creation Flow</h3>
          <p class="side-subtitle">AI agent collaboration visualization</p>
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
        <div v-if="showInputPanel" class="center-panel input-panel">
          <h1 class="center-title">AI Article Creation</h1>
          <p class="center-subtitle">
            Enter a topic and let AI generate a complete article with outline, content, and images.
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
          <a-button
            type="primary"
            size="large"
            class="start-btn"
            :loading="creationStore.isCreating"
            :disabled="!topicInput.trim() || !creationStore.canStart"
            @click="handleStart"
          >
            <RocketOutlined />
            Start Creation
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
            title="Article Created Successfully!"
            class="success-result"
          >
            <template #extra>
              <a-button type="primary" @click="goToArticle">
                View Article
              </a-button>
              <a-button @click="handleReset">
                Create Another
              </a-button>
            </template>
          </a-result>
        </div>

        <!-- Preview Panel -->
        <div v-else class="center-panel preview-panel">
          <div class="preview-header">
            <span class="topic-tag">Topic: {{ topicInput }}</span>
            <a-tag v-if="creationStore.isConnected" color="processing">Streaming</a-tag>
            <a-tag v-else-if="creationStore.isCreating" color="processing">Creating</a-tag>
            <a-tag v-else-if="creationStore.isCompleted" color="success">Completed</a-tag>
            <a-tag v-else-if="creationStore.isFailed" color="error">Failed</a-tag>
          </div>

          <a-tabs v-model:activeKey="activeTabKey" class="preview-tabs">
            <a-tab-pane key="title" tab="Title Preview">
              <div class="content-preview">
                <div v-if="creationStore.articleTitle" class="markdown-body">
                  <h2>Generated Title</h2>
                  <p>{{ creationStore.articleTitle }}</p>
                </div>
                <div v-else class="empty-placeholder">
                  <a-spin v-if="creationStore.currentStepIndex >= 0 && !creationStore.articleTitle" />
                  <p v-else>Title will appear here once generation starts.</p>
                </div>
              </div>
            </a-tab-pane>

            <a-tab-pane key="outline" tab="Outline Preview">
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
                <span v-if="isTypingOutline" class="typing-cursor" />
              </div>
            </a-tab-pane>

            <a-tab-pane key="content" tab="Content Preview">
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
                <span v-if="isTypingContent" class="typing-cursor" />
              </div>
            </a-tab-pane>
          </a-tabs>

          <div v-if="creationStore.isCompleted || creationStore.isFailed" class="bottom-actions">
            <a-button v-if="creationStore.isCompleted" type="primary" @click="goToArticle">
              View Full Article
            </a-button>
            <a-button @click="handleReset">
              {{ creationStore.isCompleted ? 'Create Another' : 'Try Again' }}
            </a-button>
          </div>
        </div>
      </a-col>

      <!-- Right: Sidebar -->
      <a-col :xs="24" :lg="6" class="side-col">
        <!-- Quota Card -->
        <div class="side-card">
          <h4 class="card-title">
            <CrownOutlined />
            Creation Quota
          </h4>
          <div class="quota-body">
            <a-tag v-if="loginUserStore.isAdmin" color="gold">Admin</a-tag>
            <span class="quota-text">{{ loginUserStore.isAdmin ? 'Unlimited' : '10 / 10 remaining' }}</span>
          </div>
        </div>

        <!-- Hot Topics -->
        <div class="side-card">
          <h4 class="card-title">
            <FireOutlined />
            Hot Topics
          </h4>
          <div class="topic-tags">
            <a-tag
              v-for="topic in hotTopics"
              :key="topic"
              class="topic-tag-item"
              @click="selectTopic(topic)"
            >
              {{ topic }}
            </a-tag>
          </div>
        </div>

        <!-- Writing Tips -->
        <div class="side-card">
          <h4 class="card-title">
            <StarOutlined />
            Writing Tips
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
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
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

// Auto-switch tabs based on progress
watch(
  () => creationStore.currentStepIndex,
  (idx) => {
    if (idx >= 2) {
      activeTabKey.value = 'content'
    } else if (idx >= 1) {
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

const renderOutline = computed(() => {
  return markdownToHtml(creationStore.outlineDisplay)
})

const renderContent = computed(() => {
  return markdownToHtml(creationStore.contentDisplay)
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
  { name: 'Hit the Pain Point', desc: 'Address what users care about most' },
  { name: 'Create Suspense', desc: 'Spark curiosity in readers' },
  { name: 'Use Numbers', desc: 'Concrete data adds persuasion' },
  { name: 'Tell a Story', desc: 'Narratives make content memorable' },
  { name: 'Keep It Concise', desc: 'Short paragraphs improve readability' },
  { name: 'Use Active Voice', desc: 'Direct sentences feel more energetic' },
  { name: 'Add a Call to Action', desc: 'Guide readers on what to do next' },
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
  // Keep SSE alive for navigation
})
</script>

<style scoped>
.create-page {
  min-height: calc(100vh - 64px);
  background: #f8fafc;
  padding: 24px;
}

.main-row {
  margin: 0 auto;
  align-items: flex-start;
}

/* ==================== Side Column ==================== */
.side-col {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-section,
.side-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.side-title {
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
  color: #1f1f1f;
}

.side-subtitle {
  font-size: 12px;
  color: #94a3b8;
  margin: 0 0 16px;
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
  padding: 12px 0;
  position: relative;
}

.step-item:not(:last-child)::after {
  content: '';
  position: absolute;
  left: 15px;
  top: 40px;
  bottom: 0;
  width: 1px;
  background: #e2e8f0;
}

.step-marker {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #f1f5f9;
  color: #94a3b8;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
  flex-shrink: 0;
  z-index: 1;
}

.step-check {
  font-size: 14px;
  color: #22c55e;
}

.step-active .step-marker {
  background: #22c55e;
  color: #fff;
}

.step-completed .step-marker {
  background: #dcfce7;
  color: #22c55e;
}

.step-name {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
  line-height: 20px;
}

.step-active .step-name {
  color: #22c55e;
  font-weight: 600;
}

.step-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
  line-height: 18px;
}

/* Side Cards */
.card-title {
  font-size: 14px;
  font-weight: 600;
  margin: 0 0 12px;
  color: #1f1f1f;
  display: flex;
  align-items: center;
  gap: 8px;
}

.card-title :deep(.anticon) {
  font-size: 16px;
}

.quota-body {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quota-text {
  font-size: 14px;
  color: #475569;
}

.topic-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.topic-tag-item {
  cursor: pointer;
  font-size: 13px;
  padding: 4px 10px;
  border-radius: 6px;
  background: #f8fafc;
  border-color: #e2e8f0;
  color: #475569;
  transition: all 0.2s;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.topic-tag-item:hover {
  background: #dcfce7;
  border-color: #22c55e;
  color: #16a34a;
}

.tips-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
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
  background: #22c55e;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.tip-name {
  font-size: 13px;
  font-weight: 500;
  color: #334155;
}

.tip-desc {
  font-size: 12px;
  color: #94a3b8;
  margin-top: 2px;
}

/* ==================== Center Column ==================== */
.center-col {
  display: flex;
  flex-direction: column;
}

.center-panel {
  background: #fff;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
  min-height: 600px;
}

/* Input Panel */
.center-title {
  font-size: 24px;
  font-weight: 700;
  text-align: center;
  margin: 0 0 8px;
  color: #0f172a;
}

.center-subtitle {
  font-size: 14px;
  color: #64748b;
  text-align: center;
  margin: 0 0 32px;
}

.topic-textarea {
  border-radius: 10px;
  font-size: 15px;
  resize: none;
}

.topic-textarea :deep(.ant-input) {
  border-radius: 10px;
}

.char-count {
  text-align: right;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
}

.start-btn {
  width: 100%;
  margin-top: 16px;
  border-radius: 10px;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  background: #22c55e;
  border-color: #22c55e;
}

.start-btn:hover {
  background: #16a34a;
  border-color: #16a34a;
}

.start-btn:disabled {
  background: #e2e8f0;
  border-color: #e2e8f0;
  color: #94a3b8;
}

.error-alert {
  margin-top: 20px;
}

.success-result {
  margin-top: 20px;
}

/* Preview Panel */
.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f1f5f9;
}

.topic-tag {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.preview-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 16px;
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
  color: #cbd5e1;
}

.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1.2em;
  background: #1890ff;
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
  gap: 12px;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f1f5f9;
}

/* Markdown body styles */
.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3),
.markdown-body :deep(h4),
.markdown-body :deep(h5),
.markdown-body :deep(h6) {
  margin-top: 16px;
  margin-bottom: 12px;
  font-weight: 600;
  line-height: 1.4;
  color: #262626;
}

.markdown-body :deep(h1) { font-size: 24px; }
.markdown-body :deep(h2) { font-size: 20px; }
.markdown-body :deep(h3) { font-size: 18px; }
.markdown-body :deep(h4) { font-size: 16px; }
.markdown-body :deep(p) {
  margin-bottom: 12px;
  line-height: 1.8;
  color: #434343;
}

.markdown-body :deep(code) {
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: monospace;
  font-size: 0.9em;
}

.markdown-body :deep(pre) {
  background: #f5f5f5;
  padding: 12px 16px;
  border-radius: 6px;
  overflow-x: auto;
  margin-bottom: 12px;
}

.markdown-body :deep(pre code) {
  background: none;
  padding: 0;
}

.markdown-body :deep(blockquote) {
  border-left: 4px solid #d9d9d9;
  padding-left: 12px;
  margin-left: 0;
  color: #595959;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  padding-left: 20px;
  margin-bottom: 12px;
}

.markdown-body :deep(li) {
  margin-bottom: 4px;
  line-height: 1.8;
}

.markdown-body :deep(img) {
  max-width: 100%;
  border-radius: 4px;
  margin: 12px 0;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 16px 0;
}

.markdown-body :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.markdown-body :deep(a:hover) {
  text-decoration: underline;
}

/* ==================== Responsive ==================== */
@media (max-width: 992px) {
  .side-col {
    margin-top: 16px;
  }

  .center-panel {
    padding: 20px;
    min-height: auto;
  }
}
</style>
