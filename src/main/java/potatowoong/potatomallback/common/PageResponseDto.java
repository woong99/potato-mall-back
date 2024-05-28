package potatowoong.potatomallback.common;

import java.util.List;

public record PageResponseDto<T>(
    List<T> result,
    long totalElements) {

}
