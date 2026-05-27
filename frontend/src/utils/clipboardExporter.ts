/**
 * WeChat-compatible clipboard exporter.
 * Adapts Rico MD's clipboard-exporter for the current project's backend-driven image URLs.
 */

import { getStyle } from '@/constants/themes'

const CLIPBOARD_IMAGE_MAX_BYTES = 1024 * 1024
const CLIPBOARD_IMAGE_MAX_DIMENSION = 1200
const CLIPBOARD_IMAGE_JPEG_QUALITY = 0.6
const IMAGE_READ_TIMEOUT_MS = 8000

function withTimeout<T>(promise: Promise<T>, ms: number, message = 'Operation timed out'): Promise<T> {
  let timer: ReturnType<typeof setTimeout>
  return Promise.race([
    promise.finally(() => clearTimeout(timer)),
    new Promise<T>((_, reject) => {
      timer = setTimeout(() => reject(new Error(message)), ms)
    }),
  ])
}

function blobToDataURL(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onloadend = () => resolve(reader.result as string)
    reader.onerror = reject
    reader.readAsDataURL(blob)
  })
}

function loadImageFromBlob(blob: Blob): Promise<HTMLImageElement> {
  return new Promise((resolve, reject) => {
    const image = new Image()
    const objectURL = URL.createObjectURL(blob)
    image.onload = () => {
      URL.revokeObjectURL(objectURL)
      resolve(image)
    }
    image.onerror = () => {
      URL.revokeObjectURL(objectURL)
      reject(new Error('Image decode failed'))
    }
    image.src = objectURL
  })
}

async function recompressForClipboard(blob: Blob): Promise<Blob> {
  if (!blob || blob.size <= CLIPBOARD_IMAGE_MAX_BYTES) return blob
  if (!blob.type?.startsWith('image/') || blob.type === 'image/gif') return blob

  try {
    const image = await loadImageFromBlob(blob)
    const sourceWidth = image.naturalWidth || image.width
    const sourceHeight = image.naturalHeight || image.height
    const scale = Math.min(1, CLIPBOARD_IMAGE_MAX_DIMENSION / Math.max(sourceWidth, sourceHeight))
    const width = Math.max(1, Math.round(sourceWidth * scale))
    const height = Math.max(1, Math.round(sourceHeight * scale))
    const canvas = document.createElement('canvas')
    const ctx = canvas.getContext('2d')
    if (!ctx) return blob

    canvas.width = width
    canvas.height = height
    ctx.fillStyle = '#ffffff'
    ctx.fillRect(0, 0, width, height)
    ctx.drawImage(image, 0, 0, width, height)

    const compressed = await new Promise<Blob | null>((resolve) => {
      canvas.toBlob(resolve, 'image/jpeg', CLIPBOARD_IMAGE_JPEG_QUALITY)
    })

    return compressed && compressed.size < blob.size ? compressed : blob
  } catch {
    return blob
  }
}

async function fetchImageBlob(src: string): Promise<Blob> {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), IMAGE_READ_TIMEOUT_MS)
  try {
    const response = await fetch(src, {
      mode: 'cors',
      cache: 'default',
      signal: controller.signal,
    })
    if (!response.ok) throw new Error(`HTTP ${response.status}`)
    return await response.blob()
  } finally {
    clearTimeout(timer)
  }
}

async function convertImageToBase64(imgElement: HTMLImageElement): Promise<string> {
  const src = imgElement.getAttribute('src') || ''
  if (!src) throw new Error('Image src is empty')
  if (src.startsWith('data:')) return src

  const blob = await fetchImageBlob(src)
  return blobToDataURL(await recompressForClipboard(blob))
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;')
}

function buildWechatCodeHTML(codeText: string): string {
  const normalized = codeText
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/\t/g, '  ')

  if (!normalized) return '&nbsp;'

  return escapeHtml(normalized)
    .split('\n')
    .map((line) => (line.length ? line.replace(/ /g, '&nbsp;') : '&nbsp;'))
    .join('<br>')
}

