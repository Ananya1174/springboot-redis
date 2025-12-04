package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import com.example.model.Tutorial;
import com.example.repository.TutorialRepository;

@Service
public class TutorialService {

    @Autowired
    private TutorialRepository repo;

    // cache name "tutorials" stores result of findAll
    @Cacheable(value = "tutorials")
    public List<Tutorial> findAll() {
        simulateSlow();
        return repo.findAll();
    }

    // cache with key = title param
    @Cacheable(value = "tutorials", key = "#title")
    public List<Tutorial> findByTitle(String title) {
        simulateSlow();
        return repo.findByTitleContaining(title);
    }

    @Cacheable(value = "tutorial", key = "#id")
    public Optional<Tutorial> findById(Long id) {
        simulateSlow();
        return repo.findById(id);
    }

    // on save, evict relevant caches so next read reloads data
    @Caching(evict = {
            @CacheEvict(value = "tutorials", allEntries = true),
            @CacheEvict(value = "tutorial", allEntries = true)
    })
    public Tutorial save(Tutorial t) {
        return repo.save(t);
    }

    @Caching(evict = {
            @CacheEvict(value = "tutorials", allEntries = true),
            @CacheEvict(value = "tutorial", key = "#id")
    })
    public void delete(Long id) {
        repo.deleteById(id);
    }

    private void simulateSlow() {
        try { Thread.sleep(2000); } catch (InterruptedException e) { /* ignore */ }
    }
}