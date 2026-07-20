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
        club.setDescription(body.getOrDefault("description", ""));
        club.setCategory(body.getOrDefault("category", "Social"));
        club.setImageUrl(body.getOrDefault("imageUrl", ""));
        club.setContactEmail(body.getOrDefault("contactEmail", ""));
        club.setWebsite(body.getOrDefault("website", ""));
        club.setLocation(body.getOrDefault("location", ""));
        club.setTags(body.getOrDefault("tags", ""));
        club.setActive(true);

        try {
            Integer memberCount = Integer.parseInt(body.getOrDefault("memberCount", "0"));
            club.setMemberCount(memberCount);
        } catch (NumberFormatException ignored) {
            club.setMemberCount(0);
        }

        Club saved = clubRepository.save(club);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateClub(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return clubRepository.findById(id).map(club -> {
            if (body.containsKey("name")) {
                club.setName(body.get("name"));
            }
            if (body.containsKey("description")) {
                club.setDescription(body.get("description"));
            }
            if (body.containsKey("category")) {
                club.setCategory(body.get("category"));
            }
            if (body.containsKey("imageUrl")) {
                club.setImageUrl(body.get("imageUrl"));
            }
            if (body.containsKey("contactEmail")) {
                club.setContactEmail(body.get("contactEmail"));
            }
            if (body.containsKey("website")) {
                club.setWebsite(body.get("website"));
            }
            if (body.containsKey("location")) {
                club.setLocation(body.get("location"));
            }
            if (body.containsKey("tags")) {
                club.setTags(body.get("tags"));
            }
            if (body.containsKey("memberCount")) {
                try {
                    club.setMemberCount(Integer.parseInt(body.get("memberCount")));
                } catch (NumberFormatException ignored) {
                }
            }
            if (body.containsKey("active")) {
                club.setActive(Boolean.parseBoolean(body.get("active")));
            }

            Club updated = clubRepository.save(club);
            return ResponseEntity.ok(updated);
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
