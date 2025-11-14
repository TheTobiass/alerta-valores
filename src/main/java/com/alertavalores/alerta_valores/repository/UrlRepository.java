package com.alertavalores.alerta_valores.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.alertavalores.alerta_valores.model.UrlModel;

@Repository
public interface UrlRepository extends MongoRepository<UrlModel, String> {
    // Exemplo de método personalizado:
    // Optional<UrlModel> findByEndereco(String endereco);
}