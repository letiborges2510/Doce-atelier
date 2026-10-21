package br.com.fatec.relatorio.messaging;

import java.io.Serializable;
import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mesmo formato do ProdutoEvento publicado pelo gestao-service.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProdutoEventoRecebido implements Serializable {
    private String produtoId;
    private String nome;
    private BigDecimal preco;
    private int estoque;
}
