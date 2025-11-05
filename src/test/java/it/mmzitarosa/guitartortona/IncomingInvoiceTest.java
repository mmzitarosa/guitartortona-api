package it.mmzitarosa.guitartortona;

import it.mmzitarosa.guitartortona.controller.IncomingInvoiceController;
import it.mmzitarosa.guitartortona.controller.SingleController;
import it.mmzitarosa.guitartortona.dto.SupplierDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.CreateIncomingInvoiceDTO;
import it.mmzitarosa.guitartortona.dto.purchase.incominginvoice.IncomingInvoiceDTO;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("unittest")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class IncomingInvoiceTest extends DBTest{

	@Autowired private SingleController singleController;
	@Autowired private IncomingInvoiceController controller;

	@Test @Order(1) void createIncomingInvoice() {
		List<SupplierDTO> suppliers = singleController.readSuppliers();
		CreateIncomingInvoiceDTO request = new CreateIncomingInvoiceDTO();
		request.setSupplierName("Fornitore");
		request.setNumber("A123");
		request.setDate(LocalDate.now());
		request.setAmount(123.34);
		request.setNotes("Fattura base");
		IncomingInvoiceDTO response = controller.createIncomingInvoice(request);
		assertNotNull(response);
		assertNotNull(response.getId());
		assertNotNull(response.getSupplier());
		assertNotNull(response.getSupplier().getId());
		assertEquals(request.getSupplierName(), response.getSupplier().getName());
		assertEquals(request.getDate().toString(), response.getDate().toString());
		assertEquals(request.getAmount(), response.getAmount());
		assertEquals(request.getNotes(), response.getNotes());
		assertNull(response.getDaysLeft());
		assertEquals(suppliers.size() + 1, singleController.readSuppliers().size());
	}

	@Test @Order(2) void createIncomingInvoiceExistingSupplier() {
		List<SupplierDTO> suppliers = singleController.readSuppliers();
		assertEquals(1, suppliers.size());
		CreateIncomingInvoiceDTO request = new CreateIncomingInvoiceDTO();
		request.setSupplierId(suppliers.getFirst().getId());
		request.setNumber("A234");
		request.setDate(LocalDate.now());
		request.setAmount(234.45);
		request.setNotes("Fattura base 2");
		IncomingInvoiceDTO response = controller.createIncomingInvoice(request);
		assertNotNull(response);
		assertNotNull(response.getId());
		assertNotNull(response.getSupplier());
		assertNotNull(response.getSupplier().getId());
		assertEquals(request.getSupplierId(), response.getSupplier().getId());
		assertEquals(suppliers.getFirst().getName(), response.getSupplier().getName());
		assertEquals(request.getDate().toString(), response.getDate().toString());
		assertEquals(request.getAmount(), response.getAmount());
		assertEquals(request.getNotes(), response.getNotes());
		assertNull(response.getDaysLeft());
		assertEquals(suppliers.size(), singleController.readSuppliers().size());
	}

}
