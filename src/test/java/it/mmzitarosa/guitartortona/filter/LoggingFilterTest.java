package it.mmzitarosa.guitartortona.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoggingFilterTest {

	private final Logger logger = (Logger) LoggerFactory.getLogger(LoggingFilter.class);
	private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

	@BeforeEach void setUp() {
		logger.setLevel(Level.DEBUG);
		appender.start();
		logger.addAppender(appender);
	}

	@AfterEach void tearDown() {
		logger.detachAppender(appender);
		logger.setLevel(null);
	}

	@Test void masksSensitiveHeadersAndParams() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/products");
		request.setQueryString("code=ABC&access_token=super-secret");
		request.addHeader("Authorization", "Bearer eyJ.secret.jwt");
		request.addHeader("Cookie", "session=abc");
		request.addHeader("Accept", "application/json");
		MockHttpServletResponse response = new MockHttpServletResponse();

		new LoggingFilter().doFilter(request, response, new MockFilterChain());

		String logs = String.join("\n", appender.list.stream().map(ILoggingEvent::getFormattedMessage).toList());
		assertFalse(logs.contains("eyJ.secret.jwt"), logs);
		assertFalse(logs.contains("session=abc"), logs);
		assertFalse(logs.contains("super-secret"), logs);
		assertTrue(logs.contains("code=[ABC]"), logs);
		assertTrue(logs.contains("application/json"), logs);
	}

	@Test void truncatesLongJsonBodies() throws Exception {
		MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/product/1");
		request.setContentType("application/json");
		request.setContent(("{\"v\":\"" + "x".repeat(5000) + "\"}").getBytes());
		MockHttpServletResponse response = new MockHttpServletResponse();

		new LoggingFilter().doFilter(request, response, (req, res) -> req.getInputStream().readAllBytes());

		String detail = appender.list.stream().map(ILoggingEvent::getFormattedMessage).filter(m -> m.contains("[DETAIL]")).findFirst().orElseThrow();
		assertTrue(detail.contains("truncated"), detail);
		assertTrue(detail.length() < 3500, "log troppo lungo: " + detail.length());
	}

	@Test void ignoresNonApiRequests() throws Exception {
		new LoggingFilter().doFilter(new MockHttpServletRequest("GET", "/other"), new MockHttpServletResponse(), new MockFilterChain());
		assertEquals(0, appender.list.size());
	}

}
