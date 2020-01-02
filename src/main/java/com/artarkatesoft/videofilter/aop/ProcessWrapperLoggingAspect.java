package com.artarkatesoft.videofilter.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Aspect
@Configuration
public class ProcessWrapperLoggingAspect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* com.artarkatesoft.videofilter.processwrapper.*.*(..))")
    public Object calculateExecutionTime2(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object[] args = joinPoint.getArgs();
        logger.info("Arguments of {} are:\n{}", joinPoint, Arrays.toString(args));
        Object result = joinPoint.proceed();
        long timeTaken = System.currentTimeMillis() - startTime;
        logger.info("Time Taken by {} is {} ms", joinPoint, timeTaken);
        return result;
    }
}
