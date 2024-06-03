package potatowoong.potatomallback.file.dto;

import lombok.Builder;
import potatowoong.potatomallback.file.entity.AtchFile;
import potatowoong.potatomallback.file.enums.S3Folder;

@Builder
public record AtchFileDto(
    Long atchFileId,
    String originalFileName,
    String storedFileName,
    long fileSize,
    S3Folder s3Folder
) {

    public static AtchFileDto of(AtchFile atchFile) {
        return AtchFileDto.builder()
            .atchFileId(atchFile.getAtchFileId())
            .originalFileName(atchFile.getOriginalFileName())
            .storedFileName(atchFile.getStoredFileName())
            .fileSize(atchFile.getFileSize())
            .s3Folder(S3Folder.valueOf(atchFile.getS3Folder().toUpperCase()))
            .build();
    }
}
