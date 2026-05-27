-- Add VIP membership and Stripe payment support.

USE ai_article_writer;

ALTER TABLE `user`
    ADD COLUMN vipTime DATETIME DEFAULT NULL COMMENT 'VIP activation time';

CREATE TABLE IF NOT EXISTS payment_record (
    id BIGINT AUTO_INCREMENT COMMENT 'Primary key' PRIMARY KEY,
    userId BIGINT NOT NULL COMMENT 'User ID',
    stripeSessionId VARCHAR(128) DEFAULT NULL COMMENT 'Stripe Checkout Session ID',
    stripePaymentIntentId VARCHAR(128) DEFAULT NULL COMMENT 'Stripe PaymentIntent ID',
    amount DECIMAL(10, 2) NOT NULL COMMENT 'Amount in major currency unit',
    currency VARCHAR(8) NOT NULL DEFAULT 'usd' COMMENT 'Currency',
    status VARCHAR(32) NOT NULL COMMENT 'Status: PENDING/SUCCEEDED/FAILED/REFUNDED',
    productType VARCHAR(32) NOT NULL COMMENT 'Product type: VIP_PERMANENT',
    description VARCHAR(256) DEFAULT NULL COMMENT 'Description',
    refundTime DATETIME DEFAULT NULL COMMENT 'Refund time',
    refundReason VARCHAR(512) DEFAULT NULL COMMENT 'Refund reason',
    createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    KEY idx_userId (userId),
    KEY idx_stripeSessionId (stripeSessionId),
    KEY idx_status (status),
    KEY idx_createTime (createTime)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Payment record table';
