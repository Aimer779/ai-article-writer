// @ts-ignore
/* eslint-disable */
import request from "@/utils/request";

/** Get dashboard statistics GET /statistics/overview */
export async function getStatistics(options?: { [key: string]: any }) {
  return request<API.BaseResponseStatisticsVO>("/statistics/overview", {
    method: "GET",
    ...(options || {}),
  });
}
