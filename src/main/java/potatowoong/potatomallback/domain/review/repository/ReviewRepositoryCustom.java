package potatowoong.potatomallback.domain.review.repository;

import potatowoong.potatomallback.domain.review.dto.request.UserReviewReqDto;
import potatowoong.potatomallback.domain.review.dto.response.UserReviewResDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

public interface ReviewRepositoryCustom {

    PageResponseDto<UserReviewResDto.Detail> findReviewWithPage(UserReviewReqDto.Search dto);
}
