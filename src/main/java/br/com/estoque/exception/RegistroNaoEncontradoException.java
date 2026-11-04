package br.com.estoque.exception;

public class RegistroNaoEncontradoException extends RuntimeException {
    public RegistroNaoEncontradoException(String mensagem) { super(mensagem); }
}
