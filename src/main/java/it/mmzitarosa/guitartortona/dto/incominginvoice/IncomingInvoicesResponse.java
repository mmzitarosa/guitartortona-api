package it.mmzitarosa.guitartortona.dto.incominginvoice;

import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.web.PagedModel;

import java.util.List;

@Getter @Setter @AllArgsConstructor
public class IncomingInvoicesResponse {
	private List<IncomingInvoiceDTO> content;
	private PagedModel.PageMetadata page;
	private Long totalDrafts;
}

