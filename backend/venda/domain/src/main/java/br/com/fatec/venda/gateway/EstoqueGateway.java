package br.com.fatec.venda.gateway;

/**
 * Porta de saida SINCRONA: o venda-service precisa confirmar e
 * descontar o estoque no gestao-service ANTES de confirmar a venda.
 * Essa chamada acontece via REST (servico pra servico) - diferente
 * do evento assincrono que avisa o relatorio depois que a venda já
 * foi confirmada.
 */
public interface EstoqueGateway {

    /**
     * Pede pro gestao-service dar baixa no estoque. Se não tiver
     * estoque suficiente (ou o produto não existir/estiver inativo),
     * a implementação deve propagar o erro, e a venda não acontece.
     */
    InfoProduto baixarEstoque(String produtoId, int quantidade);

    /**
     * Compensação: devolve ao estoque unidades que já tinham tido baixa
     * (usada quando um pedido com vários itens falha no meio).
     */
    void reporEstoque(String produtoId, int quantidade);

    record InfoProduto(String nome, java.math.BigDecimal preco) {
    }
}
