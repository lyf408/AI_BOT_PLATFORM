package org.example.repository;

import org.example.model.entity.CreditHistory;
import org.example.model.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CreditHistoryRepository extends JpaRepository<CreditHistory, Long> {
}
