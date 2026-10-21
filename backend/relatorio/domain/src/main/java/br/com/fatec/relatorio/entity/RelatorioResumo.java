package br.com.fatec.relatorio.entity;

import java.math.BigDecimal;
import java.util.Map;

public record RelatorioResumo(
        BigDecimal faturamentoTotal,
        long pedidosRecebidos,
        Map<String, Long> pedidosPorStatus,
        long produtosAtivos,
        long clientesCadastrados
) {
}
