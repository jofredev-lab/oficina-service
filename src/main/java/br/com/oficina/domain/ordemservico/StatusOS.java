package br.com.oficina.domain.ordemservico;

import java.util.EnumSet;
import java.util.Set;

/** Ciclo de vida da Ordem de Serviço, com as regras de transição permitidas. */
public enum StatusOS {
    RECEBIDA("Recebida"),
    EM_DIAGNOSTICO("Em diagnóstico"),
    AGUARDANDO_APROVACAO("Aguardando aprovação"),
    EM_EXECUCAO("Em execução"),
    FINALIZADA("Finalizada"),
    ENTREGUE("Entregue");

    private final String descricao;

    StatusOS(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public Set<StatusOS> proximosPermitidos() {
        return switch (this) {
            case RECEBIDA -> EnumSet.of(EM_DIAGNOSTICO);
            case EM_DIAGNOSTICO -> EnumSet.of(AGUARDANDO_APROVACAO);
            // Aprovado -> execução; rejeitado -> finalizada sem execução (veículo devolvido ao cliente)
            case AGUARDANDO_APROVACAO -> EnumSet.of(EM_EXECUCAO, FINALIZADA);
            case EM_EXECUCAO -> EnumSet.of(FINALIZADA);
            case FINALIZADA -> EnumSet.of(ENTREGUE);
            case ENTREGUE -> EnumSet.noneOf(StatusOS.class);
        };
    }

    public boolean podeTransicionarPara(StatusOS destino) {
        return proximosPermitidos().contains(destino);
    }
}
