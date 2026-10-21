package br.com.fatec.gestao.messaging;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Formato do evento publicado na fila quando um produto é criado.
 * Mesmo padrão do VendaEvento do venda-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoEvento implements Serializable {
    private String produtoId;
    private String nome;
    private BigDecimal preco;
    private int estoque;
}