function resolveCodeBlockExportStyles(themeKey?: string) {
  const theme = themeKey ? getStyle(themeKey) : null
  const preStyle = theme?.styles?.pre || ''
  const codeStyle = theme?.styles?.code || ''

  // Extract color from pre style for fallback
  const preColorMatch = preStyle.match(/color:\s*([^;]+)/i)
  const preColor = preColorMatch ? preColorMatch[1].trim() : ''

  const codeColorMatch = codeStyle.match(/color:\s*([^;]+)/i)
  const codeColor = codeColorMatch ? codeColorMatch[1].trim() : preColor || '#1B365D'

  const bgMatch = preStyle.match(/background(?:-color)?:\s*([^;]+)/i)
  const bg = bgMatch ? bgMatch[1].trim() : '#f5f5f5'

  return {
    wrapper: 'margin: 24px 0 !important;',
    frame: `padding: 16px !important; background: ${bg} !important; border-radius: 8px !important; overflow-x: auto !important; -webkit-overflow-scrolling: touch !important;`,
    code: `display: block !important; background: transparent !important; color: ${codeColor} !important; font-family: "SF Mono", Consolas, Monaco, "Courier New", monospace !important; font-size: 14px !important; line-height: 1.7 !important; white-space: pre !important; word-break: normal !important; overflow-wrap: normal !important; tab-size: 2 !important;`,
  }
}

function convertCodeBlocks(doc: Document, themeKey?: string) {
  const blocks = doc.querySelectorAll('pre.hljs')
  const resolvedStyles = resolveCodeBlockExportStyles(themeKey)

  blocks.forEach((block) => {
    const code = block.querySelector('code')
    if (!code) return

    const wrapper = doc.createElement('section')
    wrapper.setAttribute('style', resolvedStyles.wrapper)

    const frame = doc.createElement('section')
    frame.setAttribute('style', resolvedStyles.frame)

    const codeNode = doc.createElement('code')
    codeNode.setAttribute('style', resolvedStyles.code)
    codeNode.innerHTML = buildWechatCodeHTML(code.textContent || '')

    frame.appendChild(codeNode)
    wrapper.appendChild(frame)
    block.parentNode?.replaceChild(wrapper, block)
  })

  // Also handle plain pre > code without highlight.js
  doc.querySelectorAll('pre:not([class*="hljs"])').forEach((block) => {
    const code = block.querySelector('code')
    if (!code || block.closest('section[data-wechat-code]')) return
    const resolvedStyles = resolveCodeBlockExportStyles(themeKey)

    const wrapper = doc.createElement('section')
    wrapper.setAttribute('style', resolvedStyles.wrapper)
    wrapper.setAttribute('data-wechat-code', 'true')

    const frame = doc.createElement('section')
    frame.setAttribute('style', resolvedStyles.frame)

    const codeNode = doc.createElement('code')
    codeNode.setAttribute('style', resolvedStyles.code)
    codeNode.innerHTML = buildWechatCodeHTML(code.textContent || '')

    frame.appendChild(codeNode)
    wrapper.appendChild(frame)
    block.parentNode?.replaceChild(wrapper, block)
  })
}

function convertOrderedListsToWechatParagraphs(doc: Document, themeKey?: string) {
  const theme = themeKey ? getStyle(themeKey) : null
  const containerStyle = theme?.styles?.container || ''
  const paragraphStyle = theme?.styles?.p || ''
  const listItemStyle = theme?.styles?.li || ''

  const fontSize = extractStyleValue(listItemStyle, 'font-size')
    || extractStyleValue(paragraphStyle, 'font-size')
    || extractStyleValue(containerStyle, 'font-size')
  const lineHeight = extractStyleValue(listItemStyle, 'line-height')
    || extractStyleValue(paragraphStyle, 'line-height')
    || extractStyleValue(containerStyle, 'line-height')
  const color = extractStyleValue(listItemStyle, 'color')
    || extractStyleValue(paragraphStyle, 'color')
    || extractStyleValue(containerStyle, 'color')
  const fontFamily = extractStyleValue(listItemStyle, 'font-family')
    || extractStyleValue(paragraphStyle, 'font-family')
    || extractStyleValue(containerStyle, 'font-family')

  const typographyParts: string[] = []
  if (fontSize) typographyParts.push(`font-size: ${fontSize} !important;`)
  if (lineHeight) typographyParts.push(`line-height: ${lineHeight} !important;`)
  if (color) typographyParts.push(`color: ${color} !important;`)
  if (fontFamily) typographyParts.push(`font-family: ${fontFamily} !important;`)
  const typography = typographyParts.join(' ')

  doc.querySelectorAll('ol').forEach((list) => {
    const items = Array.from(list.children).filter(
      (child) => child.tagName?.toUpperCase() === 'LI'
    )
    if (items.length === 0) {
      list.remove()
      return
    }

    const fragment = doc.createDocumentFragment()
    items.forEach((item, index) => {
      const paragraph = doc.createElement('p')
      const prefix = doc.createElement('span')
      prefix.textContent = `${index + 1}. `
      prefix.setAttribute('style', 'display: inline !important; white-space: nowrap !important;')

      const text = (item.textContent || '').replace(/\s+/g, ' ').trim()
      paragraph.setAttribute(
        'style',
        `${typography} margin: 0 0 14px !important; white-space: normal !important; word-break: break-word !important; overflow-wrap: anywhere !important;`
      )
      paragraph.appendChild(prefix)
      paragraph.appendChild(doc.createTextNode(text))
      fragment.appendChild(paragraph)
    })

    list.parentNode?.replaceChild(fragment, list)
  })

}

