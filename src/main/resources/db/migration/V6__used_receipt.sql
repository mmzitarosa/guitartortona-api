-- Anagrafica clienti (venditori di materiale usato), riutilizzabile tra le ricevute
CREATE TABLE `customer` (
    `id`              bigint(20)   NOT NULL AUTO_INCREMENT,
    `first_name`      varchar(100) NOT NULL,
    `last_name`       varchar(100) NOT NULL,
    `address`         varchar(255) NOT NULL,
    `zip_code`        varchar(10)  NOT NULL,
    `city`            varchar(100) NOT NULL,
    `province`        varchar(50)  NOT NULL,
    `document_type`   varchar(50)  NULL,
    `document_number` varchar(50)  NULL,
    `tax_code`        varchar(20)  NULL,
    `created_date`    datetime     NOT NULL,
    `updated_date`    datetime     NOT NULL,
    PRIMARY KEY (`id`),
    KEY (`last_name`, `first_name`)
);

-- Ricevute dell'usato: il numero progressivo (per anno della data ricevuta) nasce alla Registra.
-- I dati cliente sono una copia sulla ricevuta; customer_id e' solo il riferimento all'anagrafica.
ALTER TABLE `used_receipt` DROP COLUMN `number`;
-- Il codice interno viene valorizzato dopo l'insert (dipende dall'id), come per i prodotti
ALTER TABLE `used_receipt` MODIFY `internal_code` varchar(25) NULL;
ALTER TABLE `used_receipt`
    ADD COLUMN `number`                   int          NULL,
    ADD COLUMN `year`                     smallint     NULL,
    ADD COLUMN `last_letter_index`        int          NOT NULL DEFAULT 0,
    ADD COLUMN `customer_id`              bigint(20)   NULL,
    ADD COLUMN `customer_first_name`      varchar(100) NULL,
    ADD COLUMN `customer_last_name`       varchar(100) NULL,
    ADD COLUMN `customer_address`         varchar(255) NULL,
    ADD COLUMN `customer_zip_code`        varchar(10)  NULL,
    ADD COLUMN `customer_city`            varchar(100) NULL,
    ADD COLUMN `customer_province`        varchar(50)  NULL,
    ADD COLUMN `customer_document_type`   varchar(50)  NULL,
    ADD COLUMN `customer_document_number` varchar(50)  NULL,
    ADD COLUMN `customer_tax_code`        varchar(20)  NULL,
    ADD UNIQUE KEY `uk_used_receipt_year_number` (`year`, `number`),
    ADD KEY (`customer_id`),
    ADD CONSTRAINT `fk_used_receipt_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`id`) ON DELETE SET NULL;

-- Ultimo numero assegnato per anno: la riga e' bloccata durante la Registra per evitare duplicati e buchi
CREATE TABLE `used_receipt_counter` (
    `year`        smallint NOT NULL,
    `last_number` int      NOT NULL DEFAULT 0,
    PRIMARY KEY (`year`)
);

-- Lettera del prodotto nella ricevuta (A, B, ... Z, AA, ...), assegnata alla Conferma e poi stabile
ALTER TABLE `purchase_item` ADD COLUMN `letter` varchar(5) NULL;
