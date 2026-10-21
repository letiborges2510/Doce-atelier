package br.com.fatec.venda.controller;

import br.com.fatec.venda.controller.adapter.VendaControllerAdapter;
import br.com.fatec.venda.controller.request.AtualizarStatusRequest;
import br.com.fatec.venda.controller.request.VendaRequest;
import br.com.fatec.venda.controller.response.VendaResponse;
import br.com.fatec.venda.entity.Venda;
import br.com.fatec.venda.entity.enumerable.StatusVenda;
import br.com.fatec.venda.repository.VendaRepository;
import br.com.fatec.venda.usecase.AtualizarStatusVendaUseCase;
import br.com.fatec.venda.usecase.RealizarVendaUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/fatec/venda")
public class VendaController {

    // O API Gateway valida o JWT e informa o usuário logado neste header.

    private final RealizarVendaUseCase realizarVendaUseCase;
    private final AtualizarStatusVendaUseCase atualizarStatusVendaUseCase;
    private final VendaRepository vendaRepository;

    public VendaController(
            RealizarVendaUseCase realizarVendaUseCase,
            AtualizarStatusVendaUseCase atualizarStatusVendaUseCase,
            VendaRepository vendaRepository) {
        this.realizarVendaUseCase = realizarVendaUseCase;
        this.atualizarStatusVendaUseCase = atualizarStatusVendaUseCase;
        this.vendaRepository = vendaRepository;
    }

    @Transactional
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/venda")
    public VendaResponse vender(@RequestBody VendaRequest request, @RequestHeader("X-User-Name") String clienteId) {
        List<RealizarVendaUseCase.ItemPedido> itens = request.itens().stream()
                .map(item -> new RealizarVendaUseCase.ItemPedido(item.produtoId(), item.quantidade()))
                .toList();
        Venda venda = realizarVendaUseCase.executar(clienteId, itens);
        return VendaControllerAdapter.cast(venda);
    }

    @GetMapping("/v1/venda/minhas")
    public List<VendaResponse> minhasVendas(@RequestHeader("X-User-Name") String clienteId) {
        return vendaRepository.findByClienteId(clienteId).stream()
                .map(VendaControllerAdapter::cast)
                .toList();
    }

    // Lista todas as vendas - usado pelo painel administrativo (dashboard
    // de pedidos). Somente ADMIN pode acessar (quem barra é o API Gateway).
    // Opcional: ?status=PENDENTE filtra pelo status do pedido.
    @GetMapping("/v1/venda")
    public List<VendaResponse> listarTodas(@RequestParam(required = false) String status) {
        List<Venda> vendas = status == null || status.isBlank()
                ? vendaRepository.findAll()
                : vendaRepository.findByStatus(parseStatus(status));
        return vendas.stream().map(VendaControllerAdapter::cast).toList();
    }

    // Consulta um pedido. O cliente só enxerga o próprio; o admin vê qualquer um.
    @GetMapping("/v1/venda/{id}")
    public VendaResponse buscarPorId(
            @PathVariable String id,
            @RequestHeader("X-User-Name") String usuario,
            @RequestHeader(value = "X-User-Roles", required = false) String roles) {
        Venda venda = vendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada"));
        boolean admin = roles != null && List.of(roles.split(",")).contains("ADMIN");
        if (!admin && !venda.clienteId().equals(usuario)) {
            throw new IllegalArgumentException("Venda não encontrada");
        }
        return VendaControllerAdapter.cast(venda);
    }

    // Avança o status do pedido (um passo por vez). Só ADMIN (quem barra é o API Gateway).
    @Transactional
    @PatchMapping("/v1/venda/{id}/status")
    public VendaResponse atualizarStatus(@PathVariable String id, @RequestBody AtualizarStatusRequest request) {
        return VendaControllerAdapter.cast(atualizarStatusVendaUseCase.executar(id, parseStatus(request.status())));
    }

    private StatusVenda parseStatus(String valor) {
        try {
            return StatusVenda.valueOf(valor.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw new IllegalStateException("Status inválido: " + valor);
        }
    }
}
