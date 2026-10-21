package br.com.fatec.venda.entity;

import java.math.BigDecimal;

public record ItemVenda(
        String produtoId,
        String nomeProduto,
        int quantidade,
        BigDecimal precoUnitario
) {
    public BigDecimal subtotal() {
        return precoUnitario.multiply(BigDecimal.valueOf(quantidade));
    }
}
