package br.com.fatec.gestao.usecase;

import br.com.fatec.gestao.entity.Produto;
import br.com.fatec.gestao.repository.ProdutoRepository;

/**
 * Regra de negocio da baixa de estoque, chamada pelo venda-service
 * no momento da venda. E aqui que a concorrencia e tratada: o
 * repository trava a linha do produto (buscarComLockParaAtualizar)
 * ate essa operacao terminar, entao duas vendas do mesmo produto,
 * simultaneas, sao processadas uma de cada vez.
 */
public class BaixarEstoqueUseCase {

    private final ProdutoRepository produtoRepository;

    public BaixarEstoqueUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto executar(String produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade precisa ser maior que zero");
        }

        Produto produto = produtoRepository.buscarComLockParaAtualizar(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        if (!produto.ativo()) {
            throw new IllegalStateException("Produto \"" + produto.nome() + "\" não está disponível");
        }

        if (produto.estoque() < quantidade) {
            throw new IllegalStateException(
                    "Estoque insuficiente para \"" + produto.nome() + "\". Disponível: " + produto.estoque());
        }

        Produto atualizado = new Produto(
                produto.id(),
                produto.nome(),
                produto.descricao(),
                produto.categoria(),
                produto.preco(),
                produto.imagemUrl(),
                produto.estoque() - quantidade,
                produto.ativo());

        return produtoRepository.save(atualizado);
    }
}
