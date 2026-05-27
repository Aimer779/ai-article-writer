# AI Article Writer — Frontend Design System

## 1. Visual Theme and Atmosphere

A warm conversational enterprise workspace. The mood is that of a well-lit editor’s desk: warm paper surfaces, a single amber accent for action and state, and zero decorative gradients. Multi-Agent article generation is presented as a reviewable conversation thread, not a mechanical pipeline. Density is moderate — enterprise tools need information density, but the warm palette prevents fatigue.

## 2. Color Palette and Roles

All colors defined in OKLCH for perceptual uniformity. The palette is built around a warm paper canvas with a single amber-terracotta accent.

| Token | Value | Role |
|-------|-------|------|
| `--canvas` | `oklch(97% 0.005 80)` | Page background — warm off-white, like laid paper |
| `--surface` | `#ffffff` | Cards, panels, popovers — pure white for max contrast against canvas |
| `--surface-elevated` | `oklch(99% 0.004 80)` | Subtle elevation step for hovered cards or active rows |
| `--ink` | `oklch(28% 0.012 80)` | Primary text — deep warm charcoal |
| `--text-secondary` | `oklch(48% 0.014 80)` | Secondary text, descriptions |
| `--text-muted` | `oklch(62% 0.01 80)` | Placeholders, disabled, timestamps |
| `--border` | `oklch(88% 0.01 80)` | Dividers, card outlines, input borders |
| `--border-strong` | `oklch(78% 0.012 80)` | Focused inputs, active borders |
| `--accent` | `oklch(55% 0.14 55)` | Primary action, active nav, links — amber terracotta |
| `--accent-hover` | `oklch(50% 0.15 55)` | Buttons and links on hover |
| `--accent-subtle` | `oklch(97% 0.025 55)` | Tags, badges, light backgrounds behind accent elements |
| `--success` | `oklch(55% 0.14 145)` | Completed states, positive indicators — warm green |
| `--success-subtle` | `oklch(96% 0.02 145)` | Success backgrounds |
| `--warning` | `oklch(72% 0.12 85)` | Warnings, quota alerts |
| `--warning-subtle` | `oklch(97% 0.015 85)` | Warning backgrounds |
| `--error` | `oklch(52% 0.17 25)` | Errors, failed states — warm brick red |
| `--error-subtle` | `oklch(96% 0.02 25)` | Error backgrounds |
| `--vip-gold` | `oklch(72% 0.14 85)` | VIP badges only — warm gold, used sparingly |

### Neutrals tinting rule
All grays are tinted toward the warm hue (80°) at very low chroma (0.005–0.015). This creates subconscious cohesion without appearing colored.

## 3. Typography Rules

| Role | Font | Weight | Size | Line-height | Letter-spacing |
|------|------|--------|------|-------------|----------------|
| Display / H1 | Manrope | 600 | 40px | 1.15 | -0.022em |
| Page Title / H2 | Manrope | 600 | 28px | 1.25 | -0.018em |
| Section Title / H3 | Manrope | 600 | 20px | 1.3 | -0.012em |
| Card Title / H4 | Manrope | 600 | 16px | 1.35 | -0.01em |
| Body | Manrope, system Chinese | 400 | 14px | 1.6 | 0 |
| Body Small | Manrope, system Chinese | 400 | 13px | 1.55 | 0 |
| Caption / Label | Manrope, system Chinese | 500 | 12px | 1.4 | 0.01em |
| Mono (code, logs) | JetBrains Mono | 400 | 13px | 1.5 | 0 |

**Font stacks:**
- Latin: `Manrope, sans-serif`
- Chinese: `"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Noto Sans SC", sans-serif`
- Mono: `"JetBrains Mono", "Fira Code", ui-monospace, monospace`

**Rules:**
- Headings use `text-wrap: balance`
- Body paragraphs use `text-wrap: pretty`
- Numbers in tables/counters use `font-variant-numeric: tabular-nums`
- Negative tracking on display sizes only; normal tracking at 14px and below

## 4. Component Stylings

