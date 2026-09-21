package com.example.hackaton_1.repository;

import com.example.hackaton_1.model.Playthrough;
import com.example.hackaton_1.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaythroughRepository extends JpaRepository<Playthrough, Long> {

    boolean existsByPlayerTag(String playerTag);

    Optional<Playthrough> findByPlayerTag(String playerTag);

    List<Playthrough> findByUserOrderByCreatedAtDesc(User user);
}