-- =====================================================================
-- ECOSSISTEMA ODS — trilhas por Objetivo de Desenvolvimento Sustentável,
-- conquistas e campeonatos regionais/estaduais/nacionais criados por admin.
-- Rodar DEPOIS de 01/02/03/04.
-- =====================================================================

-- ===================== TB_ODS (os 17 ODS oficiais da ONU) =====================
CREATE TABLE TB_ODS (
    id_ods       INTEGER       NOT NULL,
    nome_ods     VARCHAR2(100) NOT NULL,
    cor_ods      VARCHAR2(7)   NOT NULL,
    icone_ods    VARCHAR2(50),
    CONSTRAINT PK_TB_ODS PRIMARY KEY (id_ods),
    CONSTRAINT CK_ODS_NUMERO CHECK (id_ods BETWEEN 1 AND 17)
);

INSERT INTO TB_ODS VALUES (1,  'Erradicacao da Pobreza', '#E5243B', 'sem-pobreza');
INSERT INTO TB_ODS VALUES (2,  'Fome Zero e Agricultura Sustentavel', '#DDA63A', 'fome-zero');
INSERT INTO TB_ODS VALUES (3,  'Saude e Bem-Estar', '#4C9F38', 'saude');
INSERT INTO TB_ODS VALUES (4,  'Educacao de Qualidade', '#C5192D', 'educacao');
INSERT INTO TB_ODS VALUES (5,  'Igualdade de Genero', '#FF3A21', 'igualdade-genero');
INSERT INTO TB_ODS VALUES (6,  'Agua Potavel e Saneamento', '#26BDE2', 'agua-limpa');
INSERT INTO TB_ODS VALUES (7,  'Energia Limpa e Acessivel', '#FCC30B', 'energia-limpa');
INSERT INTO TB_ODS VALUES (8,  'Trabalho Decente e Crescimento Economico', '#A21942', 'trabalho-decente');
INSERT INTO TB_ODS VALUES (9,  'Industria, Inovacao e Infraestrutura', '#FD6925', 'industria-inovacao');
INSERT INTO TB_ODS VALUES (10, 'Reducao das Desigualdades', '#DD1367', 'reducao-desigualdades');
INSERT INTO TB_ODS VALUES (11, 'Cidades e Comunidades Sustentaveis', '#FD9D24', 'cidades-sustentaveis');
INSERT INTO TB_ODS VALUES (12, 'Consumo e Producao Responsaveis', '#BF8B2E', 'consumo-responsavel');
INSERT INTO TB_ODS VALUES (13, 'Acao Contra a Mudanca Global do Clima', '#3F7E44', 'acao-climatica');
INSERT INTO TB_ODS VALUES (14, 'Vida na Agua', '#0A97D9', 'vida-agua');
INSERT INTO TB_ODS VALUES (15, 'Vida Terrestre', '#56C02B', 'vida-terrestre');
INSERT INTO TB_ODS VALUES (16, 'Paz, Justica e Instituicoes Eficazes', '#00689D', 'paz-justica');
INSERT INTO TB_ODS VALUES (17, 'Parcerias e Meios de Implementacao', '#19486A', 'parcerias');

-- ===================== Liga TB_ACAO_SUSTENTAVEL a um ODS real =====================
ALTER TABLE TB_ACAO_SUSTENTAVEL ADD (id_ods INTEGER);
ALTER TABLE TB_ACAO_SUSTENTAVEL ADD CONSTRAINT FK_ACAO_ODS FOREIGN KEY (id_ods) REFERENCES TB_ODS (id_ods);

-- Liga as 7 atividades existentes ao ODS correspondente (mesmo texto
-- que ja estava em objetivo_sustentavel, so que formalizado por FK agora).
UPDATE TB_ACAO_SUSTENTAVEL SET id_ods = 3  WHERE slug_acao = 'doacao-sangue';
UPDATE TB_ACAO_SUSTENTAVEL SET id_ods = 12 WHERE slug_acao = 'reciclagem';
UPDATE TB_ACAO_SUSTENTAVEL SET id_ods = 2  WHERE slug_acao = 'cesta-basica';
UPDATE TB_ACAO_SUSTENTAVEL SET id_ods = 10 WHERE slug_acao = 'brecho-beneficente';
UPDATE TB_ACAO_SUSTENTAVEL SET id_ods = 12 WHERE slug_acao = 'ancat';
UPDATE TB_ACAO_SUSTENTAVEL SET id_ods = 2  WHERE slug_acao = 'casa-marta-maria';
UPDATE TB_ACAO_SUSTENTAVEL SET id_ods = 3  WHERE slug_acao = 'esporte-grupo';

-- ===================== TB_CONQUISTA (catalogo de conquistas) =====================
CREATE TABLE TB_CONQUISTA (
    id_conquista       INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    nome_conquista     VARCHAR2(100) NOT NULL,
    descricao_conquista VARCHAR2(300) NOT NULL,
    tipo_criterio      VARCHAR2(30)  NOT NULL,
    valor_criterio     INTEGER       NOT NULL,
    id_ods_relacionado INTEGER,
    slug_acao_relacionada VARCHAR2(50),
    icone_conquista    VARCHAR2(50),
    CONSTRAINT PK_TB_CONQUISTA PRIMARY KEY (id_conquista),
    CONSTRAINT FK_CONQUISTA_ODS FOREIGN KEY (id_ods_relacionado) REFERENCES TB_ODS (id_ods),
    CONSTRAINT CK_CONQUISTA_TIPO CHECK (tipo_criterio IN
        ('CONTAGEM_TOTAL', 'STREAK_DIAS', 'ODS_DIFERENTES', 'CAMPEAO_CAMPEONATO'))
);

