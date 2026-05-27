-- Add agent execution log support.

USE ai_article_writer;

CREATE TABLE IF NOT EXISTS agent_log (
    id BIGINT AUTO_INCREMENT COMMENT 'Primary key' PRIMARY KEY,
    taskId VARCHAR(64) NOT NULL COMMENT 'Article generation task ID',
    agentName VARCHAR(50) NOT NULL COMMENT 'Agent name',
    startTime DATETIME NOT NULL COMMENT 'Start time',
    endTime DATETIME DEFAULT NULL COMMENT 'End time',
    durationMs INT DEFAULT NULL COMMENT 'Duration in milliseconds',
    status VARCHAR(20) NOT NULL COMMENT 'Status: SUCCESS/FAILED',
    errorMessage TEXT DEFAULT NULL COMMENT 'Error message',
    prompt TEXT DEFAULT NULL COMMENT 'Prompt identifier or prompt summary',
    inputData JSON DEFAULT NULL COMMENT 'Input data in JSON format',
    outputData JSON DEFAULT NULL COMMENT 'Output data in JSON format',
    createTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Create time',
    updateTime DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    isDelete TINYINT NOT NULL DEFAULT 0 COMMENT 'Logic delete flag: 0-normal, 1-deleted',
    KEY idx_taskId (taskId),
    KEY idx_agentName (agentName),
    KEY idx_status (status),
    KEY idx_createTime (createTime)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='Agent execution log table';
