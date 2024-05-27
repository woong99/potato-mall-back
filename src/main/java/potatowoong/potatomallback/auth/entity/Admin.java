package potatowoong.potatomallback.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Comment;
import potatowoong.potatomallback.config.db.BaseEntity;

@Entity
@Comment("관리자 계정 정보")
@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin extends BaseEntity {

    @Id
    @Column(name = "user_id", length = 20, nullable = false, updatable = false)
    @Comment("아이디")
    private String adminId;

    @Column(name = "password", length = 100, nullable = false)
    @Comment("비밀번호")
    private String password;

    @Column(name = "name", length = 10, nullable = false)
    @Comment("이름")
    private String name;
}
