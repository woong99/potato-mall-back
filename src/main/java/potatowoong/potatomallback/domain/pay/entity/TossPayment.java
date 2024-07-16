package potatowoong.potatomallback.domain.pay.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import potatowoong.potatomallback.domain.pay.dto.response.PaymentResDto;

@Entity
@Comment("결제 정보")
@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TossPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("결제 정보 IDX")
    private Long tossPaymentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, updatable = false)
    @Comment("주문 IDX")
    private TossPaymentPayTransaction tossPaymentPayTransaction;

    @Column(nullable = false, updatable = false, length = 200)
    @Comment("결제 키")
    private String paymentKey;

    @Column(nullable = false, updatable = false, length = 64)
    @Comment("구매 상품명")
    private String orderName;

    @Column(nullable = false, updatable = false, length = 14)
    @Comment("상점아이디")
    private String mid;

    @Column(nullable = false, updatable = false, length = 10)
    @Comment("결제수단")
    private String method;

    @Column(nullable = false, updatable = false)
    @Comment("총 결제 금액")
    private int totalAmount;

    @Column(nullable = false, updatable = false, length = 30)
    @Comment("결제 처리 상태")
    private String status;

    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME")
    @Comment("결제가 일어난 일시")
    private LocalDateTime requestedAt;

    @Column(nullable = false, updatable = false, columnDefinition = "DATETIME")
    @Comment("결제 승인 일시")
    private LocalDateTime approvedAt;

    @Column(nullable = false, updatable = false)
    @Comment("공급가액")
    private int suppliedAmount;

    @Column(nullable = false, updatable = false)
    @Comment("부가세")
    private int vat;

    @Column(updatable = false)
    @Comment("결제취소 - 결제 취소 금액")
    private int cancelAmount;

    @Column(updatable = false, length = 200)
    @Comment("결제취소 - 결제 취소 사유")
    private String cancelReason;

    @Column(updatable = false, columnDefinition = "DATETIME")
    @Comment("결제취소 - 결제 취소 일시")
    private LocalDateTime canceledAt;

    @Column(updatable = false, length = 64)
    @Comment("결제취소 - 취소 건의 키 값")
    private String cancelTransactionKey;

    @Column(updatable = false, length = 200)
    @Comment("결제취소 - 취소 건의 현금영수증 키 값")
    private String cancelReceiptKey;

    @Column(updatable = false, length = 10)
    @Comment("결제취소 - 취소 상태(DONE 이면 성공적으로 취소)")
    private String cancelStatus;

    @Column(updatable = false)
    @Comment("카드결제 - 카드사에 결제 요청한 금액")
    private int cardAmount;

    @Column(updatable = false, length = 100)
    @Comment("카드결제 - 카드 발급사 숫자 코드")
    private String cardIssuerCode;

    @Column(updatable = false, length = 100)
    @Comment("카드결제 - 카드 매입사 숫자 코드")
    private String cardAcquirerCode;

    @Column(updatable = false, length = 20)
    @Comment("카드결제 - 카드 번호")
    private String cardNumber;

    @Column(updatable = false)
    @Comment("카드결제 - 할부 개월 수(일시불이면 0)")
    private int cardInstallmentPlanMonths;

    @Column(updatable = false, length = 8)
    @Comment("카드결제 - 카드사 승인번호")
    private String cardApproveNo;

    @Column(updatable = false, length = 10)
    @Comment("카드결제 - 카드 종류(신용, 체크, 기프트, 미확인)")
    private String cardType;

    @Column(updatable = false, length = 10)
    @Comment("카드결제 - 카드의 소유자 타입(개인, 법인, 미확인)")
    private String cardOwnerType;

    @Column(updatable = false, length = 20)
    @Comment("카드결제 - 카드 결제의 매입 상태")
    private String cardAcquireStatus;

    @Column(updatable = false, length = 100)
    @Comment("계좌이체 - 은행 숫자 코드")
    private String transferBankCode;

    @Column(updatable = false, length = 20)
    @Comment("계좌이체 - 정산 상태(INCOMPLETED: 미정산, COMPLETED : 정산 완료)")
    private String transferSettlementStatus;

    @Column(updatable = false, length = 10)
    @Comment("현금영수증 - 종류(소득공제, 지출증빙)")
    private String cashReceiptType;

    @Column(updatable = false, length = 200)
    @Comment("현금영수증 - 키 값")
    private String cashReceiptKey;

    @Column(updatable = false, length = 9)
    @Comment("현금영수증 - 발급 번호")
    private String cashReceiptIssueNumber;

    @Column(updatable = false)
    @Comment("현금영수증 - 처리 금액")
    private int cashReceiptAmount;

    @Builder
    public TossPayment(TossPaymentPayTransaction tossPaymentPayTransaction, String paymentKey, String orderName, String mid,
        String method, int totalAmount, String status, LocalDateTime requestedAt, LocalDateTime approvedAt,
        int suppliedAmount, int vat, int cancelAmount, String cancelReason, LocalDateTime canceledAt,
        String cancelTransactionKey, String cancelReceiptKey, String cancelStatus, int cardAmount,
        String cardIssuerCode, String cardAcquirerCode, String cardNumber, int cardInstallmentPlanMonths,
        String cardApproveNo, String cardType, String cardOwnerType, String cardAcquireStatus,
        String transferBankCode, String transferSettlementStatus, String cashReceiptType,
        String cashReceiptKey, String cashReceiptIssueNumber, int cashReceiptAmount) {
        this.tossPaymentPayTransaction = tossPaymentPayTransaction;
        this.paymentKey = paymentKey;
        this.orderName = orderName;
        this.mid = mid;
        this.method = method;
        this.totalAmount = totalAmount;
        this.status = status;
        this.requestedAt = requestedAt;
        this.approvedAt = approvedAt;
        this.suppliedAmount = suppliedAmount;
        this.vat = vat;
        this.cancelAmount = cancelAmount;
        this.cancelReason = cancelReason;
        this.canceledAt = canceledAt;
        this.cancelTransactionKey = cancelTransactionKey;
        this.cancelReceiptKey = cancelReceiptKey;
        this.cancelStatus = cancelStatus;
        this.cardAmount = cardAmount;
        this.cardIssuerCode = cardIssuerCode;
        this.cardAcquirerCode = cardAcquirerCode;
        this.cardNumber = cardNumber;
        this.cardInstallmentPlanMonths = cardInstallmentPlanMonths;
        this.cardApproveNo = cardApproveNo;
        this.cardType = cardType;
        this.cardOwnerType = cardOwnerType;
        this.cardAcquireStatus = cardAcquireStatus;
        this.transferBankCode = transferBankCode;
        this.transferSettlementStatus = transferSettlementStatus;
        this.cashReceiptType = cashReceiptType;
        this.cashReceiptKey = cashReceiptKey;
        this.cashReceiptIssueNumber = cashReceiptIssueNumber;
        this.cashReceiptAmount = cashReceiptAmount;
    }

    public static TossPayment of(TossPaymentPayTransaction tossPaymentPayTransaction, PaymentResDto dto) {
        PaymentResDto.Cancel cancel = dto.cancels();
        PaymentResDto.Card card = dto.card();
        PaymentResDto.Transfer transfer = dto.transfer();
        PaymentResDto.CashReceipt cashReceipt = dto.cashReceipt();

        TossPayment.TossPaymentBuilder builder = TossPayment.builder()
            .tossPaymentPayTransaction(tossPaymentPayTransaction)
            .paymentKey(dto.paymentKey())
            .orderName(dto.orderName())
            .mid(dto.mId())
            .method(dto.method())
            .totalAmount(dto.totalAmount())
            .status(dto.status())
            .requestedAt(ZonedDateTime.parse(dto.requestedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime())
            .approvedAt(ZonedDateTime.parse(dto.approvedAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime())
            .suppliedAmount(dto.suppliedAmount())
            .vat(dto.vat());

        if (cancel != null) {
            builder.cancelAmount(cancel.cancelAmount())
                .cancelReason(cancel.cancelReason())
                .canceledAt(ZonedDateTime.parse(cancel.canceledAt(), DateTimeFormatter.ISO_OFFSET_DATE_TIME).toLocalDateTime())
                .cancelTransactionKey(cancel.transactionKey())
                .cancelReceiptKey(cancel.receiptKey())
                .cancelStatus(cancel.cancelStatus());
        }

        if (card != null) {
            builder.cardAmount(card.amount())
                .cardIssuerCode(card.issuerCode())
                .cardAcquirerCode(card.acquirerCode())
                .cardNumber(card.number())
                .cardInstallmentPlanMonths(card.installmentPlanMonths())
                .cardApproveNo(card.approveNo())
                .cardType(card.cardType())
                .cardOwnerType(card.ownerType())
                .cardAcquireStatus(card.acquireStatus());
        }

        if (transfer != null) {
            builder.transferBankCode(transfer.bankCode())
                .transferSettlementStatus(transfer.settlementStatus());
        }

        if (cashReceipt != null) {
            builder.cashReceiptType(cashReceipt.type())
                .cashReceiptKey(cashReceipt.receiptKey())
                .cashReceiptIssueNumber(cashReceipt.issueNumber())
                .cashReceiptAmount(cashReceipt.amount());
        }

        return builder.build();
    }
}
