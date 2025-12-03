package it.mmzitarosa.guitartortona.specification;

import it.mmzitarosa.guitartortona.entity.IncomingInvoiceEntity;
import it.mmzitarosa.guitartortona.utils.Constant.Status;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

import static it.mmzitarosa.guitartortona.utils.Constant.Status.COMPLETED;
import static it.mmzitarosa.guitartortona.utils.Constant.Status.DRAFT;

public class IncomingInvoiceSpecification {

	public static Specification<IncomingInvoiceEntity> withFilters(Status status, Long supplierId) {
		return (root, query, cb) -> {
			List<Predicate> predicates = new ArrayList<>();

			// Gestione status
			if (status != null) {
				predicates.add(cb.equal(root.get("status"), status));
			} else {
				predicates.add(root.get("status").in(DRAFT, COMPLETED));
			}

			// Gestione supplierId
			if (supplierId != null) {
				predicates.add(cb.equal(root.get("supplier").get("id"), supplierId));
			}

			return cb.and(predicates.toArray(new Predicate[0]));
		};
	}
}

