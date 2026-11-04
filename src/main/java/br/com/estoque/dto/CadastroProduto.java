package br.com.estoque.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CadastroProduto(
    @NotBlank @Size(max = 100) String nome,
    @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal preco,
    @NotNull @Min(0) @Max(10000) Integer quantidade,
    @NotNull @Positive Long categoriaId
) {}
