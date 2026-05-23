import request from '@/utils/request'

export interface BaseResponse<T> {
  code: number
  data?: T
  message?: string
}

export interface LoginUserVO {
  id?: number
  userAccount?: string
  userName?: string
  userAvatar?: string
  userProfile?: string
  userRole?: string
  editTime?: string
  createTime?: string
  updateTime?: string
}

export interface UserLoginRequest {
  userAccount: string
  userPassword: string
}

export interface UserRegisterRequest {
  userAccount: string
  userPassword: string
  checkPassword: string
}

export interface UserQueryRequest {
  id?: number
  userAccount?: string
  userName?: string
  userProfile?: string
  userRole?: string
  pageNum?: number
  pageSize?: number
  sortField?: string
  sortOrder?: string
}

export interface PageResult<T> {
  records?: T[]
  totalRow?: number
  total?: number
  pageNumber?: number
  pageNum?: number
  pageSize?: number
}

export const userRegister = (data: UserRegisterRequest) => {
  return request.post<BaseResponse<number>>('/user/register', data)
}

export const userLogin = (data: UserLoginRequest) => {
  return request.post<BaseResponse<LoginUserVO>>('/user/login', data)
}

export const getLoginUser = () => {
  return request.get<BaseResponse<LoginUserVO>>('/user/get/login')
}

export const userLogout = () => {
  return request.post<BaseResponse<boolean>>('/user/logout')
}

export const listUserByPage = (data: UserQueryRequest) => {
  return request.post<BaseResponse<PageResult<LoginUserVO>>>('/user/list/page', data)
}

export const deleteUser = (id: number) => {
  return request.post<BaseResponse<boolean>>('/user/delete', { id })
}
