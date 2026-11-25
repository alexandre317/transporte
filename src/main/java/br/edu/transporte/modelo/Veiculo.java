package br.edu.transporte.modelo;

import br.edu.transporte.enums.TipoVeiculoEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "veiculos")
public class Veiculo extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 10)
    private String placa;

    private String modelo;

    private int capacidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoVeiculoEnum tipo;

    private boolean ativo;

    private Long quilometragemAtual;

}
