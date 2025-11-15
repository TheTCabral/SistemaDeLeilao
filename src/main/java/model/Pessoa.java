package model;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public abstract class Pessoa {

    private int id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;



    public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode ser nulo ou vazio.");
        }
        this.nome = nome;
    }

    public void setEmail(String email) {
        if (email == null || !email.contains("@")) {
            throw new IllegalArgumentException("E-mail inválido.");
        }
        this.email = email;
    }

    public void setCpf(String cpf) {

        if (cpf == null || !cpf.matches("\\d{11}")) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos numéricos.");
        }
        this.cpf = cpf;
    }

    public void setSenha(String senha) {

        if (senha == null || senha.length() < 6) {
            throw new IllegalArgumentException("Senha deve ter no mínimo 6 caracteres.");
        }

        this.senha = senha;
    }
}