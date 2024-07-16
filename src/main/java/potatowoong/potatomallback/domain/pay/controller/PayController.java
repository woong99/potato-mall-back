package potatowoong.potatomallback.domain.pay.controller;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.pay.dto.request.UserPayReqDto;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.service.PayConfirmService;
import potatowoong.potatomallback.domain.pay.service.PayService;
import potatowoong.potatomallback.domain.product.service.UserProductService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.ResponseText;
import potatowoong.potatomallback.global.exception.PayApiException;

@RestController
@RequestMapping("/api/user/pay")
@RequiredArgsConstructor
@Slf4j
public class PayController {

    private final PayService payService;

    private final PayConfirmService payConfirmService;

    private final UserProductService userProductService;

    /**
     * 결제 가능 여부 확인 API
     */
    @PostMapping("/check-available")
    public ResponseEntity<ApiResponseEntity<String>> check(@Valid @RequestBody UserPayReqDto.CheckRequest dto) {
        // 결제 가능 여부 확인
        payService.checkAvailablePay(dto);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_CHECK_AVAILABLE_PAY));
    }

    /**
     * 결제 후 파라미터 검증 API
     */
    @PostMapping("/verify-payment")
    public ResponseEntity<ApiResponseEntity<String>> checkAfterPay(@Valid @RequestBody UserPayReqDto.VerifyPayment dto) {
        // 결제 후 처리
        payService.checkAfterPay(dto);

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_CHECK_AFTER_PAY));
    }

    /**
     * 결제 승인 API
     */
    @PostMapping("/confirm")
    public ResponseEntity<ApiResponseEntity<String>> confirm(@Valid @RequestBody UserPayReqDto.VerifyPayment dto) {
        Map<Long, Integer> transactionMap = new HashMap<>();
        TossPaymentPayTransaction tossPaymentPayTransaction;

        try {
            // 재고량 확인 및 수정
            tossPaymentPayTransaction = payConfirmService.modifyProductStockQuantity(transactionMap, dto.orderId());
        } catch (Exception e) {
            log.error("재고량 확인 및 수정 실패 :: ", e);
            rollbackProductQuantity(transactionMap);
            throw new PayApiException();
        }

        try {
            // 결제 승인 API 호출
            payConfirmService.payConfirm(tossPaymentPayTransaction, dto);
        } catch (Exception e) {
            log.error("결제 승인 API 호출 실패 :: ", e);
            rollbackProductQuantity(transactionMap);
            throw new PayApiException();
        }

        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_PAY));
    }

    /**
     * 재고량 복원
     */
    private void rollbackProductQuantity(Map<Long, Integer> transactionMap) {
        for (Entry<Long, Integer> entry : transactionMap.entrySet()) {
            userProductService.increaseProductQuantityWithLock(entry.getKey(), entry.getValue());
        }
    }
}
