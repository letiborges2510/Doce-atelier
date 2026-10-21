package br.com.fatec.relatorio.repository.orm;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Modelo de leitura (read model) do relatório: uma linha por pedido,
 * atualizada pelos eventos do venda-service. O id é o id da venda, por isso
 * o mesmo evento repetido não duplica nada.
 */
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoRelatorioOrm {

    @Id
    private String vendaId;

    private BigDecimal valorTotal;

    private String status;
}