### Buttons
- **Primary**: `background: var(--accent)`, `color: white`, `border-radius: 6px`, `height: 36px`, `padding: 0 16px`, `font-weight: 500`, `font-size: 14px`
  - Hover: `background: var(--accent-hover)`, `translateY(-1px)`
  - Active: `scale(0.95)`, `translateY(0)`
  - Disabled: `opacity: 0.45`, no transform
- **Secondary**: `background: transparent`, `border: 1px solid var(--border)`, `color: var(--ink)`, same radius/size
  - Hover: `background: var(--canvas)`, `border-color: var(--border-strong)`
  - Active: `scale(0.95)`
- **Ghost**: no border, `color: var(--text-secondary)`
  - Hover: `background: rgba(0,0,0,0.04)`

### Cards (used sparingly — default to cardless sections)
- `background: var(--surface)`
- `border-radius: 10px`
- `border: 1px solid var(--border)` — prefer visible border over subtle shadow
- `box-shadow: none` by default
- Elevated variant: `box-shadow: 0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)`

### Inputs
- `border-radius: 6px`
- `border: 1px solid var(--border)`
- `background: var(--surface)`
- Focus: `border-color: var(--accent)`, `box-shadow: 0 0 0 3px var(--accent-subtle)`
- No inner shadows

### Navigation
- Header: `background: var(--surface)`, `border-bottom: 1px solid var(--border)`, height 60px
- Active nav item: `color: var(--accent)`, bottom border indicator `2px solid var(--accent)`
- Inactive: `color: var(--text-secondary)`
- Hover: `color: var(--ink)`, `background: oklch(95% 0.005 80)`

### Tags / Badges
- `border-radius: 2px` (sharp for small elements)
- Status tags: subtle background + matching text, no border
  - Success: `bg: var(--success-subtle)`, `color: var(--success)`
  - Warning: `bg: var(--warning-subtle)`, `color: darkened warning`
  - Error: `bg: var(--error-subtle)`, `color: var(--error)`
  - Processing: `bg: var(--accent-subtle)`, `color: var(--accent)`

## 5. Layout Principles

- **Spacing scale**: 4px base unit — 4, 8, 12, 16, 24, 32, 48, 64
- **Page padding**: 24px on desktop, 16px on mobile
- **Content max-width**: 1280px for dashboards, 960px for reading surfaces (article detail)
- **Grid**: No strict column grid; use flexbox + gap for component groups. Tables and card grids use CSS Grid.
- **Whitespace philosophy**: Tight vertical rhythm within components (8–12px), generous whitespace between major sections (32–48px)
- **App shell**: Header is fixed. Main content area has `background: var(--canvas)`. Cards sit on top with `background: var(--surface)`.

## 6. Depth and Elevation

Elevation is communicated primarily through background color steps and 1px borders, not heavy shadows.

| Level | Treatment |
|-------|-----------|
| Base canvas | `var(--canvas)` — page bg |
| Surface | `var(--surface)` + `1px solid var(--border)` — cards, panels |
| Elevated | `var(--surface)` + `border` + `shadow-level-1` — dropdowns, hover cards |
| Floating | `var(--surface)` + `shadow-level-2` — popovers, tooltips |
| Modal | `var(--surface)` + `shadow-level-3` — modals, drawers |

Shadows:
- `shadow-level-1`: `0 1px 3px rgba(0,0,0,0.06), 0 1px 2px rgba(0,0,0,0.04)`
- `shadow-level-2`: `0 4px 12px rgba(0,0,0,0.08), 0 2px 4px rgba(0,0,0,0.04)`
- `shadow-level-3`: `0 12px 40px rgba(0,0,0,0.12), 0 4px 8px rgba(0,0,0,0.06)`

Adjacent surfaces must differ by at least 4% lightness or use a visible border.

## 7. Do's and Don'ts

