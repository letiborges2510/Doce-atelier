package br.com.fatec.gestao.controller.adapter;

import br.com.fatec.gestao.controller.request.ProdutoRequest;
import br.com.fatec.gestao.controller.response.ProdutoResponse;
import br.com.fatec.gestao.entity.Produto;

public class ProdutoControllerAdapter {
    private ProdutoControllerAdapter() {
    }

    public static Produto cast(ProdutoRequest request) {
        return new Produto(
                null,
                request.nome(),
                request.descricao(),
                request.categoria(),
                request.preco(),
                request.imagemUrl(),
                request.estoque(),
                request.ativo());
    }

    // Usado na atualizacao (PUT): mantem o id existente para que o
    // save() faca um merge/update em vez de criar um produto novo.
    public static Produto cast(String id, ProdutoRequest request) {
        return new Produto(
                id,
                request.nome(),
                request.descricao(),
                request.categoria(),
                request.preco(),
                request.imagemUrl(),
                request.estoque(),
                request.ativo());
    }

    public static ProdutoResponse cast(Produto produto) {
        return new ProdutoResponse(
                produto.id(),
                produto.nome(),
                produto.descricao(),
                produto.categoria(),
                produto.preco(),
                produto.imagemUrl(),
                produto.estoque(),
                produto.ativo());
    }
}
