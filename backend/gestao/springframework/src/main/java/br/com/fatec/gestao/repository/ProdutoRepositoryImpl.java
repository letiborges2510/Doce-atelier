package br.com.fatec.gestao.repository;

import br.com.fatec.gestao.entity.Produto;
import br.com.fatec.gestao.repository.adapter.ProdutoRepositoryImplAdapter;
import br.com.fatec.gestao.repository.client.ProdutoRepositoryWithJpa;
import br.com.fatec.gestao.repository.orm.ProdutoOrm;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ProdutoRepositoryImpl implements ProdutoRepository {

    private final ProdutoRepositoryWithJpa repository;

    public ProdutoRepositoryImpl(ProdutoRepositoryWithJpa repository) {
        this.repository = repository;
    }

    @Override
    public Produto save(Produto produto) {
        try {
            ProdutoOrm orm = repository.save(ProdutoRepositoryImplAdapter.cast(produto));
            return ProdutoRepositoryImplAdapter.cast(orm);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Optional<Produto> findById(String id) {
        return repository.findById(id).map(ProdutoRepositoryImplAdapter::cast);
    }

    @Override
    public List<Produto> findAll() {
        return repository.findAll().stream().map(ProdutoRepositoryImplAdapter::cast).toList();
    }

    @Override
    public long countAtivos() {
        return repository.countByAtivoTrue();
    }

    @Override
    public Optional<Produto> buscarComLockParaAtualizar(String id) {
        return repository.buscarComLockParaAtualizar(id).map(ProdutoRepositoryImplAdapter::cast);
    }
}
