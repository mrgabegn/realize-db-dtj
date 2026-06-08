package realize.db.dtj.service;

import jakarta.validation.Valid;
import realize.db.dtj.dto.request.TransferRequest;
import realize.db.dtj.dto.response.TransferResponse;

public interface TransferService {
    TransferResponse transfer(@Valid TransferRequest request);
}
