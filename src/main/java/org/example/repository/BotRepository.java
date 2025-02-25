package org.example.repository;

import org.example.model.entity.Bot;
import org.example.model.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BotRepository extends JpaRepository<Bot, Long> {
    Bot findByBotName(String botName);
    Bot findByBotId(Long botId);
    List<Bot> findAllByModel(Model model);
    void deleteAllByModel(Model model);
}
