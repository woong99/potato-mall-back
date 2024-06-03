package potatowoong.potatomallback.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    EXIST_USER_ID(HttpStatus.BAD_REQUEST, "AU001", "이미 존재하는 아이디입니다."),
    MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM(HttpStatus.BAD_REQUEST, "AU002", "비밀번호가 일치하지 않습니다."),
    EXPIRED_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU003", "만료된 엑세스 토큰입니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "AU004", "유효하지 않은 엑세스 토큰입니다."),
    FAILED_TO_LOGIN(HttpStatus.UNAUTHORIZED, "AU005", "로그인에 실패하였습니다. 아이디 또는 비밀번호를 확인해주세요."),
    ADMIN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AU006", "존재하지 않는 관리자입니다."),
    INCORRECT_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "AU007", "비밀번호는 8자 이상 20자 이하로 입력해주세요."),
    SELF_DELETION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AU008", "자신의 계정은 삭제할 수 없습니다."),
    DUPLICATED_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "PC001", "이미 존재하는 카테고리명입니다."),
    NOT_FOUND_CATEGORY(HttpStatus.BAD_REQUEST, "PC002", "존재하지 않는 카테고리입니다."),
    FAILED_TO_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "PF001", "파일 업로드에 실패하였습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "PF002", "지원하지 않는 파일 확장자입니다."),
    EXCEEDED_FILE_SIZE(HttpStatus.BAD_REQUEST, "PF003", "파일 크기가 초과되었습니다."),
    FAILED_TO_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "PF004", "파일 삭제에 실패하였습니다."),
    ATCH_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PF005", "존재하지 않는 첨부파일입니다.");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
