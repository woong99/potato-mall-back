package potatowoong.potatomallback.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

    /**
     * 파일이 존재하는지 확인
     *
     * @param file 파일
     * @return 파일이 존재하는지 여부
     */
    public static boolean hasFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    /**
     * 파일이 존재하지 않는지 확인
     *
     * @param file 파일
     * @return 파일이 존재하지 않는지 여부
     */
    public static boolean hasNotFile(MultipartFile file) {
        return !hasFile(file);
    }
}
