export const USER_ROLE = {
  USER: 'user',
  ADMIN: 'admin',
  VIP: 'vip',
} as const

export type UserRole = (typeof USER_ROLE)[keyof typeof USER_ROLE]

export const USER_ROLE_TEXT: Record<UserRole, string> = {
  [USER_ROLE.USER]: 'User',
  [USER_ROLE.ADMIN]: 'Admin',
  [USER_ROLE.VIP]: 'VIP',
}

// Individual role constants for convenience
export const USER_ROLE_USER = 'user'
export const USER_ROLE_ADMIN = 'admin'
export const USER_ROLE_VIP = 'vip'

// Defaults
export const DEFAULT_USERNAME = 'Not logged in'
export const DEFAULT_QUOTA = 5

export const DEFAULT_LOGIN_USER = {
  userName: DEFAULT_USERNAME,
  userRole: USER_ROLE.USER,
}
