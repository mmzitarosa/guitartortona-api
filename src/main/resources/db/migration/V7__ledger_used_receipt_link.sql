-- Riga di brogliaccio generata dalla registrazione di una ricevuta dell'usato: riferimento reale alla ricevuta.
-- Se la ricevuta viene eliminata fisicamente la riga resta (la scelta spetta all'admin) e perde solo il collegamento.
ALTER TABLE `ledger`
    ADD COLUMN `used_receipt_id` bigint(20) NULL,
    ADD UNIQUE KEY `uk_ledger_used_receipt` (`used_receipt_id`),
    ADD CONSTRAINT `fk_ledger_used_receipt` FOREIGN KEY (`used_receipt_id`) REFERENCES `used_receipt` (`id`) ON DELETE SET NULL;
