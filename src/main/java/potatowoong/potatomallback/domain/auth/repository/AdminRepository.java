package potatowoong.potatomallback.domain.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.auth.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, String>, AdminRepositoryCustom {

}
