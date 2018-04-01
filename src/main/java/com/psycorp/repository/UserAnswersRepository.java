package com.psycorp.repository;

import com.psycorp.model.entity.UserAnswers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAnswersRepository extends JpaRepository<UserAnswers, Long> {
}
