package br.com.fatec.venda.messaging;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * O venda-service só PUBLICA eventos - ele não tem nenhum
 * @RabbitListener próprio. Por isso aqui só declaramos a exchange
 * (pra garantir que ela existe, mesmo se o venda-service subir antes
 * de qualquer consumidor) e o RabbitTemplate. Quem declara as FILAS
 * é cada serviço que for consumir (ex: relatorio-service) - declarar
 * uma fila aqui, sem ninguém pra consumi-la, só acumularia mensagem
 * à toa.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${event.venda.exchange}")
    private String exchange;

    @Bean
    public DirectExchange vendaExchange() {
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
