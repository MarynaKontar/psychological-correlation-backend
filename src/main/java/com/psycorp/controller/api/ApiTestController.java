package com.psycorp.controller.api;

import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.UserAnswersDto;
import com.psycorp.service.UserAnswersService;
import com.psycorp.service.UserMatchService;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.ChoiceDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ApiTestController {
    private final UserAnswersService userAnswersService;
    private final UserService userService;
    private final UserMatchService userMatchService;
    private HttpHeaders httpHeaders;

    @Autowired
    public ApiTestController(UserAnswersService userAnswersService, UserService userService,
                             UserMatchService userMatchService) {
        this.userAnswersService = userAnswersService;
        this.userService = userService;
        this.userMatchService = userMatchService;
        httpHeaders = new HttpHeaders();
        httpHeaders.add("success", "true");
    }

    @GetMapping(value = "/test", produces = "application/json")
    public ResponseEntity<List<ChoiceDto>> test(){
        return new ResponseEntity<>(userAnswersService.choiceDtoList(), HttpStatus.OK);
}



//    @GetMapping(produces = "application/json")
//    public ResponseEntity<List<Book>> books(){
//        return new ResponseEntity<>(bookService.getAll(), HttpStatus.OK);
//    }
//
//    @PostMapping("/add")
//    public ResponseEntity<Book> save(@RequestBody Book book){
//        return ResponseEntity.ok().headers(httpHeaders).body(bookService.save(book));
////        return new ResponseEntity<>(bookService.save(book), HttpStatus.CREATED);
//    }
//
//    @GetMapping(value = "/{id}", produces = "application/json")
//    public ResponseEntity<Book> get(@PathVariable("id") Long id) {
//        return ResponseEntity.ok().headers(httpHeaders).body(bookService.get(id));
//    }



}
