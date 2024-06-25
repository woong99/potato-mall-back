package potatowoong.potatomallback.domain.review.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import potatowoong.potatomallback.domain.review.entity.Review;

public interface ReviewRepository extends JpaRepository<Review, Long>, ReviewRepositoryCustom {

    boolean existsByProductProductIdAndMemberUserId(long productId, String userId);

    @EntityGraph(attributePaths = {"member"})
    Optional<Review> findByReviewIdAndMemberUserId(long reviewId, String userId);
}
