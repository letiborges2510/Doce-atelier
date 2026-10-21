package br.com.fatec.fretec.controller;

import br.com.fatec.fretec.controller.adapter.AuthControllerAdapter;
import br.com.fatec.fretec.controller.request.LoginRequest;
import br.com.fatec.fretec.controller.response.AuthClaimsResponse;
import br.com.fatec.fretec.entity.Token;
import br.com.fatec.fretec.entity.User;
import br.com.fatec.fretec.repository.UserRepository;
import br.com.fatec.fretec.security.JwtSecurity;
import br.com.fatec.fretec.security.TokenSecurity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fatec/login")
public class AuthController {

    // Nome do cookie HttpOnly onde o JWT é armazenado
    public static final String ACCESS_TOKEN_COOKIE = "access_token";

    private final TokenSecurity tokenSecurity;
    private final JwtSecurity jwtSecurity;
    private final UserRepository userRepository;

    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public AuthController(TokenSecurity tokenSecurity, JwtSecurity jwtSecurity, UserRepository userRepository) {
        this.tokenSecurity = tokenSecurity;
        this.jwtSecurity = jwtSecurity;
        this.userRepository = userRepository;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/v1/auth")
    public AuthClaimsResponse login(@RequestBody LoginRequest request, HttpServletResponse response) {
        User user = tokenSecurity.autenticar(AuthControllerAdapter.cast(request));
        Token token = tokenSecurity.gerarToken(user);

        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, token.value())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtSecurity.getExpirationSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new AuthClaimsResponse(
                user.id(),
                user.nome(),
                user.username(),
                user.roles().stream().map(Enum::name).toList());
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/v1/logout")
    public void logout(HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v1/auth/forget/{username}")
    public String forgetPassword(@PathVariable("username") String username) {
        return "Olá " + username + " enviamos sua senha para o seu email";
    }

    // Devolve os claims do usuário logado usando o cookie já presente na
    // requisição (sem precisar mandar usuário/senha de novo). Usado pelo
    // front pra saber se a sessão ainda é válida ao recarregar a página.
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/v1/me")
    public AuthClaimsResponse me(@RequestHeader("X-User-Name") String username) {
        // O gateway já validou o cookie e informa o usuário neste header
        User user = userRepository.findByUsername(username);
        return new AuthClaimsResponse(
                user.id(),
                user.nome(),
                user.username(),
                user.roles().stream().map(Enum::name).toList());
    }
}