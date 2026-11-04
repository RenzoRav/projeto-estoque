package br.com.estoque.service;

import br.com.estoque.dto.*;
import br.com.estoque.entity.*;
import br.com.estoque.exception.*;
import br.com.estoque.repository.ProdutoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class ProdutoService {
    private final ProdutoRepository produtoRepository;
    private final CategoriaService categoriaService;
    public ProdutoService(ProdutoRepository produtoRepository, CategoriaService categoriaService) {
        this.produtoRepository = produtoRepository;
        this.categoriaService = categoriaService;
    }
    public List<ProdutoDto> listar(String nome, Long categoriaId) {
        return produtoRepository.filtrar(nome.strip(), categoriaId).stream().map(this::converter).toList();
    }
    public ProdutoDto buscar(Long id) { return converter(buscarEntidade(id)); }
    private Produto buscarEntidade(Long id) {
        return produtoRepository.findById(id).orElseThrow(() -> new RegistroNaoEncontradoException("Produto não encontrado."));
    }
    @Transactional
    public ProdutoDto cadastrar(CadastroProduto dados) {
        validarNome(dados.nome(), 0L);
        Categoria categoria = categoriaService.buscarEntidade(dados.categoriaId());
        return converter(produtoRepository.save(new Produto(normalizar(dados.nome()), dados.preco(), dados.quantidade(), categoria)));
    }
    @Transactional
    public ProdutoDto atualizar(Long id, CadastroProduto dados) {
        Produto produto = buscarEntidade(id);
        validarNome(dados.nome(), id);
        produto.atualizar(normalizar(dados.nome()), dados.preco(), dados.quantidade(), categoriaService.buscarEntidade(dados.categoriaId()));
        return converter(produto);
    }
    @Transactional
    public void excluir(Long id) {
        Produto produto = buscarEntidade(id);
        if (produto.getQuantidade() > 0) {
            throw new RegraNegocioException("Produto com estoque positivo não pode ser excluído. Atualize a quantidade para zero antes de excluir.");
        }
        produtoRepository.delete(produto);
    }
    private void validarNome(String nome, Long id) {
        if (produtoRepository.existsByNomeIgnoreCaseAndIdNot(normalizar(nome), id)) {
            throw new RegraNegocioException("Já existe um produto com esse nome.");
        }
    }
    private String normalizar(String nome) { return nome.strip().toLowerCase(Locale.ROOT); }
    private ProdutoDto converter(Produto produto) {
        Categoria categoria = produto.getCategoria();
        BigDecimal valorEstoque = produto.getPreco().multiply(BigDecimal.valueOf(produto.getQuantidade()));
        return new ProdutoDto(produto.getId(), produto.getNome(), produto.getPreco(), produto.getQuantidade(), valorEstoque,
            new CategoriaDto(categoria.getId(), categoria.getNome()));
    }
}
