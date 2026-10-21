package br.com.fatec.gestao.event;

import br.com.fatec.gestao.entity.Produto;

/**
 * Publica um evento assíncrono (RabbitMQ) quando um produto é criado,
 * do mesmo jeito que o venda-service publica um evento quando uma
 * venda é confirmada. Quem consome esse evento (ex.: relatorio-service)
 * não bloqueia a criação do produto - é "dispara e esquece".
 */
public interface ProdutoPublisher {
    void publicarCriacao(Produto produto);
}
