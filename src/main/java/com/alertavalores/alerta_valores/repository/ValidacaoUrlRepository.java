package com.alertavalores.alerta_valores.repository;

import com.alertavalores.alerta_valores.model.ValidacaoUrl;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ValidacaoUrlRepository extends MongoRepository<ValidacaoUrl, String> {
}
