package model;

import dao.RelatorioDAO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class Administrador extends Pessoa {

    public Administrador(String nome, String email, String cpfCnpj, String senhaHash) {
        super(nome, email, cpfCnpj, senhaHash);
    }

      public Object gerarRelatorioSucessoVendas(RelatorioDAO relatorioDAO) {
        System.out.println("Admin " + this.getNome() + " gerando relatório de sucesso de vendas.");
        return relatorioDAO.gerarRelatorioSucessoVendas(); //
    }


    public Object gerarRelatorioValoresMedios(RelatorioDAO relatorioDAO) {
        System.out.println("Admin " + this.getNome() + " gerando relatório de valores médios.");
        return relatorioDAO.gerarRelatorioValoresMedios(); //
    }

    public int gerarRelatorioNumeroParticipantes(RelatorioDAO relatorioDAO) {
        System.out.println("Admin " + this.getNome() + " gerando relatório de número de participantes.");
        return relatorioDAO.gerarRelatorioNumeroParticipantes(); //
    }
    public void gerenciarParticipante(Participante p, boolean bloquear) {
        // Lógica de gerenciamento
        System.out.println("Admin " + this.getNome() + " gerenciando usuário " + p.getNome());
    }
}

