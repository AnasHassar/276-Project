package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.model.Club;
import com.cmpt276.SFSS.Nexus.repository.ClubRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {

    private final ClubRepository clubRepository;

    public ClubController(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    @GetMapping
    public ResponseEntity<List<Club>> getClubs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category) {
        
        List<Club> clubs = clubRepository.findByActiveTrue();
        
        if (q != null && !q.isBlank()) {
            clubs = clubs.stream()
                    .filter(c -> c.getName().toLowerCase().contains(q.toLowerCase()) ||
                            c.getDescription().toLowerCase().contains(q.toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (category != null && !category.isBlank() && !category.equals("All")) {
            clubs = clubs.stream()
                    .filter(c -> c.getCategory().equals(category))
                    .collect(Collectors.toList());
        }
        
        clubs.sort((a, b) -> a.getName().compareTo(b.getName()));
        return ResponseEntity.ok(clubs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Club> getClub(@PathVariable Long id) {
        return clubRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/categories/list")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(Arrays.asList(
                "Academic", "Creative", "Adventure", "Sports",
                "Cultural", "Professional", "Social"
        ));
    }
}