-- slug_acao_relacionada, quando presente, restringe a contagem a UMA
-- atividade especifica (ex: so "doacao-sangue"), nao o ODS inteiro.
-- Sem isso, "Doador Bronze" contaria qualquer acao de Saude (ODS 3),
-- inclusive esporte em grupo — o que nao faz sentido pro nome da conquista.

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Primeiro Passo', 'Concluiu a primeira acao sustentavel', 'CONTAGEM_TOTAL', 1, NULL, NULL, 'primeiro-passo');

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Doador Bronze', '3 doacoes de sangue realizadas', 'CONTAGEM_TOTAL', 3, 3, 'doacao-sangue', 'doador-bronze');

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Doador Prata', '6 doacoes de sangue realizadas', 'CONTAGEM_TOTAL', 6, 3, 'doacao-sangue', 'doador-prata');

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Doador Ouro', '10 doacoes de sangue realizadas', 'CONTAGEM_TOTAL', 10, 3, 'doacao-sangue', 'doador-ouro');

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Sequencia de 7 dias', 'Streak de 7 dias seguidos com alguma acao', 'STREAK_DIAS', 7, NULL, NULL, 'streak-7');

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Sequencia de 30 dias', 'Streak de 30 dias seguidos com alguma acao', 'STREAK_DIAS', 30, NULL, NULL, 'streak-30');

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Explorador de ODS', 'Concluiu ao menos uma acao em 3 ODS diferentes', 'ODS_DIFERENTES', 3, NULL, NULL, 'explorador');

INSERT INTO TB_CONQUISTA (nome_conquista, descricao_conquista, tipo_criterio, valor_criterio, id_ods_relacionado, slug_acao_relacionada, icone_conquista)
VALUES ('Campeao Regional', 'Venceu um campeonato de escopo regional', 'CAMPEAO_CAMPEONATO', 1, NULL, NULL, 'campeao');

-- ===================== TB_USUARIO_CONQUISTA (quem desbloqueou o que) =====================
CREATE TABLE TB_USUARIO_CONQUISTA (
    id_usuario_conquista INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    id_usuario           INTEGER   NOT NULL,
    id_conquista          INTEGER   NOT NULL,
    data_desbloqueio      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_TB_USUARIO_CONQUISTA PRIMARY KEY (id_usuario_conquista),
    CONSTRAINT FK_USRCONQ_USUARIO FOREIGN KEY (id_usuario) REFERENCES TB_USUARIO (id_usuario),
    CONSTRAINT FK_USRCONQ_CONQUISTA FOREIGN KEY (id_conquista) REFERENCES TB_CONQUISTA (id_conquista),
    CONSTRAINT UK_USRCONQ_PAR UNIQUE (id_usuario, id_conquista)
);

-- ===================== TB_CAMPEONATO (cadastrado por admin) =====================
CREATE TABLE TB_CAMPEONATO (
    id_campeonato      INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    nome_campeonato    VARCHAR2(150) NOT NULL,
    escopo             VARCHAR2(20)  NOT NULL,
    estado_campeonato  VARCHAR2(2),
    id_ods_relacionado INTEGER,
    data_inicio        DATE NOT NULL,
    data_fim           DATE NOT NULL,
    premiacao          VARCHAR2(300),
    id_usuario_criador INTEGER NOT NULL,
    status_campeonato  VARCHAR2(20) DEFAULT 'AGENDADO' NOT NULL,
    CONSTRAINT PK_TB_CAMPEONATO PRIMARY KEY (id_campeonato),
    CONSTRAINT FK_CAMP_ODS FOREIGN KEY (id_ods_relacionado) REFERENCES TB_ODS (id_ods),
    CONSTRAINT FK_CAMP_CRIADOR FOREIGN KEY (id_usuario_criador) REFERENCES TB_USUARIO (id_usuario),
    CONSTRAINT CK_CAMP_ESCOPO CHECK (escopo IN ('REGIONAL', 'ESTADUAL', 'NACIONAL')),
    CONSTRAINT CK_CAMP_STATUS CHECK (status_campeonato IN ('AGENDADO', 'EM_ANDAMENTO', 'ENCERRADO')),
    CONSTRAINT CK_CAMP_DATAS CHECK (data_fim > data_inicio),
    -- Campeonato ESTADUAL/REGIONAL precisa dizer qual estado; NACIONAL nao.
    CONSTRAINT CK_CAMP_ESTADO_ESCOPO CHECK (
        (escopo = 'NACIONAL' AND estado_campeonato IS NULL)
        OR (escopo IN ('REGIONAL', 'ESTADUAL') AND estado_campeonato IS NOT NULL)
    )
);

-- ===================== TB_CAMPEONATO_PARTICIPANTE =====================
CREATE TABLE TB_CAMPEONATO_PARTICIPANTE (
    id_participacao   INTEGER GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1),
    id_campeonato     INTEGER NOT NULL,
    id_usuario        INTEGER NOT NULL,
    pontos_campeonato INTEGER DEFAULT 0 NOT NULL,
    data_inscricao    TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    CONSTRAINT PK_TB_CAMPEONATO_PARTICIPANTE PRIMARY KEY (id_participacao),
    CONSTRAINT FK_CAMPPART_CAMPEONATO FOREIGN KEY (id_campeonato) REFERENCES TB_CAMPEONATO (id_campeonato),
    CONSTRAINT FK_CAMPPART_USUARIO FOREIGN KEY (id_usuario) REFERENCES TB_USUARIO (id_usuario),
    CONSTRAINT UK_CAMPPART_PAR UNIQUE (id_campeonato, id_usuario),
    CONSTRAINT CK_CAMPPART_PONTOS CHECK (pontos_campeonato >= 0)
);

COMMIT;
