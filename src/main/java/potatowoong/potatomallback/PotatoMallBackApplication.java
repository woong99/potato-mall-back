package potatowoong.potatomallback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class PotatoMallBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PotatoMallBackApplication.class, args);
    }

}
