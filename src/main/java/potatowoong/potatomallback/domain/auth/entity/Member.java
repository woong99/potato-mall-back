package potatowoong.potatomallback.domain.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import potatowoong.potatomallback.domain.auth.dto.request.UserSignUpReqDto;
import potatowoong.potatomallback.global.auth.oauth.enums.SocialType;

@Entity
@Comment("사용자 계정 정보")
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Member {

    @Id
    @Column(name = "user_id", length = 50, nullable = false, updatable = false)
    @Comment("아이디")
    private String userId;

    @Column(name = "password", length = 100)
    @Comment("비밀번호")
    private String password;

    @Column(name = "nickname", length = 20, nullable = false)
    @Comment("닉네임")
    private String nickname;

    @Column(name = "social_type", updatable = false)
    @Comment("SNS 로그인 유형(KAKAO, NAVER, GOOGLE)")
    @Enumerated(value = EnumType.STRING)
    private SocialType socialType;

    @CreatedDate
    @Column(name = "reg_date", updatable = false, nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("등록일자")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "upd_date", nullable = false, columnDefinition = "DATETIME")
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Comment("수정일자")
    private LocalDateTime updatedAt;

    @LastModifiedBy
    @Column(name = "upd_id", nullable = false)
    @Comment("수정자")
    private String updatedBy;

    @Builder
    public Member(String userId, String password, String nickname, SocialType socialType, String updatedBy) {
        this.userId = userId;
        this.password = password;
        this.nickname = nickname;
        this.socialType = socialType;
        this.updatedBy = updatedBy;
    }

    public static Member of(UserSignUpReqDto dto) {
        return Member.builder()
            .userId(dto.getUserId())
            .password(dto.getPassword())
            .nickname(dto.getNickname())
            .updatedBy(dto.getUserId())
            .build();
    }
}
