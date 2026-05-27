import type { SseMessageType } from '@/constants/article'

/**
 * SSE message payload structure from backend.
 */
export interface SseMessage {
  type: SseMessageType
  content?: string
  message?: string
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
 * Simple Markdown to HTML converter for article display.
 * Handles headings, bold, italic, code, lists, blockquotes, horizontal rules, and paragraphs.
 */
export function markdownToHtml(md: string): string {
  if (!md) return ''

  let html = md
    // Escape HTML entities
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  // Horizontal rules
  html = html.replace(/^-{3,}$/gm, '<hr>')

  // Headings
  html = html.replace(/^###### (.*$)/gim, '<h6>$1</h6>')
  html = html.replace(/^##### (.*$)/gim, '<h5>$1</h5>')
  html = html.replace(/^#### (.*$)/gim, '<h4>$1</h4>')
  html = html.replace(/^### (.*$)/gim, '<h3>$1</h3>')
  html = html.replace(/^## (.*$)/gim, '<h2>$1</h2>')
  html = html.replace(/^# (.*$)/gim, '<h1>$1</h1>')

  // Bold & Italic
  html = html.replace(/\*\*\*(.*?)\*\*\*/g, '<strong><em>$1</em></strong>')
  html = html.replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
  html = html.replace(/\*(.*?)\*/g, '<em>$1</em>')
  html = html.replace(/__(.*?)__/g, '<strong>$1</strong>')
  html = html.replace(/_(.*?)_/g, '<em>$1</em>')

  // Inline code
  html = html.replace(/`([^`]+)`/g, '<code>$1</code>')

  // Code blocks
  html = html.replace(/```(?:\w*\n)?([\s\S]*?)```/g, '<pre><code>$1</code></pre>')

  // Images
  html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img alt="$1" src="$2" style="max-width:100%">')

  // Links
  html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" target="_blank" rel="noopener">$1</a>')

  // Unordered lists
  html = html.replace(/^(\s*)[-*+] (.*$)/gim, (_match, indent, text) => {
    const depth = Math.floor(indent.length / 2)
    return `${'<ul>'.repeat(depth + 1)}<li>${text}</li>${'</ul>'.repeat(depth + 1)}`
  })

  // Ordered lists
  html = html.replace(/^(\s*)\d+\. (.*$)/gim, (_match, indent, text) => {
    const depth = Math.floor(indent.length / 2)
    return `${'<ol>'.repeat(depth + 1)}<li>${text}</li>${'</ol>'.repeat(depth + 1)}`
  })

  // Blockquotes
  html = html.replace(/^> (.*$)/gim, '<blockquote>$1</blockquote>')

  // Paragraphs (wrap remaining text blocks)
  const lines = html.split('\n')
  const result: string[] = []
  let inParagraph = false

  for (const line of lines) {
    const trimmed = line.trim()
    const isBlock =
      trimmed.startsWith('<h') ||
      trimmed.startsWith('<ul') ||
      trimmed.startsWith('<ol') ||
      trimmed.startsWith('<li') ||
      trimmed.startsWith('<pre') ||
      trimmed.startsWith('<blockquote') ||
      trimmed.startsWith('<hr') ||
      trimmed === ''

    if (isBlock) {
      if (inParagraph) {
        result.push('</p>')
        inParagraph = false
      }
      if (trimmed !== '') {
        result.push(line)
      } else {
        result.push('<br>')
      }
    } else {
      if (!inParagraph) {
        result.push('<p>')
        inParagraph = true
      }
      result.push(line)
    }
  }

  if (inParagraph) {
    result.push('</p>')
  }

  html = result.join('\n')

  // Clean up consecutive <br>
  html = html.replace(/(<br>\s*){2,}/g, '<br>')

  return html
}
