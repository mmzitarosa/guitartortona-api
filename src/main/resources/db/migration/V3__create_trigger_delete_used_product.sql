DELIMITER $$

CREATE TRIGGER delete_used_product_after_purchase_item_delete
AFTER DELETE ON purchase_item
FOR EACH ROW
BEGIN
  IF EXISTS (SELECT 1 FROM product p WHERE p.id = OLD.product_id AND p.condition_id = 1)
     AND NOT EXISTS (SELECT 1 FROM purchase_item pi WHERE pi.product_id = OLD.product_id) THEN
    DELETE FROM product WHERE id = OLD.product_id;
  END IF;
END$$

DELIMITER ;