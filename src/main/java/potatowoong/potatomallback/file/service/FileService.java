package potatowoong.potatomallback.file.service;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.file.entity.AtchFile;
import potatowoong.potatomallback.file.enums.S3Folder;
import potatowoong.potatomallback.file.repository.AtchFileRepository;

@Service
@RequiredArgsConstructor
public class FileService {

    private final S3Service s3Service;

    private final AtchFileRepository atchFileRepository;

    @Value("${file.image-extension}")
    private String imageExtension;

    @Transactional
    public AtchFile saveImageAtchFile(S3Folder s3Folder, MultipartFile file) {
        // S3 폴더 파라미터 확인
        if (s3Folder == null) {
            throw new CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        // 파일이 비어있는지 확인
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        // 파일 확장자 확인
        final String fileExtension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (StringUtils.isBlank(fileExtension) || !imageExtension.contains(fileExtension)) {
            throw new CustomException(ErrorCode.INVALID_FILE_EXTENSION);
        }

        // 파일 이름 생성
        final String fileName = UUID.randomUUID() + "." + fileExtension;

        // S3에 파일 업로드
        s3Service.uploadFile(fileName, s3Folder, file);

        // 파일 정보 저장
        AtchFile atchFile = AtchFile.builder()
            .originalFileName(file.getOriginalFilename())
            .storedFileName(fileName)
            .fileSize(file.getSize())
            .s3Folder(s3Folder.getFolderName())
            .build();
        return atchFileRepository.save(atchFile);
    }

    @Transactional
    public void removeAtchFile(final long atchFileId) {
        // 파일 정보 조회
        AtchFile atchFile = atchFileRepository.findById(atchFileId)
            .orElseThrow(() -> new CustomException(ErrorCode.ATCH_FILE_NOT_FOUND));

        // S3에서 파일 삭제
        s3Service.removeFile(atchFile.getStoredFileName(), S3Folder.valueOf(atchFile.getS3Folder().toUpperCase()));

        // 파일 정보 삭제
        atchFileRepository.delete(atchFile);
    }
}
