package com.p1h.p1htactics.service;

import com.p1h.p1htactics.dto.SummonerDto;
import com.p1h.p1htactics.dto.SummonerRegistrationRequest;
import com.p1h.p1htactics.entity.Summoner;
import com.p1h.p1htactics.mapper.SummonerMapper;
import com.p1h.p1htactics.repository.UserRepository;
import com.p1h.p1htactics.util.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RiotApiService riotApiService;
    @Value("${riot.api.history.default}")
    private int defaultCount;

    public Summoner registerSummoner(SummonerRegistrationRequest newSummoner) {
        var potentialSummoner = userRepository.findSummonerByGameNameAndTag(newSummoner.gameName(), newSummoner.riotTag());
        if (potentialSummoner.isPresent()) {
            var summoner = new Summoner(
                    potentialSummoner.get().getId(),
                    newSummoner.username(),
                    newSummoner.password(),
                    potentialSummoner.get().getGameName(),
                    potentialSummoner.get().getTag(),
                    potentialSummoner.get().getPuuid(),
                    potentialSummoner.get().getMatchHistory(),
                    LocalDateTime.now(),
                    potentialSummoner.get().getFriends()
            );
            return userRepository.save(summoner);
        } else {
            var puuId = riotApiService.getPuuId(newSummoner.gameName(), newSummoner.riotTag());
            var matchHistory = riotApiService.getMatchHistoryByPuuId(puuId, defaultCount);
            Collections.reverse(matchHistory);
            return createSummoner(
                    newSummoner.username(),
                    newSummoner.password(),
                    newSummoner.gameName(),
                    newSummoner.riotTag(),
                    puuId,
                    matchHistory
            );
        }
    }

    public SummonerDto registerFriend(SummonerDto newFriend) {
        //TODO: check if such puuId exists
        var puuId = riotApiService.getPuuId(newFriend.gameName(), newFriend.tag());
        var matchHistory = riotApiService.getMatchHistoryByPuuId(puuId, defaultCount);
        Collections.reverse(matchHistory);

        var currentLoggedSummoner = getCurrentLoggedSummoner(UserUtils.getCurrentUsername()).orElseThrow();
        currentLoggedSummoner.getFriends().add(newFriend);
        userRepository.save(currentLoggedSummoner);

        if (userRepository.findSummonerByGameNameAndTag(newFriend.gameName(), newFriend.tag()).isPresent()) {
            return newFriend;
        } else {
            return SummonerMapper.summonerToSummonerDto(createSummoner(
                    null,
                    null,
                    newFriend.gameName(),
                    newFriend.tag(),
                    puuId,
                    matchHistory
            ));
        }
    }

    public List<Summoner> getAllSummoners() {
        return userRepository.findAll();
    }

    public void saveSummoner(Summoner summoner) {
        summoner.setLastUpdated(LocalDateTime.now());
        userRepository.save(summoner);
    }

    public Optional<Summoner> getSummonerBy(String gameName, String tag) {
        return userRepository.findSummonerByGameNameAndTag(gameName, tag);
    }

    public Optional<Summoner> getSummonerBy(String gameName) {
        return userRepository.findSummonerByGameName(gameName);
    }

    public List<SummonerDto> getFriends() {
        return userRepository.findByUsername(UserUtils.getCurrentUsername()).orElseThrow()
                .getFriends();
    }

    private Summoner createSummoner(String username, String password, String gameName, String tag, String puuId, List<String> matchIds) {
        var summoner = new Summoner(null, username, password, gameName, tag, puuId, matchIds, LocalDateTime.now(), new ArrayList<>());

        return userRepository.save(summoner);
    }

    private Optional<Summoner> getCurrentLoggedSummoner(String username) {
        return userRepository.findByUsername(username);
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
