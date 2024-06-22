package potatowoong.potatomallback.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.auth.entity.AdminLoginLog;

public interface AdminLoginLogRepository extends JpaRepository<AdminLoginLog, Long>, AdminLoginLogRepositoryCustom {

}
