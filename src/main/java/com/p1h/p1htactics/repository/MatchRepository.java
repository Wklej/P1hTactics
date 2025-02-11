package com.p1h.p1htactics.repository;

import com.p1h.p1htactics.entity.Match;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MatchRepository extends MongoRepository<Match, String> {

    Match findMatchByMatchId(String matchId);
}
