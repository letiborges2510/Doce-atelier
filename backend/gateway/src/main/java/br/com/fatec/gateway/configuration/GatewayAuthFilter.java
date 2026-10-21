package br.com.fatec.gateway.configuration;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * AUTENTICAÇÃO E CONTROLE DE ACESSO - tudo centralizado aqui, no gateway.
 *
 * Para cada requisição:
 *  1. lê o JWT (cookie "access_token" ou header Authorization: Bearer);
 *  2. valida assinatura e expiração;
 *  3. confere a regra da rota (ver RegrasDeAcesso, onde está a lista
 *     clara de quem acessa o quê): PUBLICO, LOGADO, ADMIN ou BLOQUEADO;
 *  4. barra com 401 (não logado) / 403 (sem permissão), ou repassa para o
 *     microsserviço com os headers X-User-Name e X-User-Roles.
 *
 * Os microsserviços NÃO têm Spring Security: confiam nesses headers.
 * Por isso qualquer X-User-* que venha do cliente é descartado aqui
 * (ninguém consegue se passar por outro usuário).
 */
@Component
@Order(-100) // depois do CorsFilter (-102), antes do roteamento
public class GatewayAuthFilter extends OncePerRequestFilter {

    private static final String SECRET = "u7x!A$9zLq#2vNf@eR6tYpWmZcXbGdQh";
    private static final String ACCESS_TOKEN_COOKIE = "access_token";
    static final String HEADER_USER = "X-User-Name";
    static final String HEADER_ROLES = "X-User-Roles";

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // pré-flight do CORS nunca leva cookie; o CorsFilter já cuida dele
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        Claims claims = lerToken(request);
        boolean logado = claims != null;
        List<String> roles = logado ? lerRoles(claims) : List.of();
        Acesso exigido = RegrasDeAcesso.acessoExigido(request.getMethod(), request.getRequestURI());

        if (exigido == Acesso.BLOQUEADO) {
            erro(response, 403, "Forbidden", "Rota interna, não disponível pelo gateway");
            return;
        }
        if (exigido != Acesso.PUBLICO && !logado) {
            erro(response, 401, "Unauthorized", "Faça login para acessar este recurso");
            return;
        }
        if (exigido == Acesso.ADMIN && !roles.contains("ADMIN")) {
            erro(response, 403, "Forbidden", "Acesso restrito a administradores");
            return;
        }

        chain.doFilter(new IdentityRequest(request, logado ? claims.getSubject() : null, roles), response);
    }

    /** Devolve os claims se o token existe e é válido (assinatura + expiração); senão null. */
    private Claims lerToken(HttpServletRequest request) {
        String token = extrairToken(request);
        if (token == null) {
            return null;
        }
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException ex) {
            return null; // expirado, adulterado ou malformado
        }
    }

    private String extrairToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        return Arrays.stream(cookies)
                .filter(c -> ACCESS_TOKEN_COOKIE.equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    private List<String> lerRoles(Claims claims) {
        Object raw = claims.get("roles");
        List<String> roles = new ArrayList<>();
        if (raw instanceof List<?> list) {
            for (Object item : list) {
                // aceita "ADMIN" e também "ROLE_ADMIN" (tokens antigos)
                roles.add(String.valueOf(item).replaceFirst("^ROLE_", ""));
            }
        }
        return roles;
    }

    private void erro(HttpServletResponse response, int status, String titulo, String mensagem) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                "{\"status\":" + status + ",\"error\":\"" + titulo + "\",\"message\":\"" + mensagem + "\"}");
    }

    /**
     * Repassa a requisição aos microsserviços com a identidade verificada
     * e SEM qualquer X-User-* enviado pelo cliente.
     */
    private static class IdentityRequest extends HttpServletRequestWrapper {
        private final String username;
        private final String roles;

        IdentityRequest(HttpServletRequest request, String username, List<String> roles) {
            super(request);
            this.username = username;
            this.roles = roles.isEmpty() ? null : String.join(",", roles);
        }

        private static boolean ehIdentidade(String name) {
            return HEADER_USER.equalsIgnoreCase(name) || HEADER_ROLES.equalsIgnoreCase(name);
        }

        private String valorDe(String name) {
            return HEADER_USER.equalsIgnoreCase(name) ? username : roles;
        }

        @Override
        public String getHeader(String name) {
            return ehIdentidade(name) ? valorDe(name) : super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (!ehIdentidade(name)) {
                return super.getHeaders(name);
            }
            String valor = valorDe(name);
            return valor == null ? Collections.emptyEnumeration() : Collections.enumeration(List.of(valor));
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> nomes = new ArrayList<>();
            for (String nome : Collections.list(super.getHeaderNames())) {
                if (!ehIdentidade(nome)) {
                    nomes.add(nome);
                }
            }
            if (username != null) {
                nomes.add(HEADER_USER);
            }
            if (roles != null) {
                nomes.add(HEADER_ROLES);
            }
            return Collections.enumeration(nomes);
        }
    }
}
