CREATE TABLE `audit_log` (
    `id`           bigint(20)   NOT NULL AUTO_INCREMENT,
    `created_date` datetime(6)  NOT NULL,
    `username`     varchar(100) NOT NULL,
    `entity_type`  varchar(50)  NOT NULL,
    `entity_id`    bigint(20)   NOT NULL,
    `product_id`   bigint(20)   NULL,
    `operation`    varchar(10)  NOT NULL,
    `field`        varchar(100) NULL,
    `old_value`    text         NULL,
    `new_value`    text         NULL,
    PRIMARY KEY (`id`),
    KEY `idx_audit_entity` (`entity_type`, `entity_id`),
    KEY `idx_audit_product` (`product_id`, `created_date`),
    KEY `idx_audit_date` (`created_date`),
    KEY `idx_audit_user` (`username`)
) ENGINE = InnoDB;
