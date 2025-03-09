package org.example.repository;

import org.example.model.entity.ChatHistory;
import org.example.model.entity.Session;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatHistoryRepository extends JpaRepository<ChatHistory, Long> {

    @Query("""
        SELECT ch
        FROM ChatHistory ch
        WHERE ch.session.sessionId = :sessionId
        AND ch.id <= :lastMessageId
        ORDER BY ch.createdAt DESC
        LIMIT 20
    """)
    List<ChatHistory> findAllBySessionBeforeMessage(@Param("sessionId") Long sessionId,
                                                    @Param("lastMessageId") Long lastMessageId);

    List<ChatHistory> findAllBySession(Session session);

    void deleteAllBySession(Session session);
}
