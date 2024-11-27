package pupket.togedogserver.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Pointcut("execution(* pupket.togedogserver.domain.*.controller.*.*(..))")
    private void cut() {}

    @Around("cut()")
    public Object aroundLog(ProceedingJoinPoint proceedingJoinPoint) throws Throwable {

        // 메서드 정보 받아오기
        Method method;
        String uri;
        try{
             method = getMethod(proceedingJoinPoint);
            uri = getRequestURI();
        }catch (Exception e){
            return proceedingJoinPoint.proceed();
        }

        // 요청 URI 확인
        if ("/health-check".equals(uri)) {
            return proceedingJoinPoint.proceed(); // /health-check API는 로그 출력하지 않음
        }

        // 클래스 정보 받아오기
        log.info("class name = {}", proceedingJoinPoint.getTarget().getClass().getName());
        log.info("======= method name = {} =======", method.getName());

        // 파라미터 받아오기
        Object[] args = proceedingJoinPoint.getArgs();
        if (args.length == 0) {
            log.info("no parameter");
        } else {
            for (Object arg : args) {
                if (arg == null) {
                    log.info("parameter = null");
                } else {
                    log.info("parameter type = {}", arg.getClass().getSimpleName());
                    log.info("parameter value = {}", arg);
                }
            }
        }

        // 메서드 실행 및 결과 로깅
        Object returnObj = proceedingJoinPoint.proceed();
        if (returnObj == null) {
            log.info("return value = null");
        } else {
            log.info("return type = {}", returnObj.getClass().getSimpleName());
            log.info("return value = {}", returnObj);
        }

        return returnObj;
    }

    private Method getMethod(ProceedingJoinPoint proceedingJoinPoint) {
        MethodSignature signature = (MethodSignature) proceedingJoinPoint.getSignature();
        return signature.getMethod();
    }

    private String getRequestURI() {
        // 현재 스레드의 요청 URI를 가져오는 코드
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest().getRequestURI();
    }

    // Service의 메서드를 포인트컷으로 지정
    @Pointcut("within(pupket.togedogserver..*Service)")
    public void service() {}

    // Service 메서드 호출 전후 로깅
    @Around("service()")
    public Object loggingService(ProceedingJoinPoint joinPoint) throws Throwable {
         // RedisSortedSetService의 addToSortedSet 메소드는 로깅 제외
         if (joinPoint.getSignature().getDeclaringType().getSimpleName().equals("RedisSortedSetService") 
         && joinPoint.getSignature().getName().contains("addToSortedSet")) {
         return joinPoint.proceed();
     }
        
        String serviceName = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // 서비스 시작 로그
        log.info("Service method started: {}.{}()", serviceName, methodName);
        
        // 메서드 파라미터 로깅 (민감한 정보 제외)
        Object[] args = joinPoint.getArgs();
        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] != null && !containsSensitiveData(args[i])) {
                    log.info("Arg[{}]: {}", i, args[i]);
                }
            }
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long elapsedTime = System.currentTimeMillis() - startTime;
            log.info("Service method finished: {}.{}() [Execution time: {} ms]", 
                    serviceName, methodName, elapsedTime);
            
            // 결과 로깅 (민감한 정보 제외)
            if (result != null && !containsSensitiveData(result)) {
                log.info("Return value: {}", result);
            }
        }
    }

    private boolean containsSensitiveData(Object obj) {
        // 민감한 정보를 포함하는 객체인지 확인하는 로직
        return obj.toString().contains("password") || 
               obj.toString().contains("token") ||
               obj.toString().contains("key");
    }

    @AfterReturning(pointcut = "service()", returning = "returnValue")
    public void afterReturningServiceLogging(JoinPoint joinPoint, Object returnValue) {
        log.info("### Service method finished: {}", joinPoint.getSignature().toShortString());
        if (returnValue != null) {
            log.info("Service return value: {}", returnValue);
        }
    }

}
