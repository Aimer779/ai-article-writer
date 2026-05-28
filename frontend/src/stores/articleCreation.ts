import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { confirmOutline, createArticleTask, regenerateTitles, selectTitle } from '@/api/articleController'
import { SSE_MESSAGE_TYPE, CREATION_STEPS } from '@/constants/article'
import { createProgressEventSource, parseSseMessage } from '@/utils/article'
import type { SseMessage } from '@/utils/article'

interface TypingQueueItem {
  target: 'outline' | 'content'
  chars: string[]
}

interface TitleOption {
  mainTitle?: string
  subTitle?: string
}

export const useArticleCreationStore = defineStore('articleCreation', () => {
  // ==================== State ====================
  const taskId = ref<string | null>(null)
  const status = ref<
    | 'idle'
    | 'creating'
    | 'connecting'
    | 'streaming'
    | 'waitingTitle'
    | 'regeneratingTitle'
    | 'waitingOutline'
    | 'confirmingOutline'
    | 'completed'
    | 'failed'
  >('idle')
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
  const articleSubTitle = ref('')
  const articleId = ref<number | null>(null)
  const titleOptions = ref<TitleOption[]>([])
  const selectedTitleIndex = ref<number | null>(null)
  const editableOutline = ref('')

  // Internal refs
  let eventSource: EventSource | null = null
  const typingQueue: TypingQueueItem[] = []
  let typingFrameId: number | null = null

  // ==================== Computed ====================
  const isCreating = computed(() => status.value === 'creating')
  const isStreaming = computed(() => status.value === 'streaming' || status.value === 'connecting')
  const isWaitingTitle = computed(() => status.value === 'waitingTitle')
  const isRegeneratingTitle = computed(() => status.value === 'regeneratingTitle')
  const isWaitingOutline = computed(() => status.value === 'waitingOutline')
  const isConfirmingOutline = computed(() => status.value === 'confirmingOutline')
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

  function enterOutlineReview() {
    stopTyping()
    outlineDisplay.value = outlineBuffer.value
    editableOutline.value = outlineDisplay.value
    currentStepIndex.value = 1
    if (status.value !== 'waitingOutline') {
      status.value = 'waitingOutline'
      addLog('Outline generated. Waiting for your review.')
    }
  }

  // ==================== SSE Handlers ====================
  function handleSseMessage(msg: SseMessage) {
    switch (msg.type) {
      case SSE_MESSAGE_TYPE.AGENT1_COMPLETE:
        if (msg.titleOptions?.length) {
          titleOptions.value = msg.titleOptions
          selectedTitleIndex.value = null
          articleTitle.value = ''
          articleSubTitle.value = ''
          status.value = 'waitingTitle'
          currentStepIndex.value = 0
          addLog('Title options generated. Waiting for your selection.')
          break
        }

        advanceStep()
        addLog('Title selected. Continuing article generation.')
        titleOptions.value = []
        if (msg.selectedTitle?.mainTitle) {
          articleTitle.value = msg.selectedTitle.mainTitle
          articleSubTitle.value = msg.selectedTitle.subTitle || ''
        } else if (msg.content) {
          articleTitle.value = msg.content
        }
        break

      case SSE_MESSAGE_TYPE.WAITING_USER_INPUT:
        if (msg.step === 'TITLE_SELECTION') {
          status.value = 'waitingTitle'
          currentStepIndex.value = 0
        } else if (msg.step === 'OUTLINE_REVIEW') {
          enterOutlineReview()
        }
        break

      case SSE_MESSAGE_TYPE.AGENT2_STREAMING:
        if (msg.content) {
          outlineBuffer.value += msg.content
          pushTyping('outline', msg.content)
        }
        break

      case SSE_MESSAGE_TYPE.AGENT2_COMPLETE:
        stopTyping()
        if (msg.outline !== undefined) {
          outlineBuffer.value = msg.outline
        } else {
          outlineBuffer.value = outlineDisplay.value || outlineBuffer.value
        }
        addLog('Outline completed.')
        enterOutlineReview()
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
        error.value = msg.content || msg.message || 'Unknown error occurred during creation.'
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

  async function chooseTitle(index: number): Promise<boolean> {
    if (!taskId.value) return false

    selectedTitleIndex.value = index
    try {
      const res = await selectTitle({ taskId: taskId.value }, { titleIndex: index })
      if (res.data.code === 0 && res.data.data) {
        status.value = 'streaming'
        addLog(`Title option ${index + 1} selected.`)
        return true
      }
      throw new Error(res.data.message || 'Failed to select title')
    } catch (err) {
      status.value = 'failed'
      const msg = err instanceof Error ? err.message : 'Network error'
      error.value = msg
      addLog(`Failed to select title: ${msg}`)
      return false
    }
  }

  async function regenerateTitleOptions(additionalRequirement: string): Promise<boolean> {
    if (!taskId.value || !additionalRequirement.trim()) return false

    status.value = 'regeneratingTitle'
    titleOptions.value = []
    selectedTitleIndex.value = null
    addLog('Regenerating title options with your added direction...')

    try {
      const res = await regenerateTitles(
        { taskId: taskId.value },
        { additionalRequirement: additionalRequirement.trim() },
      )
      if (res.data.code === 0 && res.data.data) {
        return true
      }
      throw new Error(res.data.message || 'Failed to regenerate titles')
    } catch (err) {
      status.value = 'failed'
      const msg = err instanceof Error ? err.message : 'Network error'
      error.value = msg
      addLog(`Failed to regenerate titles: ${msg}`)
      return false
    }
  }

  async function confirmReviewedOutline(outlineMarkdown: string): Promise<boolean> {
    if (!taskId.value || !outlineMarkdown.trim()) return false

    status.value = 'confirmingOutline'
    addLog('Confirming outline and continuing article generation...')

    try {
      const confirmedOutline = outlineMarkdown.trim()
      const res = await confirmOutline(
        { taskId: taskId.value },
        { outlineMarkdown: confirmedOutline },
      )
      if (res.data.code === 0 && res.data.data) {
        outlineBuffer.value = confirmedOutline
        outlineDisplay.value = confirmedOutline
        editableOutline.value = confirmedOutline
        status.value = 'streaming'
        currentStepIndex.value = 2
        addLog('Outline confirmed. Continuing content creation.')
        return true
      }
      throw new Error(res.data.message || 'Failed to confirm outline')
    } catch (err) {
      status.value = 'failed'
      const msg = err instanceof Error ? err.message : 'Network error'
      error.value = msg
      addLog(`Failed to confirm outline: ${msg}`)
      return false
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
        if (
          status.value !== 'waitingTitle' &&
          status.value !== 'regeneratingTitle' &&
          status.value !== 'waitingOutline' &&
          status.value !== 'confirmingOutline' &&
          status.value !== 'completed' &&
          status.value !== 'failed'
        ) {
          status.value = 'streaming'
        }
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
    articleSubTitle.value = ''
    articleId.value = null
    titleOptions.value = []
    selectedTitleIndex.value = null
    editableOutline.value = ''
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
    articleSubTitle,
    articleId,
    titleOptions,
    selectedTitleIndex,
    editableOutline,
    // Computed
    isCreating,
    isStreaming,
    isWaitingTitle,
    isRegeneratingTitle,
    isWaitingOutline,
    isConfirmingOutline,
    isCompleted,
    isFailed,
    currentStep,
    canStart,
    // Actions
    createTask,
    connectSSE,
    chooseTitle,
    regenerateTitleOptions,
    confirmReviewedOutline,
    disconnectSSE,
    reset,
    addLog,
  }
})
