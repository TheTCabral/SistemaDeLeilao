package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public abstract class Pessoa {

    private int id;
    private String nome;
    private String email;
    private String cpf;
    private String senha;


    public Pessoa(String nome, String email, String cpf, String senha) {
        setNome(nome);
        setEmail(email);
        setCpf(cpf);
        setSenha(senha);
    }

     public void setNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("O nome não pode ser vazio.");
        }
        if (nome.trim().length() < 3) {
            throw new IllegalArgumentException("O nome deve ter no mínimo 3 caracteres.");
        }
        if (nome.trim().length() > 100) {
            throw new IllegalArgumentException("O nome deve ter no máximo 100 caracteres.");
        }
        if (!nome.matches("^[A-Za-zÀ-ÿ\\s]+$")) {
            throw new IllegalArgumentException("O nome deve conter apenas letras.");
        }
        this.nome = nome.trim();
    }


    public void setEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("O email não pode ser vazio.");
        }

        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new IllegalArgumentException("Email inválido. Use o formato: usuario@dominio.com");
        }

        if (email.length() > 100) {
            throw new IllegalArgumentException("Email deve ter no máximo 100 caracteres.");
        }

        this.email = email.toLowerCase().trim();
    }

    /**
     * Define CPF da pessoa
     * Regra 2: Dados devem ser válidos e únicos
     */
    public void setCpf(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            throw new IllegalArgumentException("O CPF não pode ser vazio.");
        }

        // Remover formatação (pontos e traços)
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");

        // Validar tamanho
        if (cpfLimpo.length() != 11) {
            throw new IllegalArgumentException("CPF deve conter 11 dígitos.");
        }

        // Validar se não são todos dígitos iguais
        if (cpfLimpo.matches("(\\d)\\1{10}")) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        // Validar dígitos verificadores
        if (!validarCPF(cpfLimpo)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        this.cpf = cpfLimpo;
    }

    /**
     * Define senha da pessoa
     * Regra 27: Senha deve ter no mínimo 6 caracteres
     * Nota: A criptografia é feita no DAO com BCrypt
     */
    public void setSenha(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new IllegalArgumentException("A senha não pode ser vazia.");
        }
        if (senha.length() < 6) {
            throw new IllegalArgumentException("A senha deve ter no mínimo 6 caracteres.");
        }
        if (senha.length() > 50) {
            throw new IllegalArgumentException("A senha deve ter no máximo 50 caracteres.");
        }

        // Validar complexidade da senha (opcional, mas recomendado)
        if (!senha.matches(".*[A-Za-z].*")) {
            throw new IllegalArgumentException("A senha deve conter pelo menos uma letra.");
        }

        this.senha = senha;
    }

    // ===== MÉTODOS DE VALIDAÇÃO =====

    /**
     * Valida CPF usando algoritmo oficial
     * Regra 2: Dados devem ser válidos
     */
    private boolean validarCPF(String cpf) {
        try {
            // Calcular primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int digito1 = 11 - (soma % 11);
            if (digito1 >= 10) digito1 = 0;

            // Verificar primeiro dígito
            if (digito1 != Character.getNumericValue(cpf.charAt(9))) {
                return false;
            }

            // Calcular segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int digito2 = 11 - (soma % 11);
            if (digito2 >= 10) digito2 = 0;

            // Verificar segundo dígito
            return digito2 == Character.getNumericValue(cpf.charAt(10));

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retorna CPF formatado (XXX.XXX.XXX-XX)
     */
    public String getCpfFormatado() {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return String.format("%s.%s.%s-%s",
                cpf.substring(0, 3),
                cpf.substring(3, 6),
                cpf.substring(6, 9),
                cpf.substring(9, 11));
    }

    /**
     * Valida se todos os campos obrigatórios estão preenchidos
     * Regra 1: Todo participante deve estar registrado
     * Regra 2: Dados válidos e únicos
     */
    public boolean isValido() {
        try {
            return nome != null && !nome.isEmpty() &&
                    email != null && !email.isEmpty() &&
                    cpf != null && cpf.length() == 11 &&
                    senha != null && senha.length() >= 6;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retorna tipo da pessoa (para uso em relatórios)
     * Regra 4: Vendedores e compradores têm papéis diferentes
     */
    public String getTipo() {
        if (this instanceof Administrador) {
            return "ADMINISTRADOR";
        } else if (this instanceof Vendedor) {
            return "VENDEDOR";
        } else if (this instanceof Comprador) {
            return "COMPRADOR";
        }
        return "DESCONHECIDO";
    }

    /**
     * Verifica se a pessoa pode dar lances
     * Regra 9: Somente usuários autenticados podem dar lances
     */
    public boolean podeDarLances() {
        return this instanceof Comprador && this.id > 0;
    }

    /**
     * Verifica se a pessoa pode cadastrar itens
     * Regra 5: Somente vendedores cadastrados podem cadastrar itens
     */
    public boolean podeCadastrarItens() {
        return this instanceof Vendedor && this.id > 0;
    }

    /**
     * Verifica se a pessoa é administrador
     * Regra 28: Apenas usuários autorizados podem acessar áreas administrativas
     */
    public boolean isAdministrador() {
        return this instanceof Administrador;
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - %s", nome, getTipo(), email);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pessoa pessoa = (Pessoa) o;
        return id == pessoa.id || (cpf != null && cpf.equals(pessoa.cpf));
    }

    @Override
    public int hashCode() {
        return cpf != null ? cpf.hashCode() : Integer.hashCode(id);
    }
}