--
-- 1. DROP incoming_invoice (vecchia struttura)
DROP TABLE IF EXISTS `incoming_invoice`;

-- 2. CREA le nuove tabelle mancanti

-- Brand table
CREATE TABLE `brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY (`name`)
);

-- Category table
CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `parent_category_id` bigint(20) DEFAULT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY (`name`),
  KEY (`parent_category_id`),
  CONSTRAINT FOREIGN KEY (`parent_category_id`) REFERENCES `category` (`id`)
);

-- Product table
CREATE TABLE `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(50) DEFAULT NULL,
  `internal_code` varchar(25) DEFAULT NULL,
  `category_id` bigint(20) NOT NULL,
  `brand_id` bigint(20) NOT NULL,
  `description` varchar(255) NOT NULL,
  `condition_id` tinyint(4) NOT NULL CHECK (`condition_id` IN (0,1)),
  `price` decimal(12,2) DEFAULT NULL,
  `reorder_point` smallint(4) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `status` tinyint(4) NOT NULL CHECK (`status` IN (0,1,2,3)),

  PRIMARY KEY (`id`),
  UNIQUE KEY (`code`),
  UNIQUE KEY (`internal_code`),
  KEY (`category_id`),
  CONSTRAINT FOREIGN KEY (`category_id`) REFERENCES `category` (`id`),
  KEY (`brand_id`),
  CONSTRAINT FOREIGN KEY (`brand_id`) REFERENCES `brand` (`id`)
);

ALTER TABLE `product` MODIFY COLUMN `condition_id` tinyint(4) NOT NULL COMMENT '0=NEW, 1=USED';
ALTER TABLE `product` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED, 3=ARCHIVED';

-- Purchase table
CREATE TABLE `purchase` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `amount` decimal(12,2) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `status` tinyint(4) NOT NULL CHECK (`status` IN (0,1,2,3)),

  PRIMARY KEY (`id`)
);

ALTER TABLE `purchase` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED, 3=ARCHIVED';

-- 3. RICREA incoming_invoice con nuova struttura
CREATE TABLE `incoming_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint(20) NOT NULL,
  `number` varchar(50) NOT NULL,

  PRIMARY KEY (`id`),
  CONSTRAINT FOREIGN KEY (`id`) REFERENCES `purchase` (`id`) ON DELETE CASCADE,
  UNIQUE KEY (`supplier_id`, `number`),
  KEY (`supplier_id`),
  CONSTRAINT FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
);

-- 4. Used receipt table
CREATE TABLE `used_receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `internal_code` varchar(25) NOT NULL,
  `number` varchar(50) NOT NULL,

  PRIMARY KEY (`id`),
  CONSTRAINT FOREIGN KEY (`id`) REFERENCES `purchase` (`id`) ON DELETE CASCADE,
  UNIQUE KEY (`internal_code`)
);

-- 5. Purchase item table
CREATE TABLE `purchase_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `purchase_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `vat` decimal(5,2) DEFAULT NULL,
  `purchase_price` decimal(12,2) DEFAULT NULL,
  `quantity` smallint(4) NOT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `status` tinyint(4) NOT NULL CHECK (`status` IN (0,1,2,3)),

  PRIMARY KEY (`id`),
  UNIQUE KEY (`purchase_id`, `product_id`),
  KEY (`purchase_id`),
  CONSTRAINT FOREIGN KEY (`purchase_id`) REFERENCES `purchase` (`id`) ON DELETE CASCADE,
  KEY (`product_id`),
  CONSTRAINT FOREIGN KEY (`product_id`) REFERENCES `product` (`id`)
);

ALTER TABLE `purchase_item` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED, 3=ARCHIVED';

-- 6. MODIFICA ledger (aggiungi colonne mancanti e riordina notes)
ALTER TABLE `ledger` MODIFY COLUMN `notes` varchar(255) DEFAULT NULL AFTER `amount`;
ALTER TABLE `ledger` ADD COLUMN `updated_date` datetime NULL AFTER `created_date`;
ALTER TABLE `ledger` ADD COLUMN `status` tinyint(4) NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED, 3=ARCHIVED' AFTER `updated_date`;

UPDATE `ledger` SET `updated_date` = `created_date`;
UPDATE `ledger` SET `status` = 2;

-- Ora rendile NOT NULL
ALTER TABLE `ledger` MODIFY COLUMN `updated_date` datetime NOT NULL;
ALTER TABLE `ledger` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED, 3=ARCHIVED';