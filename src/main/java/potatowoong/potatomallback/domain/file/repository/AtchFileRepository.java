package potatowoong.potatomallback.domain.file.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.file.entity.AtchFile;

public interface AtchFileRepository extends JpaRepository<AtchFile, Long> {

}
