package com.alertavalores.alerta_valores.controller;

import com.alertavalores.alerta_valores.model.UrlModel;
import com.alertavalores.alerta_valores.repository.UrlRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Objects;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlRepository urlRepository;

    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @PostMapping
    public ResponseEntity<UrlModel> createUrl(@RequestBody UrlModel url) {
        UrlModel saved = urlRepository.save(Objects.requireNonNull(url, "UrlModel não pode ser nulo"));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<UrlModel> listUrls() {
        return urlRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UrlModel> getUrl(@PathVariable String id) {
        Optional<UrlModel> opt = urlRepository.findById(Objects.requireNonNull(id, "ID não pode ser nulo"));
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
