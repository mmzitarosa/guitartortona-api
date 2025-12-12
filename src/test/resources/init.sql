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

CREATE TABLE `incoming_invoice` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `supplier_id` bigint(20) NOT NULL,
  `number` varchar(50) NOT NULL,
  `date` date NOT NULL,
  `amount` decimal(12,2) NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY (`number`,`supplier_id`),
  KEY (`supplier_id`),
  CONSTRAINT FOREIGN KEY (`supplier_id`) REFERENCES `supplier` (`id`)
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
  `movement_type` tinyint(4) DEFAULT NULL COMMENT '0=INCOME, 1=EXPENSE',
  `payment_method` tinyint(4) DEFAULT NULL COMMENT '0=BANK, 1=CASH',
  `payment_type` tinyint(4) DEFAULT NULL COMMENT '0=DEPOSIT, 1=BALANCE',
  `amount` decimal(12,2) DEFAULT NULL,
  `created_date` datetime NOT NULL,
  `notes` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY (`bank_id`),
  CONSTRAINT FOREIGN KEY (`bank_id`) REFERENCES `bank` (`id`)
);

INSERT INTO bank (name) VALUES
	 ('B.Posta'),
	 ('B.R.E.'),
	 ('B.Sella'),
	 ('BPER'),
	 ('Cred.Valt.'),
	 ('Credit Agricole'),
	 ('Intesa SanPaolo'),
	 ('PayPal'),
	 ('UBI');