1. **Do not** use blue-to-cyan gradients anywhere. The accent is amber-terracotta only.
2. **Do not** use `border-left: 4px solid var(--accent)` as a section accent. Use a colored dot, weight shift, or background swatch instead.
3. **Do not** make every content block a white card with shadow. Default to open sections on the canvas; add card treatment only when the content type demands containment.
4. **Do not** use purple, blue, or green as the primary action color. Green is reserved for success states only.
5. **Do not** use `transition: all`. List exact properties: `transition: transform 0.15s ease, opacity 0.15s ease`.
6. **Do** keep button radius consistent within each type: 6px for all buttons, 10px for all cards, 2px for all tags.
7. **Do** apply `scale(0.95)` on active/press for every interactive element (buttons, nav items, list rows, cards with click handlers).
8. **Do** use sentence case for all UI labels and headings. No Title Case.
9. **Do not** use exclamation marks in success states ("Saved" not "Saved!").
10. **Do** ensure adjacent nested surfaces differ visually — either by background step or by border.

## 8. Responsive Behavior

- **Breakpoints**: 640px (sm), 1024px (md), 1280px (lg)
- **Navigation**: On < 1024px, collapse center nav into a hamburger menu. Right-side actions (login/user dropdown) remain visible.
- **Article create page**: Three-column layout collapses to single column on < 1024px: left step sidebar becomes a horizontal stepper at top, right info panel stacks below center content.
- **Tables**: Horizontal scroll with `overflow-x: auto` on mobile; freeze first column if action-heavy.
- **Touch targets**: Minimum 40×40px for all interactive elements.

## 9. Agent Prompt Guide

### Color quick reference
```
canvas: oklch(97% 0.005 80)
surface: #ffffff
ink: oklch(28% 0.012 80)
text-secondary: oklch(48% 0.014 80)
border: oklch(88% 0.01 80)
accent: oklch(55% 0.14 55)
accent-hover: oklch(50% 0.15 55)
accent-subtle: oklch(97% 0.025 55)
success: oklch(55% 0.14 145)
error: oklch(52% 0.17 25)
```

### Prompt templates

**"Create a primary CTA button"**
> A primary button on `surface` bg, `background: var(--accent)`, `color: white`, `border-radius: 6px`, `height: 36px`, `padding: 0 16px`, `font: 14px/1 Manrope weight 500`, `letter-spacing: 0`. Hover: `background: var(--accent-hover)`, `translateY(-1px)`. Active: `scale(0.95)`. Transition: `transform 0.15s ease, background-color 0.15s ease`.

**"Create a status card for the article list"**
> A card on `canvas` background. Card: `background: var(--surface)`, `border-radius: 10px`, `border: 1px solid var(--border)`. No shadow by default. Padding 16px. Title in `ink` at 16px weight 600. Meta line in `text-secondary` at 13px. Status tag with `border-radius: 2px`, `background: var(--accent-subtle)`, `color: var(--accent)`, `font-size: 12px`, `font-weight: 500`. Hover: `border-color: var(--border-strong)`, `translateY(-1px)`, `shadow-level-1`.

**"Create the article creation step indicator"**
> A vertical step list on the left sidebar. Each step: number circle `24px`, `border-radius: 50%`, `border: 2px solid var(--border)`, `color: var(--text-muted)`, `font: 13px/1 Manrope weight 500`. Active step: `border-color: var(--accent)`, `color: var(--accent)`, number circle has subtle pulse animation (`scale 1 → 1.05 → 1`, 2s infinite). Completed step: `background: var(--accent)`, `border-color: var(--accent)`, `color: white`, checkmark icon. Connecting line between steps: `1px solid var(--border)`, completed segments in `var(--accent)`. Transition between states: `background-color 0.3s ease, border-color 0.3s ease`.

**"Create a data stat card for the dashboard"**
> A card on `canvas` background. Card: `background: var(--surface)`, `border-radius: 10px`, `border: 1px solid var(--border)`, padding 20px. Label in `text-secondary` at 12px weight 500 uppercase, `letter-spacing: 0.01em`. Value in `ink` at 32px weight 600, `font-variant-numeric: tabular-nums`, `letter-spacing: -0.022em`. Trend indicator in `success` or `error` at 13px. No shadow. Clean, number-dense.

**"Create an empty state illustration"**
> Centered content on `canvas` background. A monogram or simple geometric placeholder in `border` color, 64px. Title in `ink` at 16px weight 600: "No articles yet". Description in `text-secondary` at 14px: "Start your first article and the Agents will handle the rest." Primary CTA below at `var(--accent)`, 6px radius. No decorative gradient. No exclamation marks.
