package br.com.fatec.gestao.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O gestao-service só PUBLICA o evento de produto criado - não tem
 * nenhum @RabbitListener próprio. Igual ao padrão do venda-service:
 * só declaramos a exchange aqui; quem consome (relatorio-service)
 * declara a própria fila.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${event.produto.exchange}")
    private String exchange;

    @Bean
    public DirectExchange produtoExchange() {
        return new DirectExchange(exchange);
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
