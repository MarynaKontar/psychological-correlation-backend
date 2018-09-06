package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserDtoConverter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/user")
@PropertySource("classpath:errormessages.properties")
public class ApiUserController {

    private final UserService userService;
    private final UserDtoConverter userDtoConverter;

    @Autowired
    public ApiUserController(UserService userService, UserDtoConverter userDtoConverter) {
        this.userService = userService;
        this.userDtoConverter = userDtoConverter;
    }


    @PostMapping()
    public ResponseEntity<SimpleUserDto> save(@RequestBody @NotNull @Valid SimpleUserDto userDto) {

        User user = userService.createUser(userDtoConverter.transform(userDto));
        return new ResponseEntity<>(userDtoConverter.transform(user), HttpStatus.CREATED);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<SimpleUserDto> get(@PathVariable ObjectId userId){
        return ResponseEntity.ok().body(userDtoConverter.transform(userService.findById(userId)));
    }

    @GetMapping
    public ResponseEntity<List<SimpleUserDto>> getAll(){
        return ResponseEntity.ok().body(userDtoConverter.transform(userService.findAll()));
    }

    @GetMapping("/email")
    public ResponseEntity<SimpleUserDto> getByEmail(@RequestParam String email){
        return ResponseEntity.ok().body(userDtoConverter.transform(userService.findFirstUserByEmail(email)));
    }

    @PutMapping
    public ResponseEntity<SimpleUserDto> update(@RequestBody @NotNull SimpleUserDto userDto) {

        User user = userService.updateUser(userDtoConverter.transform(userDto));
        return ResponseEntity.ok().body(userDtoConverter.transform(user));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<SimpleUserDto> delete(@PathVariable ObjectId userId){

        return ResponseEntity.ok().body(userDtoConverter.transform(userService.deleteUser(userId)));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.add("success", "false");
        //TODO Body<CustomException>
//        (CustomExeption)ex
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage()
                + "; path: " + request.getServletPath());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
