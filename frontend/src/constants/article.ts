/**
 * Article status constants matching backend enum values.
 */
export const ARTICLE_STATUS = {
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  WAITING_USER_INPUT: 'WAITING_USER_INPUT',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
} as const

export type ArticleStatus = (typeof ARTICLE_STATUS)[keyof typeof ARTICLE_STATUS]

/**
 * SSE message types from SseMessageTypeEnum.
 */
export const SSE_MESSAGE_TYPE = {
  AGENT1_COMPLETE: 'AGENT1_COMPLETE',
  WAITING_USER_INPUT: 'WAITING_USER_INPUT',
  AGENT2_STREAMING: 'AGENT2_STREAMING',
  AGENT2_COMPLETE: 'AGENT2_COMPLETE',
  AGENT3_STREAMING: 'AGENT3_STREAMING',
  AGENT3_COMPLETE: 'AGENT3_COMPLETE',
  AGENT4_COMPLETE: 'AGENT4_COMPLETE',
  IMAGE_COMPLETE: 'IMAGE_COMPLETE',
  AGENT5_COMPLETE: 'AGENT5_COMPLETE',
  MERGE_COMPLETE: 'MERGE_COMPLETE',
  ALL_COMPLETE: 'ALL_COMPLETE',
  ERROR: 'ERROR',
} as const

export type SseMessageType = (typeof SSE_MESSAGE_TYPE)[keyof typeof SSE_MESSAGE_TYPE]

/**
 * Workflow step configuration for the creation progress UI.
 * Maps SSE events to user-friendly step labels.
 */
export const CREATION_STEPS = [
  {
    key: 'title',
    title: 'Title decision',
    description: 'Generate and choose title',
    completeTypes: [SSE_MESSAGE_TYPE.AGENT1_COMPLETE, SSE_MESSAGE_TYPE.WAITING_USER_INPUT],
  },
  {
    key: 'outline',
    title: 'Outline Writing',
    description: 'Streaming article outline',
    completeTypes: [SSE_MESSAGE_TYPE.AGENT2_COMPLETE],
    streamingType: SSE_MESSAGE_TYPE.AGENT2_STREAMING,
  },
  {
    key: 'content',
    title: 'Content Creation',
    description: 'Writing article body',
    completeTypes: [SSE_MESSAGE_TYPE.AGENT3_COMPLETE],
    streamingType: SSE_MESSAGE_TYPE.AGENT3_STREAMING,
  },
  {
    key: 'imageRequirement',
    title: 'Image Analysis',
    description: 'Analyzing image requirements',
    completeTypes: [SSE_MESSAGE_TYPE.AGENT4_COMPLETE],
  },
  {
    key: 'imageGeneration',
    title: 'Image Generation',
    description: 'Generating article images',
    completeTypes: [SSE_MESSAGE_TYPE.AGENT5_COMPLETE, SSE_MESSAGE_TYPE.IMAGE_COMPLETE],
  },
  {
    key: 'merge',
    title: 'Final Assembly',
    description: 'Merging content and images',
    completeTypes: [SSE_MESSAGE_TYPE.MERGE_COMPLETE, SSE_MESSAGE_TYPE.ALL_COMPLETE],
  },
] as const

/**
 * Status display mapping for article list/filter.
 */
export const ARTICLE_STATUS_TEXT: Record<string, string> = {
  [ARTICLE_STATUS.PENDING]: 'Pending',
  [ARTICLE_STATUS.PROCESSING]: 'Processing',
  [ARTICLE_STATUS.WAITING_USER_INPUT]: 'Waiting for input',
  [ARTICLE_STATUS.COMPLETED]: 'Completed',
  [ARTICLE_STATUS.FAILED]: 'Failed',
}

export const ARTICLE_STATUS_COLOR: Record<string, string> = {
  [ARTICLE_STATUS.PENDING]: 'default',
  [ARTICLE_STATUS.PROCESSING]: 'processing',
  [ARTICLE_STATUS.WAITING_USER_INPUT]: 'warning',
  [ARTICLE_STATUS.COMPLETED]: 'success',
  [ARTICLE_STATUS.FAILED]: 'error',
}
