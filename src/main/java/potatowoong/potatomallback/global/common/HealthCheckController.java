package potatowoong.potatomallback.global.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health-check")
public class HealthCheckController {

    @RequestMapping
    public ResponseEntity<Void> healthCheck() {
        return ResponseEntity.ok().build();
    }
}
