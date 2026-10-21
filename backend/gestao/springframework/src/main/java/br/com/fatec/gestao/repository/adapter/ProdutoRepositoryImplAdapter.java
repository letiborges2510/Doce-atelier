package br.com.fatec.gestao.repository.adapter;

import br.com.fatec.gestao.entity.Produto;
import br.com.fatec.gestao.repository.orm.ProdutoOrm;

public class ProdutoRepositoryImplAdapter {
    private ProdutoRepositoryImplAdapter() {
    }

    public static Produto cast(ProdutoOrm orm) {
        return new Produto(
                orm.getId(),
                orm.getNome(),
                orm.getDescricao(),
                orm.getCategoria(),
                orm.getPreco(),
                orm.getImagemUrl(),
                orm.getEstoque(),
                orm.isAtivo());
    }

    public static ProdutoOrm cast(Produto produto) {
        return new ProdutoOrm(
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
