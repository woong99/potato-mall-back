package potatowoong.potatomallback.domain.pay.service;

import com.google.gson.Gson;
import java.security.InvalidKeyException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import potatowoong.potatomallback.domain.auth.entity.Member;
import potatowoong.potatomallback.domain.auth.repository.MemberRepository;
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto;
import potatowoong.potatomallback.domain.pay.dto.response.PaymentConfirmApiResDto;
import potatowoong.potatomallback.domain.pay.dto.response.PaymentResDto;
import potatowoong.potatomallback.domain.pay.entity.OrderHistory;
import potatowoong.potatomallback.domain.pay.entity.OrderProduct;
import potatowoong.potatomallback.domain.pay.entity.PayErrorLog;
import potatowoong.potatomallback.domain.pay.entity.PayTransaction;
import potatowoong.potatomallback.domain.pay.entity.TossPayment;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.enums.PurchaseHistoryStatus;
import potatowoong.potatomallback.domain.pay.repository.OrderHistoryRepository;
import potatowoong.potatomallback.domain.pay.repository.PayErrorLogRepository;
import potatowoong.potatomallback.domain.pay.repository.TossPaymentPayTransactionRepository;
import potatowoong.potatomallback.domain.pay.repository.TossPaymentRepository;
import potatowoong.potatomallback.domain.product.service.UserProductService;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;
import potatowoong.potatomallback.global.exception.PayApiException;
import potatowoong.potatomallback.global.utils.SecurityUtils;
import potatowoong.potatomallback.global.utils.TossPaymentUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayConfirmService {

    private final TossPaymentPayTransactionRepository tossPaymentPayTransactionRepository;

    private final TossPaymentRepository tossPaymentRepository;

    private final MemberRepository memberRepository;

    private final OrderHistoryRepository orderHistoryRepository;

    private final PayErrorLogRepository payErrorLogRepository;

    private final RestTemplate restTemplate;

    private final PayCancelService payCancelService;

    private final UserProductService userProductService;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    /**
     * 상품 재고량 검증 및 감소
     */
    @Transactional
    public TossPaymentPayTransaction modifyProductStockQuantity(Map<Long, Integer> transactionMap, final String orderId) {
        // 결제 트랜잭션 정보 조회
        TossPaymentPayTransaction tossPaymentPayTransaction = tossPaymentPayTransactionRepository.findTossPaymentPayTransactionAndPayTransactionAndProductByOrderId(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.PAY_TRANSACTION_NOT_FOUND));

        // 상품 재고량 수정
        List<PayTransaction> payTransactions = tossPaymentPayTransaction.getPayTransactions();
        for (PayTransaction payTransaction : payTransactions) {
            final long productId = payTransaction.getProduct().getProductId();
            userProductService.decreaseProductQuantityWithLock(productId, payTransaction.getQuantity());
            transactionMap.put(productId, payTransaction.getQuantity());
        }
        return tossPaymentPayTransaction;
    }

    /**
     * 결제 승인 API 호출
     */
    @Transactional
    public void payConfirm(TossPaymentPayTransaction tossPaymentPayTransaction, UserPayReqDto.VerifyPayment dto) {
        String paymentKey = null;
        try {
            // 결제 승인 API 호출
            PaymentConfirmApiResDto paymentConfirmApiResDto = fetchConfirmApi(dto);
            JSONObject jsonObject = paymentConfirmApiResDto.jsonObject();
            paymentKey = (String) jsonObject.get("paymentKey");
            if (paymentConfirmApiResDto.isSuccess()) {
                // 결제 승인 성공
                processPayConfirmSuccess(jsonObject, tossPaymentPayTransaction, dto.amount());
            } else {
                // 결제 승인 실패
                processPayConfirmFail(jsonObject, dto.orderId());
            }
        } catch (Exception e) {
            // 망 취소
            if (StringUtils.isNotBlank(paymentKey)) {
                payCancelService.fetchNetCancelApi(tossPaymentPayTransaction, paymentKey);
            }
            throw new PayApiException();
        }
    }

    /**
     * 결제 승인 API 호출
     */
    @SuppressWarnings("unchecked")
    private PaymentConfirmApiResDto fetchConfirmApi(UserPayReqDto.VerifyPayment dto) {
        JSONObject obj = new JSONObject();
        obj.put("orderId", dto.orderId());
        obj.put("amount", dto.amount());
        obj.put("paymentKey", dto.paymentKey());

        HttpHeaders headers = TossPaymentUtils.getCommonApiHeaders(tossSecretKey);
        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);

        final String tossConfirmApi = "https://api.tosspayments.com/v1/payments/confirm";
        ResponseEntity<JSONObject> response = restTemplate.exchange(tossConfirmApi, HttpMethod.POST, request, JSONObject.class);

        return PaymentConfirmApiResDto.builder()
            .isSuccess(response.getStatusCode().is2xxSuccessful())
            .jsonObject(response.getBody())
            .build();
    }

    /**
     * 결제 승인 성공 처리
     */
    private void processPayConfirmSuccess(JSONObject jsonObject, TossPaymentPayTransaction tossPaymentPayTransaction, final int totalAmount) throws InvalidKeyException {
        // JSON -> DTO 변환
        Gson gson = new Gson();
        PaymentResDto paymentResDto = gson.fromJson(jsonObject.toJSONString(), PaymentResDto.class);

        // 주문자 정보 조회
        Member member = memberRepository.getReferenceById(SecurityUtils.getCurrentUserId());

        // 결제 정보 저장
        TossPayment tossPayment = TossPayment.of(tossPaymentPayTransaction, paymentResDto);
        tossPaymentRepository.save(tossPayment);

        // 주문 정보 저장
        OrderHistory orderHistory = OrderHistory.builder()
            .tossPayment(tossPayment)
            .member(member)
            .status(PurchaseHistoryStatus.SUCCESS)
            .totalAmount(totalAmount)
            .build();
        List<OrderProduct> orderProducts = tossPaymentPayTransaction.getPayTransactions().stream()
            .map(d -> OrderProduct.builder()
                .product(d.getProduct())
                .orderHistory(orderHistory)
                .quantity(d.getQuantity())
                .amount(d.getPrice() * d.getQuantity())
                .build())
            .toList();
        orderHistory.updateOrderProducts(orderProducts);
        orderHistoryRepository.save(orderHistory);
    }

    /**
     * 결제 승인 실패 처리
     */
    private void processPayConfirmFail(JSONObject jsonObject, final String orderId) {
        final String errorCode = (String) jsonObject.get("code");
        final String errorMessage = (String) jsonObject.get("message");

        // 결제 실패 로그 저장
        PayErrorLog payErrorLog = PayErrorLog.builder()
            .orderId(orderId)
            .code(errorCode)
            .message(errorMessage)
            .build();
        payErrorLogRepository.save(payErrorLog);

        throw new PayApiException();
    }
}
