package br.com.estoque.exception;

import br.com.estoque.dto.ErroApi;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class TratamentoExcecoes {
    @ExceptionHandler(RegistroNaoEncontradoException.class)
    public ResponseEntity<ErroApi> tratarAusente(RegistroNaoEncontradoException excecao) {
        return resultado(404, excecao.getMessage(), Map.of());
    }
    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroApi> tratarRegra(RegraNegocioException excecao) {
        return resultado(409, excecao.getMessage(), Map.of());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroApi> tratarValidacao(MethodArgumentNotValidException excecao) {
        Map<String, String> campos = new LinkedHashMap<>();
        excecao.getBindingResult().getFieldErrors().forEach(erro -> campos.put(erro.getField(), erro.getDefaultMessage()));
        return resultado(400, "Existem campos inválidos.", campos);
    }
    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ErroApi> tratarFormato(Exception excecao) {
        return resultado(400, "Formato de dados inválido. Confira os tipos e o corpo da requisição.", Map.of());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroApi> tratarIntegridade(DataIntegrityViolationException excecao) {
        return resultado(409, "A operação viola um vínculo ou um registro único.", Map.of());
    }
    private ResponseEntity<ErroApi> resultado(int status, String mensagem, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ErroApi(Instant.now(), status, mensagem, campos));
    }
}
