package org.example.repository;

import org.example.model.entity.Bot;
import org.example.model.entity.Model;
import org.example.model.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {
    Session findBySessionId(Long sessionId);
    List<Session> findAllByBot(Bot bot);

    @Query("""
        SELECT s
        FROM Session s
        WHERE s.user.userId = :userId
        ORDER BY s.updatedAt DESC
        LIMIT :limit OFFSET :offset
    """)
    List<Session> findAllByUser(@Param("userId") Long userId,
                                @Param("limit") Integer limit,
                                @Param("offset") Integer offset);

    void deleteAllByBot(Bot bot);
}
