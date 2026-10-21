package br.com.fatec.venda.configuration;

import br.com.fatec.venda.event.VendaPublisher;
import br.com.fatec.venda.gateway.EstoqueGateway;
import br.com.fatec.venda.repository.VendaRepository;
import br.com.fatec.venda.usecase.AtualizarStatusVendaUseCase;
import br.com.fatec.venda.usecase.AvancarPedidosAutomaticamenteUseCase;
import br.com.fatec.venda.usecase.RealizarVendaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RealizarVendaUseCase realizarVendaUseCase(
            EstoqueGateway estoqueGateway,
            VendaRepository vendaRepository,
            VendaPublisher vendaPublisher) {
        return new RealizarVendaUseCase(estoqueGateway, vendaRepository, vendaPublisher);
    }

    @Bean
    public AtualizarStatusVendaUseCase atualizarStatusVendaUseCase(
            VendaRepository vendaRepository, VendaPublisher vendaPublisher) {
        return new AtualizarStatusVendaUseCase(vendaRepository, vendaPublisher);
    }

    @Bean
    public AvancarPedidosAutomaticamenteUseCase avancarPedidosAutomaticamenteUseCase(
            VendaRepository vendaRepository, AtualizarStatusVendaUseCase atualizarStatusVendaUseCase) {
        return new AvancarPedidosAutomaticamenteUseCase(vendaRepository, atualizarStatusVendaUseCase);
    }
}
