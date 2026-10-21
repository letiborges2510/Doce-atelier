package br.com.fatec.venda.messaging;

import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.event.VendaPublisher;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VendaPublisherImpl implements VendaPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final String queue;
    private final String exchange;

    public VendaPublisherImpl(
            RabbitTemplate rabbitTemplate,
            @Value("${event.venda.queue}") String queue,
            @Value("${event.venda.exchange}") String exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.queue = queue;
        this.exchange = exchange;
    }

    @Override
    public void publicar(Venda venda) {
        VendaEvento evento = new VendaEvento(
                venda.id(), venda.clienteId(), venda.valorTotal(), venda.status().name(), venda.dataHora());
        try {
            rabbitTemplate.convertAndSend(exchange, queue, evento);
        } catch (Exception ex) {
            throw new AmqpRejectAndDontRequeueException(ex);
        }
    }
}
