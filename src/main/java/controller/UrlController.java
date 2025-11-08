package controller;

import model.UrlModel;
import repository.UrlRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlRepository urlRepository;

    @Autowired
    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @PostMapping
    public ResponseEntity<UrlModel> createUrl(@RequestBody UrlModel url) {
        UrlModel saved = urlRepository.save(url);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public List<UrlModel> listUrls() {
        return urlRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UrlModel> getUrl(@PathVariable Long id) {
        Optional<UrlModel> opt = urlRepository.findById(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
