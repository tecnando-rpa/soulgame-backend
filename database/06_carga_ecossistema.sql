-- =====================================================================
-- CARGA DE DADOS — ECOSSISTEMA ODS (conquistas, campeonatos, desafios
-- agendados) + reforço da carga original.
--
-- Motivo deste arquivo: quando o ecossistema ODS foi criado (05), as
-- tabelas novas (TB_DESAFIO_AGENDADO, TB_CAMPEONATO,
-- TB_CAMPEONATO_PARTICIPANTE, TB_USUARIO_CONQUISTA) ficaram sem carga de
-- exemplo, e TB_VALIDACAO tinha só 4 registros (o mínimo pedido pela
-- Sprint 4 é 5 por tabela principal). Este script fecha as duas lacunas.
--
-- Pré-requisitos, NESTA ORDEM: 01, 02, 03, 04, 05 (todos já executados).
-- Usa subqueries por chave natural (email, nome, slug) em todo lugar —
-- não depende da ordem exata de geração dos IDs.
-- =====================================================================

-- ===================== TB_VALIDACAO (+1 registro, fecha em 5) =====================
-- A cesta básica do Bruno já estava VALIDADA em TB_REGISTRO_ACAO mas
-- nunca tinha ganho o registro de validação correspondente — inconsistência
-- que esta carga aproveita para corrigir.
INSERT INTO TB_VALIDACAO (id_parceiro, id_registro, resultado_validacao, observacao_validacao)
VALUES (
    (SELECT id_parceiro FROM TB_PARCEIRO WHERE nome_parceiro = 'Instituto Planeta Novo'),
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'bruno.almeida@email.com' AND a.slug_acao = 'cesta-basica'),
    'APROVADO', 'Kit de cesta basica entregue conforme protocolo'
);

-- ===================== TB_DESAFIO_AGENDADO (5 registros) =====================
INSERT INTO TB_DESAFIO_AGENDADO (id_usuario, id_acao, prazo)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'ana.costa@email.com'),
    (SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'cesta-basica'),
    SYSDATE + 7
);

INSERT INTO TB_DESAFIO_AGENDADO (id_usuario, id_acao, prazo)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'bruno.almeida@email.com'),
    (SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'doacao-sangue'),
    SYSDATE + 10
);

INSERT INTO TB_DESAFIO_AGENDADO (id_usuario, id_acao, prazo)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'carla.souza@email.com'),
    (SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'reciclagem'),
    SYSDATE + 5
);

INSERT INTO TB_DESAFIO_AGENDADO (id_usuario, id_acao, prazo)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'diego.ferreira@email.com'),
    (SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'esporte-grupo'),
    SYSDATE + 14
);

INSERT INTO TB_DESAFIO_AGENDADO (id_usuario, id_acao, prazo)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
    (SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'brecho-beneficente'),
    SYSDATE + 3
);

-- ===================== TB_CAMPEONATO (5 registros, todos os escopos/status) =====================
INSERT INTO TB_CAMPEONATO (nome_campeonato, escopo, estado_campeonato, id_ods_relacionado, data_inicio, data_fim, premiacao, id_usuario_criador, status_campeonato)
VALUES (
    'Campeonato Reciclagem Vila Mariana', 'REGIONAL', 'SP',
    (SELECT id_ods FROM TB_ODS WHERE nome_ods = 'Consumo e Producao Responsaveis'),
    SYSDATE - 5, SYSDATE + 10, '200 pontos de bonus + camiseta SoulUp',
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
    'EM_ANDAMENTO'
);

INSERT INTO TB_CAMPEONATO (nome_campeonato, escopo, estado_campeonato, id_ods_relacionado, data_inicio, data_fim, premiacao, id_usuario_criador, status_campeonato)
VALUES (
    'Campeonato Doacao de Sangue SP', 'ESTADUAL', 'SP',
    (SELECT id_ods FROM TB_ODS WHERE nome_ods = 'Saude e Bem-Estar'),
    SYSDATE + 5, SYSDATE + 35, '500 pontos de bonus + kit SoulUp',
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'ana.costa@email.com'),
    'AGENDADO'
);

INSERT INTO TB_CAMPEONATO (nome_campeonato, escopo, estado_campeonato, id_ods_relacionado, data_inicio, data_fim, premiacao, id_usuario_criador, status_campeonato)
VALUES (
    'Campeonato Nacional Sustentabilidade', 'NACIONAL', NULL, NULL,
    SYSDATE - 15, SYSDATE + 45, '1000 pontos de bonus + viagem simbolica',
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
    'EM_ANDAMENTO'
);

