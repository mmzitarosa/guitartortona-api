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
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component @Slf4j
public class LoggingFilter extends OncePerRequestFilter {

	@Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
		long start = System.currentTimeMillis();
		String traceId = String.valueOf(UUID.randomUUID());
		MDC.put("traceId", traceId);

		String queryParams = getQueryParams(request.getQueryString());

		ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
		ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

		try {
			logRequest(wrappedRequest, queryParams);
			filterChain.doFilter(wrappedRequest, wrappedResponse);
		} finally {
			logResponse(wrappedRequest, wrappedResponse, queryParams, start);
			wrappedResponse.copyBodyToResponse();
		}
	}

	private void logRequest(ContentCachingRequestWrapper request, String queryParams) throws IOException {
		String body = getBody(request.getContentAsByteArray(), request.getCharacterEncoding());

		log.info("[TRACING] [REQUEST] [HTTP] - TraceId={} - Method={} - URI={} - Params={} - Headers={} - Body={} - Size={}", MDC.get("traceId"), request.getMethod(), request.getRequestURI(), queryParams, getHeaders(Collections.list(request.getHeaderNames()), request::getHeader), body.isEmpty() ? "[empty]" : body, body.length());
	}

	private void logResponse(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response, String queryParams, long startTime) throws IOException {
		long duration = System.currentTimeMillis() - startTime;
		String body = getBody(response.getContentAsByteArray(), response.getCharacterEncoding());

		log.info("[TRACING] [RESPONSE] [HTTP] - TraceId={} - Method={} - URI={} - Params={} - Status={} - Headers={} - Body={} - Size={} - ElapsedTime={}ms",
				MDC.get("traceId"),
				request.getMethod(),
				request.getRequestURI(),
				queryParams,
				response.getStatus(),
				getHeaders(response.getHeaderNames(), response::getHeader),
				body.isEmpty() ? "[empty]" : body,
				body.length(),
				duration);
	}

	private String getQueryParams(String queryString) {
		if (queryString == null || queryString.isEmpty()) return "{}";
		Pattern pattern = Pattern.compile("([^&=]+)=([^&]*)");
		Matcher matcher = pattern.matcher(queryString);

		StringBuilder sb = new StringBuilder("{");

		while (matcher.find()) {
			String key = matcher.group(1);
			String value = matcher.group(2);

			sb.append(key).append("=[").append(value).append("], ");
		}

		// Rimuove la virgola finale e spazio
		if (sb.length() > 1) {
			sb.setLength(sb.length() - 2);
		}
		sb.append("}");

		return sb.toString();
	}

	private Map<String, String> getHeaders(Collection<String> headerNames, Function<String, String> getHeader) {
		return headerNames.stream().collect(Collectors.toMap(key -> key, key -> "[" + getHeader.apply(key) + "]", (existing, replacement) -> existing));
	}

	private String getBody(byte[] buf, String charset) throws UnsupportedEncodingException {
		if (buf == null || buf.length == 0) return "";
		return new String(buf, charset);
	}

}
