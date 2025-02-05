package com.p1h.p1htactics.controller;

import com.p1h.p1htactics.dto.ApiResponse;
import com.p1h.p1htactics.dto.SummonerRegistrationRequest;
import com.p1h.p1htactics.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
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

}
