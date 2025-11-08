package com.alertavalores.alerta_valores;

import model.UrlModel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import repository.UrlRepository;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
public class UrlRepositoryIntegrationTest {

    @Autowired
    private UrlRepository urlRepository;

    @Test
    public void saveAndFindById() {
        UrlModel u = new UrlModel("http://exemplo.test","Teste");
        UrlModel saved = urlRepository.save(u);

        assertThat(saved.getId()).isNotNull();

        UrlModel found = urlRepository.findById(saved.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getEndereco()).isEqualTo("http://exemplo.test");
    }
}
