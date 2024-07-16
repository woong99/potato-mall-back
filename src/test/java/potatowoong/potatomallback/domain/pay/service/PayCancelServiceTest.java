package potatowoong.potatomallback.domain.pay.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import potatowoong.potatomallback.domain.pay.entity.TossPaymentPayTransaction;
import potatowoong.potatomallback.domain.pay.repository.PayNetCancelLogRepository;
import potatowoong.potatomallback.global.utils.TossPaymentUtils;

@ExtendWith(MockitoExtension.class)
class PayCancelServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PayNetCancelLogRepository payNetCancelLogRepository;

    @InjectMocks
    private PayCancelService payCancelService;

    @Nested
    @DisplayName("망 취소 API 호출")
    @SuppressWarnings("unchecked")
    class 망_취소_API_호출 {

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(payCancelService, tossSecretKey, tossSecretKey);
        }

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            given(restTemplate.exchange(tossCancelApi, HttpMethod.POST, getRequest(), JSONObject.class)).willReturn(ResponseEntity.ok().build());

            // when
            payCancelService.fetchNetCancelApi(tossPaymentPayTransaction, paymentKey);

            // then
            then(restTemplate).should().exchange(tossCancelApi, HttpMethod.POST, getRequest(), JSONObject.class);
            then(payNetCancelLogRepository).should().save(any());
        }

        @Test
        @DisplayName("실패 - 망취소 API 호출 실패")
        void 실패_망취소_API_호출_실패() {
            // given
            given(restTemplate.exchange(tossCancelApi, HttpMethod.POST, getRequest(), JSONObject.class)).willReturn(ResponseEntity.badRequest().build());

            // when
            payCancelService.fetchNetCancelApi(tossPaymentPayTransaction, paymentKey);

            // then
            then(restTemplate).should().exchange(tossCancelApi, HttpMethod.POST, getRequest(), JSONObject.class);
            then(payNetCancelLogRepository).should(never()).save(any());
        }

        private final String paymentKey = "paymentKey";

        private final String tossCancelApi = "https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel";

        private final String tossSecretKey = "tossSecretKey";

        private final TossPaymentPayTransaction tossPaymentPayTransaction = TossPaymentPayTransaction.builder()
            .build();

        private HttpEntity<String> getRequest() {
            JSONObject obj = new JSONObject();
            obj.put("cancelReason", "망취소");

            HttpHeaders headers = TossPaymentUtils.getCommonApiHeaders(tossSecretKey);
            return new HttpEntity<>(obj.toString(), headers);
        }
    }
}