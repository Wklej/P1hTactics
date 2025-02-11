package com.p1h.p1htactics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("matches")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Match {
    @Id String id;
    String matchId;
    String details;
}
