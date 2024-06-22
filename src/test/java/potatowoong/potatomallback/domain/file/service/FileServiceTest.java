package potatowoong.potatomallback.domain.file.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import potatowoong.potatomallback.domain.file.entity.AtchFile;
import potatowoong.potatomallback.domain.file.enums.S3Folder;
import potatowoong.potatomallback.domain.file.repository.AtchFileRepository;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private AtchFileRepository atchFileRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private FileService fileService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(fileService, "imageExtension", "jpg,jpeg,png,gif");
    }

    @Nested
    @DisplayName("이미지 파일 저장")
    class 이미지_파일_저장 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            // when
            fileService.saveImageAtchFile(S3Folder.PRODUCT, file);

            // then
            then(atchFileRepository).should().save(any());
            then(s3Service).should().uploadFile(any(), any(), any());
        }

        @Test
        @DisplayName("실패 - S3 폴더 파라미터가 null인 경우")
        void 실패_S3_폴더_파라미터가_null인_경우() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());

            // when
            assertThatThrownBy(() -> fileService.saveImageAtchFile(null, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        @Test
        @DisplayName("실패 - 파일이 비어있는 경우")
        void 실패_파일이_비어있는_경우() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", new byte[0]);

            // when
            assertThatThrownBy(() -> fileService.saveImageAtchFile(null, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        @Test
        @DisplayName("실패 - 이미지 파일이 아닌 경우")
        void 실패_이미지_파일이_아닌_경우() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "test".getBytes());

            // when
            assertThatThrownBy(() -> fileService.saveImageAtchFile(S3Folder.PRODUCT, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_FILE_EXTENSION);
        }

        @Test
        @DisplayName("실패 - S3에 파일 업로드 실패")
        void 실패_S3에_파일_업로드_실패() {
            // given
            MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "test".getBytes());
            willThrow(new CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE)).given(s3Service).uploadFile(any(), any(), any());

            // when
            assertThatThrownBy(() -> fileService.saveImageAtchFile(S3Folder.PRODUCT, file))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_UPLOAD_FILE);
        }
    }

    @Nested
    @DisplayName("파일 삭제")
    class 파일_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() {
            // given
            AtchFile atchFile = AtchFile.builder()
                .originalFileName("test.jpg")
                .storedFileName("test.jpg")
                .fileSize(1000L)
                .s3Folder(S3Folder.PRODUCT.getFolderName())
                .build();

            given(atchFileRepository.findById(1L)).willReturn(Optional.of(atchFile));

            // when
            fileService.removeAtchFile(1L);

            // then
            then(atchFileRepository).should().findById(1L);
            then(s3Service).should().removeFile("test.jpg", S3Folder.PRODUCT);
            then(atchFileRepository).should().delete(atchFile);
        }

        @Test
        @DisplayName("실패 - 파일 정보가 없는 경우")
        void 실패_파일_정보가_없는_경우() {
            // given
            given(atchFileRepository.findById(1L)).willReturn(Optional.empty());

            // when
            assertThatThrownBy(() -> fileService.removeAtchFile(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ATCH_FILE_NOT_FOUND);

            // then
            then(atchFileRepository).should().findById(1L);
        }

        @Test
        @DisplayName("실패 - S3에서 파일 삭제 실패")
        void 실패_S3에서_파일_삭제_실패() {
            // given
            AtchFile atchFile = AtchFile.builder()
                .originalFileName("test.jpg")
                .storedFileName("test.jpg")
                .fileSize(1000L)
                .s3Folder(S3Folder.PRODUCT.getFolderName())
                .build();

            given(atchFileRepository.findById(1L)).willReturn(Optional.of(atchFile));
            willThrow(new CustomException(ErrorCode.FAILED_TO_DELETE_FILE)).given(s3Service).removeFile(any(), any());

            // when
            assertThatThrownBy(() -> fileService.removeAtchFile(1L))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_TO_DELETE_FILE);

            // then
            then(atchFileRepository).should().findById(1L);
            then(s3Service).should().removeFile("test.jpg", S3Folder.PRODUCT);
        }
    }
}