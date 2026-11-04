package br.com.estoque.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "nome"))
public class Produto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nome;
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal preco;
    @Column(nullable = false)
    private Integer quantidade;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    protected Produto() {}
    public Produto(String nome, BigDecimal preco, Integer quantidade, Categoria categoria) {
        atualizar(nome, preco, quantidade, categoria);
    }
    public void atualizar(String nome, BigDecimal preco, Integer quantidade, Categoria categoria) {
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
        this.categoria = categoria;
    }
    public Long getId() { return id; }
    public String getNome() { return nome; }
    public BigDecimal getPreco() { return preco; }
    public Integer getQuantidade() { return quantidade; }
    public Categoria getCategoria() { return categoria; }
}
