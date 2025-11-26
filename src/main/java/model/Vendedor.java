package model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Vendedor extends Participante {
    public Vendedor(String nome, String email, String cpfCnpj, String senhaHash) {
        super(nome, email, cpfCnpj, senhaHash);
    }


    public void cadastrarItem(Item item) {
        System.out.println("Vendedor " + this.nome + " cadastrou o item: " + item.getDescricao());
    }




    }

