<template>
  <div class="create-page">
    <!-- Idle / Input State -->
    <div v-if="showInputPanel" class="input-panel">
      <div class="input-container">
        <h1 class="page-title">AI Article Creation</h1>
        <p class="page-subtitle">
          Enter a topic and let AI generate a complete article with outline, content, and images.
        </p>
        <a-input
          v-model:value="topicInput"
          size="large"
          placeholder="e.g., The Future of Artificial Intelligence in Healthcare"
          :disabled="!creationStore.canStart"
          @pressEnter="handleStart"
        />
        <a-button
          type="primary"
          size="large"
          :loading="creationStore.isCreating"
          :disabled="!topicInput.trim() || !creationStore.canStart"
          @click="handleStart"
        >
          Start Creation
        </a-button>

        <!-- Error Alert -->
        <a-alert
          v-if="creationStore.isFailed && creationStore.error"
          type="error"
          :message="creationStore.error"
          show-icon
          closable
          class="error-alert"
          @close="creationStore.reset()"
        />

        <!-- Completed Summary -->
        <div v-if="creationStore.isCompleted" class="completed-summary">
          <a-result status="success" title="Article Created Successfully!">
            <template #extra>
              <a-button type="primary" @click="goToArticle">
                View Article
              </a-button>
              <a-button @click="creationStore.reset()">
                Create Another
              </a-button>
            </template>
          </a-result>
        </div>
      </div>
    </div>

    <!-- Creation Progress State -->
    <div v-else class="progress-panel">
      <!-- Top bar -->
      <div class="progress-header">
        <div class="topic-display">
          <span class="label">Topic:</span>
          <span class="value">{{ topicInput }}</span>
        </div>
        <div class="status-display">
          <a-tag v-if="creationStore.isConnected" color="processing">Streaming</a-tag>
          <a-tag v-else-if="creationStore.isCreating" color="processing">Creating Task</a-tag>
          <a-tag v-else-if="creationStore.isCompleted" color="success">Completed</a-tag>
          <a-tag v-else-if="creationStore.isFailed" color="error">Failed</a-tag>
          <span v-if="creationStore.taskId" class="task-id">Task: {{ creationStore.taskId }}</span>
        </div>
      </div>

      <a-row :gutter="24" class="progress-body">
        <!-- Left: Progress & Logs -->
        <a-col :xs="24" :lg="8" class="left-panel">
          <a-card title="Progress" :bordered="false" class="progress-card">
            <a-steps
              direction="vertical"
              :current="creationStore.currentStepIndex"
              size="small"
              class="creation-steps"
            >
              <a-step
                v-for="step in CREATION_STEPS"
                :key="step.key"
                :title="step.title"
                :description="step.description"
              />
            </a-steps>
          </a-card>

          <a-card title="Execution Log" :bordered="false" class="log-card">
            <div ref="logContainer" class="log-container">
              <div
                v-for="(log, index) in creationStore.logs"
                :key="index"
                class="log-item"
              >
                {{ log }}
              </div>
              <div v-if="creationStore.logs.length === 0" class="log-empty">
                Waiting to start...
              </div>
            </div>
          </a-card>

          <a-card v-if="creationStore.imageCount > 0" :bordered="false" class="image-card">
            <div class="image-stat">
              <PictureOutlined />
              <span>Images generated: {{ creationStore.imageCount }}</span>
            </div>
          </a-card>
        </a-col>

        <!-- Right: Content Preview -->
        <a-col :xs="24" :lg="16" class="right-panel">
          <a-card :bordered="false" class="preview-card">
            <a-tabs v-model:activeKey="activeTabKey">
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
                  <!-- Typing cursor indicator -->
                  <span
                    v-if="isTypingOutline"
                    class="typing-cursor"
                  />
                </div>
              </a-tab-pane>

              <a-tab-pane key="content" tab="Content" :disabled="creationStore.currentStepIndex < 2">
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
                  <span
                    v-if="isTypingContent"
                    class="typing-cursor"
                  />
                </div>
              </a-tab-pane>
            </a-tabs>
          </a-card>
        </a-col>
      </a-row>

      <!-- Bottom actions -->
      <div v-if="creationStore.isCompleted || creationStore.isFailed" class="bottom-actions">
        <a-button v-if="creationStore.isCompleted" type="primary" @click="goToArticle">
          View Full Article
        </a-button>
        <a-button @click="handleReset">
          {{ creationStore.isCompleted ? 'Create Another' : 'Try Again' }}
        </a-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { PictureOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useArticleCreationStore } from '@/stores/articleCreation'
import { CREATION_STEPS } from '@/constants/article'
import { markdownToHtml } from '@/utils/article'

const router = useRouter()
const creationStore = useArticleCreationStore()

const topicInput = ref('')
const activeTabKey = ref('outline')
const logContainer = ref<HTMLDivElement | null>(null)

// Auto-switch to content tab when content generation starts
watch(
  () => creationStore.currentStepIndex,
  (idx) => {
    if (idx >= 2) {
      activeTabKey.value = 'content'
    } else if (idx >= 1) {
      activeTabKey.value = 'outline'
    }
  },
)

// Auto-scroll logs
watch(
  () => creationStore.logs.length,
  async () => {
    await nextTick()
    if (logContainer.value) {
      logContainer.value.scrollTop = logContainer.value.scrollHeight
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
  // Do not disconnect on unmount so user can navigate away and back
  // creationStore.disconnectSSE()
})
</script>

<style scoped>
.create-page {
  min-height: calc(100vh - 128px);
}

/* Input Panel */
.input-panel {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 500px;
  padding: 48px 24px;
}

.input-container {
  width: 100%;
  max-width: 640px;
  text-align: center;
}

.page-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 12px;
  color: #1f1f1f;
}

.page-subtitle {
  font-size: 16px;
  color: #595959;
  margin-bottom: 32px;
}

.input-container .ant-input {
  margin-bottom: 16px;
}

.input-container .ant-btn {
  min-width: 160px;
}

.error-alert {
  margin-top: 24px;
  text-align: left;
}

.completed-summary {
  margin-top: 32px;
}

/* Progress Panel */
.progress-panel {
  padding: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.topic-display .label {
  color: #8c8c8c;
  margin-right: 8px;
}

.topic-display .value {
  font-weight: 600;
  color: #262626;
}

.status-display {
  display: flex;
  align-items: center;
  gap: 12px;
}

.task-id {
  font-size: 12px;
  color: #8c8c8c;
  font-family: monospace;
}

.progress-body {
  align-items: flex-start;
}

.left-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-card,
.log-card,
.image-card,
.preview-card {
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}

.creation-steps :deep(.ant-steps-item-description) {
  font-size: 12px;
  color: #8c8c8c;
}

.log-container {
  max-height: 300px;
  overflow-y: auto;
  font-family: monospace;
  font-size: 12px;
  line-height: 1.8;
  background: #fafafa;
  padding: 12px;
  border-radius: 4px;
}

.log-item {
  color: #595959;
  white-space: pre-wrap;
  word-break: break-all;
}

.log-empty {
  color: #bfbfbf;
  text-align: center;
  padding: 24px 0;
}

.image-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #595959;
}

/* Content Preview */
.preview-card {
  min-height: 600px;
}

.content-preview {
  min-height: 500px;
  padding: 8px 4px;
  position: relative;
}

.empty-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #bfbfbf;
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

/* Bottom actions */
.bottom-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 24px;
  padding: 16px;
}

/* Responsive */
@media (max-width: 992px) {
  .progress-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .left-panel {
    margin-bottom: 16px;
  }
}
</style>
