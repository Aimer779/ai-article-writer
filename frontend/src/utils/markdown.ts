/**
 * Markdown rendering utilities using markdown-it and highlight.js.
 * Provides theme-aware HTML generation for article display.
 */

import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'
import { getStyle } from '@/constants/themes'

let mdInstance: MarkdownIt | null = null

function getMarkdownEngine(): MarkdownIt {
  if (mdInstance) return mdInstance

  mdInstance = new MarkdownIt({
    html: true,
    linkify: true,
    typographer: false,
    highlight: (str: string, lang: string): string => {
      if (lang && hljs.getLanguage(lang)) {
        try {
          const result = hljs.highlight(str, { language: lang, ignoreIllegals: true })
          return `<pre class="hljs"><code class="language-${lang}">${result.value}</code></pre>`
        } catch {
          // fall through
        }
      }
      return `<pre class="hljs"><code>${mdInstance!.utils.escapeHtml(str)}</code></pre>`
    },
  })

  return mdInstance
}

/**
 * Convert markdown string to HTML with optional theme styling.
 * @param markdown Raw markdown content
 * @param themeKey Optional theme key from constants/themes
 * @returns HTML string
 */
export function markdownToHtml(markdown: string, themeKey?: string): string {
  if (!markdown) return ''

  const md = getMarkdownEngine()
  const rawHtml = md.render(markdown)

  if (!themeKey) {
    return rawHtml
  }

  return applyThemeToHtml(rawHtml, themeKey)
}

/**
 * Apply a WeChat theme's inline styles to rendered HTML elements.
 * This injects style attributes based on the theme configuration.
 */
function applyThemeToHtml(html: string, themeKey: string): string {
  const theme = getStyle(themeKey)
  if (!theme) return html

  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  const styles = theme.styles

  // Map of tag names to theme style keys
  const tagMap: Record<string, string> = {
    h1: 'h1',
    h2: 'h2',
    h3: 'h3',
    h4: 'h4',
    h5: 'h5',
    h6: 'h6',
    p: 'p',
    strong: 'strong',
    em: 'em',
    a: 'a',
    ul: 'ul',
    ol: 'ol',
    li: 'li',
    blockquote: 'blockquote',
    hr: 'hr',
    img: 'img',
    table: 'table',
    th: 'th',
    td: 'td',
    tr: 'tr',
  }

  // Apply styles to mapped tags
  Object.entries(tagMap).forEach(([tag, styleKey]) => {
    const styleValue = styles[styleKey]
    if (!styleValue) return
    doc.querySelectorAll(tag).forEach((el) => {
      const existing = el.getAttribute('style') || ''
      el.setAttribute('style', mergeStyles(existing, styleValue))
    })
  })

  // Code blocks: highlight.js wraps in <pre class="hljs">, apply pre style
  const preStyle = styles.pre
  if (preStyle) {
    doc.querySelectorAll('pre').forEach((el) => {
      const existing = el.getAttribute('style') || ''
      el.setAttribute('style', mergeStyles(existing, preStyle))
    })
  }

  // Inline code
  const codeStyle = styles.code
  if (codeStyle) {
    doc.querySelectorAll('code:not(pre code)').forEach((el) => {
      const existing = el.getAttribute('style') || ''
      el.setAttribute('style', mergeStyles(existing, codeStyle))
    })
  }

  // Container wrapper
  const container = doc.body.firstElementChild
  if (container && styles.container) {
    const existing = container.getAttribute('style') || ''
    container.setAttribute('style', mergeStyles(existing, styles.container))
  }

  return doc.body.innerHTML
}

/**
 * Merge two CSS style strings, with new styles overriding existing ones.
 */
function mergeStyles(existing: string, incoming: string): string {
  const existingMap = parseStyleString(existing)
  const incomingMap = parseStyleString(incoming)

  // Incoming overrides existing
  const merged = { ...existingMap, ...incomingMap }

  return Object.entries(merged)
    .map(([k, v]) => `${k}: ${v}`)
    .join('; ')
}

function parseStyleString(style: string): Record<string, string> {
  const result: Record<string, string> = {}
  if (!style) return result

  style.split(';').forEach((declaration) => {
    const colonIndex = declaration.indexOf(':')
    if (colonIndex === -1) return
    const prop = declaration.slice(0, colonIndex).trim()
    const val = declaration.slice(colonIndex + 1).trim()
    if (prop) result[prop] = val
  })

  return result
}

/**
 * Get theme CSS as a raw style string for clipboard export.
 * @param themeKey Theme identifier
 * @returns CSS string suitable for injection into a style tag
 */
export function getThemeCss(themeKey: string): string {
  const theme = getStyle(themeKey)
  if (!theme) return ''

  const styles = theme.styles
  const selectors: Record<string, string> = {
    '.markdown-body': styles.container || '',
    '.markdown-body h1': styles.h1 || '',
    '.markdown-body h2': styles.h2 || '',
    '.markdown-body h3': styles.h3 || '',
    '.markdown-body h4': styles.h4 || '',
    '.markdown-body h5': styles.h5 || '',
    '.markdown-body h6': styles.h6 || '',
    '.markdown-body p': styles.p || '',
    '.markdown-body strong': styles.strong || '',
    '.markdown-body em': styles.em || '',
    '.markdown-body a': styles.a || '',
    '.markdown-body ul': styles.ul || '',
    '.markdown-body ol': styles.ol || '',
    '.markdown-body li': styles.li || '',
    '.markdown-body blockquote': styles.blockquote || '',
    '.markdown-body pre': styles.pre || '',
    '.markdown-body code': styles.code || '',
    '.markdown-body hr': styles.hr || '',
    '.markdown-body img': styles.img || '',
    '.markdown-body table': styles.table || '',
    '.markdown-body th': styles.th || '',
    '.markdown-body td': styles.td || '',
    '.markdown-body tr': styles.tr || '',
  }

  return Object.entries(selectors)
    .filter(([, css]) => css)
    .map(([selector, css]) => `${selector} { ${css} }`)
    .join('\n')
}
