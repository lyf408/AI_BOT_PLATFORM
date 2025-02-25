package org.example.repository;

import org.example.model.entity.Model;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    Model findByModelName(String modelName);
    @NotNull List<Model> findAll();
}
