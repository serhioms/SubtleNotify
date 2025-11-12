package ru.alumni.hub.subtlenotify.advice;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAdvice {

	Logger LOGGER = LoggerFactory.getLogger(LoggingAdvice.class);

    // Include only business logic layers, exclude model package
    @Pointcut("execution(* ru.alumni.hub.subtlenotify.controller..*.*(..)) " +
            "|| execution(* ru.alumni.hub.subtlenotify.service..*.*(..)) " +
            "|| execution(* ru.alumni.hub.subtlenotify.repository..*.*(..))")
    public void businessLayerPointcut() {
    }

    // Exclude getter methods (methods starting with "get" or "is" with no parameters)
    @Pointcut("execution(* get*()) || execution(* is*()) || execution(* check*())")
    public void getterMethodsPointcut() {
    }

    @Around("businessLayerPointcut() && !getterMethodsPointcut()")
	public Object applicationLogger(ProceedingJoinPoint joinPoint) throws Throwable {
		Object proceed = null;
		try {
			proceed = joinPoint.proceed();
		} finally {
			LOGGER.info("{}({}): {}", joinPoint.getSignature().getName(), joinPoint.getArgs(), proceed);
		}
		return proceed;
	}

}