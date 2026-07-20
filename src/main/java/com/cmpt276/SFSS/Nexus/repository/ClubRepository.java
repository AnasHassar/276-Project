package com.cmpt276.SFSS.Nexus.repository;

import com.cmpt276.SFSS.Nexus.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long> {
}