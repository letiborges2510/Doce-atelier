package br.com.fatec.venda.configuration;

import br.com.fatec.venda.usecase.AvancarPedidosAutomaticamenteUseCase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;

/**
 * Quem controla o andamento dos pedidos é a API de vendas (não o gateway
 * nem o front): de tempos em tempos ela avança os pedidos para o próximo
 * status. Tempos curtos de propósito, para demonstração; ajuste em
 * application.yml (pedido.automatico.*) ou desligue com ativo: false.
 */
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "pedido.automatico.ativo", havingValue = "true")
public class PedidoAutomaticoScheduler {

    private final AvancarPedidosAutomaticamenteUseCase useCase;
    private final Duration tempoPorStatus;

    public PedidoAutomaticoScheduler(
            AvancarPedidosAutomaticamenteUseCase useCase,
            @Value("${pedido.automatico.tempo-por-status-segundos:10}") long segundos) {
        this.useCase = useCase;
        this.tempoPorStatus = Duration.ofSeconds(segundos);
    }

    @Scheduled(fixedDelayString = "${pedido.automatico.intervalo-ms:3000}")
    public void avancarPedidos() {
        try {
            useCase.executar(tempoPorStatus);
        } catch (RuntimeException ex) {
            System.out.println("[venda] Falha ao avançar pedidos automaticamente: " + ex.getMessage());
        }
    }
}
