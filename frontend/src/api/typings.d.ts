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

  type BaseResponsePagePaymentRecordVO = {
    code?: number;
    data?: PagePaymentRecordVO;
    message?: string;
  };

  type BaseResponsePaymentRecordVO = {
    code?: number;
    data?: PaymentRecordVO;
    message?: string;
  };

  type BaseResponsePaymentSessionVO = {
    code?: number;
    data?: PaymentSessionVO;
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

  type getPaymentRecordByIdParams = {
    id: number;
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
    vipTime?: string;
    vip?: boolean;
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

  type PagePaymentRecordVO = {
    records?: PaymentRecordVO[];
    pageNumber?: number;
    pageSize?: number;
    totalPage?: number;
    totalRow?: number;
    optimizeCountQuery?: boolean;
  };

  type PaymentCreateRequest = {
    productType?: string;
  };

  type PaymentQueryRequest = {
    pageNum?: number;
    pageSize?: number;
    sortField?: string;
    sortOrder?: string;
    id?: number;
    userId?: number;
    stripeSessionId?: string;
    status?: string;
    productType?: string;
  };

  type PaymentRecordVO = {
    id?: number;
    userId?: number;
    stripeSessionId?: string;
    stripePaymentIntentId?: string;
    amount?: number;
    currency?: string;
    status?: string;
    productType?: string;
    description?: string;
    refundTime?: string;
    refundReason?: string;
    createTime?: string;
    updateTime?: string;
  };

  type PaymentSessionVO = {
    paymentRecordId?: number;
    stripeSessionId?: string;
    checkoutUrl?: string;
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
