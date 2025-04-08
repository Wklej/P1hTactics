package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.dto.ApiResponse;
import com.p1h.p1htactics.dto.FriendDto;
import com.p1h.p1htactics.dto.SummonerDto;
import com.p1h.p1htactics.dto.SummonerRegistrationRequest;
import com.p1h.p1htactics.mapper.SummonerMapper;
import com.p1h.p1htactics.service.UserService;
import com.p1h.p1htactics.util.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/api/register")
    public ResponseEntity<ApiResponse> register(@RequestBody SummonerRegistrationRequest newSummoner) {
        try {
            var errorMessage = validateSummoner(newSummoner);
            if (errorMessage != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(null, errorMessage));
            }

            var createdSummoner = userService.registerSummoner(newSummoner);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(createdSummoner, null));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(null, e.getMessage()));
        }
    }

    @PostMapping("/api/register/friend")
    public ResponseEntity<ApiResponse> registerFriend(@RequestBody SummonerDto newFriend) {
        try {
            var errorMessage = validateFriend(newFriend);
            if (errorMessage != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(new ApiResponse(null, errorMessage));
            }
            var createdSummoner = userService.registerFriend(newFriend);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(createdSummoner, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, e.getMessage()));
        }
    }

    @GetMapping("/api/getUsers")
    public List<SummonerDto> getUsers() {
        return userService.getAllSummoners().stream()
                .map(SummonerMapper::summonerToSummonerDto)
                .toList();
    }

    @GetMapping("/api/getUser/{gameName}")
    public SummonerDto getUser(@PathVariable String gameName) {
        return userService.getSummonerBy(gameName)
                .map(SummonerMapper::summonerToSummonerDto)
                .orElseThrow();
    }

    @GetMapping("/api/friendList")
    public List<FriendDto> getFriendList() {
        return userService.getFriends();
    }

    @GetMapping("/api/userInfo")
    public FriendDto getUserInfo() {
        return userService.getUserInfo();
    }

    @GetMapping("/api/getCurrentUser")
    public String getCurrentLoggedUser() {
        return UserUtils.getCurrentUsername();
    }

    private String validateSummoner(SummonerRegistrationRequest newSummoner) {
        if (userService.isUsernameTaken(newSummoner.username())) {
            return "Username already taken.";
        }
        if (!userService.summonerExist(newSummoner.gameName(), newSummoner.riotTag())) {
            return "Summoner does not exist.";
        }
        if (userService.userExist(newSummoner.gameName(), newSummoner.riotTag())) {
            return "There is already a user created for this Summoner.";
        }
        return null;
    }

    private String validateFriend(SummonerDto newFriend) {
        if (!userService.summonerExist(newFriend.gameName(), newFriend.tag())) {
            return "Summoner does not exist.";
        }
        if (userService.hasFriendAlready(newFriend.gameName(), newFriend.tag())) {
            return "It is Your friend already.";
        }
        return null;
    }
}
