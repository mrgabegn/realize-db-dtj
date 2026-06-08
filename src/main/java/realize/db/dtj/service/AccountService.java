package realize.db.dtj.service;

import jakarta.validation.Valid;
import realize.db.dtj.dto.request.CreateAccountRequest;
import realize.db.dtj.dto.response.AccountResponse;
import realize.db.dtj.dto.response.MovementResponse;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    AccountResponse createAccount(@Valid CreateAccountRequest request);

    AccountResponse getAccountById(UUID id);

    List<MovementResponse> getMovementsByAccountId(UUID id);
}
