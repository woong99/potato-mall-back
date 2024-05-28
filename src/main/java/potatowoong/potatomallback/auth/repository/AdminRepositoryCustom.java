package potatowoong.potatomallback.auth.repository;

import potatowoong.potatomallback.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;

public interface AdminRepositoryCustom {

    PageResponseDto<AdminResDto> findAdminWithPage(PageRequestDto pageRequestDto);
}
