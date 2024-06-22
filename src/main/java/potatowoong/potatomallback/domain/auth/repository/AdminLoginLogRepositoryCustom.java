package potatowoong.potatomallback.domain.auth.repository;

import potatowoong.potatomallback.domain.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

public interface AdminLoginLogRepositoryCustom {

    PageResponseDto<AdminLoginLogResDto> findAdminLoginLogWithPage(PageRequestDto pageRequestDto);
}
