package potatowoong.potatomallback.auth.repository;

import potatowoong.potatomallback.auth.dto.response.AdminLoginLogResDto;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;

public interface AdminLoginLogRepositoryCustom {

    PageResponseDto<AdminLoginLogResDto> findAdminLoginLogWithPage(PageRequestDto pageRequestDto);
}
