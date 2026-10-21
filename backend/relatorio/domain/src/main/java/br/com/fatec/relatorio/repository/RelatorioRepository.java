package br.com.fatec.relatorio.repository;

import java.math.BigDecimal;
import java.util.Map;

public interface RelatorioRepository {

    /**
     * Registra (ou atualiza) um pedido a partir de um evento do venda-service.
     * É por pedido (e não um contador), então receber o mesmo evento duas vezes
     * não duplica o faturamento, e cada mudança de status atualiza o relatório.
     * Chamado pelo consumer do RabbitMQ, de forma assincrona.
     */
    void registrarPedido(String vendaId, BigDecimal valorTotal, String status);

    Totais obterTotais();

    /**
     * faturamentoTotal e pedidosRecebidos NÃO contam pedidos RECUSADA.
     * pedidosPorStatus conta todos, por status (ex.: PENDENTE=2, ENTREGUE=5).
     */
    record Totais(BigDecimal faturamentoTotal, long pedidosRecebidos, Map<String, Long> pedidosPorStatus) {
    }
}
