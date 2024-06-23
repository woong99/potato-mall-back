package potatowoong.potatomallback.global.exception;

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
    FAILED_TO_LOGIN(HttpStatus.BAD_REQUEST, "AU005", "로그인에 실패하였습니다. 아이디 또는 비밀번호를 확인해주세요."),
    ADMIN_NOT_FOUND(HttpStatus.BAD_REQUEST, "AU006", "존재하지 않는 관리자입니다."),
    INCORRECT_PASSWORD_LENGTH(HttpStatus.BAD_REQUEST, "AU007", "비밀번호는 8자 이상 20자 이하로 입력해주세요."),
    SELF_DELETION_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "AU008", "자신의 계정은 삭제할 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AU009", "접근 권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AU010", "로그인이 필요합니다."),
    DUPLICATED_CATEGORY_NAME(HttpStatus.BAD_REQUEST, "PC001", "이미 존재하는 카테고리명입니다."),
    NOT_FOUND_CATEGORY(HttpStatus.BAD_REQUEST, "PC002", "존재하지 않는 카테고리입니다."),
    EXIST_PRODUCT_IN_CATEGORY(HttpStatus.BAD_REQUEST, "PC003", "해당 카테고리에 상품이 존재합니다."),
    FAILED_TO_UPLOAD_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "PF001", "파일 업로드에 실패하였습니다."),
    INVALID_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "PF002", "지원하지 않는 파일 확장자입니다."),
    EXCEEDED_FILE_SIZE(HttpStatus.BAD_REQUEST, "PF003", "파일 크기가 초과되었습니다."),
    FAILED_TO_DELETE_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "PF004", "파일 삭제에 실패하였습니다."),
    ATCH_FILE_NOT_FOUND(HttpStatus.BAD_REQUEST, "PF005", "존재하지 않는 첨부파일입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.BAD_REQUEST, "PD001", "존재하지 않는 상품입니다."),
    NOT_FOUND_THUMBNAIL(HttpStatus.BAD_REQUEST, "PD002", "썸네일 이미지를 등록해주세요."),
    DUPLICATED_PRODUCT_NAME(HttpStatus.BAD_REQUEST, "PD003", "이미 존재하는 상품명입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ISE001", "서버 내부 오류입니다."),
    DUPLICATE_USER_ID(HttpStatus.BAD_REQUEST, "UA001", "이미 존재하는 아이디입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "UA002", "이미 존재하는 닉네임입니다."),
    PASSWORD_NOT_MATCHED(HttpStatus.BAD_REQUEST, "UA003", "비밀번호가 일치하지 않습니다."),
    WRONG_LOGIN_TYPE(HttpStatus.BAD_REQUEST, "UA004", "잘못된 로그인 타입입니다."),
    ALREADY_LIKED_PRODUCT(HttpStatus.BAD_REQUEST, "PL001", "이미 좋아요한 상품입니다."),
    NOT_LIKED_PRODUCT(HttpStatus.BAD_REQUEST, "PL002", "좋아요하지 않은 상품입니다.");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
