package br.com.fatec.relatorio.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O relatorio-service tem sua PRÓPRIA fila (venda-events-relatorio),
 * ligada na mesma exchange/routing key que o venda-service publica
 * (venda-exchange / venda-events). Isso evita o problema de "dois
 * consumers competindo pela mesma fila" - cada serviço que precisar
 * reagir a esse evento declara a sua própria fila.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${event.venda.queue}")
    private String queueName;

    @Value("${event.venda.routing-key}")
    private String routingKey;

    @Value("${event.venda.exchange}")
    private String exchangeName;

    @Value("${event.produto.queue}")
    private String produtoQueueName;

    @Value("${event.produto.routing-key}")
    private String produtoRoutingKey;

    @Value("${event.produto.exchange}")
    private String produtoExchangeName;

    @Bean
    public Queue filaVendaEventsRelatorio() {
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange vendaExchange() {
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding bindingVendaEventsRelatorio(Queue filaVendaEventsRelatorio, DirectExchange vendaExchange) {
        return BindingBuilder.bind(filaVendaEventsRelatorio).to(vendaExchange).with(routingKey);
    }

    // Mesmo esquema do evento de venda, agora para o evento de produto
    // criado (gestao-service publica -> relatorio-service consome).
    @Bean
    public Queue filaProdutoEventsRelatorio() {
        return new Queue(produtoQueueName, true);
    }

    @Bean
    public DirectExchange produtoExchange() {
        return new DirectExchange(produtoExchangeName);
    }

    @Bean
    public Binding bindingProdutoEventsRelatorio(Queue filaProdutoEventsRelatorio, DirectExchange produtoExchange) {
        return BindingBuilder.bind(filaProdutoEventsRelatorio).to(produtoExchange).with(produtoRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter jsonMessageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter);
        return rabbitTemplate;
    }
}
