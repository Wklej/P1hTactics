package com.p1h.p1htactics.repository;

import com.p1h.p1htactics.entity.Match;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MatchRepository extends MongoRepository<Match, String> {

    Match findFirstByMatchId(String matchId);
    List<Match> findByMatchIdInAndGameTimeBetween(List<String> matchId, LocalDateTime start, LocalDateTime end);
}
