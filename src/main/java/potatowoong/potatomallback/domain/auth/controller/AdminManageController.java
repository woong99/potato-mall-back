package potatowoong.potatomallback.domain.auth.controller;

import static potatowoong.potatomallback.global.common.LogMessage.ADD;
import static potatowoong.potatomallback.global.common.LogMessage.ADMIN_MANAGEMENT;
import static potatowoong.potatomallback.global.common.LogMessage.MODIFY;
import static potatowoong.potatomallback.global.common.LogMessage.REMOVE;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_DETAIL;
import static potatowoong.potatomallback.global.common.LogMessage.SEARCH_LIST;

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
import potatowoong.potatomallback.domain.auth.dto.request.AdminAddReqDto;
import potatowoong.potatomallback.domain.auth.dto.request.AdminModifyReqDto;
import potatowoong.potatomallback.domain.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.domain.auth.service.AdminLogService;
import potatowoong.potatomallback.domain.auth.service.AdminManageService;
import potatowoong.potatomallback.global.common.ApiResponseEntity;
import potatowoong.potatomallback.global.common.PageRequestDto;
import potatowoong.potatomallback.global.common.PageResponseDto;

@RestController
@RequestMapping("/api/admin/admin-management")
@RequiredArgsConstructor
public class AdminManageController {

    private final AdminManageService adminManageService;

    private final AdminLogService adminLogService;

    /**
     * 관리자 목록 조회 API
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponseEntity<PageResponseDto<AdminResDto>>> searchAdmin(PageRequestDto pageRequestDto) {
        PageResponseDto<AdminResDto> result = adminManageService.getAdminList(pageRequestDto);

        // 로그 저장
        adminLogService.addAdminLog(ADMIN_MANAGEMENT, SEARCH_LIST);
        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }

    /**
     * 관리자 상세 조회 API
     */
    @GetMapping("/{adminId}")
    public ResponseEntity<ApiResponseEntity<AdminResDto>> getAdmin(@PathVariable("adminId") String adminId) {
        AdminResDto result = adminManageService.getAdmin(adminId);

        // 로그 저장
        adminLogService.addAdminLog(ADMIN_MANAGEMENT, SEARCH_DETAIL, adminId, result.name());
        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }

    /**
     * 관리자 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addAdmin(@Valid @RequestBody AdminAddReqDto dto) {
        adminManageService.addAdmin(dto);

        // 로그 저장
        adminLogService.addAdminLog(ADMIN_MANAGEMENT, ADD, dto.getAdminId(), dto.getName());
        return ResponseEntity.ok(ApiResponseEntity.of("관리자 등록 성공"));
    }

    /**
     * 관리자 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyAdmin(@Valid @RequestBody AdminModifyReqDto dto) {
        adminManageService.modifyAdmin(dto);

        // 로그 저장
        adminLogService.addAdminLog(ADMIN_MANAGEMENT, MODIFY, dto.getAdminId(), dto.getName());
        return ResponseEntity.ok(ApiResponseEntity.of("관리자 수정 성공"));
    }

    /**
     * 관리자 삭제 API
     */
    @DeleteMapping("/{adminId}")
    public ResponseEntity<ApiResponseEntity<String>> deleteAdmin(@PathVariable("adminId") String adminId) {
        adminManageService.removeAdmin(adminId);

        // 로그 저장
        adminLogService.addAdminLog(ADMIN_MANAGEMENT, REMOVE, adminId);
        return ResponseEntity.ok(ApiResponseEntity.of("관리자 삭제 성공"));
    }

    /**
     * 관리자 아이디 중복 체크 API
     */
    @GetMapping("/check-duplicate/{adminId}")
    public ResponseEntity<ApiResponseEntity<Boolean>> checkDuplicateAdminId(@PathVariable("adminId") String adminId) {
        boolean result = adminManageService.checkDuplicateAdminId(adminId);
        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }
}
