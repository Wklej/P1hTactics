package com.p1h.p1htactics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document("summoners")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Summoner {
    @Id
    private String id;
    private String username;
    private String password;
    private String gameName;
    private String tag;
    private String puuid;
    private String accountId;
    private List<String> matchHistory;
    private LocalDateTime lastUpdated;
}