function normalizeBlockquotes(doc: Document) {
  doc.querySelectorAll('blockquote').forEach((blockquote) => {
    let style = blockquote.getAttribute('style') || ''
    style = style.replace(/background(?:-color)?:\s*[^;]+;?/gi, '')
    style = style.replace(/color:\s*[^;]+;?/gi, '')
    style += '; background: rgba(0, 0, 0, 0.05) !important; color: rgba(0, 0, 0, 0.8) !important;'
    blockquote.setAttribute('style', style)
  })
}

function normalizeTablesForWechat(doc: Document) {
  doc.querySelectorAll('table').forEach((table) => {
    const tableStyle = table.getAttribute('style') || ''
    table.setAttribute(
      'style',
      `${tableStyle}; width: 100% !important; max-width: 100% !important; table-layout: fixed !important;`
    )
  })

  doc.querySelectorAll('th, td').forEach((cell) => {
    const cellStyle = cell.getAttribute('style') || ''
    cell.setAttribute(
      'style',
      `${cellStyle}; word-break: break-word; overflow-wrap: anywhere; white-space: normal;`
    )
  })
}

function wrapSectionIfNeeded(doc: Document, themeKey?: string) {
  const theme = themeKey ? getStyle(themeKey) : null
  if (!theme) return

  const containerStyle = theme.styles.container || ''
  const bgColor = extractBackgroundColor(containerStyle)
  if (!bgColor || bgColor === '#fff' || bgColor === '#ffffff') return

  // If applyThemeToHtml already wrapped content in a section, avoid nesting
  const existing = doc.body.firstElementChild
  if (existing && existing.tagName.toLowerCase() === 'section') {
    const existingStyle = existing.getAttribute('style') || ''
    if (!existingStyle.includes('background-color')) {
      existing.setAttribute('style', `background-color: ${bgColor}; ${existingStyle}`)
    }
    return
  }

  const paddingMatch = containerStyle.match(/padding:\s*([^;]+)/)
  const maxWidthMatch = containerStyle.match(/max-width:\s*([^;]+)/)

  const section = doc.createElement('section')
  section.setAttribute(
    'style',
    `background-color: ${bgColor}; padding: ${paddingMatch ? paddingMatch[1].trim() : '40px 20px'}; max-width: ${maxWidthMatch ? maxWidthMatch[1].trim() : '100%'}; margin: 0 auto; box-sizing: border-box; word-wrap: break-word;`
  )

  while (doc.body.firstChild) {
    section.appendChild(doc.body.firstChild)
  }
  doc.body.appendChild(section)
}

/**
 * Wrap direct text child nodes of block-level elements in a span.
 * This improves WeChat editor's style retention for pasted content.
 */
function wrapInlineTextNodes(doc: Document) {
  const blockSelectors = 'h1, h2, h3, h4, h5, h6, p, li, td, th'
  const spanStyle = 'box-sizing: border-box; margin: 0; display: inline;'

  doc.querySelectorAll(blockSelectors).forEach((el) => {
    // Skip if this element already only contains a single span
    if (el.children.length === 1 && el.children[0].tagName.toLowerCase() === 'span' && el.childNodes.length === 1) {
      return
    }

    const nodes = Array.from(el.childNodes)
    nodes.forEach((node) => {
      if (node.nodeType === Node.TEXT_NODE && node.textContent?.trim()) {
        const span = doc.createElement('span')
        span.setAttribute('style', spanStyle)
        span.textContent = node.textContent
        el.replaceChild(span, node)
      }
    })
  })
}

