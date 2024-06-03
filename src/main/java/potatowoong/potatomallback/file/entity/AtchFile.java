package potatowoong.potatomallback.file.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;

@Entity
@Comment("첨부파일 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AtchFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("첨부파일 정보 IDX")
    private Long atchFileId;

    @Column(nullable = false, updatable = false)
    @Comment("원 파일명")
    private String originalFileName;

    @Column(nullable = false, updatable = false)
    @Comment("저장 파일명")
    private String storedFileName;

    @Column(nullable = false, updatable = false)
    @Comment("파일 크기")
    private long fileSize;

    @Column(nullable = false, updatable = false)
    @Comment("S3 폴더명")
    private String s3Folder;

    @Builder
    public AtchFile(String originalFileName, String storedFileName, long fileSize, String s3Folder) {
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
        this.fileSize = fileSize;
        this.s3Folder = s3Folder;
    }
}

