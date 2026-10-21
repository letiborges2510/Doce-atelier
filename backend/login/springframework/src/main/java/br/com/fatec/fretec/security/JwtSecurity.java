package br.com.fatec.fretec.security;

import br.com.fatec.fretec.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Só GERA o token. A validação (assinatura, expiração, role) é feita
 * no API Gateway, que usa a mesma chave secreta.
 */
@Component
public class JwtSecurity {
    private static final String SECRET = "u7x!A$9zLq#2vNf@eR6tYpWmZcXbGdQh";
    private static final long EXP = 30L * 60 * 1000; // 30 minutos

    // Expõe o tempo de expiração em segundos, usado para o maxAge do cookie
    public long getExpirationSeconds() {
        return EXP / 1000;
    }

    public String generateToken(User user) {
        Date date = new Date();
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());
        return Jwts.builder()
                .subject(user.username())
                // roles simples: "ADMIN" / "USER"
                .claim("roles", user.roles().stream().map(Enum::name).toList())
                .issuedAt(date)
                .expiration(new Date(date.getTime() + EXP))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }
}
