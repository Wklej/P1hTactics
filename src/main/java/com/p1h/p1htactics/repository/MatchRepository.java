package com.p1h.p1htactics.repository;

import com.p1h.p1htactics.entity.Match;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends MongoRepository<Match, String> {
    List<Match> findByMatchIdInAndGameTimeBetweenAndSummonerName(List<String> matchId, LocalDateTime start, LocalDateTime end, String summonerName);
    Optional<Match> findByMatchIdAndSummonerNameAndGameModeAndSet(String matchId, String summonerName, String gameMode, String set);
}
