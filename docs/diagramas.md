# Diagramas do Sistema de Transporte

> Os blocos abaixo são Mermaid. Cole em https://mermaid.live para exportar PNG/SVG para os slides.

## 1. Casos de uso
```mermaid
flowchart LR
  V([Visitante]) --> UC2(Fazer login)
  U([Usuário]) --> UC2
  U --> UC3(Consultar veículos, motoristas e viagens)
  U --> UC6(Consultar painel e agenda)
  U --> UC9(Gerenciar minha conta)
  A([Administrador]) --> UC4(Gerenciar veículos e motoristas)
  A --> UC5(Gerenciar viagens)
  A --> UC7(Criar e excluir contas)
  A --> UC10(Registrar manutenções e exportar relatórios)
  A -.herda de.-> U
  UC5 -.inclui.-> UC8(Validar veículo ativo e conflitos de agenda)
```

## 2. Sequência: agendar viagem
```mermaid
sequenceDiagram
  actor C as Cliente (front-end)
  participant Ctl as ViagemControlador
  participant M as ViagemMapeador
  participant S as ViagemServico
  participant R as RegraAgenda (regras)
  participant DB as PostgreSQL
  C->>Ctl: POST /v1/viagens (JSON + Bearer JWT)
  Ctl->>Ctl: @Valid ViagemCadastroRequisicao
  Ctl->>M: paraViagem(requisicao)
  Ctl->>S: salvar(viagem)
  S->>DB: busca veículo e motorista
  S->>S: veículo está ativo?
  S->>R: validar(viagem) para cada regra
  R->>DB: consulta conflitos
  S->>DB: salvar(viagem)
  Ctl->>M: paraCadastroResposta(viagem)
  Ctl-->>C: 201 Created + ViagemCadastroResposta
```

## 3. DER
```mermaid
erDiagram
  USUARIOS {
    bigint id PK
    varchar nome
    varchar email UK
    varchar senha
    varchar perfil
  }
  VEICULOS {
    bigint id PK
    varchar placa UK
    varchar modelo
    int capacidade
    varchar tipo
    boolean ativo
    bigint quilometragem_atual
  }
  MOTORISTAS {
    bigint id PK
    varchar nome
    varchar cnh UK
    varchar telefone
  }
  VIAGENS {
    bigint id PK
    varchar origem
    varchar destino
    timestamp data_hora
    decimal valor
    varchar status
    bigint veiculo_id FK
    bigint motorista_id FK
  }
  MANUTENCOES {
    bigint id PK
    bigint veiculo_id FK
    varchar descricao
    date data_manutencao
    bigint quilometragem
    date proxima_revisao
    bigint proxima_revisao_km
    varchar situacao
  }
  VEICULOS ||--o{ VIAGENS : realiza
  MOTORISTAS ||--o{ VIAGENS : conduz
  VEICULOS ||--o{ MANUTENCOES : recebe
```

## 4. Arquitetura em camadas
```mermaid
flowchart TB
  FE[Front-end / cliente HTTP] -->|JSON + JWT| C[Controlador]
  C --> MP[Mapeador - MapStruct]
  C --> S[Servico - regras de negócio]
  S --> RG[RegraAgenda]
  S --> R[Repositorio - Spring Data JPA]
  R --> DB[(PostgreSQL)]
  C --> RS[RelatorioServico]
  RS --> EX[Exportador CSV / PDF]
  SEC[Spring Security - JWT, perfis ADMIN/USER] -.protege.-> C
  EH[TratadorGlobalDeErros] -.trata erros.-> C
```

## 5. Classes: herança, interfaces e polimorfismo
```mermaid
classDiagram
  class Auditoria { <<abstract>> criadoEm atualizadoEm criadoPor atualizadoPor }
  class RegraAgenda { <<interface>> +validar(Viagem) }
  class Exportador { <<interface>> +formato() String +gerar() byte[] }
  Auditoria <|-- Usuario
  Auditoria <|-- Veiculo
  Auditoria <|-- Motorista
  Auditoria <|-- Viagem
  Auditoria <|-- Manutencao
  Viagem "*" --> "1" Veiculo
  Viagem "*" --> "1" Motorista
  Manutencao "*" --> "1" Veiculo
  RegraAgenda <|.. ConflitoMotoristaRegra
  RegraAgenda <|.. ConflitoVeiculoRegra
  RegraAgenda <|.. VeiculoEmManutencaoRegra
  Exportador <|.. ExportadorCsv
  Exportador <|.. ExportadorPdf
  ViagemServico o-- RegraAgenda
  RelatorioServico o-- Exportador
  ViagemControlador --> ViagemServico
  ViagemControlador --> ViagemMapeador
  ViagemServico --> ViagemRepositorio
```
