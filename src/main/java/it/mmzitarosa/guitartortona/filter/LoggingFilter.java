package it.mmzitarosa.guitartortona.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Log di tracing delle richieste /api.
 * <ul>
 *   <li>INFO: metodo, URI, parametri (valori sensibili mascherati), status, durata, dimensione risposta</li>
 *   <li>DEBUG: in piu' header (mascherati) e body (troncato, solo JSON/testo)</li>
 * </ul>
 */
@Component @Slf4j
public class LoggingFilter extends OncePerRequestFilter {

	private static final int MAX_BODY_LENGTH = 2000;
	private static final String MASK = "[REDACTED]";
	private static final Set<String> SENSITIVE_HEADERS = Set.of("authorization", "proxy-authorization", "cookie", "set-cookie", "x-api-key", "x-auth-token");
	private static final Pattern SENSITIVE_PARAM = Pattern.compile("(?i).*(password|passwd|secret|token|key|authorization).*");

	@Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		boolean traced = request.getRequestURI().startsWith("/api");
		if (!traced) {
			filterChain.doFilter(request, response);
			return;
		}

		long start = System.currentTimeMillis();
		MDC.put("traceId", String.valueOf(UUID.randomUUID()));

		String queryParams = getQueryParams(request.getQueryString());
		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

		try {
			log.info("[TRACING] [REQUEST] [HTTP] - TraceId={} - Method={} - URI={} - Params={}", MDC.get("traceId"), request.getMethod(), request.getRequestURI(), queryParams);
			filterChain.doFilter(wrappedRequest, wrappedResponse);
		} finally {
			try {
				logResponse(wrappedRequest, wrappedResponse, queryParams, start);
			} finally {
				wrappedResponse.copyBodyToResponse();
				MDC.remove("traceId");
			}
		}
	}

	private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, String queryParams, long startTime) {
		long duration = System.currentTimeMillis() - startTime;

		log.info("[TRACING] [RESPONSE] [HTTP] - TraceId={} - Method={} - URI={} - Params={} - Status={} - Size={} - ElapsedTime={}ms",
				MDC.get("traceId"), request.getMethod(), request.getRequestURI(), queryParams, response.getStatus(), response.getContentSize(), duration);

		if (log.isDebugEnabled()) {
			log.debug("[TRACING] [DETAIL] [HTTP] - TraceId={} - RequestHeaders={} - RequestBody={} - ResponseHeaders={} - ResponseBody={}",
					MDC.get("traceId"),
					maskedHeaders(Collections.list(request.getHeaderNames()), request::getHeader),
					body(request.getContentAsByteArray(), request.getContentType(), request.getCharacterEncoding()),
					maskedHeaders(response.getHeaderNames(), response::getHeader),
					body(response.getContentAsByteArray(), response.getContentType(), response.getCharacterEncoding()));
		}
	}

	private String getQueryParams(String queryString) {
		if (queryString == null || queryString.isEmpty()) return "{}";
		Matcher matcher = Pattern.compile("([^&=]+)=([^&]*)").matcher(queryString);

		StringBuilder sb = new StringBuilder("{");
		while (matcher.find()) {
			String key = matcher.group(1);
			String value = SENSITIVE_PARAM.matcher(key).matches() ? MASK : matcher.group(2);
			sb.append(key).append("=[").append(value).append("], ");
		}
		if (sb.length() > 1) sb.setLength(sb.length() - 2);
		return sb.append("}").toString();
	}

	private Map<String, String> maskedHeaders(Collection<String> headerNames, Function<String, String> getHeader) {
		Map<String, String> headers = new LinkedHashMap<>();
		for (String name : headerNames) {
			headers.putIfAbsent(name, "[" + (SENSITIVE_HEADERS.contains(name.toLowerCase()) ? MASK : getHeader.apply(name)) + "]");
		}
		return headers;
	}

	/** Solo body testuali (JSON/testo), troncati: niente HTML renderizzato ne' contenuti binari. */
	private String body(byte[] buf, String contentType, String charset) {
		if (buf == null || buf.length == 0) return "[empty]";
		String type = contentType == null ? "" : contentType.toLowerCase();
		if (!type.contains("json") && !type.startsWith("text/plain")) return "[" + (type.isEmpty() ? "unknown" : type) + " omitted, " + buf.length + " bytes]";

		Charset cs = charset == null ? StandardCharsets.UTF_8 : Charset.forName(charset);
		String text = new String(buf, 0, Math.min(buf.length, MAX_BODY_LENGTH), cs);
		return buf.length > MAX_BODY_LENGTH ? text + "...[truncated, " + buf.length + " bytes]" : text;
	}

}
