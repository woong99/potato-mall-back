package potatowoong.potatomallback.global.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseText {

    public static final String OK = "OK";

    public static final String DUPLICATE = "DUPLICATE";

    public static final String SUCCESS_SIGN_UP = "회원가입 성공";

    public static final String SUCCESS_LOGOUT = "로그아웃 성공";

    public static final String SUCCESS_ADD_ADMIN = "관리자 등록 성공";

    public static final String SUCCESS_MODIFY_ADMIN = "관리자 수정 성공";

    public static final String SUCCESS_REMOVE_ADMIN = "관리자 삭제 성공";

    public static final String SUCCESS_ADD_PRODUCT = "상품 등록 성공";

    public static final String SUCCESS_MODIFY_PRODUCT = "상품 수정 성공";

    public static final String SUCCESS_REMOVE_PRODUCT = "상품 삭제 성공";

    public static final String SUCCESS_ADD_PRODUCT_CATEGORY = "상품 카테고리 등록 성공";

    public static final String SUCCESS_MODIFY_PRODUCT_CATEGORY = "상품 카테고리 수정 성공";

    public static final String SUCCESS_REMOVE_PRODUCT_CATEGORY = "상품 카테고리 삭제 성공";
}
