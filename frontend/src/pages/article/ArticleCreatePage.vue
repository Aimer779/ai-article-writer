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
          <div class="prompt-container" :class="{ 'prompt-disabled': !creationStore.canStart }">
            <div class="textarea-wrapper">
              <textarea
                v-model="topicInput"
                :rows="4"
                :disabled="!creationStore.canStart"
                class="prompt-textarea"
                @keydown.enter.prevent="handleStart"
                @focus="handleFocus"
                @blur="handleBlur"
              />
              <span
                v-if="!topicInput && !isFocused && creationStore.canStart"
                class="typing-placeholder"
              >
                {{ typingPlaceholder }}<span class="typing-cursor"></span>
              </span>
            </div>
            <div class="input-toolbar">
              <div class="toolbar-left">
                <span class="char-count">{{ topicInput.length }} / 500</span>
              </div>
              <div class="toolbar-right">
                <button
                  class="submit-button"
                  :class="{ 'submit-active': topicInput.trim() && creationStore.canStart && !creationStore.isCreating }"
                  :disabled="!topicInput.trim() || !creationStore.canStart || creationStore.isCreating"
                  @click="handleStart"
                >
                  <RocketOutlined />
                </button>
              </div>
            </div>
          </div>

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
                :class="{ 
                  'option-pill-active': selectedMethods.includes(method.value), 
                  'option-pill-disabled': !loginUserStore.isVip && isVipMethod(method.value) 
                }"
              >
                <CrownOutlined v-if="isVipMethod(method.value)" />
                <input
                  v-model="selectedMethods"
                  type="checkbox"
                  :value="method.value"
                  :disabled="!loginUserStore.isVip && isVipMethod(method.value)"
                  class="option-input"
                />
                {{ method.label }}
              </label>
            </div>
          </div>

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
            <a-space>
              <a-select
                v-model:value="selectedTheme"
                placeholder="Theme"
                style="width: 160px"
                :options="themeOptions"
                allow-clear
                size="small"
              />
              <span
                v-if="creationStore.isCreating"
                class="status-badge processing"
              >Creating</span>
              <span
                v-else-if="creationStore.isWaitingTitle"
                class="status-badge waiting"
              >Waiting</span>
              <span
                v-else-if="creationStore.isRegeneratingTitle"
                class="status-badge processing"
              >Regenerating</span>
              <span
                v-else-if="creationStore.isConnected"
                class="status-badge processing"
              >Streaming</span>
              <span
                v-else-if="creationStore.isCompleted"
                class="status-badge completed"
              >Completed</span>
              <span
                v-else-if="creationStore.isFailed"
                class="status-badge failed"
              >Failed</span>
            </a-space>
          </div>

          <a-tabs v-model:activeKey="activeTabKey" class="preview-tabs">
            <a-tab-pane key="title" tab="Title">
              <div class="content-preview">
                <div v-if="showTitleDecision" class="title-decision">
                  <div class="decision-header">
                    <div>
                      <h2>Choose a title</h2>
                      <p>Pick one option to continue, or add direction and regenerate the list.</p>
                    </div>
                    <span class="status-badge waiting">Decision</span>
                  </div>

                  <div class="title-options">
                    <div v-if="creationStore.isRegeneratingTitle" class="regenerating-state">
                      <a-spin />
                      <span>Regenerating title options...</span>
                    </div>
                    <button
                      v-for="(option, index) in creationStore.titleOptions"
                      :key="`${option.mainTitle}-${index}`"
                      class="title-option"
                      :class="{ 'title-option-selected': creationStore.selectedTitleIndex === index }"
                      :disabled="creationStore.isRegeneratingTitle"
                      @click="handleChooseTitle(index)"
                    >
                      <span class="option-index">{{ index + 1 }}</span>
                      <span class="option-copy">
                        <strong>{{ option.mainTitle }}</strong>
                        <span v-if="option.subTitle">{{ option.subTitle }}</span>
                      </span>
                    </button>
                  </div>

                  <div class="requirement-box">
                    <a-textarea
                      v-model:value="additionalRequirement"
                      :rows="3"
                      :maxlength="500"
                      :disabled="creationStore.isRegeneratingTitle"
                      placeholder="Add audience, tone, angle, or other title direction"
                    />
                    <div class="requirement-actions">
                      <span class="char-count">{{ additionalRequirement.length }} / 500</span>
                      <a-button
                        :loading="creationStore.isRegeneratingTitle"
                        :disabled="!additionalRequirement.trim()"
                        @click="handleRegenerateTitles"
                      >
                        <template #icon><ReloadOutlined /></template>
                        Regenerate titles
                      </a-button>
                    </div>
                  </div>
                </div>
                <div v-else-if="creationStore.articleTitle" class="markdown-body">
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
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  CheckOutlined,
  RocketOutlined,
  CrownOutlined,
  FireOutlined,
  StarOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import { useArticleCreationStore } from '@/stores/articleCreation'
