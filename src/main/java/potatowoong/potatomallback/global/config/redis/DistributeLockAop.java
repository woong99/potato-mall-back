package potatowoong.potatomallback.global.config.redis;

import java.lang.reflect.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;
import potatowoong.potatomallback.global.utils.CustomSpringELParser;

/**
 * @DistributionLock Annotation을 사용한 메소드에 대한 AOP
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributeLockAop {

    private final RedissonClient redissonClient;

    private final AopForTransaction aopForTransaction;

    @Around("@annotation(potatowoong.potatomallback.global.config.redis.DistributeLock)")
    public Object lock(final ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        DistributeLock distributeLock = method.getAnnotation(DistributeLock.class);

        final String key = CustomSpringELParser.getDynamicValue(signature.getParameterNames(), joinPoint.getArgs(), distributeLock.key()).toString();
        RLock rLock = redissonClient.getLock(key);

        try {
            boolean available = rLock.tryLock(distributeLock.waitTime(), distributeLock.leaseTime(), distributeLock.timeUnit());
            if (!available) { // 락을 획득하지 못한 경우
                return false;
            }
            return aopForTransaction.proceed(joinPoint); // 락을 획득한 경우
        } catch (InterruptedException e) {
            // 락을 획득하는 도중에 Interrupted Exception이 발생한 경우
            log.error("Interrupted Exception", e);
            throw new InterruptedException();
        } finally {
            // 락을 해제
            rLock.unlock();
        }
    }
}
