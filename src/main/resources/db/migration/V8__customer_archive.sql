-- Anagrafica clienti: eliminazione logica (archivio) prima di quella fisica, come per fatture e ricevute
ALTER TABLE `customer`
    ADD COLUMN `archived`      boolean  NOT NULL DEFAULT false,
    ADD COLUMN `archived_date` datetime NULL,
    ADD KEY (`archived`);
