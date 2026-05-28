import type { SseMessageType } from '@/constants/article'

/**
 * SSE message payload structure from backend.
 */
export interface SseMessage {
  type: SseMessageType
  taskId?: string
  content?: string
  message?: string
  step?: string
  selectedTitle?: {
    mainTitle?: string
    subTitle?: string
  }
  titleOptions?: Array<{
    mainTitle?: string
    subTitle?: string
  }>
  error?: string
}

/**
 * Create an EventSource connection for article generation progress.
 * @param taskId The article task ID
 * @returns EventSource instance
 */
export function createProgressEventSource(taskId: string): EventSource {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8567'
  const url = `${baseUrl}/api/article/progress/${encodeURIComponent(taskId)}`
  const es = new EventSource(url, { withCredentials: true })
  return es
}

/**
 * Parse SSE event data to a typed message object.
 * Returns null if parsing fails.
 */
export function parseSseMessage(data: string): SseMessage | null {
  try {
    const parsed = JSON.parse(data)
    if (parsed && typeof parsed.type === 'string') {
      return parsed as SseMessage
    }
  } catch {
    // Some legacy messages may use "TYPE:content" format
    const colonIndex = data.indexOf(':')
    if (colonIndex > 0) {
      const type = data.substring(0, colonIndex).trim()
      const content = data.substring(colonIndex + 1).trim()
      return { type: type as SseMessageType, content }
    }
  }
  return null
}

/**
 * Download text content as a file.
 * @param content File content
 * @param filename File name with extension
 * @param mimeType MIME type
 */
export function downloadFile(content: string, filename: string, mimeType = 'text/markdown'): void {
  const blob = new Blob([content], { type: `${mimeType};charset=utf-8` })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = filename
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}

/**
 * Download article as a Markdown file.
 * Includes main title, sub title, cover image, and content.
 */
export function downloadArticleAsMarkdown(article: {
  mainTitle?: string
  subTitle?: string
  coverImage?: string
  fullContent?: string
  content?: string
}): void {
  const safeTitle = (article.mainTitle || 'untitled').replace(/[\\/:*?"<>|]/g, '_')
  let markdown = ''
  if (article.mainTitle) {
    markdown += `# ${article.mainTitle}\n\n`
  }
  if (article.subTitle) {
    markdown += `> ${article.subTitle}\n\n`
  }
  if (article.coverImage) {
    markdown += `![Cover](${article.coverImage})\n\n`
  }
  markdown += article.fullContent || article.content || ''
  downloadFile(markdown, `${safeTitle}.md`, 'text/markdown')
}

/**
 * Re-export theme-aware markdown renderer.
 * For plain rendering without theme, call markdownToHtml(content).
 * For themed rendering, call markdownToHtml(content, themeKey).
 */
export { markdownToHtml } from './markdown'
