package com.psycorp.service.implementation;

import com.psycorp.model.dto.AreaDto;
import com.psycorp.model.dto.ChoiceDto;
import com.psycorp.model.dto.ScaleDto;
import com.psycorp.model.entity.User;
import com.psycorp.model.enums.Area;
import com.psycorp.model.enums.Scale;
import com.psycorp.repository.UserRepository;
import com.psycorp.service.UserService;
import com.psycorp.сonverter.AreaDtoConverter;
import com.psycorp.сonverter.ChoiceDtoConverter;
import com.psycorp.сonverter.ScaleDtoConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    //TODO добавить проверку всех значений и соответствуюшии им Exceptions
    @Override
    public User insert(User user) {
        return userRepository.insert(user);
    }

    @Override
    public User findFirstUserByEmail(String email) {
        return userRepository.findFirstByEmail(email);
    }

}
