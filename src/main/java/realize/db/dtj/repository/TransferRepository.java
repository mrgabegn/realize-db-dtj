package realize.db.dtj.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import realize.db.dtj.domain.Transfer;

import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {
}