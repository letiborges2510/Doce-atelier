package br.com.fatec.relatorio.controller;

import br.com.fatec.relatorio.controller.response.RelatorioResumoResponse;
import br.com.fatec.relatorio.entity.RelatorioResumo;
import br.com.fatec.relatorio.usecase.ObterResumoUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fatec/relatorio")
public class RelatorioController {

    private final ObterResumoUseCase obterResumoUseCase;

    public RelatorioController(ObterResumoUseCase obterResumoUseCase) {
        this.obterResumoUseCase = obterResumoUseCase;
    }

    @GetMapping("/v1/resumo")
    public RelatorioResumoResponse resumo() {
        RelatorioResumo resumo = obterResumoUseCase.executar();
        return new RelatorioResumoResponse(
                resumo.faturamentoTotal(),
                resumo.pedidosRecebidos(),
                resumo.pedidosPorStatus(),
                resumo.produtosAtivos(),
                resumo.clientesCadastrados());
    }
}
