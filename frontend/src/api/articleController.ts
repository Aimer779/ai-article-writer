// @ts-ignore
/* eslint-disable */
import request from "@/utils/request";

/** Get article by task id GET /article/${param0} */
export async function getArticleByTaskIdPath(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getArticleByTaskIdPathParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params;
  return request<API.BaseResponseArticleVO>(`/article/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** Create article task POST /article/create */
export async function createArticleTask(
  body: API.ArticleCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseString>("/article/create", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** Delete article POST /article/delete */
export async function deleteArticle(
  body: API.DeleteRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>("/article/delete", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** Get article by id GET /article/get */
export async function getArticleById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getArticleByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseArticleVO>("/article/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** Get article by task id GET /article/get/task */
export async function getArticleByTaskId(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getArticleByTaskIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseArticleVO>("/article/get/task", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** Page articles POST /article/list */
export async function listArticle(
  body: API.ArticleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageArticleVO>("/article/list", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** Page articles POST /article/list/page */
export async function listArticleByPage(
  body: API.ArticleQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageArticleVO>("/article/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** Get article generation progress GET /article/progress/${param0} */
export async function getProgress(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getProgressParams,
  options?: { [key: string]: any }
) {
  const { taskId: param0, ...queryParams } = params;
  return request<API.SseEmitter>(`/article/progress/${param0}`, {
    method: "GET",
    params: { ...queryParams },
    ...(options || {}),
  });
}

/** Update article POST /article/update */
export async function updateArticle(
  body: API.ArticleUpdateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>("/article/update", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
