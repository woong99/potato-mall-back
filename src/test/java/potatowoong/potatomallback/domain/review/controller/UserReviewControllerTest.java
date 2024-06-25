package potatowoong.potatomallback.domain.review.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.beneathPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getDocumentResponse;
import static potatowoong.potatomallback.config.restdocs.ApiDocumentUtils.getNoAuthDocumentRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto;
import potatowoong.potatomallback.domain.review.dto.response.UserReviewResDto;
import potatowoong.potatomallback.domain.review.service.UserReviewService;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.common.ResponseText;
import potatowoong.potatomallback.global.exception.CustomException;
import potatowoong.potatomallback.global.exception.ErrorCode;

@WebMvcTest(UserReviewController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureRestDocs
@WithMockUser
class UserReviewControllerTest {

    @MockBean
    private UserReviewService userReviewService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("리뷰 목록 조회")
    class 리뷰_목록_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            UserReviewResDto.Detail detail = UserReviewResDto.Detail.builder()
                .reviewId(1L)
                .contents("리뷰 내용")
                .score(5)
                .nickname("작성자 닉네임")
                .createdAt("2021-08-01 00:00:00")
                .build();
            PageResponseDto<UserReviewResDto.Detail> response = new PageResponseDto<>(Collections.singletonList(detail), 1);

            given(userReviewService.getReviewList(any())).willReturn(response);

            // when & then
            ResultActions actions = mockMvc.perform(get("/api/user/review/search")
                .with(csrf().asHeader())
                .param("productId", "1")
                .param("page", "0")
                .param("size", "10")
                .param("searchWord", "검색어")
                .param("sortCondition", "score")
                .param("sortDirection", "ASCENDING")
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.result").exists())
                .andExpect(jsonPath("$.data.totalElements").value(1));

            actions
                .andDo(document("user-review-search",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    queryParameters(
                        parameterWithName("productId").optional().description("상품 ID"),
                        parameterWithName("page").description("페이지 번호"),
                        parameterWithName("size").description("페이지 크기"),
                        parameterWithName("searchWord").description("검색어(상품명)"),
                        parameterWithName("sortCondition").description("정렬 조건(score: 평점순, 기본 : 최신순)"),
                        parameterWithName("sortDirection").description("정렬 방향(ASCENDING: 오름차순, DESCENDING: 내림차순)")
                    ),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("result[].reviewId").description("리뷰 ID"),
                        fieldWithPath("result[].contents").description("리뷰 내용"),
                        fieldWithPath("result[].score").description("별점"),
                        fieldWithPath("result[].nickname").description("작성자 닉네임"),
                        fieldWithPath("result[].createdAt").description("작성일"),
                        fieldWithPath("totalElements").description("전체 개수")
                    )
                ));

            then(userReviewService).should().getReviewList(any());
        }
    }

    @Nested
    @DisplayName("리뷰 상세 조회")
    class 리뷰_상세_조회 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            UserReviewResDto.Detail detail = UserReviewResDto.Detail.builder()
                .reviewId(1L)
                .contents("리뷰 내용")
                .score(5)
                .nickname("작성자 닉네임")
                .createdAt("2021-08-01 00:00:00")
                .build();

            given(userReviewService.getReview(anyLong())).willReturn(detail);

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/user/review/{reviewId}", 1)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").exists())
                .andExpect(jsonPath("$.data.reviewId").value(detail.reviewId()))
                .andExpect(jsonPath("$.data.contents").value(detail.contents()))
                .andExpect(jsonPath("$.data.score").value(detail.score()))
                .andExpect(jsonPath("$.data.nickname").value(detail.nickname()))
                .andExpect(jsonPath("$.data.createdAt").value(detail.createdAt()));

            actions
                .andDo(document("user-review-detail",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    responseFields(
                        beneathPath("data").withSubsectionId("data"),
                        fieldWithPath("reviewId").description("리뷰 ID"),
                        fieldWithPath("contents").description("리뷰 내용"),
                        fieldWithPath("score").description("별점"),
                        fieldWithPath("nickname").description("작성자 닉네임"),
                        fieldWithPath("createdAt").description("작성일")
                    )
                ));

            then(userReviewService).should().getReview(anyLong());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰 ID")
        void 실패_존재하지_않는_리뷰_ID() throws Exception {
            // given
            given(userReviewService.getReview(anyLong())).willThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND));

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.get("/api/user/review/{reviewId}", 1)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.REVIEW_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-review-detail-fail",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse()
                ));

            then(userReviewService).should().getReview(anyLong());
        }
    }

    @Nested
    @DisplayName("리뷰 등록")
    class 리뷰_등록 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            willDoNothing().given(userReviewService).addReview(any());

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/review")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_ADD_REVIEW));

            actions
                .andDo(document("user-review-add",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("contents").optional().description("리뷰 내용"),
                        fieldWithPath("score").optional().description("별점")
                    )
                ));

            then(userReviewService).should().addReview(any());
        }

        @Test
        @DisplayName("실패 - 상품 정보가 없는 경우")
        void 실패_상품_정보가_없는_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.PRODUCT_NOT_FOUND)).given(userReviewService).addReview(any());

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/review")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PRODUCT_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-review-add-fail-product-not-found",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("contents").optional().description("리뷰 내용"),
                        fieldWithPath("score").optional().description("별점")
                    )
                ));

            then(userReviewService).should().addReview(any());
        }

        @Test
        @DisplayName("실패 - 이미 리뷰를 작성한 경우")
        void 실패_이미_리뷰를_작성한_경우() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.REVIEW_ALREADY_EXISTS)).given(userReviewService).addReview(any());

            // when & then
            ResultActions actions = mockMvc.perform(post("/api/user/review")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.REVIEW_ALREADY_EXISTS.getMessage()));

            actions
                .andDo(document("user-review-add-fail-review-already-exists",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("productId").optional().description("상품 ID"),
                        fieldWithPath("contents").optional().description("리뷰 내용"),
                        fieldWithPath("score").optional().description("별점")
                    )
                ));

            then(userReviewService).should().addReview(any());
        }

        @Test
        @DisplayName("실패 - 구매하지 않은 상품에 대한 리뷰 작성 시도")
        void 실패_구매하지_않은_상품에_대한_리뷰_작성_시도() throws Exception {
            // TODO : 결제 기능 구현 후 작성
        }

        private final UserReviewReqDto.Add dto = UserReviewReqDto.Add.builder()
            .productId(1L)
            .contents("리뷰 내용")
            .score(5)
            .build();
    }

    @Nested
    @DisplayName("리뷰 수정")
    class 리뷰_수정 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            willDoNothing().given(userReviewService).modifyReview(any());

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.put("/api/user/review")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(ResponseText.SUCCESS_MODIFY_REVIEW));

            actions
                .andDo(document("user-review-modify",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("reviewId").optional().description("리뷰 ID"),
                        fieldWithPath("contents").optional().description("리뷰 내용"),
                        fieldWithPath("score").optional().description("별점")
                    )
                ));

            then(userReviewService).should().modifyReview(any());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰 ID")
        void 실패_존재하지_않는_리뷰_ID() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND)).given(userReviewService).modifyReview(any());

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.put("/api/user/review")
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(objectMapper.writeValueAsString(dto)));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.REVIEW_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-review-modify-fail",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse(),
                    requestFields(
                        fieldWithPath("reviewId").optional().description("리뷰 ID"),
                        fieldWithPath("contents").optional().description("리뷰 내용"),
                        fieldWithPath("score").optional().description("별점")
                    )
                ));

            then(userReviewService).should().modifyReview(any());
        }

        private final UserReviewReqDto.Modify dto = UserReviewReqDto.Modify.builder()
            .reviewId(1L)
            .contents("리뷰 내용 수정")
            .score(4)
            .build();
    }

    @Nested
    @DisplayName("리뷰 삭제")
    class 리뷰_삭제 {

        @Test
        @DisplayName("성공")
        void 성공() throws Exception {
            // given
            willDoNothing().given(userReviewService).removeReview(anyLong());

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/user/review/{reviewId}", 1)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isOk());

            actions
                .andDo(document("user-review-remove",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse()
                ));

            then(userReviewService).should().removeReview(anyLong());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 리뷰 ID")
        void 실패_존재하지_않는_리뷰_ID() throws Exception {
            // given
            willThrow(new CustomException(ErrorCode.REVIEW_NOT_FOUND)).given(userReviewService).removeReview(anyLong());

            // when & then
            ResultActions actions = mockMvc.perform(RestDocumentationRequestBuilders.delete("/api/user/review/{reviewId}", 1)
                .with(csrf().asHeader())
                .contentType(MediaType.APPLICATION_JSON_VALUE));

            actions
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.REVIEW_NOT_FOUND.getMessage()));

            actions
                .andDo(document("user-review-remove-fail",
                    getNoAuthDocumentRequest(),
                    getDocumentResponse()
                ));

            then(userReviewService).should().removeReview(anyLong());
        }
    }
}