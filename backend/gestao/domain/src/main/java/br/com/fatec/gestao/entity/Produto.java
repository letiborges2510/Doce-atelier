package br.com.fatec.gestao.entity;

import java.math.BigDecimal;

/**
 * Um doce do catalogo da confeitaria.
 * estoque e a fonte de verdade de quantas unidades existem pra
 * venda - e o campo protegido contra concorrencia na baixa de estoque.
 */
public record Produto(
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
