package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor


public class Comprador extends Participante {


    public void darLance(Leilao leilao, double valor) {

        System.out.println("Comprador " + this.nome + " deu um lance de " + valor + " no leilão " + leilao.getId());
    }


}


