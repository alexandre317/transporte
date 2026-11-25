package br.edu.transporte.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoVeiculoEnum {
    CARRO("Carro"),
    VAN("Van"),
    ONIBUS("Ônibus"),
    CAMINHAO("Caminhão");

    private final String descricao;
}
