package com.psycorp.controller.api;

import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.model.objects.UserAccount;
import com.psycorp.service.UserAccountService;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Rest controller for user account.
 * url : "/account"
 * @author Maryna Kontar
 */
@RestController
@RequestMapping("account")
public class ApiUserAccountController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiUserAccountController.class);
    @Autowired
    private HttpServletRequest request;

    private final UserAccountService userAccountService;
    private final UserAccountDtoConverter userAccountDtoConverter;

    public ApiUserAccountController(UserAccountService userAccountService,
                                    UserAccountDtoConverter userAccountDtoConverter) {
        this.userAccountService = userAccountService;
        this.userAccountDtoConverter = userAccountDtoConverter;
    }
    
    /**
     * Endpoint for url ":/account/getAllRegisteredAndPassedTestUsersForMatching".
     * TODO: 08.08.19 after adding the functionality of CLOSERD accounts, add to those accounts those who invited to compare value profiles
     * @return registered, passed test user accounts, that already matched test with principal
     * or sent message with invite token to principal.
     */
    @GetMapping("/getAllRegisteredAndPassedTestUsersForMatching")
    public ResponseEntity<List<UserAccountDto>> getAllRegisteredAndPassedTestUsersForMatching(){
        LOGGER.trace("getAllRegisteredAndPassedTestUsersForMatching");
        List<UserAccount> userAccounts = userAccountService.getAllUserForMatchingPassedTest();
        return ResponseEntity.ok()
                .body(this.userAccountDtoConverter.transform(userAccounts));
    }

    /**
     * Endpoint for url ":/account/getAllRegisteredAndPassedTestUsers".
     * Get pageable registered and passed test user accounts except principal.
     * @param page page number to be returned.
     * @param size quantity of user accounts on page.
     * @return a page by all registered and passed test user accounts,
     * except principal user account.
     */
    @GetMapping("/getAllRegisteredAndPassedTestUsers")
    public ResponseEntity<Page<UserAccountDto>> getAllRegisteredAndPassedTestPageable(@RequestParam("page") int page , @RequestParam("size")int size){
        Sort sort = Sort.by(new Sort.Order(Sort.Direction.DESC, "userId"));
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<UserAccount> userAccountPage = userAccountService.getAllRegisteredAndPassedTestPageable(pageable);
        int totalElements = (int) userAccountPage.getTotalElements();
        return ResponseEntity.ok()
                .body(new PageImpl<>(userAccountPage
                    .stream()
                    .map(userAccountDtoConverter::transform)
                    .collect(Collectors.toList()), pageable, totalElements));
    }

    /**
     * Endpoint for url ":/account/inviteForMatching".
     * Invites user from userAccountDto to match test`s result.
     * @param userAccountDto user account, which are invited to compare the test results.
     * @return invitee user account with added to usersWhoInvitedYou field principal user.
     */
    @PostMapping("/inviteForMatching")
    public ResponseEntity<UserAccountDto> inviteForMatching(@RequestBody @NotNull @Valid UserAccountDto userAccountDto) {
        LOGGER.trace("inviteForMatching: {}", userAccountDto);
        return ResponseEntity.ok(userAccountDtoConverter.transform(
                userAccountService.inviteForMatching(userAccountDtoConverter.transform(userAccountDto))));
    }

    /**
     * Endpoint for url ":/account".
     * Update user account data. It only means changing the user's personal data (password is not considered).
     * @param userAccountDto account to update.
     * @return updated user account.
     */
    @PutMapping
    public ResponseEntity<UserAccountDto> update(@RequestBody @NotNull @Valid UserAccountDto userAccountDto) {
        LOGGER.trace("update: {}", userAccountDto);
        return ResponseEntity.ok()
                .body(userAccountDtoConverter.transform(
                        userAccountService.update(userAccountDtoConverter.transform(userAccountDto))));
    }

    /**
     * RuntimeException handler.
     * @param ex RuntimeException for handling.
     * @param request HttpServletRequest which caused the RuntimeException.
     * @return ResponseEntity with HttpStatus BAD_REQUEST and exception message in header.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        LOGGER.trace("IP: {}:{}:{} : EXCEPTION: {}", request.getRemoteHost(), request.getRemotePort(), request.getRemoteUser(), ex);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                        + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
