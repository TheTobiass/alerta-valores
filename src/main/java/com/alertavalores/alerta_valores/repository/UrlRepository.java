package com.alertavalores.alerta_valores.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.alertavalores.alerta_valores.model.UrlModel;

@Repository
public interface UrlRepository extends JpaRepository<UrlModel, Long> {
    // Exemplo de método personalizado:
    // Optional<UrlModel> findByEndereco(String endereco);
}