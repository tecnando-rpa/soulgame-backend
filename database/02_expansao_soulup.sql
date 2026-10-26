-- =====================================================================
-- EXPANSÃO DO SCHEMA — alinhar TB_USUARIO com o modelo completo do
-- SoulUp MVP (7 tipos de conta, avatar, indicação, nível, streak).
-- Rodar DEPOIS de script_sprint3_tetramerge.sql (schema base).
-- Usa ALTER TABLE (não recria as tabelas) para preservar os dados que
-- já existirem da Sprint anterior.
-- =====================================================================

-- CPF e bairro passam a ser opcionais: o cadastro do SoulUp não coleta
-- esses dados (o Sprint 3 original coletava, mas o produto real não).
ALTER TABLE TB_USUARIO MODIFY (cpf_usuario NULL);
ALTER TABLE TB_USUARIO MODIFY (bairro_usuario NULL);

ALTER TABLE TB_USUARIO ADD (
    telefone_usuario             VARCHAR2(20),
    data_nascimento_usuario      DATE,
    estado_usuario                VARCHAR2(2),

    tipo_usuario                  VARCHAR2(20) DEFAULT 'soulgamer' NOT NULL,

    nivel_usuario                 INTEGER DEFAULT 1 NOT NULL,
    streak_dias_usuario           INTEGER DEFAULT 0 NOT NULL,

    -- Programa de indicação: só contas "multiplicadoras" têm codigo próprio.
    codigo_indicacao_usuario      VARCHAR2(20),
    indicado_por_codigo_usuario   VARCHAR2(20),

    -- Avatar: OU um personagem pronto, OU foto própria (base64 em CLOB).
    avatar_tipo_usuario           VARCHAR2(20) DEFAULT 'personagem' NOT NULL,
    avatar_personagem_id_usuario  VARCHAR2(50),
    avatar_foto_usuario           CLOB
);

ALTER TABLE TB_USUARIO ADD CONSTRAINT CK_USUARIO_TIPO
    CHECK (tipo_usuario IN (
        'soulgamer', 'soulfluencer', 'soulhealth', 'soulsport',
        'soulbrain', 'educasoul', 'ecosoul', 'admin'
    ));

ALTER TABLE TB_USUARIO ADD CONSTRAINT CK_USUARIO_AVATAR_TIPO
    CHECK (avatar_tipo_usuario IN ('personagem', 'foto'));

ALTER TABLE TB_USUARIO ADD CONSTRAINT CK_USUARIO_NIVEL
    CHECK (nivel_usuario >= 1);

ALTER TABLE TB_USUARIO ADD CONSTRAINT CK_USUARIO_STREAK
    CHECK (streak_dias_usuario >= 0);

ALTER TABLE TB_USUARIO ADD CONSTRAINT UK_USUARIO_CODIGO_INDICACAO
    UNIQUE (codigo_indicacao_usuario);

-- =====================================================================
-- TB_DESAFIO_AGENDADO — prazo que o usuário escolheu para uma atividade
-- que "iniciou" na trilha, mas ainda não concluiu. Não confundir com
-- TB_REGISTRO_ACAO (que só existe quando a atividade já foi de fato
-- realizada e enviada para validação).
-- =====================================================================
CREATE TABLE TB_DESAFIO_AGENDADO (
    id_desafio_agendado  INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    id_usuario           INTEGER NOT NULL,
    id_acao              INTEGER NOT NULL,
    prazo                DATE    NOT NULL,
    CONSTRAINT PK_TB_DESAFIO_AGENDADO PRIMARY KEY (id_desafio_agendado),
    CONSTRAINT FK_DESAF_AGEND_USUARIO FOREIGN KEY (id_usuario) REFERENCES TB_USUARIO (id_usuario),
    CONSTRAINT FK_DESAF_AGEND_ACAO FOREIGN KEY (id_acao) REFERENCES TB_ACAO_SUSTENTAVEL (id_acao),
    CONSTRAINT UK_DESAF_AGEND_USUARIO_ACAO UNIQUE (id_usuario, id_acao)
);

COMMIT;

-- =====================================================================
-- Alinha TB_ACAO_SUSTENTAVEL com as 7 atividades reais do SoulUp (o
-- front-end usa um "slug" em texto como id, não o id_acao numerico do
-- Oracle — ex: "doacao-sangue"). Adiciona a coluna e resseeda os dados.
-- =====================================================================

ALTER TABLE TB_ACAO_SUSTENTAVEL ADD (slug_acao VARCHAR2(50));
ALTER TABLE TB_ACAO_SUSTENTAVEL ADD CONSTRAINT UK_ACAO_SLUG UNIQUE (slug_acao);

-- Limpa os dados de exemplo do Sprint anterior (nenhum registro real
-- deveria referenciar esses ids ainda, já que são de teste).
DELETE FROM TB_ACAO_SUSTENTAVEL;

INSERT INTO TB_ACAO_SUSTENTAVEL (slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao)
VALUES ('doacao-sangue', 'Doação de Sangue', 'Saude e bem-estar', 500, 'ALTA', 'Doe sangue e ajude a salvar vidas.');

INSERT INTO TB_ACAO_SUSTENTAVEL (slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao)
VALUES ('reciclagem', 'Doação para Cooperativas de Reciclagem', 'Consumo responsavel', 100, 'MEDIA', 'Destine materiais reciclaveis a cooperativas parceiras.');

INSERT INTO TB_ACAO_SUSTENTAVEL (slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao)
VALUES ('cesta-basica', 'Kit Cestas Basicas', 'Fome zero', 150, 'MEDIA', 'Contribua com kits de cestas basicas para familias.');

INSERT INTO TB_ACAO_SUSTENTAVEL (slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao)
VALUES ('brecho-beneficente', 'Doação para Brechós Beneficentes', 'Redução das desigualdades', 80, 'BAIXA', 'Doe roupas e itens para brechos beneficentes.');

INSERT INTO TB_ACAO_SUSTENTAVEL (slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao)
VALUES ('ancat', 'Contribuição para ANCAT', 'Consumo responsavel', 120, 'MEDIA', 'Apoie a Associação Nacional dos Catadores de Materiais Reciclaveis.');

INSERT INTO TB_ACAO_SUSTENTAVEL (slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao)
VALUES ('casa-marta-maria', 'Doação de Kit Cestas para Casa Marta e Maria', 'Fome zero', 150, 'MEDIA', 'Contribua com cestas para a instituição Casa Marta e Maria.');

INSERT INTO TB_ACAO_SUSTENTAVEL (slug_acao, nome_acao, objetivo_sustentavel, pontuacao_base, nivel_dificuldade, descricao_acao)
VALUES ('esporte-grupo', 'Atividade Esportiva em Grupo', 'Saude e bem-estar', 90, 'BAIXA', 'Participe de um evento esportivo coletivo (pedalada, yoga, corrida etc).');

COMMIT;
