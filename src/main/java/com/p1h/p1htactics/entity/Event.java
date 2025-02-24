package com.p1h.p1htactics.entity;

import com.p1h.p1htactics.dto.ResultDto;
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
public class Event {
    @Id String id;
    String title;
    LocalDate start;
    LocalDate end;
    List<String> participants;
    List<ResultDto> finalResults;
    boolean active;
}
