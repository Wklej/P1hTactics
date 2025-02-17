package com.p1h.p1htactics.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String customLogin() {
        return "login";
    }

    @GetMapping("/addFriend")
    public String addFriend() {
        return "addFriend";
    }
}
