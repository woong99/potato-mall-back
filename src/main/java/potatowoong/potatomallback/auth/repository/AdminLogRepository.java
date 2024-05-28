package potatowoong.potatomallback.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.auth.entity.AdminLog;

public interface AdminLogRepository extends JpaRepository<AdminLog, Long> {

}
