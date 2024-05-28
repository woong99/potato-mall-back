package potatowoong.potatomallback.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.auth.entity.Admin;

public interface AdminRepository extends JpaRepository<Admin, String>, AdminRepositoryCustom {

}
