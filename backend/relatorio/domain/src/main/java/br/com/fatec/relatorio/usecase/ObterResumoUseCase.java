package br.com.fatec.relatorio.usecase;

import br.com.fatec.relatorio.entity.RelatorioResumo;
import br.com.fatec.relatorio.gateway.CatalogoGateway;
import br.com.fatec.relatorio.gateway.ClientesGateway;
import br.com.fatec.relatorio.repository.RelatorioRepository;

/**
 * Monta o resumo do painel administrativo: faturamento e pedidos
 * vêm do próprio relatorio-service (construidos de forma assincrona,
 * via eventos); produtos ativos e clientes cadastrados são
 * consultados ao vivo (sincrono) no gestao-service e no
 * login-service, porque esses números não mudam com tanta frequência
 * e não precisam de um modelo de leitura próprio.
 */
public class ObterResumoUseCase {

    private final RelatorioRepository relatorioRepository;
    private final CatalogoGateway catalogoGateway;
    private final ClientesGateway clientesGateway;

    public ObterResumoUseCase(
            RelatorioRepository relatorioRepository,
            CatalogoGateway catalogoGateway,
            ClientesGateway clientesGateway) {
        this.relatorioRepository = relatorioRepository;
        this.catalogoGateway = catalogoGateway;
        this.clientesGateway = clientesGateway;
    }

    public RelatorioResumo executar() {
        RelatorioRepository.Totais totais = relatorioRepository.obterTotais();
        long produtosAtivos = catalogoGateway.contarProdutosAtivos();
        long clientesCadastrados = clientesGateway.contarClientes();

        return new RelatorioResumo(
                totais.faturamentoTotal(),
                totais.pedidosRecebidos(),
                totais.pedidosPorStatus(),
                produtosAtivos,
                clientesCadastrados);
    }
}
