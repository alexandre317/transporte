# Sistema de Transporte (API REST)

Java 21 · Spring Boot 4 · Spring Data JPA · Spring Security (JWT) · MapStruct · Lombok · Springdoc (Swagger) · PostgreSQL (Neon compatível)

API REST no padrão do projeto **agro-guard** (controladores finos, DTOs de requisição/resposta, mapeadores MapStruct, serviços com as regras de negócio, tratador global de erros e autenticação stateless por JWT), com todo o código, rotas, campos JSON e mensagens em português do Brasil.

## Executar

1. Copie `.envTemplate` para `.env` e preencha os valores.
2. Para PostgreSQL local, suba o banco: `docker compose --env-file .env up -d`.
3. Exporte as variáveis do `.env` no ambiente e rode `./mvnw spring-boot:run`.

### Neon (PostgreSQL gerenciado)

A aplicação usa Spring Data JPA e o driver PostgreSQL JDBC já declarado no `pom.xml`. A configuração `NeonDataSourceConfiguration` aceita a variável `DATABASE_URL` (ou `NEON_DATABASE_URL`) fornecida pelo Neon, converte a URI `postgres://`/`postgresql://` para JDBC e exige `sslmode=require` quando a opção não estiver presente. Também aceita `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e `SPRING_DATASOURCE_PASSWORD`, além das variáveis `BANCO_*` para uso local.

O Spring Boot importa automaticamente `.env` e `.env.local` como arquivos de propriedades. Portanto, quando o Neon CLI gravar `DATABASE_URL` em `.env.local`, a aplicação poderá lê-la ao iniciar no diretório do projeto. Não comite esses arquivos: ambos estão no `.gitignore`.

Configuração do Neon CLI (execute no diretório raiz do projeto e conclua o login no navegador, se solicitado):

```bash
npm i -g neon@latest
neon init -y --org-id org-red-morning-97237323 --project-id flat-frost-45165535 --branch production --services postgres
neon deploy
```

Se a instalação global falhar, use `npx neon@latest` no lugar de `neon`. O CLI e o deploy precisam de autenticação e acesso ao projeto Neon; este repositório não contém credenciais. Depois do deploy, execute o build Maven e inicie a aplicação com as variáveis geradas.

Porta padrão `8080` (ou `PORT`).

### Variáveis de ambiente

| Variável | Descrição |
|---|---|
| `DATABASE_URL` / `NEON_DATABASE_URL` | URI PostgreSQL do Neon; preferencial para conexão gerenciada |
| `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | Sobrescritas JDBC/Spring, se usadas |
| `BANCO_HOST`, `BANCO_PORTA`, `BANCO_NOME` | Conexão PostgreSQL manual/local (porta padrão 5432) |
| `BANCO_USUARIO`, `BANCO_SENHA` | Credenciais PostgreSQL manual/local |
| `JWT_SEGREDO` | Segredo do JWT, **mínimo 32 caracteres** (a aplicação não inicia com menos) |
| `JWT_EXPIRACAO_MINUTOS` | Validade do token (padrão 60) |
| `ORIGEM_PERMITIDA` | Origens CORS permitidas, separadas por vírgula |
| `ADMIN_EMAIL`, `ADMIN_SENHA` | **Primeiro administrador.** Não existe nenhuma credencial no código |
| `CARREGAR_DADOS_DEMO` | `true` cadastra dados fictícios se o banco não tiver motoristas |
| `SWAGGER_HABILITADO` | `true` liga `/swagger-ui.html` (desligado por padrão) |

### Primeiro administrador

Não há conta padrão no código. Se o banco ainda não tem nenhum administrador, a aplicação **exige** `ADMIN_EMAIL` e `ADMIN_SENHA` e **não inicia** sem eles. A senha precisa seguir a política (10+ caracteres com maiúscula, minúscula, número e símbolo). Depois da primeira inicialização, remova `ADMIN_SENHA` do ambiente: ela é ignorada quando já existe um administrador.

## Segurança

- Contas só são **criadas e excluídas por administradores** (`POST` e `DELETE` em `/v1/usuarios`). Não existe cadastro público.
- Não é possível excluir a própria conta nem o único administrador.
- A cada requisição o servidor confere no banco se a conta do token ainda existe e usa o perfil atual: uma conta excluída perde o acesso na hora, sem esperar o token expirar.
- Login com limite de tentativas (5 falhas em 15 minutos por e-mail + IP, depois responde `429`) e resposta idêntica para e-mail inexistente ou senha errada, inclusive no tempo de resposta.
- Senhas com BCrypt, limite de 72 caracteres e política forte.
- Swagger desligado por padrão, CORS restrito aos cabeçalhos necessários, sem stack trace nas respostas, container Docker sem root.

