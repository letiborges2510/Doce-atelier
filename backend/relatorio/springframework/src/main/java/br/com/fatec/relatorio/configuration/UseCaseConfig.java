package br.com.fatec.relatorio.configuration;

import br.com.fatec.relatorio.gateway.CatalogoGateway;
import br.com.fatec.relatorio.gateway.ClientesGateway;
import br.com.fatec.relatorio.repository.RelatorioRepository;
import br.com.fatec.relatorio.usecase.ObterResumoUseCase;
import br.com.fatec.relatorio.usecase.RegistrarVendaUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegistrarVendaUseCase registrarVendaUseCase(RelatorioRepository relatorioRepository) {
        return new RegistrarVendaUseCase(relatorioRepository);
    }

    @Bean
    public ObterResumoUseCase obterResumoUseCase(
            RelatorioRepository relatorioRepository,
            CatalogoGateway catalogoGateway,
            ClientesGateway clientesGateway) {
        return new ObterResumoUseCase(relatorioRepository, catalogoGateway, clientesGateway);
    }
}
