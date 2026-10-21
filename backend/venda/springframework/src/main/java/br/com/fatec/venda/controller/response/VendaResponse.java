package br.com.fatec.venda.controller.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponse(
        String id,
        String clienteId,
        List<ItemVendaResponse> itens,
        BigDecimal valorTotal,
        String status,
        LocalDateTime dataHora
) {
}
