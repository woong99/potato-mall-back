package potatowoong.potatomallback.global.utils;

import java.util.Base64;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TossPaymentUtils {

    // TODO : toss secret key 주입

    /**
     * toss api 호출시 사용되는 header
     *
     * @param tossSecretKey toss secret key
     * @return HttpHeaders
     */
    public static HttpHeaders getCommonApiHeaders(final String tossSecretKey) {
        Base64.Encoder encoder = Base64.getEncoder();
        byte[] encodedBytes = encoder.encode((tossSecretKey + ":").getBytes());
        final String authorizations = "Basic " + new String(encodedBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", authorizations);
        headers.set("Content-Type", "application/json");
        return headers;
    }
}
