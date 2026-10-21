package br.com.fatec.fretec.controller;

import br.com.fatec.fretec.controller.adapter.UserControllerAdapter;
import br.com.fatec.fretec.controller.request.UserRequest;
import br.com.fatec.fretec.controller.response.AuthClaimsResponse;
import br.com.fatec.fretec.controller.response.UserResponse;
import br.com.fatec.fretec.entity.Token;
import br.com.fatec.fretec.entity.User;
import br.com.fatec.fretec.repository.UserRepository;
import br.com.fatec.fretec.security.JwtSecurity;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fatec/login")
public class UserController {
    private final UserRepository repository;
    private final JwtSecurity jwtSecurity;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    public UserController(
            UserRepository repository,
            JwtSecurity jwtSecurity,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.jwtSecurity = jwtSecurity;
        this.passwordEncoder = passwordEncoder;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/user/save")
    public UserResponse save(@RequestBody UserRequest request) {
        String senhaCriptografada = passwordEncoder.encode(request.password());
        User save = repository.save(UserControllerAdapter.cast(request, senhaCriptografada));
        return new UserResponse(
                save.id(),
                save.nome(),
                save.username(),
                save.email(),
                save.cep(),
                save.roles());
    }

    @GetMapping("/v1/user/count")
    public long count() {
        return repository.count();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/v1/user/create")
    public AuthClaimsResponse create(@RequestBody UserRequest request, HttpServletResponse response) {
        String senhaCriptografada = passwordEncoder.encode(request.password());
        User saved = repository.save(UserControllerAdapter.cast(request, senhaCriptografada));

        Token token = new Token(jwtSecurity.generateToken(saved));

        ResponseCookie cookie = ResponseCookie.from(AuthController.ACCESS_TOKEN_COOKIE, token.value())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Strict")
                .path("/")
                .maxAge(jwtSecurity.getExpirationSeconds())
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return new AuthClaimsResponse(
                saved.id(),
                saved.nome(),
                saved.username(),
                saved.roles().stream().map(Enum::name).toList());
    }
}