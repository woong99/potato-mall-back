package potatowoong.potatomallback.domain.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.pay.entity.TossPayment;

public interface TossPaymentRepository extends JpaRepository<TossPayment, String> {

}
