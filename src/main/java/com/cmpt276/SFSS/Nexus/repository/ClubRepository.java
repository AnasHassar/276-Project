package com.cmpt276.SFSS.Nexus.repository;

import com.cmpt276.SFSS.Nexus.model.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubRepository extends JpaRepository<Club, Long>, JpaSpecificationExecutor<Club> {
    List<Club> findByActiveTrue();
    List<Club> findByCategory(String category);
    List<Club> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
