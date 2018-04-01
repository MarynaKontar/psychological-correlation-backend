package com.psycorp.repository;

import com.psycorp.model.entity.TestConstant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRepository extends JpaRepository<TestConstant, Long> {
}
