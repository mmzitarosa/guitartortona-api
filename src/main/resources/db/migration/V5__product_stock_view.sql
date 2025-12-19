CREATE VIEW `product_stock_view` AS
SELECT p.id,
       p.code,
       p.internal_code,
       c.id           AS category_id,
       c.name         AS category_name,
       b.id           AS brand_id,
       b.name         AS brand_name,
       p.description,
       p.condition_id,
       p.price,
       p.reorder_point,
       p.notes,
       p.created_date,
       p.updated_date,
       p.completed_date,
       p.archived_date,
       p.status,
       p.archived,
       -- Stock disponibile (solo COMPLETED e non archiviati)
       COALESCE(SUM(
                        CASE
                            WHEN pur.status = 2 AND pur.archived = false
                                AND pi.status = 2 AND pi.archived = false
                                THEN pi.quantity
                            ELSE 0
                            END
                ), 0) AS available_stock,
       -- Stock in attesa (DRAFT o PENDING, non archiviati)
       COALESCE(SUM(
                        CASE
                            WHEN pur.status IN (0, 1) AND pur.archived = false
                                AND pi.status IN (0, 1) AND pi.archived = false
                                THEN pi.quantity
                            ELSE 0
                            END
                ), 0) AS pending_stock
FROM product p
         LEFT JOIN purchase_item pi ON p.id = pi.product_id
         LEFT JOIN purchase pur ON pi.purchase_id = pur.id
         LEFT JOIN category c ON p.category_id = c.id
         LEFT JOIN brand b ON p.brand_id = b.id
WHERE p.archived = false -- Escludi prodotti archiviati
GROUP BY p.id, p.code, p.internal_code, c.id, c.name, b.id, b.name, p.description, p.condition_id, p.price,
         p.reorder_point, p.notes, p.created_date, p.updated_date, p.completed_date, p.archived_date, p.archived;