package potatowoong.potatomallback.domain.pay.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import potatowoong.potatomallback.domain.pay.entity.PayNetCancelLog;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.repository.PayNetCancelLogRepository;
import potatowoong.potatomallback.global.exception.PayApiException;
import potatowoong.potatomallback.global.utils.TossPaymentUtils;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayCancelService {

    private final RestTemplate restTemplate;

    private final PayNetCancelLogRepository payNetCancelLogRepository;

    @Value("${toss.secret-key}")
    private String tossSecretKey;

    /**
     * 망 취소 API 호출
     */
    @SuppressWarnings("unchecked")
    @Transactional(propagation = Propagation.REQUIRES_NEW, noRollbackFor = PayApiException.class)
    public void fetchNetCancelApi(TossPaymentPayTransaction tossPaymentPayTransaction, final String paymentKey) {
        log.error("[{}] :: 망취소 API 호출", paymentKey);

        JSONObject obj = new JSONObject();
        obj.put("cancelReason", "망취소");

        HttpHeaders headers = TossPaymentUtils.getCommonApiHeaders(tossSecretKey);
        HttpEntity<String> request = new HttpEntity<>(obj.toString(), headers);

        final String tossCancelApi = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";
        ResponseEntity<JSONObject> response = restTemplate.exchange(tossCancelApi, HttpMethod.POST, request, JSONObject.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            log.error("[{}] :: 망취소 API 호출 실패", paymentKey);
            return;
        }

        // 망취소 로그 저장
        PayNetCancelLog payNetCancelLog = PayNetCancelLog.builder()
            .tossPaymentPayTransaction(tossPaymentPayTransaction)
            .paymentKey(paymentKey)
            .build();
        payNetCancelLogRepository.save(payNetCancelLog);
        log.error("[{}] :: 망취소 API 호출 성공", paymentKey);
    }
}
