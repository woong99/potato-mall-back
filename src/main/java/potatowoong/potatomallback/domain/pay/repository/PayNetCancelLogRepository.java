package potatowoong.potatomallback.domain.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.pay.entity.PayNetCancelLog;

public interface PayNetCancelLogRepository extends JpaRepository<PayNetCancelLog, Long> {

}
