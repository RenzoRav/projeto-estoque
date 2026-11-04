package br.com.estoque;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GestaoEstoqueIntegrationTest {
    @Autowired private MockMvc cliente;
    @Autowired private ObjectMapper conversor;

    private long cadastrarCategoria(String nome) throws Exception {
        String resultado = cliente.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"" + nome + "\"}"))
            .andExpect(status().isCreated()).andExpect(header().exists("Location"))
            .andReturn().getResponse().getContentAsString();
        return conversor.readTree(resultado).get("id").asLong();
    }
    private String produto(long categoria, int quantidade) {
        return "{\"nome\":\"Teclado\",\"preco\":125.50,\"quantidade\":" + quantidade + ",\"categoriaId\":" + categoria + "}";
    }
    private long cadastrarProduto(long categoria, int quantidade) throws Exception {
        String resultado = cliente.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(produto(categoria, quantidade)))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return conversor.readTree(resultado).get("id").asLong();
    }
    @Test
    void deveExecutarCrudCategoria() throws Exception {
        long id = cadastrarCategoria("Periféricos");
        cliente.perform(get("/api/categorias/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("periféricos"));
        cliente.perform(put("/api/categorias/" + id).contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Acessórios\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.nome").value("acessórios"));
        cliente.perform(get("/api/categorias").param("nome", "ACESS")).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        cliente.perform(delete("/api/categorias/" + id)).andExpect(status().isNoContent()).andExpect(content().string(""));
        cliente.perform(get("/api/categorias/" + id)).andExpect(status().isNotFound());
    }
    @Test
    void deveExecutarCrudProdutoECalcularValorEstoque() throws Exception {
        long categoria = cadastrarCategoria("Periféricos");
        long id = cadastrarProduto(categoria, 2);
        cliente.perform(get("/api/produtos/" + id)).andExpect(status().isOk()).andExpect(jsonPath("$.valorEstoque").value(251.0));
        cliente.perform(put("/api/produtos/" + id).contentType(MediaType.APPLICATION_JSON).content(produto(categoria, 0)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.quantidade").value(0));
        cliente.perform(delete("/api/produtos/" + id)).andExpect(status().isNoContent());
        cliente.perform(get("/api/produtos/" + id)).andExpect(status().isNotFound());
    }
    @Test
    void deveCombinarFiltrosPorNomeECategoria() throws Exception {
        long categoria = cadastrarCategoria("Periféricos");
        long outraCategoria = cadastrarCategoria("Livros");
        cadastrarProduto(categoria, 2);
        cliente.perform(get("/api/produtos").param("nome", "TECLA").param("categoriaId", Long.toString(categoria)))
            .andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1));
        cliente.perform(get("/api/produtos").param("nome", "mouse")).andExpect(jsonPath("$.length()").value(0));
        cliente.perform(get("/api/produtos").param("categoriaId", Long.toString(outraCategoria))).andExpect(jsonPath("$.length()").value(0));
    }
    @Test
    void deveImpedirDuplicidadeDeCategoriaEProduto() throws Exception {
        long categoria = cadastrarCategoria("Periféricos");
        cliente.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" PERIFÉRICOS \"}"))
            .andExpect(status().isConflict());
        cadastrarProduto(categoria, 0);
        cliente.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(produto(categoria, 0))).andExpect(status().isConflict());
    }
    @Test
    void deveImpedirExclusaoComDependenciasOuEstoque() throws Exception {
        long categoria = cadastrarCategoria("Periféricos");
        long id = cadastrarProduto(categoria, 2);
        cliente.perform(delete("/api/categorias/" + categoria)).andExpect(status().isConflict());
        cliente.perform(delete("/api/produtos/" + id)).andExpect(status().isConflict());
    }
    @Test
    void deveValidarDadosEFormatos() throws Exception {
        cliente.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\" \"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.nome").exists());
        cliente.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Produto\",\"preco\":-1,\"quantidade\":-1,\"categoriaId\":1}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.campos.preco").exists()).andExpect(jsonPath("$.campos.quantidade").exists());
        cliente.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content("{" )).andExpect(status().isBadRequest());
        cliente.perform(get("/api/produtos/abc")).andExpect(status().isBadRequest());
        cliente.perform(get("/api/produtos").param("categoriaId", "abc")).andExpect(status().isBadRequest());
    }
    @Test
    void deveRejeitarCategoriaInexistente() throws Exception {
        cliente.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON).content(produto(999999, 0)))
            .andExpect(status().isNotFound());
    }
    @Test
    void deveDocumentarEndpoints() throws Exception {
        cliente.perform(get("/v3/api-docs")).andExpect(status().isOk()).andExpect(jsonPath("$.paths['/api/produtos']").exists());
        cliente.perform(get("/documentacao")).andExpect(status().is3xxRedirection());
    }
}
