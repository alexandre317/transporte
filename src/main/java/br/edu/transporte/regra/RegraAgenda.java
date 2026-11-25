package br.edu.transporte.regra;

import br.edu.transporte.modelo.Viagem;

public interface RegraAgenda {

    long DURACAO_HORAS = 2;

    void validar(Viagem viagem);
}
