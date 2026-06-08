package realize.db.dtj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import realize.db.dtj.domain.Movement;

import java.util.List;
import java.util.UUID;

public interface MovementRepository extends JpaRepository<Movement, UUID> {

    List<Movement> findByAccountIdOrderByCreatedAtDesc(UUID accountId);
}