package com.alertavalores.alerta_valores.repository;

import com.alertavalores.alerta_valores.repository.ResultadoAnaliseRepository;
import com.alertavalores.alerta_valores.model.ResultadoAnalise;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultadoAnaliseRepository extends MongoRepository<ResultadoAnalise, String> {
    // Métodos customizados podem ser adicionados aqui
}
