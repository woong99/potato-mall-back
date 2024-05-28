package potatowoong.potatomallback.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.auth.entity.AdminLoginLog;

public interface AdminLoginLogRepository extends JpaRepository<AdminLoginLog, Long>, AdminLoginLogRepositoryCustom {

}
