package com.example.controller;


import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.model.Tutorial;
import com.example.service.TutorialService;

@RestController
@RequestMapping("/api/tutorials")
public class TutorialController {

    @Autowired
    private TutorialService service;

    @GetMapping
    public List<Tutorial> getAll(@RequestParam(required = false) String title) {
        if (title == null) {
            return service.findAll();
        } else {
            return service.findByTitle(title);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tutorial> getById(@PathVariable Long id) {
        Optional<Tutorial> t = service.findById(id);
        return t.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public Tutorial create(@RequestBody Tutorial tutorial) {
        return service.save(tutorial);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
