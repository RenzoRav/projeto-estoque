package br.com.estoque.dto;

import java.time.Instant;
import java.util.Map;

public record ErroApi(Instant momento, int status, String mensagem, Map<String, String> campos) {}
