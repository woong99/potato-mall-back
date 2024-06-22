package potatowoong.potatomallback.domain.auth.repository;

import potatowoong.potatomallback.domain.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

public interface AdminRepositoryCustom {

    PageResponseDto<AdminResDto> findAdminWithPage(PageRequestDto pageRequestDto);
}
