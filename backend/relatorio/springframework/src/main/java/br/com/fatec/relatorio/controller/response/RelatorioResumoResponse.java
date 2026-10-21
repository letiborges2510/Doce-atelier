package br.com.fatec.relatorio.controller.response;

import java.math.BigDecimal;
import java.util.Map;

public record RelatorioResumoResponse(
        BigDecimal faturamentoTotal,
        long pedidosRecebidos,
        Map<String, Long> pedidosPorStatus,
        long produtosAtivos,
        long clientesCadastrados
) {
}
