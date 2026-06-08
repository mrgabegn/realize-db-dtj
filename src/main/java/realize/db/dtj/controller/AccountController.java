package realize.db.dtj.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import realize.db.dtj.dto.request.CreateAccountRequest;
import realize.db.dtj.dto.response.AccountResponse;
import realize.db.dtj.dto.response.MovementResponse;
import realize.db.dtj.service.AccountService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return accountService.createAccount(request);
    }

    @GetMapping("/{id}")
    public AccountResponse getAccountById(@PathVariable UUID id) {
        return accountService.getAccountById(id);
    }

    @GetMapping("/{id}/movements")
    public List<MovementResponse> getMovementsByAccountId(@PathVariable UUID id) {
        return accountService.getMovementsByAccountId(id);
    }
}