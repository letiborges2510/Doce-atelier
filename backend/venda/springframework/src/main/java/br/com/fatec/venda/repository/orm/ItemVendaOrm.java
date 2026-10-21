package br.com.fatec.venda.repository.orm;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemVendaOrm {
    private String produtoId;
    private String nomeProduto;
    private int quantidade;
    private BigDecimal precoUnitario;
}
