package com.psycorp.service.implementation;

import com.psycorp.repository.TestRepository;
import com.psycorp.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TestServiceImpl implements TestService{

    @Autowired
    private TestRepository testRepository;
}
