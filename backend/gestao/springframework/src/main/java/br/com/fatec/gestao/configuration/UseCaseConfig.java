package br.com.fatec.gestao.configuration;

import br.com.fatec.gestao.repository.ProdutoRepository;
import br.com.fatec.gestao.usecase.BaixarEstoqueUseCase;
import br.com.fatec.gestao.usecase.ReporEstoqueUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public BaixarEstoqueUseCase baixarEstoqueUseCase(ProdutoRepository produtoRepository) {
        return new BaixarEstoqueUseCase(produtoRepository);
    }

    @Bean
    public ReporEstoqueUseCase reporEstoqueUseCase(ProdutoRepository produtoRepository) {
        return new ReporEstoqueUseCase(produtoRepository);
    }
}
