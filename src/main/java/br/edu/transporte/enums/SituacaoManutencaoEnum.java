package br.edu.transporte.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SituacaoManutencaoEnum {
    AGENDADA("Agendada"),
    EM_ANDAMENTO("Em andamento"),
    CONCLUIDA("Concluída");

    private final String descricao;
}
