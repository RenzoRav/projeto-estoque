package br.com.estoque.dto;

import java.math.BigDecimal;

public record ProdutoDto(Long id, String nome, BigDecimal preco,
    Integer quantidade, BigDecimal valorEstoque, CategoriaDto categoria) {}
