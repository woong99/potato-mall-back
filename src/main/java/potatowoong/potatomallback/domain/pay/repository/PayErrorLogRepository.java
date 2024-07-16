package potatowoong.potatomallback.domain.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.pay.entity.PayErrorLog;

public interface PayErrorLogRepository extends JpaRepository<PayErrorLog, Long> {

}
