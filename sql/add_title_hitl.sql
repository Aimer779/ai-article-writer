USE ai_article_writer;

ALTER TABLE article
    ADD COLUMN titleOptions JSON DEFAULT NULL COMMENT 'Candidate title options in JSON format' AFTER subTitle,
    ADD COLUMN userRequirement TEXT DEFAULT NULL COMMENT 'User writing requirement accumulated during creation' AFTER titleOptions,
    ADD COLUMN currentStep VARCHAR(64) DEFAULT NULL COMMENT 'Current generation workflow step' AFTER userRequirement,
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        COMMENT 'Task status: PENDING/PROCESSING/WAITING_USER_INPUT/COMPLETED/FAILED';
