package br.edu.transporte.configuracao;

import br.edu.transporte.enums.PerfilEnum;
import br.edu.transporte.enums.SituacaoManutencaoEnum;
import br.edu.transporte.enums.StatusViagemEnum;
import br.edu.transporte.enums.TipoVeiculoEnum;
import br.edu.transporte.modelo.Manutencao;
import br.edu.transporte.modelo.Motorista;
import br.edu.transporte.modelo.Usuario;
import br.edu.transporte.modelo.Veiculo;
import br.edu.transporte.modelo.Viagem;
import br.edu.transporte.repositorio.ManutencaoRepositorio;
import br.edu.transporte.repositorio.MotoristaRepositorio;
import br.edu.transporte.repositorio.VeiculoRepositorio;
import br.edu.transporte.repositorio.ViagemRepositorio;
import br.edu.transporte.servico.UsuarioServico;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Log4j2
@Configuration
public class InicializadorDeDados {

    /**
     * Cria o primeiro administrador a partir das variáveis de ambiente ADMIN_EMAIL e ADMIN_SENHA.
     * Nenhuma credencial existe no código-fonte. Se não houver administrador e as variáveis
     * estiverem ausentes, a aplicação NÃO inicia, para nunca ficar sem acesso administrativo nem
     * subir com credencial padrão.
     */
    @Bean
    @Order(1)
    CommandLineRunner criarPrimeiroAdministrador(UsuarioServico usuarioServico,
                                                 @Value("${app.admin.email:}") String emailAdmin,
                                                 @Value("${app.admin.senha:}") String senhaAdmin) {
        return args -> {
            if (usuarioServico.existeAdministrador()) {
                log.info("Já existe um administrador cadastrado; as variáveis ADMIN_EMAIL/ADMIN_SENHA são ignoradas "
                        + "(você pode removê-las do ambiente).");
                return;
            }

            if (emailAdmin.isBlank() || senhaAdmin.isBlank()) {
                throw new IllegalStateException(
                        "Nenhum administrador cadastrado. Defina as variáveis de ambiente ADMIN_EMAIL e ADMIN_SENHA "
                                + "para criar o primeiro administrador na inicialização.");
            }

            var admin = Usuario.builder()
                    .nome("Administrador")
                    .email(emailAdmin)
                    .senha(senhaAdmin)
                    .perfil(PerfilEnum.ADMIN)
                    .build();

            usuarioServico.cadastrar(admin);

            log.info("Primeiro administrador criado com o login '{}'. Remova ADMIN_SENHA do ambiente.", admin.getEmail());
        };
    }

