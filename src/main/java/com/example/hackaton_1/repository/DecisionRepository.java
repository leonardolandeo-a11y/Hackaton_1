package com.example.hackaton_1.repository;

import com.example.hackaton_1.model.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface DecisionRepository
        extends JpaRepository<Decision, Long>,
        JpaSpecificationExecutor<Decision> {

    List<Decision>
    findByPlaythroughIdAndResolvedNodeCodeIsNotNullOrderByCreatedAtAsc(
            Long playthroughId
    );
}