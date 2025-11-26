package model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Participante extends Pessoa {
    protected int id;
    protected String nome;
    protected String email;
    protected String cpfCnpj; // Deve ser único
    protected String senhaHash; // Senha deve ser armazenada com hash [cite: 35]

    public Participante(String nome, String email, String cpfCnpj, String senhaHash) {
        this.nome = nome;
        this.email = email;
        this.cpfCnpj = cpfCnpj;
        this.senhaHash = senhaHash;
    }

    public boolean autenticar(String senha) {
        // Lógica de verificação de hash (ex: BCrypt.checkpw)
        return true; // Exemplo
    }

    // Sobrescrevemos equals() e hashCode() para garantir a regra de
    // dados únicos (ex: por e-mail ou cpfCnpj)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Participante that = (Participante) o;
        return email.equals(that.email) || cpfCnpj.equals(that.cpfCnpj);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, cpfCnpj);
    }
}
