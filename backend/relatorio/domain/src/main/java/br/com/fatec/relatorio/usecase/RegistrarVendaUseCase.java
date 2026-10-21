package br.com.fatec.relatorio.usecase;

import br.com.fatec.relatorio.repository.RelatorioRepository;

import java.math.BigDecimal;

/**
 * Executado pelo consumer da fila de vendas. Roda em segundo plano, bem
 * depois da venda já ter sido respondida pro cliente - é o lado assincrono
 * do fluxo. Recebe tanto o evento de venda criada quanto os de mudança de status.
 */
public class RegistrarVendaUseCase {

    private final RelatorioRepository relatorioRepository;

    public RegistrarVendaUseCase(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    public void executar(String vendaId, BigDecimal valorTotal, String status) {
        relatorioRepository.registrarPedido(vendaId, valorTotal, status);
    }
}
