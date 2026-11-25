package br.edu.transporte.modelo;

import br.edu.transporte.enums.StatusViagemEnum;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "viagens")
public class Viagem extends Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String origem;

    private String destino;

    private LocalDateTime dataHora;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusViagemEnum status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "veiculo_id")
    private Veiculo veiculo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "motorista_id")
    private Motorista motorista;

}
