/**
 * WeChat article themes - imported from Rico MD
 * Add new themes by creating files in this directory and registering them below.
 */

import { theme as wechatDefault } from './wechat-default'
import { theme as latepostDepth } from './latepost-depth'
import { theme as wechatFt } from './wechat-ft'
import { theme as wechatAnthropic } from './wechat-anthropic'
import { theme as wechatTech } from './wechat-tech'
import { theme as wechatElegant } from './wechat-elegant'
import { theme as wechatDeepread } from './wechat-deepread'
import { theme as wechatNyt } from './wechat-nyt'
import { theme as wechatJonyive } from './wechat-jonyive'
import { theme as wechatMedium } from './wechat-medium'
import { theme as wechatApple } from './wechat-apple'
import { theme as kenyaEmptiness } from './kenya-emptiness'
import { theme as hischeEditorial } from './hische-editorial'
import { theme as andoConcrete } from './ando-concrete'
import { theme as gaudiOrganic } from './gaudi-organic'
import { theme as guardian } from './guardian'
import { theme as nikkei } from './nikkei'
import { theme as lemonde } from './lemonde'
import { theme as minimalism } from './minimalism'
import { theme as wechatPaperpress } from './wechat-paperpress'
import { theme as kamiPaper } from './kami-paper'

export interface ThemeStyle {
  name: string
  styles: Record<string, string>
}

export const STYLES: Record<string, ThemeStyle> = {
  'wechat-default': wechatDefault,
  'latepost-depth': latepostDepth,
  'wechat-ft': wechatFt,
  'wechat-anthropic': wechatAnthropic,
  'wechat-tech': wechatTech,
  'wechat-elegant': wechatElegant,
  'wechat-deepread': wechatDeepread,
  'wechat-nyt': wechatNyt,
  'wechat-jonyive': wechatJonyive,
  'wechat-medium': wechatMedium,
  'wechat-apple': wechatApple,
  'kenya-emptiness': kenyaEmptiness,
  'hische-editorial': hischeEditorial,
  'ando-concrete': andoConcrete,
  'gaudi-organic': gaudiOrganic,
  'guardian': guardian,
  'nikkei': nikkei,
  'lemonde': lemonde,
  'minimalism': minimalism,
  'wechat-paperpress': wechatPaperpress,
  'kami-paper': kamiPaper,
}

export const THEME_CATEGORIES: Record<string, string[]> = {
  'Minimalism': [
    'wechat-default',
    'minimalism',
    'wechat-tech',
    'wechat-elegant',
    'wechat-deepread',
    'wechat-jonyive',
  ],
  'Tech Reading': [
    'wechat-anthropic',
    'wechat-ft',
    'wechat-medium',
    'wechat-apple',
    'kami-paper',
  ],
  'Traditional': [
    'latepost-depth',
    'wechat-paperpress',
    'wechat-nyt',
    'nikkei',
    'lemonde',
  ],
  'Design': [
    'kenya-emptiness',
    'hische-editorial',
    'ando-concrete',
    'gaudi-organic',
    'guardian',
  ],
}

export const RECOMMENDED_THEMES = [
  'wechat-default',
  'wechat-anthropic',
  'minimalism',
  'wechat-paperpress',
  'kenya-emptiness',
  'wechat-jonyive',
  'kami-paper',
]

export function getStyleList(): { key: string; name: string }[] {
  return Object.entries(STYLES).map(([key, val]) => ({ key, name: val.name }))
}

export function getStyle(key: string): ThemeStyle | null {
  return STYLES[key] || null
}

export function getStyleName(key: string): string {
  return STYLES[key]?.name || key
}

export function isRecommended(key: string): boolean {
  return RECOMMENDED_THEMES.includes(key)
}

export function getCategorizedThemes(): {
  category: string
  themes: { key: string; name: string; recommended: boolean }[]
}[] {
  return Object.entries(THEME_CATEGORIES).map(([category, keys]) => ({
    category,
    themes: keys
      .filter((key) => STYLES[key])
      .map((key) => ({
        key,
        name: STYLES[key].name,
        recommended: isRecommended(key),
      })),
  }))
}