import { useLoginUserStore } from '@/stores'
import { CREATION_STEPS } from '@/constants/article'
import { markdownToHtml } from '@/utils/article'
import { getStyleList } from '@/constants/themes'
import 'highlight.js/styles/github.css'

const router = useRouter()
const creationStore = useArticleCreationStore()
const loginUserStore = useLoginUserStore()

const topicInput = ref('')
const isFocused = ref(false)
const activeTabKey = ref('title')
const titlePreviewReady = ref(false)
let titlePreviewTimer: number | null = null
const additionalRequirement = ref('')

const selectedStyle = ref('default')
const selectedMethods = ref<string[]>([])
const selectedTheme = ref<string>('')

const themeOptions = computed(() => {
  return getStyleList().map((t) => ({ label: t.name, value: t.key }))
})

/* ---------- Typing Placeholder Effect ---------- */
const PLACEHOLDER_TOPICS = [
  'The Future of Artificial Intelligence in Healthcare',
  'How Machine Learning is Transforming Education',
  'The Rise of Remote Work in Tech Industry',
]

const typingPlaceholder = ref('')
const typingTopicIndex = ref(0)
const typingCharIndex = ref(0)
const typingPhase = ref<'typing' | 'waiting' | 'deleting'>('typing')
let typingTimer: number | null = null

function runTypingEffect() {
  if (topicInput.value) return

  const currentTopic = PLACEHOLDER_TOPICS[typingTopicIndex.value]

  if (typingPhase.value === 'typing') {
    if (typingCharIndex.value < currentTopic.length) {
      typingCharIndex.value++
      typingPlaceholder.value = currentTopic.slice(0, typingCharIndex.value)
      typingTimer = window.setTimeout(runTypingEffect, 80)
    } else {
      typingPhase.value = 'waiting'
      typingTimer = window.setTimeout(() => {
        typingPhase.value = 'deleting'
        runTypingEffect()
      }, 2000)
    }
  } else if (typingPhase.value === 'deleting') {
    if (typingCharIndex.value > 0) {
      typingCharIndex.value--
      typingPlaceholder.value = currentTopic.slice(0, typingCharIndex.value)
      typingTimer = window.setTimeout(runTypingEffect, 40)
    } else {
      typingTopicIndex.value = (typingTopicIndex.value + 1) % PLACEHOLDER_TOPICS.length
      typingPhase.value = 'typing'
      runTypingEffect()
    }
  }
}

function stopTypingEffect() {
  if (typingTimer !== null) {
    window.clearTimeout(typingTimer)
    typingTimer = null
  }
}

function handleFocus() {
  isFocused.value = true
  stopTypingEffect()
}

function handleBlur() {
  isFocused.value = false
  if (!topicInput.value) {
    typingCharIndex.value = 0
    typingPhase.value = 'typing'
    runTypingEffect()
  }
}

watch(topicInput, (val) => {
  if (val) {
    stopTypingEffect()
    typingPlaceholder.value = ''
  }
})

onMounted(() => {
  runTypingEffect()
})

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

const VIP_IMAGE_METHODS = ['nano_banana', 'svg']

function isVipMethod(value: string) {
  return VIP_IMAGE_METHODS.includes(value)
}

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
  const html = markdownToHtml(creationStore.outlineDisplay, selectedTheme.value || undefined)
  return isTypingOutline.value ? appendTypingCursor(html) : html
})

const renderContent = computed(() => {
  const html = markdownToHtml(creationStore.contentDisplay, selectedTheme.value || undefined)
  return isTypingContent.value ? appendTypingCursor(html) : html
})