    @Bean
    @Order(2)
    @ConditionalOnProperty(name = "app.dados-demo", havingValue = "true")
    CommandLineRunner carregarDadosDemonstrativos(MotoristaRepositorio motoristaRepositorio,
                                                  VeiculoRepositorio veiculoRepositorio,
                                                  ViagemRepositorio viagemRepositorio,
                                                  ManutencaoRepositorio manutencaoRepositorio) {
        return args -> {
            if (motoristaRepositorio.count() > 0) {
                return;
            }

            String[][] dadosMotoristas = {
                {"Carlos Eduardo Silva", "DF-001234567", "(61) 99111-1001"},
                {"Mariana Oliveira Costa", "DF-002345678", "(61) 99111-1002"},
                {"Rafael Santos Almeida", "DF-003456789", "(61) 99111-1003"},
                {"Juliana Pereira Souza", "DF-004567890", "(61) 99111-1004"},
                {"Fernando Lima Rocha", "DF-005678901", "(61) 99111-1005"},
                {"Camila Ferreira Martins", "DF-006789012", "(61) 99111-1006"},
                {"Bruno Rodrigues Alves", "DF-007890123", "(61) 99111-1007"},
                {"Patricia Gomes Ribeiro", "DF-008901234", "(61) 99111-1008"},
                {"Diego Carvalho Mendes", "DF-009012345", "(61) 99111-1009"},
                {"Aline Barbosa Nunes", "DF-010123456", "(61) 99111-1010"},
                {"Gabriel Teixeira Lopes", "DF-011234567", "(61) 99111-1011"},
                {"Leticia Moreira Dias", "DF-012345678", "(61) 99111-1012"},
                {"Thiago Cardoso Freitas", "DF-013456789", "(61) 99111-1013"},
                {"Vanessa Araujo Pinto", "DF-014567890", "(61) 99111-1014"},
                {"Lucas Monteiro Batista", "DF-015678901", "(61) 99111-1015"},
                {"Beatriz Correia Mendes", "DF-016789012", "(61) 99111-1016"}
            };

            List<Motorista> listaMotoristas = new ArrayList<>();
            for (String[] dados : dadosMotoristas) {
                listaMotoristas.add(Motorista.builder()
                        .nome(dados[0])
                        .cnh(dados[1])
                        .telefone(dados[2])
                        .build());
            }
            listaMotoristas = motoristaRepositorio.saveAll(listaMotoristas);

            String[][] dadosVeiculos = {
                {"JDP1A01", "Fiat Ducato", "VAN", "16"},
                {"JDP1A02", "Mercedes Sprinter", "VAN", "16"},
                {"JDP1A03", "Volkswagen Delivery", "CAMINHAO", "3"},
                {"JDP1A04", "Iveco Daily", "CAMINHAO", "3"},
                {"JDP1A05", "Chevrolet Spin", "CARRO", "7"},
                {"JDP1A06", "Renault Master", "VAN", "16"},
                {"JDP1A07", "Marcopolo Senior", "ONIBUS", "28"},
                {"JDP1A08", "Fiat Fiorino", "CARRO", "2"},
                {"JDP1A09", "Mercedes Sprinter 415", "VAN", "16"},
                {"JDP1A10", "Volkswagen 9.160", "CAMINHAO", "3"},
                {"JDP1A11", "Toyota Hilux", "CARRO", "5"},
                {"JDP1A12", "Renault Master Minibus", "VAN", "16"},
                {"JDP1A13", "Volare W9", "ONIBUS", "26"},
                {"JDP1A14", "Fiat Ducato Minibus", "VAN", "15"},
                {"JDP1A15", "Chevrolet S10", "CARRO", "5"},
                {"JDP1A16", "Iveco Daily Minibus", "VAN", "16"},
                {"JDP1A17", "Mercedes Atego", "CAMINHAO", "8"},
                {"JDP1A18", "Renault Master Furgão", "VAN", "3"},
                {"JDP1A19", "Volkswagen Virtus", "CARRO", "5"},
                {"JDP1A20", "Marcopolo Torino", "ONIBUS", "40"}
            };

            List<Veiculo> listaVeiculos = new ArrayList<>();
            for (String[] dados : dadosVeiculos) {
                listaVeiculos.add(Veiculo.builder()
                        .placa(dados[0])
                        .modelo(dados[1])
                        .tipo(TipoVeiculoEnum.valueOf(dados[2]))
                        .capacidade(Integer.parseInt(dados[3]))
                        .ativo(true)
                        .quilometragemAtual(20000L + listaVeiculos.size() * 4000L)
                        .build());
            }
            listaVeiculos = veiculoRepositorio.saveAll(listaVeiculos);

            int[] quantidadeVeiculos = {2, 2, 2, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
            String[][] rotas = {
                {"Brasília - DF", "Taguatinga - DF"},
                {"Brasília - DF", "Ceilândia - DF"},
                {"Águas Claras - DF", "Plano Piloto - DF"},
                {"Gama - DF", "Santa Maria - DF"},
                {"Sobradinho - DF", "Planaltina - DF"},
                {"Guará - DF", "Núcleo Bandeirante - DF"},
                {"Samambaia - DF", "Recanto das Emas - DF"},
                {"Asa Norte - DF", "Asa Sul - DF"},
                {"Taguatinga - DF", "Águas Claras - DF"},
                {"Ceilândia - DF", "Samambaia - DF"},
                {"Plano Piloto - DF", "Lago Sul - DF"},
                {"Brazlândia - DF", "Plano Piloto - DF"},
                {"Paranoá - DF", "Itapoã - DF"},
                {"Riacho Fundo - DF", "Guará - DF"},
                {"Park Way - DF", "Núcleo Bandeirante - DF"},
                {"Santa Maria - DF", "Gama - DF"}
            };

            LocalDateTime agora = LocalDateTime.now().withSecond(0).withNano(0);
            List<Viagem> listaViagens = new ArrayList<>();
            int indiceVeiculo = 0;
            for (int i = 0; i < listaMotoristas.size(); i++) {
                List<Veiculo> veiculosDoMotorista = new ArrayList<>();
                for (int j = 0; j < quantidadeVeiculos[i]; j++) {
                    veiculosDoMotorista.add(listaVeiculos.get(indiceVeiculo++));
                }

                int quantidadeViagens = 2 + (i % 3);
                for (int j = 0; j < quantidadeViagens; j++) {
                    StatusViagemEnum status;
                    LocalDateTime dataHora;

                    if (j == 0) {
                        status = StatusViagemEnum.CONCLUIDA;
                        dataHora = agora.minusDays(1 + i).minusHours(2);
                    } else if (j == 1) {
                        status = StatusViagemEnum.EM_ANDAMENTO;
                        dataHora = agora.minusMinutes(30L + i * 5L);
                    } else {
                        status = StatusViagemEnum.AGENDADA;
                        dataHora = agora.plusDays(1L + i).plusHours(j);
                    }

                    listaViagens.add(Viagem.builder()
                            .origem(rotas[i][0])
                            .destino(rotas[i][1])
                            .veiculo(veiculosDoMotorista.get(j % veiculosDoMotorista.size()))
                            .motorista(listaMotoristas.get(i))
                            .valor(new BigDecimal(String.format("%d.00", 85 + i * 7 + j * 25)))
                            .status(status)
                            .dataHora(dataHora)
                            .build());
                }
            }
            viagemRepositorio.saveAll(listaViagens);

            LocalDate hoje = LocalDate.now();
            manutencaoRepositorio.saveAll(List.of(
                    manutencao(listaVeiculos.get(0), "Revisão geral", hoje.minusMonths(6), 15000L, hoje.plusDays(90), 20500L),
                    manutencao(listaVeiculos.get(1), "Troca de óleo e filtros", hoje.minusMonths(5), 19000L, hoje.minusDays(5), null),
                    manutencao(listaVeiculos.get(2), "Revisão de freios", hoje.minusMonths(4), 22000L, null, 27000L),
                    manutencao(listaVeiculos.get(3), "Revisão geral", hoje.minusMonths(1), 31000L, hoje.plusDays(40), 40000L)
            ));

            log.info("Dados demonstrativos cadastrados.");
        };
    }

    private static Manutencao manutencao(Veiculo veiculo, String descricao, LocalDate data, long km,
                                         LocalDate proximaData, Long proximoKm) {
        return Manutencao.builder()
                .veiculo(veiculo)
                .descricao(descricao)
                .dataManutencao(data)
                .quilometragem(km)
                .proximaRevisao(proximaData)
                .proximaRevisaoKm(proximoKm)
                .situacao(SituacaoManutencaoEnum.CONCLUIDA)
                .build();
    }
}