## Erros

Todas as respostas de erro têm o mesmo formato, explicando o que aconteceu na operação:

```json
{
  "status": 400,
  "erro": "Requisição inválida",
  "mensagem": "Falha de validação: 2 campo(s) com problema. Corrija os dados e tente novamente.",
  "caminho": "/v1/veiculos",
  "momento": "2026-11-23T10:15:30-03:00",
  "detalhes": ["campo 'capacidade': A capacidade deve ser maior que zero.", "campo 'placa': O campo 'placa' é obrigatório."]
}
```

Exemplos do que é explicado: campos inválidos (um item por campo), valores de opções aceitos, datas em formato errado, placa/CNH/e-mail duplicados, conflito de agenda (com a janela de horário), veículo ou motorista que não pode ser excluído por ter viagens vinculadas (com as quantidades), token expirado ou conta removida, acesso negado por perfil, excesso de tentativas de login. Erros inesperados (500) retornam um **código** que também é registrado no log do servidor.

## Estrutura

```
br.edu.transporte
├── configuracao   SegurancaConfig (JWT/CORS), AuditoriaConfig, InicializadorDeDados
├── controlador    controladores REST finos
├── dto
│   ├── requisicao  *CadastroRequisicao / *AtualizacaoRequisicao (Bean Validation)
│   └── resposta    *ConsultaResposta / *CadastroResposta / *AtualizacaoResposta, PainelResposta, PaginaResposta
├── enums          PerfilEnum, StatusViagemEnum, TipoVeiculoEnum, SituacaoManutencaoEnum
├── excecao        exceções de negócio, MensagemErro e TratadorGlobalDeErros
├── exportacao     Exportador (interface), ExportadorCsv, ExportadorPdf, RelatorioServico
├── mapeador       MapStruct
├── modelo         entidades JPA (Auditoria = criado_em/por, atualizado_em/por)
├── regra          RegraAgenda (interface) e regras de conflito de agenda
├── repositorio    Spring Data JPA
└── servico        regras de negócio
```

## Rotas

Todas sob `/v1`. Envie `Authorization: Bearer <token>`.

| Rota | Métodos | Acesso |
|---|---|---|
| `/v1/autenticacao/entrar` | POST (`email`, `senha`) | público |
| `/v1/minha-conta` | GET, PUT | autenticado |
| `/v1/painel` | GET | autenticado |
| `/v1/agenda?data&motoristaId&veiculoId&status` | GET | autenticado |
| `/v1/veiculos`, `/v1/motoristas` | GET (`?id=`), POST, PUT `/{id}`, DELETE `/{id}` | GET: autenticado; demais: admin |
| `/v1/viagens` | GET (`status`, `inicio`, `fim`, `pagina`, `tamanho`), GET `/{id}`, GET `/exportar?formato=csv\|pdf`, POST, PUT `/{id}`, DELETE `/{id}` | GET: autenticado; demais: admin |
| `/v1/manutencoes` | GET (`situacao`, `inicio`, `fim`, `pagina`, `tamanho`), GET `/{id}`, GET `/exportar`, POST | admin |
| `/v1/usuarios` | GET (`?id=`), **POST** (`nome`, `email`, `senha`, `perfil`), **DELETE** `/{id}` | admin |

O login retorna `{ token, usuarioId, nome, perfil }`.

## Regras de negócio

- Veículo inativo não faz viagem.
- Sem conflito de agenda: mesmo motorista ou veículo não pode ter outra viagem (não cancelada) em até 2h; veículo com manutenção em andamento, ou agendada para o dia, é bloqueado.
- Placa, CNH e e-mail únicos. Veículo/motorista com viagens vinculadas não pode ser excluído.
- Registrar manutenção atualiza a quilometragem do veículo quando maior que a atual.
- Alertas de revisão no painel (até 15 dias / 1000 km, ou vencida).
- Auditoria: `criado_em`, `atualizado_em`, `criado_por`, `atualizado_por`.

## Docker

```bash
docker build -t transporte .
docker run --env-file .env -p 8080:8080 transporte
```

Diagramas em `docs/diagramas.md`.
