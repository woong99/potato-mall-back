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

    public static final String SUCCESS_ADD_PRODUCT_LIKE = "상품 좋아요 추가 성공";

    public static final String SUCCESS_REMOVE_PRODUCT_LIKE = "상품 좋아요 삭제 성공";

    public static final String SUCCESS_ADD_REVIEW = "리뷰 등록 성공";

    public static final String SUCCESS_MODIFY_REVIEW = "리뷰 수정 성공";

    public static final String SUCCESS_REMOVE_REVIEW = "리뷰 삭제 성공";

    public static final String SUCCESS_ADD_SHOPPING_CART = "장바구니 상품 등록 성공";

    public static final String SUCCESS_MODIFY_SHOPPING_CART = "장바구니 상품 수정 성공";

    public static final String SUCCESS_REMOVE_SHOPPING_CART = "장바구니 상품 삭제 성공";

    public static final String SUCCESS_CHECK_AVAILABLE_PAY = "결제 가능 여부 확인 성공";
    
    public static final String SUCCESS_CHECK_AFTER_PAY = "결제 후 파라미터 검증 성공";

    public static final String SUCCESS_PAY = "결제 성공";
}
