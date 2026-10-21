package br.com.fatec.gateway.configuration;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * O front-end React (Vite, http://localhost:5173) chama o gateway em
 * http://localhost:8080 - origem diferente, então o navegador exige
 * CORS. Como o login usa cookie HttpOnly (access_token), é obrigatório
 * allowCredentials(true) + uma origem explícita (nunca "*" com
 * credentials). Isso é feito aqui, no gateway, porque é o único ponto
 * que o navegador acessa diretamente; os outros serviços são chamados
 * pelo gateway (servidor-a-servidor), não pelo navegador.
 *
 * Um CorsFilter (em vez de WebMvcConfigurer) porque ele atua no nível
 * de Filter do servlet, antes de qualquer roteamento do Gateway MVC -
 * garante que o CORS seja aplicado independente de como as rotas são
 * resolvidas internamente.
 */
@Configuration
public class CorsConfig {

    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(-102);
        return bean;
    }
}
