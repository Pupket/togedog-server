package pupket.togedogserver.global.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
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
        Method method = getMethod(proceedingJoinPoint);

        // 요청 URI 확인
        String uri = getRequestURI();
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
}
