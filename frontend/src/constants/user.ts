export const USER_ROLE = {
  USER: 'user',
  ADMIN: 'admin',
} as const

export type UserRole = (typeof USER_ROLE)[keyof typeof USER_ROLE]

export const USER_ROLE_TEXT: Record<UserRole, string> = {
  [USER_ROLE.USER]: 'User',
  [USER_ROLE.ADMIN]: 'Admin',
}

export const DEFAULT_LOGIN_USER = {
  userName: 'Not logged in',
  userRole: USER_ROLE.USER,
}
