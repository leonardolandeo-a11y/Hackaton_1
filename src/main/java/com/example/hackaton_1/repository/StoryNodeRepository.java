package com.example.hackaton_1.repository;

import com.example.hackaton_1.model.StoryNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StoryNodeRepository extends JpaRepository<StoryNode, Long> {

    Optional<StoryNode> findByNodeCode(String nodeCode);

    boolean existsByNodeCode(String nodeCode);
}