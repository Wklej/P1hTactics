package com.p1h.p1htactics.repository;

import com.p1h.p1htactics.entity.Summoner;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<Summoner, String> {
    Optional<Summoner> findByUsername(String username);
}
