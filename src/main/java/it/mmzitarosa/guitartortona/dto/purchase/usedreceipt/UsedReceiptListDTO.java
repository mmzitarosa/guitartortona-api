package it.mmzitarosa.guitartortona.dto.purchase.usedreceipt;

import com.fasterxml.jackson.annotation.JsonFormat;
import it.mmzitarosa.guitartortona.entity.ProductEntity;
import it.mmzitarosa.guitartortona.entity.PurchaseItemEntity;
import it.mmzitarosa.guitartortona.entity.SaleEntity;
import it.mmzitarosa.guitartortona.entity.UsedReceiptEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public record UsedReceiptListDTO(
		Long id,
		@JsonFormat(pattern = "dd/MM/yyyy") LocalDate date,
		String customer,
		String number,
		Double amount,
		Status status,
		List<Item> items
) {
	/** Prodotto della ricevuta; i dati di vendita (e il margine) sono valorizzati solo a vendita effettuata e si riferiscono ad essa. */
	public record Item(
			Long id,
			Long productId,
			String reference,
			String category,
			String brand,
			String description,
			Double cost,
			@JsonFormat(pattern = "dd/MM/yyyy") LocalDate saleDate,
			Double salePrice,
			Double margin,
			Double saleVat,
			String saleReceipt
	) {
		public static Item of(UsedReceiptEntity receipt, PurchaseItemEntity item) {
			ProductEntity product = item.getProduct();
			SaleEntity sale = product.getSales().stream()
					.max(Comparator.comparing(SaleEntity::getDate, Comparator.nullsFirst(Comparator.naturalOrder())).thenComparing(SaleEntity::getId)).orElse(null);
			Double cost = item.getPurchasePrice();
			Double salePrice = sale == null ? null : sale.getSalePrice();
			Double margin = salePrice == null || cost == null ? null : salePrice - cost;
			return new Item(item.getId(), product.getId(), receipt.getItemReference(item.getLetter()),
					product.getCategory() == null ? null : product.getCategory().getName(),
					product.getBrand() == null ? null : product.getBrand().getName(),
					product.getDescription(), cost,
					sale == null ? null : sale.getDate(), salePrice, margin, sale == null ? null : sale.getVat(),
					sale == null ? null : sale.getReceiptNumber());
		}
	}

	public static UsedReceiptListDTO of(UsedReceiptEntity entity) {
		return new UsedReceiptListDTO(entity.getId(), entity.getDate(), entity.getCustomerFullName(), entity.getReference(), entity.getAmount(), entity.getStatus(),
				entity.getItems().stream().map(i -> Item.of(entity, i)).toList());
	}
}
