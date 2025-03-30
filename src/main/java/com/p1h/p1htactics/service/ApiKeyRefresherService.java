//package com.p1h.p1htactics.service;
//
//import com.p1h.p1htactics.config.ApiKeyProvider;
//import lombok.RequiredArgsConstructor;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ApiKeyRefresherService {
//    private final ApiKeyProvider apiKeyProvider;
//
//    @Scheduled(fixedRate = 1 * 60 * 1000)
//    public void refreshApiKey() {
//        System.out.println("Refreshing API Key...");
//        System.out.println("New API Key: " + apiKeyProvider.getApiKey());
//    }
//
//}