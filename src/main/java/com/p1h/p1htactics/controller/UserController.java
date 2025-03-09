package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.dto.ApiResponse;
import com.p1h.p1htactics.dto.SummonerDto;
import com.p1h.p1htactics.dto.SummonerRegistrationRequest;
import com.p1h.p1htactics.mapper.SummonerMapper;
import com.p1h.p1htactics.service.UserService;
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
        //TODO: check for existence
//            var userExist = userService.isUsernameTaken(newUser.username());
//            if (userExist) {
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken.");
//            }
        try {
            var createdSummoner = userService.registerSummoner(newSummoner);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(createdSummoner, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(null, e.getMessage()));
        }
    }

    @PostMapping("/api/register/friend")
    public ResponseEntity<ApiResponse> registerFriend(@RequestBody SummonerDto newFriend) {
        //TODO: check for existence
//            var userExist = userService.isUsernameTaken(newUser.username());
//            if (userExist) {
//                return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already taken.");
//            }
        try {
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
    public List<SummonerDto> getFriendList() {
        return userService.getFriends();
    }
}
