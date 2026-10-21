package br.com.fatec.relatorio.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mesmo formato do VendaEvento publicado pelo venda-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaEventoRecebido implements Serializable {
    private String vendaId;
    private String clienteId;
    private BigDecimal valorTotal;
    private String status;
    private LocalDateTime dataHora;
}
