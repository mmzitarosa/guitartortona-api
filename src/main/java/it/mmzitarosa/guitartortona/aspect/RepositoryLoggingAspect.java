package it.mmzitarosa.guitartortona.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@Aspect @Component @Slf4j
public class RepositoryLoggingAspect {

	private static final int MAX_ARGS_LENGTH = 500;

	// intercetta tutti i metodi dei repository nel package indicato
	@Around("execution(* it.mmzitarosa.guitartortona.repository..*(..))")
	public Object logRepositoryCalls(ProceedingJoinPoint joinPoint) throws Throwable {
		if (!log.isDebugEnabled()) return joinPoint.proceed();

		long startTime = System.currentTimeMillis();

		Object result = joinPoint.proceed();
		long duration = System.currentTimeMillis() - startTime;

		String repositoryName = joinPoint.getTarget().getClass().getInterfaces()[0].getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		// Solo riepilogo: argomenti troncati e tipo/dimensione del risultato, mai il contenuto delle entity
		log.debug("[TRACING] [REPOSITORY] - TraceId={} - Repository={} - Method={} - Args={} - Result={} - ElapsedTime={}ms", MDC.get("traceId"), repositoryName, methodName, truncate(Arrays.toString(joinPoint.getArgs())), summarize(result), duration);

		return result;
	}

	private String summarize(Object result) {
		if (result == null) return "null";
		if (result instanceof Collection<?> c) return "Collection[size=" + c.size() + "]";
		if (result instanceof Optional<?> o) return o.isPresent() ? "Optional[present]" : "Optional[empty]";
		return result.getClass().getSimpleName();
	}

	private String truncate(String value) {
		return value.length() > MAX_ARGS_LENGTH ? value.substring(0, MAX_ARGS_LENGTH) + "...[truncated]" : value;
	}

}
