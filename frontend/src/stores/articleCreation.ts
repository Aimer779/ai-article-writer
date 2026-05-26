import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { createArticleTask } from '@/api/articleController'
import { SSE_MESSAGE_TYPE, CREATION_STEPS } from '@/constants/article'
import { createProgressEventSource, parseSseMessage } from '@/utils/article'
import type { SseMessageType } from '@/constants/article'

interface TypingQueueItem {
  target: 'outline' | 'content'
  chars: string[]
}

export const useArticleCreationStore = defineStore('articleCreation', () => {
  // ==================== State ====================
  const taskId = ref<string | null>(null)
  const status = ref<'idle' | 'creating' | 'connecting' | 'streaming' | 'completed' | 'failed'>('idle')
  const currentStepIndex = ref(-1)
  const outlineBuffer = ref('')
  const outlineDisplay = ref('')
  const contentBuffer = ref('')
  const contentDisplay = ref('')
  const logs = ref<string[]>([])
  const imageCount = ref(0)
  const error = ref<string | null>(null)
  const isConnected = ref(false)
  const articleTitle = ref('')
  const articleId = ref<number | null>(null)

  // Internal refs
  let eventSource: EventSource | null = null
  const typingQueue: TypingQueueItem[] = []
  let typingFrameId: number | null = null

  // ==================== Computed ====================
  const isCreating = computed(() => status.value === 'creating')
  const isStreaming = computed(() => status.value === 'streaming' || status.value === 'connecting')
  const isCompleted = computed(() => status.value === 'completed')
  const isFailed = computed(() => status.value === 'failed')

  const currentStep = computed(() => {
    if (currentStepIndex.value < 0) return null
    return CREATION_STEPS[currentStepIndex.value] ?? null
  })

  const canStart = computed(() => status.value === 'idle' || status.value === 'completed' || status.value === 'failed')

  // ==================== Helpers ====================
  function addLog(message: string) {
    const time = new Date().toLocaleTimeString()
    logs.value.push(`[${time}] ${message}`)
  }

  function advanceStep() {
    if (currentStepIndex.value < CREATION_STEPS.length - 1) {
      currentStepIndex.value++
    }
  }

  // ==================== Typing Effect ====================
  function startTyping() {
    if (typingFrameId !== null) return

    const typeFrame = () => {
      if (typingQueue.length === 0) {
        typingFrameId = null
        return
      }

      const item = typingQueue[0]
      // Type 3 characters per frame for a smooth but fast effect
      const chunkSize = 3
      const chars = item.chars.splice(0, chunkSize)

      if (item.target === 'outline') {
        outlineDisplay.value += chars.join('')
      } else {
        contentDisplay.value += chars.join('')
      }

      if (item.chars.length === 0) {
        typingQueue.shift()
      }

      typingFrameId = requestAnimationFrame(typeFrame)
    }

    typingFrameId = requestAnimationFrame(typeFrame)
  }

  function pushTyping(target: 'outline' | 'content', text: string) {
    if (!text) return
    typingQueue.push({ target, chars: text.split('') })
    startTyping()
  }

  function stopTyping() {
    if (typingFrameId !== null) {
      cancelAnimationFrame(typingFrameId)
      typingFrameId = null
    }
    // Flush remaining queue immediately
    while (typingQueue.length > 0) {
      const item = typingQueue.shift()!
      if (item.target === 'outline') {
        outlineDisplay.value += item.chars.join('')
      } else {
        contentDisplay.value += item.chars.join('')
      }
    }
  }

  // ==================== SSE Handlers ====================
  function handleSseMessage(msg: { type: SseMessageType; content?: string }) {
    switch (msg.type) {
      case SSE_MESSAGE_TYPE.AGENT1_COMPLETE:
        advanceStep()
        addLog('Title options generated.')
        if (msg.content) {
          articleTitle.value = msg.content
        }
        break

      case SSE_MESSAGE_TYPE.AGENT2_STREAMING:
        if (msg.content) {
          outlineBuffer.value += msg.content
          pushTyping('outline', msg.content)
        }
        break

      case SSE_MESSAGE_TYPE.AGENT2_COMPLETE:
        advanceStep()
        addLog('Outline completed.')
        stopTyping()
        // Ensure full outline is displayed
        outlineDisplay.value = outlineBuffer.value
        break

      case SSE_MESSAGE_TYPE.AGENT3_STREAMING:
        if (msg.content) {
          contentBuffer.value += msg.content
          pushTyping('content', msg.content)
        }
        break

      case SSE_MESSAGE_TYPE.AGENT3_COMPLETE:
        advanceStep()
        addLog('Content creation completed.')
        stopTyping()
        contentDisplay.value = contentBuffer.value
        break

      case SSE_MESSAGE_TYPE.AGENT4_COMPLETE:
        advanceStep()
        addLog('Image requirements analyzed.')
        break

      case SSE_MESSAGE_TYPE.IMAGE_COMPLETE:
        imageCount.value++
        addLog(`Image generated (${imageCount.value}).`)
        break

      case SSE_MESSAGE_TYPE.AGENT5_COMPLETE:
        advanceStep()
        addLog('All images generated.')
        break

      case SSE_MESSAGE_TYPE.MERGE_COMPLETE:
        addLog('Content merged with images.')
        break

      case SSE_MESSAGE_TYPE.ALL_COMPLETE:
        status.value = 'completed'
        isConnected.value = false
        addLog('Article creation completed!')
        stopTyping()
        // Ensure everything is fully displayed
        outlineDisplay.value = outlineBuffer.value
        contentDisplay.value = contentBuffer.value
        break

      case SSE_MESSAGE_TYPE.ERROR:
        status.value = 'failed'
        isConnected.value = false
        error.value = msg.content || 'Unknown error occurred during creation.'
        addLog(`Error: ${error.value}`)
        stopTyping()
        break

      default:
        // Unknown type, ignore or log
        if (msg.content) {
          addLog(msg.content)
        }
    }
  }

  // ==================== Actions ====================
  async function createTask(topic: string): Promise<string | null> {
    if (!topic.trim()) return null

    reset()
    status.value = 'creating'

    try {
      const res = await createArticleTask({ topic: topic.trim() })
      if (res.data.code === 0 && res.data.data) {
        const newTaskId = res.data.data
        taskId.value = newTaskId
        status.value = 'connecting'
        currentStepIndex.value = 0
        addLog(`Task created: ${newTaskId}`)
        return newTaskId
      } else {
        throw new Error(res.data.message || 'Failed to create task')
      }
    } catch (err) {
      status.value = 'failed'
      const msg = err instanceof Error ? err.message : 'Network error'
      error.value = msg
      addLog(`Failed to create task: ${msg}`)
      return null
    }
  }

  function connectSSE(id: string) {
    disconnectSSE()
    taskId.value = id
    status.value = 'connecting'
    currentStepIndex.value = 0
    addLog('Connecting to progress stream...')

    try {
      eventSource = createProgressEventSource(id)

      eventSource.onopen = () => {
        isConnected.value = true
        status.value = 'streaming'
        addLog('Connected to progress stream.')
      }

      eventSource.onmessage = (event) => {
        const msg = parseSseMessage(event.data)
        if (msg) {
          handleSseMessage(msg)
        }
      }

      eventSource.onerror = (_err) => {
        // EventSource error event does not carry much detail
        isConnected.value = false
        if (status.value !== 'completed' && status.value !== 'failed') {
          status.value = 'failed'
          error.value = 'SSE connection error. Please refresh to retry.'
          addLog(`Connection error.`)
        }
        disconnectSSE()
      }
    } catch (err: any) {
      status.value = 'failed'
      error.value = err.message || 'Failed to connect SSE'
      addLog(`Connection failed: ${error.value}`)
    }
  }

  function disconnectSSE() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
    isConnected.value = false
    stopTyping()
  }

  function reset() {
    disconnectSSE()
    taskId.value = null
    status.value = 'idle'
    currentStepIndex.value = -1
    outlineBuffer.value = ''
    outlineDisplay.value = ''
    contentBuffer.value = ''
    contentDisplay.value = ''
    logs.value = []
    imageCount.value = 0
    error.value = null
    articleTitle.value = ''
    articleId.value = null
  }

  return {
    // State
    taskId,
    status,
    currentStepIndex,
    outlineBuffer,
    outlineDisplay,
    contentBuffer,
    contentDisplay,
    logs,
    imageCount,
    error,
    isConnected,
    articleTitle,
    articleId,
    // Computed
    isCreating,
    isStreaming,
    isCompleted,
    isFailed,
    currentStep,
    canStart,
    // Actions
    createTask,
    connectSSE,
    disconnectSSE,
    reset,
    addLog,
  }
})
