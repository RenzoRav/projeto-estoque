package br.com.estoque;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EndpointsTest {
    @Autowired private MockMvc cliente;
    @Autowired private ObjectMapper conversor;

    private JsonNode json(MockHttpServletResponse resultado) throws Exception {
        return conversor.readTree(resultado.getContentAsString());
    }

    private long categoria() throws Exception {
        var resultado = cliente.perform(post("/api/categorias")
            .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Perifericos\"}"))
            .andReturn().getResponse();
        assertEquals(201, resultado.getStatus());
        return json(resultado).get("id").asLong();
    }

    private String dadosProduto(long categoria, int quantidade) {
        return "{\"nome\":\"Teclado\",\"preco\":125.50,\"quantidade\":" + quantidade
            + ",\"categoriaId\":" + categoria + "}";
    }

    private long produto(long categoria, int quantidade) throws Exception {
        var resultado = cliente.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
            .content(dadosProduto(categoria, quantidade))).andReturn().getResponse();
        assertEquals(201, resultado.getStatus());
        return json(resultado).get("id").asLong();
    }

    @Test
    void postCategoria() throws Exception {
        var resultado = cliente.perform(post("/api/categorias").contentType(MediaType.APPLICATION_JSON)
            .content("{\"nome\":\"Livros\"}")).andReturn().getResponse();
        assertEquals(201, resultado.getStatus());
        var dados = json(resultado);
        assertNotNull(dados.get("id"));
        assertTrue(dados.get("id").asLong() > 0);
        assertEquals("livros", dados.get("nome").asText());
        assertEquals("/api/categorias/" + dados.get("id").asLong(), resultado.getHeader("Location"));
    }

    @Test
    void getCategorias() throws Exception {
        long id = categoria();
        var resultado = cliente.perform(get("/api/categorias")).andReturn().getResponse();
        assertEquals(200, resultado.getStatus());
        var dados = json(resultado);
        assertTrue(dados.isArray());
        assertEquals(1, dados.size());
        assertEquals(id, dados.get(0).get("id").asLong());
        assertEquals("perifericos", dados.get(0).get("nome").asText());
    }

    @Test
    void getCategoriaPorId() throws Exception {
        long id = categoria();
        var resultado = cliente.perform(get("/api/categorias/" + id)).andReturn().getResponse();
        assertEquals(200, resultado.getStatus());
        assertEquals(id, json(resultado).get("id").asLong());
        assertEquals("perifericos", json(resultado).get("nome").asText());
    }

    @Test
    void putCategoria() throws Exception {
        long id = categoria();
        var resultado = cliente.perform(put("/api/categorias/" + id)
            .contentType(MediaType.APPLICATION_JSON).content("{\"nome\":\"Livros\"}"))
            .andReturn().getResponse();
        assertEquals(200, resultado.getStatus());
        assertEquals(id, json(resultado).get("id").asLong());
        assertEquals("livros", json(resultado).get("nome").asText());
        var consulta = cliente.perform(get("/api/categorias/" + id)).andReturn().getResponse();
        assertEquals(200, consulta.getStatus());
        assertEquals("livros", json(consulta).get("nome").asText());
    }

    @Test
    void deleteCategoria() throws Exception {
        long id = categoria();
        var resultado = cliente.perform(delete("/api/categorias/" + id)).andReturn().getResponse();
        assertEquals(204, resultado.getStatus());
        assertEquals("", resultado.getContentAsString());
        var consulta = cliente.perform(get("/api/categorias/" + id)).andReturn().getResponse();
        assertEquals(404, consulta.getStatus());
    }

    @Test
    void postProduto() throws Exception {
        long categoria = categoria();
        var resultado = cliente.perform(post("/api/produtos").contentType(MediaType.APPLICATION_JSON)
            .content(dadosProduto(categoria, 2))).andReturn().getResponse();
        assertEquals(201, resultado.getStatus());
        var dados = json(resultado);
        assertNotNull(dados.get("id"));
        assertTrue(dados.get("id").asLong() > 0);
        assertEquals("teclado", dados.get("nome").asText());
        assertEquals(0, new BigDecimal("125.50").compareTo(dados.get("preco").decimalValue()));
        assertEquals(2, dados.get("quantidade").asInt());
        assertEquals(0, new BigDecimal("251.00").compareTo(dados.get("valorEstoque").decimalValue()));
        assertEquals(categoria, dados.get("categoria").get("id").asLong());
        assertEquals("/api/produtos/" + dados.get("id").asLong(), resultado.getHeader("Location"));
    }

    @Test
    void getProdutos() throws Exception {
        long categoria = categoria();
        long id = produto(categoria, 2);
        var resultado = cliente.perform(get("/api/produtos")).andReturn().getResponse();
        assertEquals(200, resultado.getStatus());
        var dados = json(resultado);
        assertTrue(dados.isArray());
        assertEquals(1, dados.size());
        assertEquals(id, dados.get(0).get("id").asLong());
        assertEquals("teclado", dados.get(0).get("nome").asText());
    }

    @Test
    void getProdutoPorId() throws Exception {
        long categoria = categoria();
        long id = produto(categoria, 2);
        var resultado = cliente.perform(get("/api/produtos/" + id)).andReturn().getResponse();
        assertEquals(200, resultado.getStatus());
        var dados = json(resultado);
        assertEquals(id, dados.get("id").asLong());
        assertEquals("teclado", dados.get("nome").asText());
        assertEquals(2, dados.get("quantidade").asInt());
        assertEquals(categoria, dados.get("categoria").get("id").asLong());
        assertEquals(0, new BigDecimal("251.00").compareTo(dados.get("valorEstoque").decimalValue()));
    }

    @Test
    void putProduto() throws Exception {
        long categoria = categoria();
        long id = produto(categoria, 2);
        var resultado = cliente.perform(put("/api/produtos/" + id).contentType(MediaType.APPLICATION_JSON)
            .content(dadosProduto(categoria, 3))).andReturn().getResponse();
        assertEquals(200, resultado.getStatus());
        assertEquals(id, json(resultado).get("id").asLong());
        assertEquals(3, json(resultado).get("quantidade").asInt());
        assertEquals(0, new BigDecimal("376.50").compareTo(json(resultado).get("valorEstoque").decimalValue()));
        var consulta = cliente.perform(get("/api/produtos/" + id)).andReturn().getResponse();
        assertEquals(200, consulta.getStatus());
        assertEquals(3, json(consulta).get("quantidade").asInt());
    }

    @Test
    void deleteProduto() throws Exception {
        long categoria = categoria();
        long id = produto(categoria, 0);
        var resultado = cliente.perform(delete("/api/produtos/" + id)).andReturn().getResponse();
        assertEquals(204, resultado.getStatus());
        assertEquals("", resultado.getContentAsString());
        var consulta = cliente.perform(get("/api/produtos/" + id)).andReturn().getResponse();
        assertEquals(404, consulta.getStatus());
        var categoriaMantida = cliente.perform(get("/api/categorias/" + categoria)).andReturn().getResponse();
        assertEquals(200, categoriaMantida.getStatus());
    }
}
