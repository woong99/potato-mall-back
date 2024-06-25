package potatowoong.potatomallback.domain.review.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto;
import potatowoong.potatomallback.domain.review.dto.response.UserReviewResDto;
import potatowoong.potatomallback.domain.review.service.UserReviewService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.PageResponseDto;
import potatowoong.potatomallback.global.common.ResponseText;

@RestController
@RequestMapping("/api/user/review")
@RequiredArgsConstructor
public class UserReviewController {

    private final UserReviewService userReviewService;

    /**
     * 리뷰 목록 조회 API
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseEntity<PageResponseDto<UserReviewResDto.Detail>>> search(@Valid UserReviewReqDto.Search dto) {
        return ResponseEntity.ok(ApiResponseEntity.of(userReviewService.getReviewList(dto)));
    }

    /**
     * 리뷰 상세 조회 API
     */
    @GetMapping("/{reviewId}")
    public ResponseEntity<ApiResponseEntity<UserReviewResDto.Detail>> detail(@PathVariable long reviewId) {
        return ResponseEntity.ok(ApiResponseEntity.of(userReviewService.getReview(reviewId)));
    }

    /**
     * 리뷰 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addReview(@Valid @RequestBody UserReviewReqDto.Add dto) {
        userReviewService.addReview(dto);
        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_ADD_REVIEW));
    }

    /**
     * 리뷰 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyReview(@Valid @RequestBody UserReviewReqDto.Modify dto) {
        userReviewService.modifyReview(dto);
        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_MODIFY_REVIEW));
    }

    /**
     * 리뷰 삭제 API
     */
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponseEntity<String>> removeReview(@PathVariable long reviewId) {
        userReviewService.removeReview(reviewId);
        return ResponseEntity.ok(ApiResponseEntity.of(ResponseText.SUCCESS_REMOVE_REVIEW));
    }
}
