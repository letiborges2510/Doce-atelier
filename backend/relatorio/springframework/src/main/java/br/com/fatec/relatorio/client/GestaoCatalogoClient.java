package br.com.fatec.relatorio.client;

import br.com.fatec.relatorio.gateway.CatalogoGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class GestaoCatalogoClient implements CatalogoGateway {

    private final RestTemplate restTemplate;
    private final String gestaoBaseUrl;

    public GestaoCatalogoClient(RestTemplate restTemplate, @Value("${service.gestao.url}") String gestaoBaseUrl) {
        this.restTemplate = restTemplate;
        this.gestaoBaseUrl = gestaoBaseUrl;
    }

    @Override
    public long contarProdutosAtivos() {
        Long count = restTemplate.getForObject(
                gestaoBaseUrl + "/fatec/gestao/v1/produto/count-ativos", Long.class);
        return count == null ? 0 : count;
    }
}
