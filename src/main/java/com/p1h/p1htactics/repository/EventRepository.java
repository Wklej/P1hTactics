package com.p1h.p1htactics.repository;

import com.p1h.p1htactics.entity.EventEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends MongoRepository<EventEntity, String> {

    EventEntity findEventByTitle(String title);
}