const showTitleDecision = computed(() => {
  return creationStore.titleOptions.length > 0 || creationStore.isWaitingTitle || creationStore.isRegeneratingTitle
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

async function handleChooseTitle(index: number) {
  const ok = await creationStore.chooseTitle(index)
  if (ok) {
    message.success('Title selected')
  }
}

async function handleRegenerateTitles() {
  const requirement = additionalRequirement.value.trim()
  if (!requirement) {
    message.warning('Please add title direction')
    return
  }
  const ok = await creationStore.regenerateTitleOptions(requirement)
  if (ok) {
    additionalRequirement.value = ''
  }
}

function handleReset() {
  const currentTopic = topicInput.value
  creationStore.reset()
  topicInput.value = currentTopic
  additionalRequirement.value = ''
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
  stopTypingEffect()
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

.prompt-container {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 24px;
  padding: 20px 20px 16px;
  transition: border-color 200ms ease, box-shadow 200ms ease;
}

.prompt-container:focus-within {
  border-color: var(--border-strong);
  box-shadow: 0 0 0 3px oklch(55% 0.14 55 / 0.15);
}

.prompt-disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.textarea-wrapper {
  position: relative;
}

.prompt-textarea {
  width: 100%;
  background: transparent;
  border: none;
  outline: none;
  resize: none;
  padding: 0;
  font-family: inherit;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.4;
  color: var(--ink);
}

.prompt-textarea::placeholder {
  color: var(--text-muted);
}

.prompt-textarea:focus,
.prompt-textarea:hover {
  background: transparent;
  border-color: transparent;
}

.prompt-textarea:disabled {
  background: transparent;
  color: var(--text-muted);
  cursor: not-allowed;
}

.typing-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  font-family: inherit;
  font-size: 14px;
  font-weight: 400;
  line-height: 1.4;
  color: var(--text-muted);
  pointer-events: none;
  user-select: none;
  white-space: pre-wrap;
  word-break: break-word;
}

.typing-cursor {
  display: inline-block;
  width: 2px;
  height: 1.2em;
  background: var(--accent);
  margin-left: 2px;
  vertical-align: text-bottom;
  animation: cursorBlink 1s step-end infinite;
}

@keyframes cursorBlink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0;
  }
}

.input-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 12px;
}

.toolbar-left {
  display: flex;
  align-items: center;
}

.toolbar-right {
  display: flex;
  align-items: center;
}

.char-count {
  font-size: 12px;
  color: var(--text-muted);
  line-height: 32px;
}

.submit-button {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.08);
  color: var(--text-muted);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 150ms ease;
  font-size: 16px;
}

.submit-button:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.14);
}

.submit-button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.submit-active {
  background: var(--accent) !important;
  color: #ffffff !important;
}

.submit-active:hover:not(:disabled) {
  background: var(--accent-hover) !important;
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

.option-pill-disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.option-pill-disabled:hover {
  border-color: var(--border);
  color: var(--text-secondary);
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

.status-badge.waiting {
  background: var(--warning-subtle);
  color: oklch(46% 0.1 75);
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

.title-decision {
  display: flex;
  flex-direction: column;
  gap: var(--space-4);
}

.decision-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: var(--space-3);
  padding-bottom: var(--space-3);
  border-bottom: 1px solid var(--border);
}

.decision-header h2 {
  margin: 0 0 4px;
  font-size: 18px;
  color: var(--ink);
}

.decision-header p {
  margin: 0;
  font-size: 13px;
  color: var(--text-secondary);
}

.title-options {
  display: grid;
  grid-template-columns: 1fr;
  gap: var(--space-3);
}

.regenerating-state {
  min-height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--space-2);
  color: var(--text-secondary);
  font-size: 13px;
}

.title-option {
  width: 100%;
  min-height: 82px;
  display: flex;
  align-items: flex-start;
  gap: var(--space-3);
  padding: var(--space-4);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--surface);
  color: var(--ink);
  text-align: left;
  cursor: pointer;
  transition: transform 0.15s ease, border-color 0.15s ease, background-color 0.15s ease;
}

.title-option:hover:not(:disabled) {
  border-color: var(--accent);
  background: var(--accent-subtle);
  transform: translateY(-1px);
}

.title-option:active:not(:disabled) {
  transform: scale(0.98);
}

.title-option:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.title-option-selected {
  border-color: var(--accent);
  background: var(--accent-subtle);
}

.option-index {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--canvas);
  border: 1px solid var(--border);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.title-option-selected .option-index,
.title-option:hover:not(:disabled) .option-index {
  background: var(--accent);
  border-color: var(--accent);
  color: #fff;
}

.option-copy {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.option-copy strong {
  font-size: 15px;
  line-height: 1.45;
  color: var(--ink);
}

.option-copy span {
  font-size: 13px;
  line-height: 1.55;
  color: var(--text-secondary);
}

.requirement-box {
  display: flex;
  flex-direction: column;
  gap: var(--space-2);
  padding: var(--space-3);
  border: 1px solid var(--border);
  border-radius: var(--radius-md);
  background: var(--canvas);
}

.requirement-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--space-3);
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

  .decision-header,
  .requirement-actions {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
