package com.psycorp.controller.api;

import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.service.UserAccountService;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
@RequestMapping("account")
public class ApiUserAccountController {
    private final UserAccountService userAccountService;
    private final UserAccountDtoConverter userAccountDtoConverter;

    public ApiUserAccountController(UserAccountService userAccountService, UserAccountDtoConverter userAccountDtoConverter) {
        this.userAccountService = userAccountService;
        this.userAccountDtoConverter = userAccountDtoConverter;
    }

    @PutMapping
    public ResponseEntity<UserAccountDto> update(@RequestBody @NotNull @Valid UserAccountDto userAccountDto) {
        return ResponseEntity.ok().body(userAccountDtoConverter.transform(userAccountService.update(userAccountDtoConverter.transform(userAccountDto))));
    }
}
