package br.com.fatec.gestao.usecase;

import br.com.fatec.gestao.entity.Produto;
import br.com.fatec.gestao.repository.ProdutoRepository;

/**
 * Devolve unidades ao estoque. Usado pelo venda-service como COMPENSAÇÃO:
 * se um pedido com vários itens falha no meio (ex.: o 1º item teve baixa e o
 * 2º está sem estoque), as unidades já descontadas voltam. Usa o mesmo lock
 * pessimista da baixa, então repor e vender ao mesmo tempo não se atropelam.
 */
public class ReporEstoqueUseCase {

    private final ProdutoRepository produtoRepository;

    public ReporEstoqueUseCase(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public Produto executar(String produtoId, int quantidade) {
        if (quantidade <= 0) {
            throw new IllegalArgumentException("A quantidade precisa ser maior que zero");
        }

        Produto produto = produtoRepository.buscarComLockParaAtualizar(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));

        Produto atualizado = new Produto(
                produto.id(),
                produto.nome(),
                produto.descricao(),
                produto.categoria(),
                produto.preco(),
                produto.imagemUrl(),
                produto.estoque() + quantidade,
                produto.ativo());

        return produtoRepository.save(atualizado);
    }
}
