package com.cmpt276.SFSS.Nexus.controller;

import com.cmpt276.SFSS.Nexus.service.ClassEventRequest;
import com.cmpt276.SFSS.Nexus.service.ClassEventResponse;
import com.cmpt276.SFSS.Nexus.service.ClassEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/class-events")
public class ClassEventController {

    private final ClassEventService classEventService;

    public ClassEventController(ClassEventService classEventService) {
        this.classEventService = classEventService;
    }

    @GetMapping
    public ResponseEntity<List<ClassEventResponse>> getMyClasses(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(classEventService.listClassEvents(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMyClass(Authentication authentication, @PathVariable Long id) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(classEventService.getClassEvent(authentication.getName(), id));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<?> createMyClass(Authentication authentication, @RequestBody ClassEventRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(classEventService.createClassEvent(authentication.getName(), request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMyClass(Authentication authentication, @PathVariable Long id,
            @RequestBody ClassEventRequest request) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            return ResponseEntity.ok(classEventService.updateClassEvent(authentication.getName(), id, request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMyClass(Authentication authentication, @PathVariable Long id) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            classEventService.deleteClassEvent(authentication.getName(), id);
            return ResponseEntity.ok(Map.of("deleted", id));
        } catch (NoSuchElementException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}