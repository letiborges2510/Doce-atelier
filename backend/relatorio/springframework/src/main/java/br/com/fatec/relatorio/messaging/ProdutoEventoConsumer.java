package br.com.fatec.relatorio.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Só demonstra o fluxo assíncrono de produto criado -> evento ->
 * relatorio-service. Não precisa alimentar nenhuma tabela porque
 * "produtosAtivos" no dashboard já é obtido na hora, via REST, direto
 * do gestao-service (ver GestaoCatalogoClient). Isso aqui é só para
 * provar, com log e com a mensagem aparecendo na fila do RabbitMQ, que
 * o produto-service publica o evento certinho quando um produto é
 * criado - o mesmo padrão usado para o evento de venda.
 */
@Component
public class ProdutoEventoConsumer {

    @RabbitListener(queues = "${event.produto.queue}")
    public void receberEvento(ProdutoEventoRecebido evento) {
        System.out.println("[relatorio] Novo produto cadastrado: " + evento.getNome()
                + " (id " + evento.getProdutoId() + ", estoque inicial " + evento.getEstoque()
                + ", preço R$ " + evento.getPreco() + ")");
    }
}
