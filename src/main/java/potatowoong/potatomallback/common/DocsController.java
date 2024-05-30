package potatowoong.potatomallback.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.exception.ErrorResponseEntity;

/**
 * RestDocs 공통 응답 snippet 생성을 위한 Controller
 */
@RestController
@RequestMapping("/api/docs")
public class DocsController {

    @GetMapping("/ok")
    public ResponseEntity<ApiResponseEntity<Void>> okDocs() {
        return ResponseEntity.ok(ApiResponseEntity.of(null));
    }

    @GetMapping("/error")
    public ResponseEntity<ErrorResponseEntity> errorDocs() {
        return ErrorResponseEntity.toResponseEntity(ErrorCode.ADMIN_NOT_FOUND);
    }
}
