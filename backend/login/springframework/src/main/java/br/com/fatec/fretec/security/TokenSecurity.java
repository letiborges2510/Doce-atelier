package br.com.fatec.fretec.security;

import br.com.fatec.fretec.entity.Login;
import br.com.fatec.fretec.entity.Token;
import br.com.fatec.fretec.entity.User;
import br.com.fatec.fretec.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.NoSuchElementException;

@Component
public class TokenSecurity {
    private final JwtSecurity jwtSecurity;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public TokenSecurity(JwtSecurity jwtSecurity, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.jwtSecurity = jwtSecurity;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Confere usuário/senha (BCrypt) e devolve o usuário; senão, 401
    public User autenticar(Login login) {
        User user;
        try {
            user = userRepository.findByUsername(login.username());
        } catch (NoSuchElementException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
        }
        if (login.password() == null || !passwordEncoder.matches(login.password(), user.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
        }
        return user;
    }

    public Token gerarToken(User user) {
        return new Token(jwtSecurity.generateToken(user));
    }
}
