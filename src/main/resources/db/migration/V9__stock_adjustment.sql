-- Rettifica inventariale: movimento registrato che riallinea la giacenza alla quantità contata
CREATE TABLE `stock_adjustment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `previous_stock` int NOT NULL,
  `counted_stock` int NOT NULL,
  `quantity` int NOT NULL,
  `reason` varchar(255) NOT NULL,
  `created_date` datetime NOT NULL,
  `updated_date` datetime NOT NULL,

  PRIMARY KEY (`id`),
  KEY (`product_id`),
  CONSTRAINT FOREIGN KEY (`product_id`) REFERENCES `product` (`id`),
  CONSTRAINT `chk_stock_adjustment_counted` CHECK (`counted_stock` >= 0)
);

DELIMITER $$

CREATE TRIGGER trg_stock_adjustment_insert
AFTER INSERT ON stock_adjustment
FOR EACH ROW
BEGIN
    UPDATE product
    SET stock = stock + NEW.quantity
    WHERE id = NEW.product_id;
END$$

DELIMITER ;
