package br.com.fatec.venda.messaging;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VendaEvento implements Serializable {
    private String vendaId;
    private String clienteId;
    private BigDecimal valorTotal;
    private String status;
    private LocalDateTime dataHora;
}
