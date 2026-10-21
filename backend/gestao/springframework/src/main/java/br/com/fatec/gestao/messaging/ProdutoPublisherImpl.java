package br.com.fatec.gestao.messaging;

import br.com.fatec.gestao.entity.Produto;
import br.com.fatec.gestao.event.ProdutoPublisher;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProdutoPublisherImpl implements ProdutoPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String queue;
    private final String exchange;

    public ProdutoPublisherImpl(
            RabbitTemplate rabbitTemplate,
            @Value("${event.produto.queue}") String queue,
            @Value("${event.produto.exchange}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
        this.exchange = exchange;
    }

    @Override
    public void publicarCriacao(Produto produto) {
        ProdutoEvento evento = new ProdutoEvento(produto.id(), produto.nome(), produto.preco(), produto.estoque());
        try {
            rabbitTemplate.convertAndSend(exchange, queue, evento);
        } catch (Exception ex) {
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }
}
