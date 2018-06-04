package com.psycorp.controller.api;

import com.psycorp.model.dto.SimpleUserDto;
import com.psycorp.model.entity.User;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.UserDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

@RestController
@RequestMapping("/api/user")
public class ApiUserController {

    private final UserService userService;
    private final UserDtoConverter userDtoConverter;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiUserController(UserService userService, UserDtoConverter userDtoConverter) {
        this.userService = userService;
        this.userDtoConverter = userDtoConverter;
        httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "true");
    }

    @PostMapping(value = "/add", produces = "application/json")
    public ResponseEntity<SimpleUserDto> save(@RequestBody SimpleUserDto userDto) {
        //TODO посмотреть как сделать это аннотациями (@NotEmpty @NotNull не помогают - в базу сохраняется документ с _id=ObjectId)
        if(userDto == null || userDto.getUserName().isEmpty()) return ResponseEntity.badRequest().build();

        User user = userService.insert(userDtoConverter.transform(userDto));

        return ResponseEntity
                .created(httpHeaders.getLocation())
                .headers(httpHeaders)
                .body(userDtoConverter.transform(user));

//    return new ResponseEntity<>(userService.insert(user), HttpStatus.CREATED);
    }

    @GetMapping(value = "/{userName}", produces = "application/json")
    public ResponseEntity<SimpleUserDto> get(@PathVariable String userName
//            , Principal principal
    ){
//        if( principal == null || !principal.getName().equals(userName)) return ResponseEntity.badRequest().build();
//        return ResponseEntity.notFound().build();
//        return ResponseEntity.ok().headers(httpHeaders).body(userService.findFirstUserByName(principal.getName()));
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(userDtoConverter.transform(userService.findFirstUserByName(userName)));
    }

    @GetMapping(value = "/email/{email}", produces = "application/json")
    public ResponseEntity<SimpleUserDto> getByEmail(@PathVariable String email
            , Principal principal
    ){
//        if(principal == null || !principal.getName().equals(userService.findFirstUserByEmail(email).getName()))
//            return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(userDtoConverter.transform(userService.findFirstUserByEmail(email)));
    }

    @PostMapping(value = "/{userName}/update", produces = "application/json")
    public ResponseEntity<SimpleUserDto> update(@RequestBody SimpleUserDto userDto, @PathVariable String userName
            , Principal principal) {
        if(!userDto.getUserName().equals(userName)
//                 principal == null || !userName().equals(principal.getName())
                ) return ResponseEntity.badRequest().build();
//        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        User user = userService.update(userDtoConverter.transform(userDto));

        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(userDtoConverter.transform(user));
    }

    @PostMapping(value = "/{userName}/changeUserName", produces = "application/json")
    public ResponseEntity<SimpleUserDto> changeUserName(@RequestBody SimpleUserDto userDto, @PathVariable String userName
            , Principal principal) {
//        if(principal == null || !userName().equals(principal.getName())
//                return ResponseEntity.badRequest().build();
//        return ResponseEntity.notFound().build();

//        User user = userService.chaneUserName(userDtoConverter.transform(userDto), principal);
        User user = userService.changeUserName(userDtoConverter.transform(userDto), principal, userName);

        //TODO после изменения имени надо перелогиниться. Это, видимо, надо будет на фронтенде делать.
        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(userDtoConverter.transform(user));
    }

    @PostMapping("/{userName}/delete")
    public ResponseEntity<SimpleUserDto> delete(@PathVariable String userName, Principal principal){
//        if(principal == null || !principal.getName().equals(userService.findFirstUserByName(userName).getName()))
// return ResponseEntity.badRequest().build();
//        return ResponseEntity.notFound().build();

//        User currentUser = userService.findFirstUserByName(principal.getName());
//        if (currentUser.getName() == userName) {
//            return ResponseEntity.ok().body(userDtoConverter.transform(userService.delete(userName)));
//        } else {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
//        }


        return ResponseEntity.ok()
                .headers(httpHeaders)
                .body(userDtoConverter.transform(userService.delete(userName)));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<HttpHeaders> handleException(RuntimeException ex, HttpServletRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "false");
        httpHeaders.add("messageError", "Something wrong: " + ex.getMessage());
        return ResponseEntity.badRequest().headers(httpHeaders).build();
    }
}
