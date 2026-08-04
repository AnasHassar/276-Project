package com.cmpt276.SFSS.Nexus.repository;

import com.cmpt276.SFSS.Nexus.model.ClassEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassEventRepository extends JpaRepository<ClassEvent, Long> {

    List<ClassEvent> findAllByUserUsername(String username);

    Optional<ClassEvent> findByIdAndUserUsername(Long id, String username);
}