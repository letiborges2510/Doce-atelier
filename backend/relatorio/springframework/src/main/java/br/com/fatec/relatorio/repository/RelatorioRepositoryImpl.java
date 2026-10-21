package br.com.fatec.relatorio.repository;

import br.com.fatec.relatorio.repository.client.PedidoRelatorioJpa;
import br.com.fatec.relatorio.repository.orm.PedidoRelatorioOrm;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RelatorioRepositoryImpl implements RelatorioRepository {

    // Ordem do ciclo de vida do pedido. RECUSADA é final (fora do fluxo).
    private static final List<String> CICLO = List.of("PENDENTE", "CONFIRMADA", "SAIU_PARA_ENTREGA", "ENTREGUE");

    private final PedidoRelatorioJpa repository;

    public RelatorioRepositoryImpl(PedidoRelatorioJpa repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void registrarPedido(String vendaId, BigDecimal valorTotal, String status) {
        PedidoRelatorioOrm pedido = repository.findById(vendaId).orElse(null);
        if (pedido == null) {
            repository.save(new PedidoRelatorioOrm(vendaId, valorTotal, status));
            return;
        }
        // Evento repetido ou fora de ordem não pode fazer o pedido "voltar" de status.
        if (podeAtualizar(pedido.getStatus(), status)) {
            pedido.setStatus(status);
            pedido.setValorTotal(valorTotal);
            repository.save(pedido);
        }
    }

    private boolean podeAtualizar(String atual, String novo) {
        if ("RECUSADA".equals(atual)) {
            return false;
        }
        if ("RECUSADA".equals(novo)) {
            return true;
        }
        return CICLO.indexOf(novo) > CICLO.indexOf(atual);
    }

    @Override
    @Transactional(readOnly = true)
    public Totais obterTotais() {
        Map<String, Long> porStatus = new LinkedHashMap<>();
        for (String status : CICLO) {
            porStatus.put(status, 0L);
        }
        porStatus.put("RECUSADA", 0L);
        for (Object[] linha : repository.contagemPorStatus()) {
            porStatus.put((String) linha[0], (Long) linha[1]);
        }
        BigDecimal faturamento = repository.faturamento();
        return new Totais(faturamento == null ? BigDecimal.ZERO : faturamento, repository.pedidosValidos(), porStatus);
    }
}