INSERT INTO TB_CAMPEONATO (nome_campeonato, escopo, estado_campeonato, id_ods_relacionado, data_inicio, data_fim, premiacao, id_usuario_criador, status_campeonato)
VALUES (
    'Campeonato Cestas Basicas Guarulhos', 'REGIONAL', 'SP',
    (SELECT id_ods FROM TB_ODS WHERE nome_ods = 'Fome Zero e Agricultura Sustentavel'),
    SYSDATE - 60, SYSDATE - 20, '150 pontos de bonus',
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'bruno.almeida@email.com'),
    'ENCERRADO'
);

INSERT INTO TB_CAMPEONATO (nome_campeonato, escopo, estado_campeonato, id_ods_relacionado, data_inicio, data_fim, premiacao, id_usuario_criador, status_campeonato)
VALUES (
    'Campeonato Esporte em Grupo SP', 'ESTADUAL', 'SP',
    (SELECT id_ods FROM TB_ODS WHERE nome_ods = 'Saude e Bem-Estar'),
    SYSDATE + 20, SYSDATE + 50, '300 pontos de bonus',
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'carla.souza@email.com'),
    'AGENDADO'
);

-- ===================== TB_CAMPEONATO_PARTICIPANTE (5 registros) =====================
INSERT INTO TB_CAMPEONATO_PARTICIPANTE (id_campeonato, id_usuario, pontos_campeonato)
VALUES (
    (SELECT id_campeonato FROM TB_CAMPEONATO WHERE nome_campeonato = 'Campeonato Reciclagem Vila Mariana'),
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'), 120
);

INSERT INTO TB_CAMPEONATO_PARTICIPANTE (id_campeonato, id_usuario, pontos_campeonato)
VALUES (
    (SELECT id_campeonato FROM TB_CAMPEONATO WHERE nome_campeonato = 'Campeonato Reciclagem Vila Mariana'),
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'ana.costa@email.com'), 90
);

INSERT INTO TB_CAMPEONATO_PARTICIPANTE (id_campeonato, id_usuario, pontos_campeonato)
VALUES (
    (SELECT id_campeonato FROM TB_CAMPEONATO WHERE nome_campeonato = 'Campeonato Cestas Basicas Guarulhos'),
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'bruno.almeida@email.com'), 200
);

INSERT INTO TB_CAMPEONATO_PARTICIPANTE (id_campeonato, id_usuario, pontos_campeonato)
VALUES (
    (SELECT id_campeonato FROM TB_CAMPEONATO WHERE nome_campeonato = 'Campeonato Nacional Sustentabilidade'),
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'carla.souza@email.com'), 50
);

INSERT INTO TB_CAMPEONATO_PARTICIPANTE (id_campeonato, id_usuario, pontos_campeonato)
VALUES (
    (SELECT id_campeonato FROM TB_CAMPEONATO WHERE nome_campeonato = 'Campeonato Doacao de Sangue SP'),
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'diego.ferreira@email.com'), 10
);

-- ===================== TB_USUARIO_CONQUISTA (5 registros) =====================
INSERT INTO TB_USUARIO_CONQUISTA (id_usuario, id_conquista)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
    (SELECT id_conquista FROM TB_CONQUISTA WHERE nome_conquista = 'Primeiro Passo')
);

INSERT INTO TB_USUARIO_CONQUISTA (id_usuario, id_conquista)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'ana.costa@email.com'),
    (SELECT id_conquista FROM TB_CONQUISTA WHERE nome_conquista = 'Primeiro Passo')
);

INSERT INTO TB_USUARIO_CONQUISTA (id_usuario, id_conquista)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'bruno.almeida@email.com'),
    (SELECT id_conquista FROM TB_CONQUISTA WHERE nome_conquista = 'Primeiro Passo')
);

INSERT INTO TB_USUARIO_CONQUISTA (id_usuario, id_conquista)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'diego.ferreira@email.com'),
    (SELECT id_conquista FROM TB_CONQUISTA WHERE nome_conquista = 'Primeiro Passo')
);

INSERT INTO TB_USUARIO_CONQUISTA (id_usuario, id_conquista)
VALUES (
    (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
    (SELECT id_conquista FROM TB_CONQUISTA WHERE nome_conquista = 'Doador Bronze')
);

COMMIT;
