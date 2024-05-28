package potatowoong.potatomallback.auth.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import potatowoong.potatomallback.auth.dto.request.AdminAddReqDto;
import potatowoong.potatomallback.auth.dto.request.AdminModifyReqDto;
import potatowoong.potatomallback.auth.dto.response.AdminResDto;
import potatowoong.potatomallback.auth.entity.Admin;
import potatowoong.potatomallback.auth.repository.AdminRepository;
import potatowoong.potatomallback.common.PageRequestDto;
import potatowoong.potatomallback.common.PageResponseDto;
import potatowoong.potatomallback.exception.CustomException;
import potatowoong.potatomallback.exception.ErrorCode;
import potatowoong.potatomallback.utils.SecurityUtils;

@Service
@RequiredArgsConstructor
public class AdminManageService {

    private final AdminRepository adminRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void addAdmin(AdminAddReqDto dto) {
        // 아이디 중복 체크
        Optional<Admin> savedAdmin = adminRepository.findById(dto.getAdminId());
        if (savedAdmin.isPresent()) {
            throw new CustomException(ErrorCode.EXIST_USER_ID);
        }

        // 비밀번호 확인
        if (!dto.getPassword().equals(dto.getPasswordConfirm())) {
            throw new CustomException(ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM);
        }

        // 비밀번호 암호화
        dto.setPassword(passwordEncoder.encode(dto.getPassword()));

        // 관리자 등록
        Admin admin = Admin.addOf(dto);
        adminRepository.save(admin);
    }

    @Transactional(readOnly = true)
    public boolean checkDuplicateAdminId(final String adminId) {
        return adminRepository.existsById(adminId);
    }

    @Transactional
    public void modifyAdmin(AdminModifyReqDto dto) {
        final String adminId = dto.getAdminId();
        String password = dto.getPassword();
        final String passwordConfirm = dto.getPasswordConfirm();

        // 관리자 조회
        Admin savedAdmin = adminRepository.findById(adminId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        // 비밀번호 변경
        if (StringUtils.isNotBlank(password) && StringUtils.isNotBlank(passwordConfirm)) {
            validatePassword(password, passwordConfirm);
            password = passwordEncoder.encode(password);
        }

        savedAdmin.modify(dto.getName(), password);
        adminRepository.save(savedAdmin);
    }

    @Transactional
    public void removeAdmin(final String adminId) {
        // 관리자 조회
        Admin savedAdmin = adminRepository.findById(adminId)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));

        // 자신의 계정 삭제 불가
        if (savedAdmin.getAdminId().equals(SecurityUtils.getCurrentUserId())) {
            throw new CustomException(ErrorCode.SELF_DELETION_NOT_ALLOWED);
        }

        savedAdmin.deleteEntity();
        adminRepository.save(savedAdmin);
    }

    @Transactional(readOnly = true)
    public AdminResDto getAdmin(final String adminId) {
        return adminRepository.findById(adminId)
            .map(AdminResDto::of)
            .orElseThrow(() -> new CustomException(ErrorCode.ADMIN_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AdminResDto> getAdminList(PageRequestDto pageRequestDto) {
        return adminRepository.findAdminWithPage(pageRequestDto);
    }

    /**
     * 비밀번호 유효성 검사
     *
     * @param password        비밀번호
     * @param passwordConfirm 비밀번호 확인
     */
    private void validatePassword(final String password, final String passwordConfirm) {
        if (!passwordConfirm.equals(password)) {
            throw new CustomException(ErrorCode.MISMATCH_PASSWORD_AND_PASSWORD_CONFIRM);
        }

        // 비밀번호 길이 체크
        if (password.length() < 8 || password.length() > 20) {
            throw new CustomException(ErrorCode.INCORRECT_PASSWORD_LENGTH);
        }
    }
}
