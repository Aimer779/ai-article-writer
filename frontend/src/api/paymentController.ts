// @ts-ignore
/* eslint-disable */
import request from "@/utils/request";

/** Create Stripe Checkout session POST /payment/checkout/session */
export async function createCheckoutSession(
  body: API.PaymentCreateRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePaymentSessionVO>(
    "/payment/checkout/session",
    {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      data: body,
      ...(options || {}),
    }
  );
}

/** Get payment record by id GET /payment/get */
export async function getPaymentRecordById(
  // 叠加生成的Param类型 (非body参数swagger默认没有生成对象)
  params: API.getPaymentRecordByIdParams,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePaymentRecordVO>("/payment/get", {
    method: "GET",
    params: {
      ...params,
    },
    ...(options || {}),
  });
}

/** Page payment records POST /payment/list/page */
export async function listPaymentRecordByPage(
  body: API.PaymentQueryRequest,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponsePagePaymentRecordVO>("/payment/list/page", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}

/** Stripe webhook callback POST /payment/stripe/webhook */
export async function handleStripeWebhook(
  body: string,
  options?: { [key: string]: any }
) {
  return request<API.BaseResponseBoolean>("/payment/stripe/webhook", {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    data: body,
    ...(options || {}),
  });
}
