package com.psycorp.controller.api;

import com.psycorp.model.dto.UserAccountDto;
import com.psycorp.service.UserAccountService;
import com.psycorp.service.security.TokenService;
import com.psycorp.service.security.implementation.TokenServiceImpl;
import com.psycorp.сonverter.UserAccountDtoConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


//@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ApiAuthenticationControllerTest {
    @Autowired
    public WebApplicationContext context;
    private MockMvc mockMvc;
    @InjectMocks
    private ApiAuthenticationController apiAuthenticationController;
    @Mock
    private TokenServiceImpl tokenService;
    @Mock
    private UserAccountDtoConverter userAccountDtoConverter;
    @Mock
    private UserAccountService userAccountService;


    @BeforeAll
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(apiAuthenticationController).build();
//        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void login() throws Exception{
//        mockMvc.perform(post("/auth/login/")
//                .accept(MediaType.APPLICATION_JSON_UTF8))
//               .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
//               .andExpect(content("{name: 'name', }"))
//               .andExpect(status().isOk());
    }

    @Test
    public void loginFriendAccount() {
    }

    @Test
    public void handleException() {
    }

//
//    @Test
//    public void getBookingsForHotelIdTest() {
//
//        ResponseEntity<BookingSearchingDto> responseEntity = restTemplate
//                .exchange("/booking/hotel/1", HttpMethod.GET, null, new ParameterizedTypeReference<BookingSearchingDto>() {
//                });
//
//        assertEquals(200, responseEntity.getStatusCodeValue());
//        BookingSearchingDto body = responseEntity.getBody();
//        assertNotNull(body);
//
//        BookingDto booking = body.getBookings().get(0);
//        assertEquals(Long.valueOf(1), booking.getId());
//        assertEquals(Long.valueOf(2), booking.getRoom().getId());
//    }
}