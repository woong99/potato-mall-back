package potatowoong.potatomallback.domain.pay.dto.response;


public record PaymentResDto(

    String paymentKey, // 결제 키

    String orderName, // 구매 상품명

    String mId, // 상점아이디

    String method, // 결제수단

    int totalAmount, // 총 결제 금액

    String status, // 결제 처리 상태

    String requestedAt, // 결제가 일어난 시간

    String approvedAt, // 결제 승인 시간

    int suppliedAmount, // 공급가액

    int vat, // 부가세

    Cancel cancels,

    Card card,

    Transfer transfer,

    CashReceipt cashReceipt

) {

    /**
     * 결제 취소 정보
     */
    public record Cancel(

        int cancelAmount, // 결제를 취소한 금액

        String cancelReason, // 결제 취소 사유

        String canceledAt, // 결제 취소 시간

        String transactionKey, // 취소 건의 키 값

        String receiptKey, // 취소 건의 현금영수증 키 값

        String cancelStatus // 취소 상태
    ) {

    }

    /**
     * 카드 정보
     */
    public record Card(

        int amount, // 카드사에 결제 요청한 금액

        String issuerCode, // 카드 발급사 숫자 코드

        String acquirerCode, // 카드 매입사 숫자 코드

        String number, // 카드번호

        int installmentPlanMonths, // 할부 개월 수

        String approveNo, // 카드사 승인번호

        String cardType, // 카드 종류(신용, 체크, 기프트, 미확인)

        String ownerType, // 카드의 소유자 타입(개인, 법인, 미확인)

        String acquireStatus // 카드 결제의 매입 상태
    ) {

    }

    /**
     * 계좌 이체
     */
    public record Transfer(

        String bankCode, // 은행 숫자 코드

        String settlementStatus // 정산 상태(INCOMPLETED: 미정산, COMPLETED: 정산 완료)
    ) {

    }

    /**
     * 현금영수증 정보
     */
    public record CashReceipt(

        String type, // 현금영수증 종류(소득공제, 지출증빙)

        String receiptKey, // 현금영수증의 키 값

        String issueNumber, // 현금영수증 발급 번호

        int amount // 현금영수증 처리 금액
    ) {


    }
}
