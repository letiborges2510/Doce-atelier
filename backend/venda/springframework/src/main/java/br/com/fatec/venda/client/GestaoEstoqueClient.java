package br.com.fatec.venda.client;

import br.com.fatec.venda.gateway.EstoqueGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Implementação (adapter) da porta EstoqueGateway, chamando o
 * gestao-service via REST. É a chamada SINCRONA entre os dois
 * microsserviços: o venda-service não confirma a venda sem antes
 * ter certeza de que o estoque foi mesmo descontado.
 */
@Component
public class GestaoEstoqueClient implements EstoqueGateway {

    private final RestTemplate restTemplate;
    private final String gestaoBaseUrl;

    public GestaoEstoqueClient(
            RestTemplate restTemplate,
            @Value("${service.gestao.url}") String gestaoBaseUrl) {
        this.restTemplate = restTemplate;
        this.gestaoBaseUrl = gestaoBaseUrl;
    }

    @Override
    public InfoProduto baixarEstoque(String produtoId, int quantidade) {
        String url = gestaoBaseUrl + "/fatec/gestao/v1/produto/" + produtoId + "/baixar-estoque";
        try {
            ProdutoResponse response = restTemplate.postForObject(
                    url, Map.of("quantidade", quantidade), ProdutoResponse.class);
            if (response == null) {
                throw new IllegalStateException("gestao-service não respondeu a baixa de estoque");
            }
            return new InfoProduto(response.nome(), response.preco());
        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException("Produto não encontrado");
        } catch (HttpClientErrorException.Conflict ex) {
            throw new IllegalStateException("Estoque insuficiente para o produto " + produtoId);
        }
    }

    @Override
    public void reporEstoque(String produtoId, int quantidade) {
        String url = gestaoBaseUrl + "/fatec/gestao/v1/produto/" + produtoId + "/repor-estoque";
        restTemplate.postForObject(url, Map.of("quantidade", quantidade), ProdutoResponse.class);
    }

    // DTO local, próprio do venda-service - não é o mesmo record do
    // gestao-service. Cada microsserviço tem o seu contrato.
    private record ProdutoResponse(
            String id, String nome, String descricao, String categoria,
            BigDecimal preco, String imagemUrl, int estoque, boolean ativo) {
    }
}
