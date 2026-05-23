// @ts-ignore
/* eslint-disable */
import request from "@/utils/request";

/** Health check GET /health/ */
export async function healthCheck(options?: { [key: string]: any }) {
  return request<string>("/health/", {
    method: "GET",
    ...(options || {}),
  });
}
