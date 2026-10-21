package br.com.fatec.gestao.repository;

import br.com.fatec.gestao.entity.Produto;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository {

    Produto save(Produto produto);

    Optional<Produto> findById(String id);

    List<Produto> findAll();

    long countAtivos();

    /**
     * Busca travando a linha do produto para atualizacao (lock
     * pessimista) - usado na baixa de estoque pra evitar duas vendas
     * simultaneas venderem a mesma unidade duas vezes.
     */
    Optional<Produto> buscarComLockParaAtualizar(String id);

}
