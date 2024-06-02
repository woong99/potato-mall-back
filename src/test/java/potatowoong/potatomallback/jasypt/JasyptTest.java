package potatowoong.potatomallback.jasypt;


import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = JasyptTest.class)
@Slf4j
class JasyptTest {

    @Test
    void jasyptTest() {
        String encryptKey = System.getProperty("jasypt.encryptor.password");

        String plainText = ""; // 암호화 하고 싶은 문자열

        StandardPBEStringEncryptor jasypt = new StandardPBEStringEncryptor();
        jasypt.setPassword(encryptKey);

        String encryptedText = jasypt.encrypt(plainText);
        String decryptedText = jasypt.decrypt(encryptedText);

        log.info("encryptedText: {}", encryptedText);

        assertThat(plainText).isEqualTo(decryptedText);
    }
}
