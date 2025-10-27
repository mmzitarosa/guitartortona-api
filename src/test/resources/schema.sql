CREATE TABLE `bank` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY (`name`)
);

CREATE TABLE `supplier` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(150) NOT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY (`name`)
);

CREATE TABLE `ledger` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `date` date NOT NULL,
  `invoice_number` varchar(50) DEFAULT NULL,
  `invoice_date` date DEFAULT NULL,
  `description` varchar(255) NOT NULL,
  `reason` varchar(100) DEFAULT NULL,
  `receipt_number` varchar(5) DEFAULT NULL,
  `bank_id` bigint(20) DEFAULT NULL,
  `movement_type` tinyint(4) DEFAULT NULL CHECK (movement_type IN (0,1)),
  `payment_method` tinyint(4) DEFAULT NULL CHECK (payment_method IN (0,1)),
  `payment_type` tinyint(4) DEFAULT NULL CHECK (payment_type IN (0,1)),
  `amount` decimal(12,2) DEFAULT NULL,
  `notes` varchar(255) DEFAULT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,
  `status` tinyint(4) NOT NULL CHECK (`status` IN (0,1,2,3)),

  PRIMARY KEY (`id`),
  KEY (`bank_id`),
  CONSTRAINT FOREIGN KEY (`bank_id`) REFERENCES `bank` (`id`)
);

ALTER TABLE `ledger` MODIFY COLUMN `movement_type` tinyint(4) DEFAULT NULL COMMENT '0=INCOME, 1=EXPENSE';
ALTER TABLE `ledger` MODIFY COLUMN `payment_method` tinyint(4) DEFAULT NULL COMMENT '0=BANK, 1=CASH';
ALTER TABLE `ledger` MODIFY COLUMN `payment_type` tinyint(4) DEFAULT NULL COMMENT '0=DEPOSIT, 1=BALANCE';
ALTER TABLE `ledger` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED, 3=ARCHIVED';

CREATE TABLE `brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY (`name`)
);

CREATE TABLE `category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `parent_category_id` bigint(20) DEFAULT NULL,

  PRIMARY KEY (`id`),
  UNIQUE KEY (`name`),
  KEY (`parent_category_id`),
  CONSTRAINT FOREIGN KEY (`parent_category_id`) REFERENCES `category` (`id`)
);


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

CREATE TABLE `used_receipt` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `internal_code` varchar(25) NOT NULL,
  `number` varchar(50) NOT NULL,

  PRIMARY KEY (`id`),
  CONSTRAINT FOREIGN KEY (`id`) REFERENCES `purchase` (`id`) ON DELETE CASCADE,
  UNIQUE KEY (`internal_code`)
);

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

