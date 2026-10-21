package br.com.fatec.gestao.controller.response;

import java.math.BigDecimal;

public record ProdutoResponse(
        String id,
        String nome,
        String descricao,
        String categoria,
        BigDecimal preco,
        String imagemUrl,
        int estoque,
        boolean ativo
) {
}
