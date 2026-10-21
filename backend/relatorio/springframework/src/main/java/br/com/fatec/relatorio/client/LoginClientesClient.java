package br.com.fatec.relatorio.client;

import br.com.fatec.relatorio.gateway.ClientesGateway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class LoginClientesClient implements ClientesGateway {

    private final RestTemplate restTemplate;
    private final String loginBaseUrl;

    public LoginClientesClient(RestTemplate restTemplate, @Value("${service.login.url}") String loginBaseUrl) {
        this.restTemplate = restTemplate;
        this.loginBaseUrl = loginBaseUrl;
    }

    @Override
    public long contarClientes() {
        Long count = restTemplate.getForObject(loginBaseUrl + "/fatec/login/v1/user/count", Long.class);
        return count == null ? 0 : count;
    }
}
