package potatowoong.potatomallback.file.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.BDDMockito.willThrow;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import java.io.IOException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.file.enums.S3Folder;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3Client amazonS3Client;

    @InjectMocks
    private S3Service s3Service;

    @Nested
    @DisplayName("S3 파일 업로드")
    class S3_파일_업로드 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            // when
            s3Service.uploadFile("test.jpg", S3Folder.PRODUCT, file);

            // then
            then(amazonS3Client).should().putObject(any(), any(), any(), any());
        }

        @Test
        @DisplayName("실패 - 파일이 비어있는 경우")
        void 실패_파일이_비어있는_경우() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

            // when
            assertThatThrownBy(() -> s3Service.uploadFile("test.jpg", S3Folder.PRODUCT, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        @Test
        @DisplayName("실패 - S3 폴더 파라미터가 null인 경우")
        void 실패_S3_폴더_파라미터가_null인_경우() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            // when
            assertThatThrownBy(() -> s3Service.uploadFile("test.jpg", null, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        @Test
        @DisplayName("실패 - IOException 발생")
        void 실패_IOException_발생() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            willAnswer(invocation -> {
                throw new IOException();
            }).given(amazonS3Client).putObject(any(), any(), any(), any());

            // when
            assertThatThrownBy(() -> s3Service.uploadFile("test.jpg", S3Folder.PRODUCT, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_UPLOAD_FILE);
        }
    }

    @Nested
    @DisplayName("S3 파일 삭제")
    class S3_파일_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // when
            assertThatCode(() -> amazonS3Client.deleteObject(any(), any()))
                .doesNotThrowAnyException();

            // then
            then(amazonS3Client).should().deleteObject(any(), any());
        }

        @Test
        @DisplayName("실패 - SdkClientException 발생")
        void 실패_SdkClientException_발생() {
            // given

            willThrow(SdkClientException.class).given(amazonS3Client).deleteObject(any(), any());
            // when
            assertThatThrownBy(() -> s3Service.removeFile("test.jpg", S3Folder.PRODUCT))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_DELETE_FILE);
        }
    }
}