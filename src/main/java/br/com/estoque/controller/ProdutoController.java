package br.com.estoque.controller;

import br.com.estoque.dto.*;
import br.com.estoque.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@Tag(name = "Produtos")
@ApiResponses({@ApiResponse(responseCode = "400", description = "Dados inválidos"),
    @ApiResponse(responseCode = "404", description = "Registro não encontrado"),
    @ApiResponse(responseCode = "409", description = "Conflito com regra de negócio")})
public class ProdutoController {
    private final ProdutoService servico;
    public ProdutoController(ProdutoService servico) { this.servico = servico; }
    @GetMapping
    @Operation(summary = "Listar produtos", description = "Filtra pelo trecho do nome, ignorando maiúsculas.", tags = {"Produtos"})
    public ResponseEntity<List<ProdutoDto>> listar(@RequestParam(defaultValue = "") String nome, @RequestParam(required = false) Long categoriaId) {
        return ResponseEntity.ok(servico.listar(nome, categoriaId));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Consultar registro pelo id")
    public ResponseEntity<ProdutoDto> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(servico.buscar(id));
    }
    @PostMapping
    @Operation(summary = "Cadastrar registro")
    @ApiResponse(responseCode = "201", description = "Registro criado")
    public ResponseEntity<ProdutoDto> cadastrar(@Valid @RequestBody CadastroProduto dados) {
        ProdutoDto resultado = servico.cadastrar(dados);
        return ResponseEntity.created(URI.create("/api/produtos/" + resultado.id())).body(resultado);
    }
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar registro")
    public ResponseEntity<ProdutoDto> atualizar(@PathVariable Long id, @Valid @RequestBody CadastroProduto dados) {
        return ResponseEntity.ok(servico.atualizar(id, dados));
    }
    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir registro respeitando as dependências e o estoque")
    @ApiResponse(responseCode = "204", description = "Registro excluído")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        servico.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
