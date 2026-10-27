# SoulUp — Back-End Java + Banco de Dados Oracle

API REST em Java puro (sem framework — `com.sun.net.httpserver`, nativo do
JDK) que expõe o ecossistema de gamificação sustentável SoulUp: cadastro e
login de usuários, registro de ações sustentáveis com validação (manual e
por IA), conquistas, campeonatos ligados aos ODS e ranking por pontos.

## Nota para avaliação por matéria

Este back-end pode ser avaliado de forma **independente** do Front-End e
do Data Modeler — testamos isso na prática, não é só teoria:

- `mvn compile` funciona sem acesso ao Oracle — o código só usa
  `java.sql.*` (interfaces padrão do JDK) no ponto de conexão — então já
  dá para avaliar a arquitetura (DAO, handlers, services) mesmo sem rede.
- `mvn exec:java -Dexec.mainClass="api.ServidorApi"` também **sobe
  normalmente sem o Oracle**: a conexão com o banco é feita só dentro de
  cada DAO, na hora da requisição — nunca no `main()`. `GET /api/health`
  responde `200 {"status":"ok"}` mesmo sem o Oracle estar acessível.
- Qualquer rota que precise de dados de verdade (`/api/login`,
  `/api/usuarios`, etc.) sem o Oracle acessível retorna um erro HTTP 500
  em JSON (`{"erro": "..."}`) de forma controlada — nunca derruba o
  servidor. Pra testar essas rotas com dados reais, é necessário acesso
  ao Oracle do Challenge (ver "Configurando a conexão com o banco").
- A modelagem (Data Modeler) e os scripts de `database/` são avaliados
  separadamente — são só o DDL e os `.sql`, não dependem do servidor Java
  nem do Front-End em nenhum momento, só do próprio Oracle (SQL
  Developer/Data Modeler conectado com as mesmas credenciais).
- Ou seja: nenhuma matéria precisa do **código** das outras rodando — o
  único recurso compartilhado entre Front-End, Back-end Java e Banco de
  Dados é o próprio Oracle (`oracle.fiap.com.br`), que o time já tem
  acesso configurado.

## Tecnologias

| Item | Detalhe |
|---|---|
| Linguagem | Java 17+ (compilado e testado também em 21) |
| Build | Maven (`pom.xml`) |
| Servidor HTTP | `com.sun.net.httpserver.HttpServer` — nativo do JDK, nenhum framework web |
| Banco de dados | Oracle Database (schema com 14 tabelas) |
| Driver JDBC | `ojdbc11` (declarado no `pom.xml`, baixado pelo Maven) |
| JSON | `org.json` 20250107 (baixado pelo Maven ou pelo `rodar-backend.ps1`; compatível com Java 8+) |
| Padrão de acesso a dados | DAO (um `*DAO.java` por tabela/entidade) |

## Estrutura de pastas

```
.
├── database/                 → scripts SQL, nesta ordem de execução:
│   ├── 01_schema_base.sql           → 9 tabelas do núcleo (usuário, grupo, ação, registro...)
│   ├── 02_expansao_soulup.sql       → colunas extras de TB_USUARIO (nível, streak, indicação...)
│   ├── 03_carga_inicial_dados_exemplo.sql
│   ├── 04_hash_evidencias.sql
│   ├── 05_ecossistema_ods.sql       → 5 tabelas do ecossistema (ODS, conquista, campeonato...)
│   ├── 06_carga_ecossistema.sql
│   └── 07_consultas_sql.sql         → consultas exigidas pela disciplina de Banco de Dados
├── rodar-backend.ps1          → roda sem Maven no Windows (baixa os .jar para lib/, compila e sobe)
├── src/main/java/
│   ├── api/                  → ServidorApi (ponto de entrada) + um Handler por grupo de rotas
│   ├── conexao/               → ConexaoOracleProjeto (conexão JDBC)
│   ├── dao/                   → um DAO por tabela (14 tabelas)
│   ├── model/                 → classes de domínio
│   ├── service/               → regras de negócio (MotorConquistas, Ranking, SistemaGamificacao)
│   ├── ia/                    → validação de evidência por IA (ValidacaoEvidenciaIA)
│   ├── interfaces/            → Calculavel
│   ├── utils/                  → HashImagem
│   └── teste/                  → Main e TesteValidacaoIA (testes manuais via main())
└── pom.xml
```

## Pré-requisitos

- JDK 17 ou superior instalado.
- Maven 3.8+ instalado (ou use o wrapper da sua IDE).
- Acesso ao banco Oracle do Challenge (`oracle.fiap.com.br`) — **só é
  necessário para rodar de verdade**; compilar não exige rede.

## Configurando a conexão com o banco (IMPORTANTE)

