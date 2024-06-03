package potatowoong.potatomallback.file.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.file.enums.S3Folder;

@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private final AmazonS3Client amazonS3Client;

    public void uploadFile(final String fileName, S3Folder s3Folder, MultipartFile multipartFile) {
        // S3 폴더 파라미터 확인
        if (s3Folder == null) {
            throw new CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        // 파일이 비어있는지 확인
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE);
        }

        ObjectMetadata metaData = new ObjectMetadata();
        metaData.setContentType(multipartFile.getContentType());
        metaData.setContentLength(multipartFile.getSize());

        final String bucketName = bucket + "/" + s3Folder.getFolderName();

        try {
            amazonS3Client.putObject(bucketName, fileName, multipartFile.getInputStream(), metaData);
        } catch (IOException e) {
            log.error("Failed to upload file to S3", e);
            throw new CustomException(ErrorCode.FAILED_TO_UPLOAD_FILE);
        }
    }

    public void removeFile(final String fileName, S3Folder s3Folder) {
        try {
            amazonS3Client.deleteObject(bucket + "/" + s3Folder.getFolderName(), fileName);
        } catch (SdkClientException e) {
            throw new CustomException(ErrorCode.FAILED_TO_DELETE_FILE);
        }
    }
}
