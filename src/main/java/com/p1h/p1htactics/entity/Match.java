package com.p1h.p1htactics.entity;

import com.p1h.p1htactics.dto.Trait;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document("matches")
@Data
@AllArgsConstructor
@NoArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "summoner_set_idx",
                def = "{'summonerName': 1, 'set': 1}")
})
public class Match {
    @Id String id;
    String matchId;
    String details;
    LocalDateTime gameTime;
    @Indexed
    String summonerName;
    @Indexed
    String set;
    int placement;
    String gameMode;
    List<Trait> traits;
}
