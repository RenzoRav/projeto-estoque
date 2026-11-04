package br.com.estoque.service;

import br.com.estoque.dto.*;
import br.com.estoque.entity.Categoria;
import br.com.estoque.exception.*;
import br.com.estoque.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;
    private final ProdutoRepository produtoRepository;
    public CategoriaService(CategoriaRepository categoriaRepository, ProdutoRepository produtoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.produtoRepository = produtoRepository;
    }
    public List<CategoriaDto> listar(String nome) {
        return categoriaRepository.findByNomeContainingIgnoreCaseOrderByNomeAsc(nome.strip()).stream().map(this::converter).toList();
    }
    public Categoria buscarEntidade(Long id) {
        return categoriaRepository.findById(id).orElseThrow(() -> new RegistroNaoEncontradoException("Categoria não encontrada."));
    }
    public CategoriaDto buscar(Long id) { return converter(buscarEntidade(id)); }
    @Transactional
    public CategoriaDto cadastrar(CadastroCategoria dados) {
        validarNome(dados.nome(), 0L);
        return converter(categoriaRepository.save(new Categoria(normalizar(dados.nome()))));
    }
    @Transactional
    public CategoriaDto atualizar(Long id, CadastroCategoria dados) {
        Categoria categoria = buscarEntidade(id);
        validarNome(dados.nome(), id);
        categoria.setNome(normalizar(dados.nome()));
        return converter(categoria);
    }
    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscarEntidade(id);
        if (produtoRepository.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Categoria com produtos vinculados não pode ser excluída.");
        }
        categoriaRepository.delete(categoria);
    }
    private void validarNome(String nome, Long id) {
        if (categoriaRepository.existsByNomeIgnoreCaseAndIdNot(normalizar(nome), id)) {
            throw new RegraNegocioException("Já existe uma categoria com esse nome.");
        }
    }
    private String normalizar(String nome) { return nome.strip().toLowerCase(Locale.ROOT); }
    private CategoriaDto converter(Categoria categoria) {
        return new CategoriaDto(categoria.getId(), categoria.getNome());
    }
}
