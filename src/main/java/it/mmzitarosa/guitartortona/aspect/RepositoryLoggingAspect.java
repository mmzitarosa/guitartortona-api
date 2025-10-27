package it.mmzitarosa.guitartortona.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

@Aspect @Component @Slf4j
public class RepositoryLoggingAspect {

	// intercetta tutti i metodi dei repository nel package indicato
	@Around("execution(* it.mmzitarosa.guitartortona.repository..*(..))")
	public Object logRepositoryCalls(ProceedingJoinPoint joinPoint) throws Throwable {
		long startTime = System.currentTimeMillis();

		Object result = joinPoint.proceed();
		long duration = System.currentTimeMillis() - startTime;

		String repositoryName = joinPoint.getTarget().getClass().getInterfaces()[0].getSimpleName();
		String methodName = joinPoint.getSignature().getName();

		log.info("[TRACING] [REPOSITORY] - TraceId={} - Repository={} - Method={} - Args={} - Result={} - ElapsedTime={}ms", MDC.get("traceId"), repositoryName, methodName, joinPoint.getArgs(), result, duration);

		return result;
	}


}