function buildClipboardPlainText(doc: Document): string {
  const clone = doc.body.cloneNode(true) as HTMLElement

  clone.querySelectorAll('br').forEach((br) => {
    br.replaceWith('\n')
  })

  clone.querySelectorAll('p, div, section, pre, blockquote, li, h1, h2, h3, h4, h5, h6, tr').forEach((node) => {
    if (!node.textContent?.endsWith('\n')) {
      node.append('\n')
    }
  })

  return (clone.textContent || '')
    .replace(/[ \t]+\n/g, '\n')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

function extractStyleValue(styleText: string, property: string): string | null {
  if (!styleText || !property) return null
  const escapedProperty = property.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const match = styleText.match(new RegExp(`(?:^|;)\\s*${escapedProperty}\\s*:\\s*([^;]+)`, 'i'))
  return match ? match[1].trim() : null
}

function extractBackgroundColor(styleString: string): string | null {
  if (!styleString) return null
  const bgColorMatch = styleString.match(/background-color:\s*([^;]+)/)
  if (bgColorMatch) return bgColorMatch[1].trim()
  const bgMatch = styleString.match(/background:\s*([#rgb][^;]+)/)
  if (bgMatch) {
    const bgValue = bgMatch[1].trim()
    if (bgValue.startsWith('#') || bgValue.startsWith('rgb')) return bgValue
  }
  return null
}

/**
 * Copy article HTML to clipboard in WeChat-compatible format.
 * @param renderedHTML HTML content from markdown renderer
 * @param themeKey Optional theme key for styling
 * @returns true if successful
 */
export async function copyToWechat(renderedHTML: string, themeKey?: string): Promise<boolean> {
  if (!renderedHTML) {
    return false
  }

  try {
    const parser = new DOMParser()
    const doc = parser.parseFromString(renderedHTML, 'text/html')

    const images = Array.from(doc.querySelectorAll('img'))
    if (images.length > 0) {
      let successCount = 0
      let failCount = 0

      for (const img of images) {
        try {
          const base64 = await withTimeout(
            convertImageToBase64(img as HTMLImageElement),
            IMAGE_READ_TIMEOUT_MS,
            'Image conversion timed out'
          )
          img.setAttribute('src', base64)
          successCount += 1
        } catch {
          failCount += 1
        }
      }
      // eslint-disable-next-line no-console
      console.log(`Clipboard images: ${successCount} succeeded, ${failCount} failed`)
    }

    convertCodeBlocks(doc, themeKey)
    convertOrderedListsToWechatParagraphs(doc, themeKey)
    normalizeBlockquotes(doc)
    normalizeTablesForWechat(doc)
    wrapSectionIfNeeded(doc, themeKey)
    wrapInlineTextNodes(doc)

    // Remove all class attributes for clean WeChat pasting
    doc.querySelectorAll('[class]').forEach((el) => {
      el.removeAttribute('class')
    })

    const text = buildClipboardPlainText(doc)
    const html = doc.body.innerHTML

    return await writeClipboard(html, text)
  } catch (error) {
    // eslint-disable-next-line no-console
    console.error('Copy to WeChat failed:', error)
    return false
  }
}

/**
 * Write rich HTML and plain text to the system clipboard.
 * Uses ClipboardItem when available, falling back to document.execCommand('copy')
 * for better cross-platform and WeChat-editor compatibility.
 */
async function writeClipboard(html: string, plainText: string): Promise<boolean> {
  // Attempt 1: Modern Clipboard API (works well on macOS / modern Chrome)
  if (typeof ClipboardItem !== 'undefined' && navigator.clipboard?.write) {
    try {
      const item = new ClipboardItem({
        'text/html': new Blob([html], { type: 'text/html' }),
        'text/plain': new Blob([plainText], { type: 'text/plain' }),
      })
      await navigator.clipboard.write([item])
      return true
    } catch {
      // Fall through to legacy method
    }
  }

  // Attempt 2: Legacy execCommand copy via a temporary contenteditable element.
  // This is more reliable on Windows and for WeChat's editor.
  const container = document.createElement('div')
  container.contentEditable = 'true'
  container.innerHTML = html
  container.style.position = 'fixed'
  container.style.left = '-9999px'
  container.style.top = '0'
  container.style.opacity = '0'
  document.body.appendChild(container)

  const selection = window.getSelection()
  const range = document.createRange()
  range.selectNodeContents(container)
  selection?.removeAllRanges()
  selection?.addRange(range)

  let success = false
  try {
    success = document.execCommand('copy')
  } catch {
    success = false
  }

  selection?.removeAllRanges()
  if (container.parentNode) {
    document.body.removeChild(container)
  }
  return success
}
