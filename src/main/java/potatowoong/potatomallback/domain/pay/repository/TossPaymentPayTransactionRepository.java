package potatowoong.potatomallback.domain.pay.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;

public interface TossPaymentPayTransactionRepository extends JpaRepository<TossPaymentPayTransaction, String> {

    @Query("SELECT t FROM TossPaymentPayTransaction t JOIN FETCH t.payTransactions WHERE t.orderId = :orderId")
    Optional<TossPaymentPayTransaction> findTossPaymentPayTransactionAndPayTransactionByOrderId(String orderId);

    @Query("SELECT t FROM TossPaymentPayTransaction t JOIN FETCH t.payTransactions pt JOIN FETCH pt.product WHERE t.orderId = :orderId")
    Optional<TossPaymentPayTransaction> findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(String orderId);
}
