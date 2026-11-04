package br.com.estoque.repository;

import br.com.estoque.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {
    boolean existsByNomeIgnoreCaseAndIdNot(String nome, Long id);
    boolean existsByCategoriaId(Long categoriaId);
    @Query("select produto from Produto produto join fetch produto.categoria where lower(produto.nome) like lower(concat('%', :nome, '%')) and (:categoriaId is null or produto.categoria.id = :categoriaId) order by produto.nome")
    List<Produto> filtrar(String nome, Long categoriaId);
}
