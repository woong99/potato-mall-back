package potatowoong.potatomallback.domain.pay.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPayReqDto {

    /**
     * 결제 가능 여부 확인 요청 DTO
     */
    @Builder
    public record CheckRequest(

        @NotBlank(message = "주문 ID는 필수값입니다.")
        String orderId,

        @Valid
        @Size(min = 1, message = "상품 정보는 최소 1개 이상이어야 합니다.")
        List<CheckProduct> products,

        @NotNull(message = "결제 금액은 필수값입니다.")
        @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
        Integer amount
    ) {

    }

    /**
     * 결제 가능 여부 확인 상품 정보 DTO
     */
    @Builder
    public record CheckProduct(

        @NotNull(message = "상품 정보 IDX는 필수값입니다.")
        Long productId,

        @NotNull(message = "구매량은 필수값입니다.")
        @Min(value = 1, message = "구매량은 1개 이상이어야 합니다.")
        Integer quantity,

        Long shoppingCartId
    ) {

    }

    /**
     * 결제 중 오류 처리 요청 DTO
     */
    @Builder
    public record Error(

        @NotBlank(message = "주문 ID는 필수값입니다.")
        String orderId,

        String errorCode,

        String errorMessage
    ) {

    }

    /**
     * 결제 후 처리 요청 DTO
     */
    @Builder
    public record VerifyPayment(

        @NotBlank(message = "주문 ID는 필수값입니다.")
        String orderId,

        @NotNull(message = "결제 금액은 필수값입니다.")
        Integer amount,

        @NotBlank(message = "결제 키는 필수값입니다.")
        String paymentKey
    ) {

    }
}
