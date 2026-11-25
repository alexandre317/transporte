package br.edu.transporte.modelo;

import br.edu.transporte.enums.SituacaoManutencaoEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "manutencoes")
public class Manutencao extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    private String descricao;

    private LocalDate dataManutencao;

    private Long quilometragem;

    private LocalDate proximaRevisao;

    private Long proximaRevisaoKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SituacaoManutencaoEnum situacao;

}
