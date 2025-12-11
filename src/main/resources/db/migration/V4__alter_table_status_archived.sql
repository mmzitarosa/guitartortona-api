ALTER TABLE `ledger` ADD COLUMN `completed_date` datetime NULL AFTER `updated_date`;
ALTER TABLE `ledger` ADD COLUMN `archived_date` datetime NULL AFTER `completed_date`;
ALTER TABLE `ledger` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED';
ALTER TABLE `ledger` ADD COLUMN `archived` boolean DEFAULT false;

CREATE INDEX ledger_status ON ledger(status);

ALTER TABLE `product` ADD COLUMN `completed_date` datetime NULL AFTER `updated_date`;
ALTER TABLE `product` ADD COLUMN `archived_date` datetime NULL AFTER `completed_date`;
ALTER TABLE `product` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED';
ALTER TABLE `product` ADD COLUMN `archived` boolean DEFAULT false;

CREATE INDEX product_status ON product(status);

ALTER TABLE `purchase` ADD COLUMN `completed_date` datetime NULL AFTER `updated_date`;
ALTER TABLE `purchase` ADD COLUMN `archived_date` datetime NULL AFTER `completed_date`;
ALTER TABLE `purchase` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED';
ALTER TABLE `purchase` ADD COLUMN `archived` boolean DEFAULT false;

CREATE INDEX purchase_status ON purchase(status);

ALTER TABLE `purchase_item` ADD COLUMN `completed_date` datetime NULL AFTER `updated_date`;
ALTER TABLE `purchase_item` ADD COLUMN `archived_date` datetime NULL AFTER `completed_date`;
ALTER TABLE `purchase_item` MODIFY COLUMN `status` tinyint(4) NOT NULL COMMENT '0=DRAFT, 1=PENDING, 2=COMPLETED';
ALTER TABLE `purchase_item` ADD COLUMN `archived` boolean DEFAULT false;

CREATE INDEX purchase_item_status ON purchase_item(status);