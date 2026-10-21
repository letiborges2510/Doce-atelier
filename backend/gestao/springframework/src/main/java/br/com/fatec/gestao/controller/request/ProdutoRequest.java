package br.com.fatec.gestao.controller.request;

import java.math.BigDecimal;

public record ProdutoRequest(
        String nome,
        String descricao,
        String categoria,
        BigDecimal preco,
        String imagemUrl,
        int estoque,
        boolean ativo
) {
}
