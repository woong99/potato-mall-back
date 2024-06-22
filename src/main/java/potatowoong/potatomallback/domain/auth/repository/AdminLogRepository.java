package potatowoong.potatomallback.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.auth.entity.AdminLog;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

}
