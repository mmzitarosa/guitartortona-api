DELIMITER $$

-- DELETE: rimuovi prodotto se usato
CREATE TRIGGER `after_purchase_used_item_delete`
AFTER DELETE ON `purchase_item`
FOR EACH ROW
BEGIN
    DELETE FROM `product`
    WHERE `id` = OLD.product_id
      AND `condition_id` = 1;
END$$

CREATE TRIGGER trg_purchase_item_insert
AFTER INSERT ON purchase_item
FOR EACH ROW
BEGIN
    DECLARE purchase_status TINYINT;
    DECLARE purchase_archived BOOLEAN;

    SELECT status, archived INTO purchase_status, purchase_archived
    FROM purchase
    WHERE id = NEW.purchase_id;

    -- Solo se NON è archived
    IF NOT purchase_archived THEN
        IF purchase_status = 2 THEN
            UPDATE product
            SET stock = stock + NEW.quantity
            WHERE id = NEW.product_id;
        ELSE
            UPDATE product
            SET stock_pending = stock_pending + NEW.quantity
            WHERE id = NEW.product_id;
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_purchase_item_update
AFTER UPDATE ON purchase_item
FOR EACH ROW
BEGIN
    DECLARE purchase_status TINYINT;
    DECLARE purchase_archived BOOLEAN;
    DECLARE qty_diff INT;

    SELECT status, archived INTO purchase_status, purchase_archived
    FROM purchase
    WHERE id = NEW.purchase_id;

    -- Solo se NON è archived
    IF NOT purchase_archived THEN
        SET qty_diff = NEW.quantity - OLD.quantity;

        -- Se è cambiato il prodotto
        IF OLD.product_id != NEW.product_id THEN
            -- Rimuovi dal vecchio prodotto
            IF purchase_status = 2 THEN
                UPDATE product
                SET stock = stock - OLD.quantity
                WHERE id = OLD.product_id;
            ELSE
                UPDATE product
                SET stock_pending = stock_pending - OLD.quantity
                WHERE id = OLD.product_id;
            END IF;

            -- Aggiungi al nuovo prodotto
            IF purchase_status = 2 THEN
                UPDATE product
                SET stock = stock + NEW.quantity
                WHERE id = NEW.product_id;
            ELSE
                UPDATE product
                SET stock_pending = stock_pending + NEW.quantity
                WHERE id = NEW.product_id;
            END IF;
        ELSE
            -- Stesso prodotto, aggiusta solo la differenza
            IF purchase_status = 2 THEN
                UPDATE product
                SET stock = stock + qty_diff
                WHERE id = NEW.product_id;
            ELSE
                UPDATE product
                SET stock_pending = stock_pending + qty_diff
                WHERE id = NEW.product_id;
            END IF;
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_purchase_item_delete
AFTER DELETE ON purchase_item
FOR EACH ROW
BEGIN
    DECLARE purchase_status TINYINT;
    DECLARE purchase_archived BOOLEAN;

    SELECT status, archived INTO purchase_status, purchase_archived
    FROM purchase
    WHERE id = OLD.purchase_id;

    -- Solo se NON era archived
    IF NOT purchase_archived THEN
        IF purchase_status = 2 THEN
            UPDATE product
            SET stock = stock - OLD.quantity
            WHERE id = OLD.product_id;
        ELSE
            UPDATE product
            SET stock_pending = stock_pending - OLD.quantity
            WHERE id = OLD.product_id;
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_purchase_update
AFTER UPDATE ON purchase
FOR EACH ROW
BEGIN
    -- CASO 1: Cambio ARCHIVED (da false a true)
    IF OLD.archived = false AND NEW.archived = true THEN
        -- Archiviazione: rimuovi tutto lo stock di questo purchase
        IF OLD.status = 2 THEN
            -- Era COMPLETED: rimuovi dallo stock
            UPDATE product p
            INNER JOIN purchase_item pi ON pi.product_id = p.id
            SET p.stock = p.stock - pi.quantity
            WHERE pi.purchase_id = NEW.id;
        ELSE
            -- Era PENDING/DRAFT: rimuovi da stock_pending
            UPDATE product p
            INNER JOIN purchase_item pi ON pi.product_id = p.id
            SET p.stock_pending = p.stock_pending - pi.quantity
            WHERE pi.purchase_id = NEW.id;
        END IF;
    END IF;

    -- CASO 2: De-archiviazione (da true a false)
    IF OLD.archived = true AND NEW.archived = false THEN
        -- Ripristino: aggiungi lo stock di questo purchase
        IF NEW.status = 2 THEN
            -- È COMPLETED: aggiungi allo stock
            UPDATE product p
            INNER JOIN purchase_item pi ON pi.product_id = p.id
            SET p.stock = p.stock + pi.quantity
            WHERE pi.purchase_id = NEW.id;
        ELSE
            -- È PENDING/DRAFT: aggiungi a stock_pending
            UPDATE product p
            INNER JOIN purchase_item pi ON pi.product_id = p.id
            SET p.stock_pending = p.stock_pending + pi.quantity
            WHERE pi.purchase_id = NEW.id;
        END IF;
    END IF;

    -- CASO 3: Cambio STATUS (solo se NON archived)
    IF OLD.archived = false AND NEW.archived = false THEN
        -- Da PENDING/DRAFT a COMPLETED
        IF OLD.status IN (0, 1) AND NEW.status = 2 THEN
            UPDATE product p
            INNER JOIN purchase_item pi ON pi.product_id = p.id
            SET p.stock = p.stock + pi.quantity,
                p.stock_pending = p.stock_pending - pi.quantity
            WHERE pi.purchase_id = NEW.id;
        END IF;

        -- Da COMPLETED a PENDING/DRAFT (se permesso dalla business logic)
        IF OLD.status = 2 AND NEW.status IN (0, 1) THEN
            UPDATE product p
            INNER JOIN purchase_item pi ON pi.product_id = p.id
            SET p.stock = p.stock - pi.quantity,
                p.stock_pending = p.stock_pending + pi.quantity
            WHERE pi.purchase_id = NEW.id;
        END IF;
    END IF;
END$$

CREATE TRIGGER trg_sale_insert
AFTER INSERT ON sale
FOR EACH ROW
BEGIN
    UPDATE product
    SET stock = stock - NEW.quantity
    WHERE id = NEW.product_id;
END$$

CREATE TRIGGER trg_sale_update
AFTER UPDATE ON sale
FOR EACH ROW
BEGIN
    DECLARE qty_diff INT;

    SET qty_diff = NEW.quantity - OLD.quantity;

    -- Se è cambiato il prodotto
    IF OLD.product_id != NEW.product_id THEN
        -- Ripristina stock vecchio prodotto
        UPDATE product
        SET stock = stock + OLD.quantity
        WHERE id = OLD.product_id;

        -- Decrementa stock nuovo prodotto
        UPDATE product
        SET stock = stock - NEW.quantity
        WHERE id = NEW.product_id;
    ELSE
        -- Stesso prodotto, aggiusta differenza
        UPDATE product
        SET stock = stock - qty_diff
        WHERE id = NEW.product_id;
    END IF;
END$$

CREATE TRIGGER trg_sale_delete
AFTER DELETE ON sale
FOR EACH ROW
BEGIN
    UPDATE product
    SET stock = stock + OLD.quantity
    WHERE id = OLD.product_id;
END$$

DELIMITER ;