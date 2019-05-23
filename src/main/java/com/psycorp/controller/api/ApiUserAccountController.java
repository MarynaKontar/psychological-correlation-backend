package com.psycorp.controller.api;

import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.service.UserAccountService;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("account")
public class ApiUserAccountController {
    private final UserAccountService userAccountService;
    private final UserAccountDtoConverter userAccountDtoConverter;

    public ApiUserAccountController(UserAccountService userAccountService, UserAccountDtoConverter userAccountDtoConverter) {
        this.userAccountService = userAccountService;
        this.userAccountDtoConverter = userAccountDtoConverter;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<UserAccountDto>> getAll() {
     return ResponseEntity.ok().body(userAccountDtoConverter.transform(userAccountService.getAllRegisteredAndPassedTest()));
    }

    @PostMapping("/inviteForMatching")
    public ResponseEntity<UserAccountDto> inviteForMatching(@RequestBody @NotNull @Valid UserAccountDto userAccountDto) {
//        userAccountService.inviteForMatching(userAccountDtoConverter.transform(userAccountDto));
//        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return ResponseEntity.ok(userAccountDtoConverter.transform(
                userAccountService.inviteForMatching(userAccountDtoConverter.transform(userAccountDto))));
    }
    @PutMapping
    public ResponseEntity<UserAccountDto> update(@RequestBody @NotNull @Valid UserAccountDto userAccountDto) {
        return ResponseEntity.ok().body(userAccountDtoConverter.transform(userAccountService.update(userAccountDtoConverter.transform(userAccountDto))));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
