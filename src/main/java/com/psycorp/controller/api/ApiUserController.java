package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserDtoConverter;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/user")
public class ApiUserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserDtoConverter userDtoConverter;


    @PostMapping()
    public ResponseEntity<SimpleUserDto> save(@RequestBody @NotNull @Valid SimpleUserDto userDto) {

        User user = userService.createUser(userDtoConverter.transform(userDto));
        return new ResponseEntity<>(userDtoConverter.transform(user), HttpStatus.CREATED);
    }

    @GetMapping(value = "/{userId}")
    public ResponseEntity<SimpleUserDto> get(@PathVariable ObjectId userId){
        return ResponseEntity.ok().body(userDtoConverter.transform(userService.findById(userId)));
    }

    @GetMapping()
    public ResponseEntity<List<SimpleUserDto>> getAll(){
        return ResponseEntity.ok().body(userDtoConverter.transform(userService.findAll()));
    }

    @GetMapping(value = "/email/{email}")
    public ResponseEntity<SimpleUserDto> getByEmail(@PathVariable String email){
        return ResponseEntity.ok().body(userDtoConverter.transform(userService.findFirstUserByEmail(email)));
    }

    @PutMapping()
    public ResponseEntity<SimpleUserDto> update(@RequestBody SimpleUserDto userDto) {

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
        httpHeaders.add("success", "false");
        //TODO Body<CustomException>
//        (CustomExeption)ex
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
