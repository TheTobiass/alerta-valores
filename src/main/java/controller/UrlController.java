package com.alertavalores.alerta_valores.controller;

import com.alertavalores.alerta_valores.model.UrlModel;
import com.alertavalores.alerta_valores.repository.UrlRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController("/urls")
public class UrlController {

    private final UrlRepository urlRepository;

    public UrlController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @GetMapping
    public Iterable<UrlModel> getUrls() {
        return urlRepository.findAll();
    }
}
