package br.com.fatec.fretec.controller.response;

import br.com.fatec.fretec.entity.enumerable.UserRole;

import java.util.List;

public record UserResponse(
        String id,
        String nome,
        String username,
        String email,
        String cep,
        List<UserRole> roles
) {
}
