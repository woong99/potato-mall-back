package potatowoong.potatomallback.domain.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.pay.entity.PayTransaction;

public interface PayTransactionRepository extends JpaRepository<PayTransaction, Long> {

}
