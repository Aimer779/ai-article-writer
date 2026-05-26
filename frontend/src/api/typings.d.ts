declare namespace API {
  type ArticleCreateRequest = {
    topic?: string;
  };

  type ArticleQueryRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    id?: number;
    taskId?: string;
    userId?: number;
    topic?: string;
    mainTitle?: string;
    status?: string;
  };

  type ArticleUpdateRequest = {
    id?: number;
    topic?: string;
    mainTitle?: string;
    subTitle?: string;
    content?: string;
    fullContent?: string;
    coverImage?: string;
  };

  type ArticleVO = {
    id?: number;
    taskId?: string;
    userId?: number;
    topic?: string;
    mainTitle?: string;
    subTitle?: string;
    outline?: string;
    content?: string;
    fullContent?: string;
    coverImage?: string;
    images?: string;
    status?: string;
    errorMessage?: string;
    createTime?: string;
    completedTime?: string;
    updateTime?: string;
  };

  type BaseResponseArticleVO = {
    code?: number;
    data?: ArticleVO;
    message?: string;
  };

  type BaseResponseBoolean = {
    code?: number;
    data?: boolean;
    message?: string;
  };

  type BaseResponseLoginUserVO = {
    code?: number;
    data?: LoginUserVO;
    message?: string;
  };

  type BaseResponseLong = {
    code?: number;
    data?: number;
    message?: string;
  };

  type BaseResponsePageArticleVO = {
    code?: number;
    data?: PageArticleVO;
    message?: string;
  };

  type BaseResponsePageLoginUserVO = {
    code?: number;
    data?: PageLoginUserVO;
    message?: string;
  };

  type BaseResponseString = {
    code?: number;
    data?: string;
    message?: string;
  };

  type DeleteRequest = {
    id?: number;
  };

  type getArticleByIdParams = {
    id: number;
  };

  type getArticleByTaskIdParams = {
    taskId: string;
  };

  type getArticleByTaskIdPathParams = {
    taskId: string;
  };

  type getProgressParams = {
    taskId: string;
  };

  type getUserByIdParams = {
    id: number;
  };

  type LoginUserVO = {
    id?: number;
    userAccount?: string;
    userName?: string;
    userAvatar?: string;
    userProfile?: string;
    userRole?: string;
    editTime?: string;
    createTime?: string;
    updateTime?: string;
  };

  type PageArticleVO = {
    records?: ArticleVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type PageLoginUserVO = {
    records?: LoginUserVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type SseEmitter = {
    timeout?: number;
  };

  type UserLoginRequest = {
    userAccount?: string;
    userPassword?: string;
  };

  type UserQueryRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    id?: number;
    userAccount?: string;
    userName?: string;
    userProfile?: string;
    userRole?: string;
  };

  type UserRegisterRequest = {
    userAccount?: string;
    userPassword?: string;
    checkPassword?: string;
  };
}
