package potatowoong.potatomallback.auth.controller;

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
import potatowoong.potatomallback.auth.dto.request.AdminAddReqDto;
import potatowoong.potatomallback.auth.dto.request.AdminModifyReqDto;
import potatowoong.potatomallback.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.auth.service.AdminLogService;
import potatowoong.potatomallback.auth.service.AdminManageService;
import potatowoong.potatomallback.common.ApiResponseEntity;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;

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
        adminLogService.addAdminLog("관리자 계정 관리", "목록 조회");
        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }

    /**
     * 관리자 상세 조회 API
     */
    @GetMapping("/{adminId}")
    public ResponseEntity<ApiResponseEntity<AdminResDto>> getAdmin(@PathVariable("adminId") String adminId) {
        AdminResDto result = adminManageService.getAdmin(adminId);

        // 로그 저장
        adminLogService.addAdminLog("관리자 계정 관리", "상세 조회", adminId, result.name());
        return ResponseEntity.ok(ApiResponseEntity.of(result));
    }

    /**
     * 관리자 등록 API
     */
    @PostMapping
    public ResponseEntity<ApiResponseEntity<String>> addAdmin(@Valid @RequestBody AdminAddReqDto dto) {
        adminManageService.addAdmin(dto);

        // 로그 저장
        adminLogService.addAdminLog("관리자 계정 관리", "등록", dto.getAdminId(), dto.getName());
        return ResponseEntity.ok(ApiResponseEntity.of("관리자 등록 성공"));
    }

    /**
     * 관리자 수정 API
     */
    @PutMapping
    public ResponseEntity<ApiResponseEntity<String>> modifyAdmin(@Valid @RequestBody AdminModifyReqDto dto) {
        adminManageService.modifyAdmin(dto);

        // 로그 저장
        adminLogService.addAdminLog("관리자 계정 관리", "수정", dto.getAdminId(), dto.getName());
        return ResponseEntity.ok(ApiResponseEntity.of("관리자 수정 성공"));
    }

    /**
     * 관리자 삭제 API
     */
    @DeleteMapping("/{adminId}")
    public ResponseEntity<ApiResponseEntity<String>> deleteAdmin(@PathVariable("adminId") String adminId) {
        adminManageService.removeAdmin(adminId);

        // 로그 저장
        adminLogService.addAdminLog("관리자 계정 관리", "삭제", adminId);
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
