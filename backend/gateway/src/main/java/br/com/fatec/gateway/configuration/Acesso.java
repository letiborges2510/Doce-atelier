package br.com.fatec.gateway.configuration;

/** Quem pode acessar uma rota. */
public enum Acesso {
    /** Qualquer pessoa, mesmo sem login. */
    PUBLICO,
    /** Qualquer usuário logado (cliente ou admin). */
    LOGADO,
    /** Somente usuário logado com perfil ADMIN. */
    ADMIN,
    /** Ninguém de fora: rota interna entre microsserviços. */
    BLOQUEADO
}
