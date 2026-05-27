// @ts-ignore
/* eslint-disable */
import request from "@/utils/request";

/** Get agent log by id GET /agent/log/get */
export async function getAgentLogById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getAgentLogByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAgentLogVO>("/agent/log/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** Page agent logs POST /agent/log/list/page */
export async function listAgentLogByPage(
  body: API.AgentLogQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePageAgentLogVO>("/agent/log/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** Get agent execution statistics POST /agent/log/stats */
export async function getExecutionStats(
  body: API.AgentLogQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseAgentExecutionStatsVO>("/agent/log/stats", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
