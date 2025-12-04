package com.example.controller;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.Tutorial;
import com.example.service.TutorialService;

@RestController
@RequestMapping("/api/tutorials")
public class TutorialController {

    private final TutorialService service;

    public TutorialController(TutorialService service) {
        this.service = service;
    }

    /**
     * GET /api/tutorials
     * Optional query param: title
     */
    @GetMapping
    public ResponseEntity<List<Tutorial>> getAll(@RequestParam(required = false) String title) {
        List<Tutorial> results;
        if (title == null || title.isBlank()) {
            results = service.findAll();
        } else {
            results = service.findByTitle(title);
        }
        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/tutorials/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Tutorial> getById(@PathVariable String id) {
        Optional<Tutorial> t = service.findById(id);
        return t.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * POST /api/tutorials
     * Create new tutorial. Evicts caches in service.
     */
    @PostMapping
    public ResponseEntity<Tutorial> create(@RequestBody Tutorial tutorial) {
        Tutorial saved = service.save(tutorial);
        // return 201 Created with Location header
        return ResponseEntity.created(URI.create("/api/tutorials/" + saved.getId())).body(saved);
    }

    /**
     * PUT /api/tutorials/{id}
     * Update existing tutorial. Uses service.update which updates single-id cache via @CachePut.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Tutorial> update(@PathVariable String id, @RequestBody Tutorial tutorial) {
        Optional<Tutorial> existing = service.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        // perform update (service.update returns the saved entity)
        Tutorial updated = service.update(id, tutorial);
        // after update, it's a good idea to evict list cache so list endpoint returns fresh data;
        // service.update currently saves the entity and returns it (you can extend to evict list cache if needed)
        return ResponseEntity.ok(updated);
    }

    /**
     * DELETE /api/tutorials/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        Optional<Tutorial> existing = service.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}