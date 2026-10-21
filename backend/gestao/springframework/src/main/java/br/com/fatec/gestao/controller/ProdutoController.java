package br.com.fatec.gestao.controller;

import br.com.fatec.gestao.controller.adapter.ProdutoControllerAdapter;
import br.com.fatec.gestao.controller.request.AtivoRequest;
import br.com.fatec.gestao.controller.request.BaixarEstoqueRequest;
import br.com.fatec.gestao.controller.request.ProdutoRequest;
import br.com.fatec.gestao.controller.response.ProdutoResponse;
import br.com.fatec.gestao.entity.Produto;
import br.com.fatec.gestao.event.ProdutoPublisher;
import br.com.fatec.gestao.repository.ProdutoRepository;
import br.com.fatec.gestao.usecase.BaixarEstoqueUseCase;
import br.com.fatec.gestao.usecase.ReporEstoqueUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fatec/gestao")
public class ProdutoController {

    private final ProdutoRepository repository;
    private final BaixarEstoqueUseCase baixarEstoqueUseCase;
    private final ReporEstoqueUseCase reporEstoqueUseCase;
    private final ProdutoPublisher produtoPublisher;

    public ProdutoController(
            ProdutoRepository repository,
            BaixarEstoqueUseCase baixarEstoqueUseCase,
            ReporEstoqueUseCase reporEstoqueUseCase,
            ProdutoPublisher produtoPublisher) {
        this.repository = repository;
        this.baixarEstoqueUseCase = baixarEstoqueUseCase;
        this.reporEstoqueUseCase = reporEstoqueUseCase;
        this.produtoPublisher = produtoPublisher;
    }

    @GetMapping("/v1/produto")
    public List<ProdutoResponse> listar() {
        return repository.findAll().stream().map(ProdutoControllerAdapter::cast).toList();
    }

    @GetMapping("/v1/produto/{id}")
    public ProdutoResponse buscarPorId(@PathVariable String id) {
        Produto produto = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        return ProdutoControllerAdapter.cast(produto);
    }

    @GetMapping("/v1/produto/count-ativos")
    public long countAtivos() {
        return repository.countAtivos();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/produto")
    public ProdutoResponse criar(@RequestBody ProdutoRequest request) {
        Produto salvo = repository.save(ProdutoControllerAdapter.cast(request));
        // Evento assíncrono (RabbitMQ) - não interfere na resposta HTTP,
        // só avisa quem estiver interessado (ex.: relatorio-service) que
        // um produto novo entrou no catálogo.
        produtoPublisher.publicarCriacao(salvo);
        return ProdutoControllerAdapter.cast(salvo);
    }

    // Atualizacao completa do produto (nome, preco, estoque, etc). Mantem o
    // id existente para que vire um update e nao uma criacao nova.
    @PutMapping("/v1/produto/{id}")
    public ProdutoResponse atualizar(@PathVariable String id, @RequestBody ProdutoRequest request) {
        repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        Produto atualizado = repository.save(ProdutoControllerAdapter.cast(id, request));
        return ProdutoControllerAdapter.cast(atualizado);
    }

    // Ativa/desativa o produto sem precisar reenviar todos os campos
    // (usado pelo toggle de visibilidade no painel admin do front).
    @PatchMapping("/v1/produto/{id}/ativo")
    public ProdutoResponse alterarAtivo(@PathVariable String id, @RequestBody AtivoRequest request) {
        Produto existente = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado"));
        Produto atualizado = new Produto(
                existente.id(),
                existente.nome(),
                existente.descricao(),
                existente.categoria(),
                existente.preco(),
                existente.imagemUrl(),
                existente.estoque(),
                request.ativo());
        return ProdutoControllerAdapter.cast(repository.save(atualizado));
    }

    // Chamado pelo venda-service no momento da venda. @Transactional
    // aqui é o que mantém o lock pessimista de pé durante toda a
    // operação (ler, validar, descontar e salvar).
    @Transactional
    @PostMapping("/v1/produto/{id}/baixar-estoque")
    public ProdutoResponse baixarEstoque(@PathVariable String id, @RequestBody BaixarEstoqueRequest request) {
        return ProdutoControllerAdapter.cast(baixarEstoqueUseCase.executar(id, request.quantidade()));
    }

    // Compensação chamada pelo venda-service quando um pedido de vários itens
    // falha no meio: devolve ao estoque o que já tinha sido descontado.
    @Transactional
    @PostMapping("/v1/produto/{id}/repor-estoque")
    public ProdutoResponse reporEstoque(@PathVariable String id, @RequestBody BaixarEstoqueRequest request) {
        return ProdutoControllerAdapter.cast(reporEstoqueUseCase.executar(id, request.quantidade()));
    }
}
