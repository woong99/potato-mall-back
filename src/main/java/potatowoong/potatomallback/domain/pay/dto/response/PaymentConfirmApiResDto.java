package potatowoong.potatomallback.domain.pay.dto.response;

import lombok.Builder;
import org.json.simple.JSONObject;

@Builder
public record PaymentConfirmApiResDto(

    boolean isSuccess,

    JSONObject jsonObject
) {

}
