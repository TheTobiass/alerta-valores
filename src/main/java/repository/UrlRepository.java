package repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import model.*;

@Repository
public interface UrlRepository extends JpaRepository<model, Long> {
    // Aqui você pode criar métodos personalizados de busca, se quiser.
    // Exemplo: Optional<UrlModel> findByEndereco(String endereco);
}