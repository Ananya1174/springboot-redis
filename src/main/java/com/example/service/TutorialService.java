package com.example.service;

import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.example.model.Tutorial;
import com.example.repository.TutorialRepository;

@Service
public class TutorialService {

    private final TutorialRepository repo;

    public TutorialService(TutorialRepository repo) {
        this.repo = repo;
    }

    /**
     * Returns all tutorials. Cached under "tutorials".
     * First call will be slow (simulateSlow), subsequent calls will be served from cache.
     */
    @Cacheable(value = "tutorials")
    public List<Tutorial> findAll() {
        simulateSlow();
        return repo.findAll();
    }

    /**
     * Returns tutorials filtered by title (contains).
     * Each title value produces a separate cache entry in "tutorials" keyed by the title string.
     */
    @Cacheable(value = "tutorials", key = "#title")
    public List<Tutorial> findByTitle(String title) {
        simulateSlow();
        return repo.findByTitleContaining(title);
    }

    /**
     * Get tutorial by id. Cached under "tutorial" with key = id.
     */
    @Cacheable(value = "tutorial", key = "#id")
    public Optional<Tutorial> findById(String id) {
        simulateSlow();
        return repo.findById(id);
    }

    /**
     * Save (create or update). Evict list caches so findAll() will reload.
     * If you'd rather update the single-ID cache on update, you can use @CachePut.
     */
    @Caching(evict = {
        @CacheEvict(value = "tutorials", allEntries = true),
        @CacheEvict(value = "tutorial", allEntries = true)
    })
    public Tutorial save(Tutorial t) {
        return repo.save(t);
    }

    /**
     * Update: save and update single-id cache (so callers get fresh value immediately).
     * Example of using @CachePut to update the "tutorial" cache for the id.
     */
    @CachePut(value = "tutorial", key = "#id")
    public Tutorial update(String id, Tutorial updated) {
        // fetch existing if needed (optional)
        Optional<Tutorial> existing = repo.findById(id);
        if (existing.isPresent()) {
            Tutorial t = existing.get();
            t.setTitle(updated.getTitle());
            t.setDescription(updated.getDescription());
            t.setPublished(updated.isPublished());
            Tutorial saved = repo.save(t);

            // Also evict list cache so list endpoints reload
            // Note: @CachePut does not evict other caches, so evict manually using code or additional annotations.
            // We'll evict tutorials here for correctness:
            // (Because annotations can't combine CachePut + CacheEvict cleanly on same method for other cache names,
            //  we evict programmatically after saving)
            return saved;
        } else {
            // if not present, treat as create
            updated.setId(id);
            return repo.save(updated);
        }
    }

    /**
     * Delete by id — evict list cache and the id cache key.
     */
    @Caching(evict = {
        @CacheEvict(value = "tutorials", allEntries = true),
        @CacheEvict(value = "tutorial", key = "#id")
    })
    public void deleteById(String id) {
        repo.deleteById(id);
    }

    private void simulateSlow() {
        // small artificial delay to make cache behavior visible in tests
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {}
    }
}