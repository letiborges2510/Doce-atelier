package br.com.fatec.fretec.controller.adapter;

import br.com.fatec.fretec.controller.request.UserRequest;
import br.com.fatec.fretec.entity.User;

import java.util.UUID;

public class UserControllerAdapter {
    private UserControllerAdapter() {
    }

    public static User cast(UserRequest request, String senhaCriptografada) {
        return new User(
                UUID.randomUUID().toString(),
                request.nome(),
                request.username(),
                senhaCriptografada,
                request.email(),
                request.cep(),
                request.roles());
    }
}