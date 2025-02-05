package com.p1h.p1htactics.service;

import com.p1h.p1htactics.dto.SummonerRegistrationRequest;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RiotApiService riotApiService;
    @Value("${riot.api.history.default}")
    private int defaultCount;

    public Summoner registerSummoner(SummonerRegistrationRequest newSummoner) {
        var puuId = riotApiService.getPuuId(newSummoner.gameName(), newSummoner.riotTag());
        var matchHistory = riotApiService.getMatchHistoryByPuuId(puuId, defaultCount);
        return createSummoner(
                newSummoner.username(),
                newSummoner.password(),
                newSummoner.gameName(),
                newSummoner.riotTag(),
                puuId,
                matchHistory
        );
    }

    public List<Summoner> getAllSummoners() {
        return userRepository.findAll();
    }

    public void saveSummoner(Summoner summoner) {
        summoner.setLastUpdated(LocalDateTime.now());
        userRepository.save(summoner);
    }

    private Summoner createSummoner(String username, String password, String gameName, String tag, String puuId, List<String> matchIds) {
        var summoner = new Summoner(null, username, password, gameName, tag, puuId, null, matchIds, LocalDateTime.now());

        return userRepository.save(summoner);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .map(summoner -> org.springframework.security.core.userdetails.User.builder()
                        .username(summoner.getUsername())
                        .password("{noop}" + summoner.getPassword())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