As credenciais do Oracle ficam em
`src/main/java/conexao/ConexaoOracleProjeto.java` (constantes `URL`,
`USUARIO`, `SENHA`). **Esse arquivo já vem preenchido com as credenciais
de desenvolvimento da equipe para facilitar o teste local — não o suba
para um repositório público nem o compartilhe fora do grupo.** Se o
repositório for público no GitHub, troque essas constantes por variáveis
de ambiente antes do push final (ex.: `System.getenv("ORACLE_USUARIO")`),
ou mantenha o arquivo fora do controle de versão com um
`.gitignore`/arquivo de exemplo (`ConexaoOracleProjeto.java.example`).

## Como compilar e rodar

```bash
# compilar (baixa as dependências do Maven Central na primeira vez)
mvn compile

# rodar o servidor (porta padrão 8080)
mvn exec:java -Dexec.mainClass="api.ServidorApi"

# alternativa: gerar o .jar e rodar com java puro (ojdbc11 precisa estar no classpath)
mvn package
# (Windows sem Maven: use .\rodar-backend.ps1)
java -cp target/classes:lib/json-20250107.jar:lib/ojdbc11-23.6.0.24.10.jar api.ServidorApi
```

O servidor sobe em `http://localhost:8080`. Teste rápido:

```bash
curl http://localhost:8080/api/health
```

> **Nota sobre compilação sem rede:** o código só usa `java.sql.*`
> (interfaces padrão do JDK) no ponto de conexão — por isso `javac`
> compila mesmo sem o `ojdbc11.jar` disponível. Só a **execução real**
> (abrir conexão com o Oracle) exige o driver no classpath, que o Maven
> baixa automaticamente quando há acesso à internet.



## Validação de evidência por IA (Groq)

- A **foto de evidência é obrigatória** em `POST /api/registros` (sem ela: HTTP 400 e nada é registrado/pontuado).
- A IA é chamada **uma vez** por envio. Resultado: `VALIDADO` (confiança ≥ 0,7 → pontos creditados), `REJEITADO` (não comprova a ação → 0 ponto) ou `PENDENTE` (IA indisponível ou em dúvida → revisão humana, 0 ponto).
- Variáveis de ambiente: `GROQ_API_KEY` (obrigatória para a IA funcionar) e `GROQ_MODELO` (opcional, padrão `qwen/qwen3.8-27b`).
- PowerShell: `$env:GROQ_API_KEY = "sua_chave"` antes de rodar `.\rodar-backend.ps1`. Railway: crie a variável `GROQ_API_KEY` no serviço.
- Sem a chave, o servidor funciona e as evidências ficam PENDENTES (a mensagem do veredito explica o motivo).

## Rotas da API

| Método | Rota | Função |
|---|---|---|
| GET | `/api/health` | verificação de que o servidor está no ar |
| POST | `/api/usuarios` | cadastro de usuário |
| GET | `/api/usuarios/ranking` | ranking por pontos |
| POST | `/api/login` | autenticação |
| GET | `/api/atividades` | lista de ações sustentáveis |
| POST | `/api/registros` | registra execução de uma ação (foto de evidência obrigatória, validada pela IA) |
| GET/POST/DELETE | `/api/desafios-agendados` | agenda/lista/cancela prazos de desafio |
| GET | `/api/historico` | histórico de ações concluídas de um usuário |
| PUT | `/api/perfil/{id}` | atualiza perfil/avatar |
| GET | `/api/ods` | os 17 ODS oficiais |
| GET | `/api/conquistas` | catálogo de conquistas / desbloqueadas por usuário |
| GET/POST | `/api/campeonatos` | lista/cria campeonatos |
| POST | `/api/campeonatos/inscrever` | inscreve usuário num campeonato |

Todas as respostas são JSON; erros seguem o formato `{"erro": "mensagem"}`
com o status HTTP apropriado (400/403/404/500).

## Executando os scripts SQL

Rode os arquivos de `database/` **na ordem numérica** (01 a 07) em um
cliente Oracle (SQL Developer, DBeaver etc.) conectado com as mesmas
credenciais usadas em `ConexaoOracleProjeto.java`. O arquivo
`07_consultas_sql.sql` contém as consultas de verificação/demonstração
exigidas pela disciplina — capture prints da execução de cada uma como
evidência (ver `Checklist_Evidencias_Execucao_SQL.pdf` na pasta de
documentação).

## Modelagem (Oracle SQL Developer Data Modeler)

O arquivo `ddl_para_data_modeler.sql` (entregue separadamente) contém o
DDL consolidado das 14 tabelas, pronto para importação direta no Data
Modeler — ver o guia `Passo_a_Passo_Data_Modeler_Marcelo.pdf`.

## Limitações conhecidas

- Não há suíte de testes automatizados (JUnit); há apenas classes de
  teste manual (`teste/Main.java`, `teste/TesteValidacaoIA.java`),
  executadas via `main()`.
- Evidências de execução real dos scripts SQL contra o Oracle da FIAP e o
  deploy do back-end (ex.: Railway) ainda dependem da equipe rodar
  localmente — não é possível a partir deste ambiente de geração de
  documentação.
