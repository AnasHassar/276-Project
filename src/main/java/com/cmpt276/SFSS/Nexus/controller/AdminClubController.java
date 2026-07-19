package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.model.Club;
import com.cmpt276.SFSS.Nexus.repository.ClubRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/clubs")
public class AdminClubController {

    private final ClubRepository clubRepository;

    public AdminClubController(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @GetMapping
    public ResponseEntity<List<Club>> getAllClubs() {
        return ResponseEntity.ok(clubRepository.findAll());
    }

    @PostMapping
    public ResponseEntity<?> createClub(@RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        }

        Club club = new Club();
        club.setName(name.trim());
        club.setCategory(body.getOrDefault("category", ""));
        club.setDescription(body.getOrDefault("description", ""));
        club.setContactEmail(body.getOrDefault("contactEmail", ""));
        club.setLogoUrl(body.getOrDefault("logoUrl", ""));

        return ResponseEntity.ok(clubRepository.save(club));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClub(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String name = body.get("name");
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "name is required"));
        }

        return clubRepository.findById(id).map(club -> {
            club.setName(name.trim());
            club.setCategory(body.getOrDefault("category", ""));
            club.setDescription(body.getOrDefault("description", ""));
            club.setContactEmail(body.getOrDefault("contactEmail", ""));
            club.setLogoUrl(body.getOrDefault("logoUrl", ""));
            return ResponseEntity.ok(clubRepository.save(club));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteClub(@PathVariable Long id) {
        return clubRepository.findById(id).map(club -> {
            clubRepository.delete(club);
            return ResponseEntity.ok(Map.of("deleted", id));
        }).orElse(ResponseEntity.notFound().build());
    }
}