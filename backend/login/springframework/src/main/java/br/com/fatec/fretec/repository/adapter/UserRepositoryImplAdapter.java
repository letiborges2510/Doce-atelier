package br.com.fatec.fretec.repository.adapter;

import br.com.fatec.fretec.entity.User;
import br.com.fatec.fretec.repository.orm.UserOrm;

public class UserRepositoryImplAdapter {
    private UserRepositoryImplAdapter() {
    }

    public static User cast(UserOrm orm) {
        return new User(
                orm.id(),
                orm.nome(),
                orm.username(),
                orm.password(),
                orm.email(),
                orm.cep(),
                orm.roles());
    }

    public static UserOrm cast(User user) {
        return new UserOrm(
                user.id(),
                user.nome(),
                user.username(),
                user.password(),
                user.email(),
                user.cep(),
                user.roles());
    }
}