package br.edu.transporte.exportacao;

import br.edu.transporte.modelo.Manutencao;
import br.edu.transporte.modelo.Viagem;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RelatorioServico {

    private static final String FORMATO_PADRAO = "csv";

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATO_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final List<String> CABECALHO_VIAGENS =
            List.of("Origem", "Destino", "Data/Hora", "Veículo", "Motorista", "Valor (R$)", "Status");

    private static final List<String> CABECALHO_MANUTENCOES =
            List.of("Veículo", "Descrição", "Data", "KM", "Próxima revisão", "Próx. revisão (km)", "Situação");

    private final Map<String, Exportador> exportadores = new HashMap<>();

    public RelatorioServico(List<Exportador> disponiveis) {
        for (var exportador : disponiveis) {
            exportadores.put(exportador.formato(), exportador);
        }
    }

    public ResponseEntity<byte[]> gerarViagens(String formato, List<Viagem> viagens) {
        var linhas = viagens.stream()
                .map(v -> List.of(
                        v.getOrigem(),
                        v.getDestino(),
                        v.getDataHora().format(FORMATO_DATA_HORA),
                        v.getVeiculo().getPlaca(),
                        v.getMotorista().getNome(),
                        v.getValor().toPlainString().replace('.', ','),
                        v.getStatus().getDescricao()))
                .toList();

        return gerar(formato, "viagens", "Relatório de viagens", CABECALHO_VIAGENS, linhas);
    }

    public ResponseEntity<byte[]> gerarManutencoes(String formato, List<Manutencao> manutencoes) {
        var linhas = manutencoes.stream()
                .map(m -> List.of(
                        m.getVeiculo().getPlaca(),
                        m.getDescricao(),
                        m.getDataManutencao().format(FORMATO_DATA),
                        String.valueOf(m.getQuilometragem()),
                        m.getProximaRevisao() == null ? "" : m.getProximaRevisao().format(FORMATO_DATA),
                        m.getProximaRevisaoKm() == null ? "" : String.valueOf(m.getProximaRevisaoKm()),
                        m.getSituacao().getDescricao()))
                .toList();

        return gerar(formato, "manutencoes", "Relatório de manutenções", CABECALHO_MANUTENCOES, linhas);
    }

    private ResponseEntity<byte[]> gerar(String formato, String nomeArquivo, String titulo,
                                         List<String> cabecalho, List<List<String>> linhas) {
        var chave = formato == null ? FORMATO_PADRAO : formato.toLowerCase();
        var exportador = exportadores.getOrDefault(chave, exportadores.get(FORMATO_PADRAO));
        var corpo = exportador.gerar(titulo, cabecalho, linhas);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + nomeArquivo + "." + exportador.formato() + "\"")
                .contentType(exportador.tipoMidia())
                .body(corpo);
    }
}
