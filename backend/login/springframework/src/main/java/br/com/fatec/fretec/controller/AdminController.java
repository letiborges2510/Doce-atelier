package br.com.fatec.fretec.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

// Rota de exemplo só acessível por ADMIN. Quem barra (401/403) é o API
// Gateway; ele só repassa para cá quem tem role ADMIN e informa o usuário
// no header X-User-Name.
@RestController
@RequestMapping("/fatec/login/v1/admin")
public class AdminController {

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/ping")
    public String ping(@RequestHeader("X-User-Name") String username) {
        return "Acesso liberado! Bem-vindo(a), " + username + ". Você é ADMIN.";
    }
}
