package br.com.estoque.repository;

import br.com.estoque.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    List<Categoria> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
