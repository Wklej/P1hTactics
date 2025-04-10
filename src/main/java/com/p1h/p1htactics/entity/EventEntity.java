package com.p1h.p1htactics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Document("events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventEntity {
    @Id String id;
    String title;
    LocalDate start;
    LocalDate end;
    List<String> participants;
    boolean active;
}
