package br.com.fatec.venda.entity;

import br.com.fatec.venda.entity.enumerable.StatusVenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Venda(
        String id,
        String clienteId,
        List<ItemVenda> itens,
        BigDecimal valorTotal,
        StatusVenda status,
        LocalDateTime dataHora,
        LocalDateTime statusAtualizadoEm
) {
    /** Desde quando a venda está no status atual (vendas antigas: desde a criação). */
    public LocalDateTime desde() {
        return statusAtualizadoEm != null ? statusAtualizadoEm : dataHora;
    }
}
