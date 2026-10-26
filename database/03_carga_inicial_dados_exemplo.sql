-- =====================================================================
-- CARGA DE DADOS DE EXEMPLO — TM CONSULTING / GAMIFICACAO SUSTENTAVEL
-- Pré-requisitos, NESTA ORDEM:
--   1. 01_schema_base.sql       (cria as 8 tabelas originais)
--   2. 02_expansao_soulup.sql   (expande TB_USUARIO, cria TB_DESAFIO_AGENDADO,
--                                 e resseeda TB_ACAO_SUSTENTAVEL com as 7
--                                 atividades REAIS do SoulUp — não insira
--                                 atividades aqui, já estão seedadas lá)
-- =====================================================================

-- ===================== TB_USUARIO (5 registros) =====================
-- cpf/bairro ficaram opcionais na expansão (o SoulUp não coleta esses
-- dados) — mantidos aqui só como dado de exemplo do Sprint anterior.
INSERT INTO TB_USUARIO (nome_usuario, email_usuario, senha_usuario, telefone_usuario, cidade_usuario, estado_usuario, tipo_usuario, cpf_usuario, bairro_usuario)
VALUES ('Pedro Silva', 'pedro.silva@email.com', 'senha123', '(11) 90000-0001', 'Sao Paulo', 'SP', 'soulgamer', '11122233301', 'Centro');

INSERT INTO TB_USUARIO (nome_usuario, email_usuario, senha_usuario, telefone_usuario, cidade_usuario, estado_usuario, tipo_usuario, cpf_usuario, bairro_usuario)
VALUES ('Ana Costa', 'ana.costa@email.com', 'senha123', '(11) 90000-0002', 'Sao Paulo', 'SP', 'soulfluencer', '11122233302', 'Vila Mariana');

INSERT INTO TB_USUARIO (nome_usuario, email_usuario, senha_usuario, telefone_usuario, cidade_usuario, estado_usuario, tipo_usuario, cpf_usuario, bairro_usuario)
VALUES ('Bruno Almeida', 'bruno.almeida@email.com', 'senha123', '(11) 90000-0003', 'Guarulhos', 'SP', 'soulgamer', '11122233303', 'Centro');

INSERT INTO TB_USUARIO (nome_usuario, email_usuario, senha_usuario, telefone_usuario, cidade_usuario, estado_usuario, tipo_usuario, cpf_usuario, bairro_usuario)
VALUES ('Carla Souza', 'carla.souza@email.com', 'senha123', '(11) 90000-0004', 'Sao Paulo', 'SP', 'soulhealth', '11122233304', 'Pinheiros');

INSERT INTO TB_USUARIO (nome_usuario, email_usuario, senha_usuario, telefone_usuario, cidade_usuario, estado_usuario, tipo_usuario, cpf_usuario, bairro_usuario)
VALUES ('Diego Ferreira', 'diego.ferreira@email.com', 'senha123', '(11) 90000-0005', 'Osasco', 'SP', 'ecosoul', '11122233305', 'Centro');

-- ===================== TB_GRUPO (5 registros) =====================
INSERT INTO TB_GRUPO (nome_grupo, tipo_grupo, cidade_grupo, bairro_grupo, pontos_minimos_entrada)
VALUES ('Reciclagem Vila Mariana', 'ABERTO', 'Sao Paulo', 'Vila Mariana', 0);

INSERT INTO TB_GRUPO (nome_grupo, tipo_grupo, cidade_grupo, bairro_grupo, pontos_minimos_entrada)
VALUES ('Doadores de Sangue SP', 'ABERTO', 'Sao Paulo', 'Centro', 0);

INSERT INTO TB_GRUPO (nome_grupo, tipo_grupo, cidade_grupo, bairro_grupo, pontos_minimos_entrada)
VALUES ('Horta Comunitaria Pinheiros', 'PRIVADO', 'Sao Paulo', 'Pinheiros', 50);

INSERT INTO TB_GRUPO (nome_grupo, tipo_grupo, cidade_grupo, bairro_grupo, pontos_minimos_entrada)
VALUES ('Voluntarios Guarulhos', 'ABERTO', 'Guarulhos', 'Centro', 0);

INSERT INTO TB_GRUPO (nome_grupo, tipo_grupo, cidade_grupo, bairro_grupo, pontos_minimos_entrada)
VALUES ('Elite Sustentavel', 'PRIVADO', 'Osasco', 'Centro', 100);

-- ===================== TB_PARCEIRO (5 registros) =====================
INSERT INTO TB_PARCEIRO (nome_parceiro, tipo_parceiro, cnpj_parceiro, cidade_parceiro, status_parceiro)
VALUES ('EcoPonto Reciclagem LTDA', 'COOPERATIVA', '12345678000191', 'Sao Paulo', 'ATIVO');

INSERT INTO TB_PARCEIRO (nome_parceiro, tipo_parceiro, cnpj_parceiro, cidade_parceiro, status_parceiro)
VALUES ('Hemocentro Municipal', 'INSTITUICAO_SAUDE', '12345678000192', 'Sao Paulo', 'ATIVO');

INSERT INTO TB_PARCEIRO (nome_parceiro, tipo_parceiro, cnpj_parceiro, cidade_parceiro, status_parceiro)
VALUES ('ONG Verde Vida', 'ONG', '12345678000193', 'Guarulhos', 'ATIVO');

INSERT INTO TB_PARCEIRO (nome_parceiro, tipo_parceiro, cnpj_parceiro, cidade_parceiro, status_parceiro)
VALUES ('Cooperativa Reciclar Mais', 'COOPERATIVA', '12345678000194', 'Osasco', 'EM_ANALISE');

INSERT INTO TB_PARCEIRO (nome_parceiro, tipo_parceiro, cnpj_parceiro, cidade_parceiro, status_parceiro)
VALUES ('Instituto Planeta Novo', 'ONG', '12345678000195', 'Sao Paulo', 'ATIVO');

-- ===================== TB_USUARIO_GRUPO (6 registros) =====================
-- Usa subquery pelo e-mail em vez de id fixo, pra não depender da ordem
-- exata de insercao dos usuarios acima.
INSERT INTO TB_USUARIO_GRUPO (id_usuario, id_grupo, pontos_no_grupo, status_participacao)
VALUES ((SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
        (SELECT id_grupo FROM TB_GRUPO WHERE nome_grupo = 'Reciclagem Vila Mariana'), 40, 'ATIVO');

INSERT INTO TB_USUARIO_GRUPO (id_usuario, id_grupo, pontos_no_grupo, status_participacao)
VALUES ((SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'ana.costa@email.com'),
        (SELECT id_grupo FROM TB_GRUPO WHERE nome_grupo = 'Reciclagem Vila Mariana'), 30, 'ATIVO');

INSERT INTO TB_USUARIO_GRUPO (id_usuario, id_grupo, pontos_no_grupo, status_participacao)
VALUES ((SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
        (SELECT id_grupo FROM TB_GRUPO WHERE nome_grupo = 'Doadores de Sangue SP'), 100, 'ATIVO');

INSERT INTO TB_USUARIO_GRUPO (id_usuario, id_grupo, pontos_no_grupo, status_participacao)
VALUES ((SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'bruno.almeida@email.com'),
        (SELECT id_grupo FROM TB_GRUPO WHERE nome_grupo = 'Voluntarios Guarulhos'), 20, 'ATIVO');

INSERT INTO TB_USUARIO_GRUPO (id_usuario, id_grupo, pontos_no_grupo, status_participacao)
VALUES ((SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'carla.souza@email.com'),
        (SELECT id_grupo FROM TB_GRUPO WHERE nome_grupo = 'Horta Comunitaria Pinheiros'), 60, 'ATIVO');

INSERT INTO TB_USUARIO_GRUPO (id_usuario, id_grupo, pontos_no_grupo, status_participacao)
VALUES ((SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'diego.ferreira@email.com'),
        (SELECT id_grupo FROM TB_GRUPO WHERE nome_grupo = 'Elite Sustentavel'), 0, 'SUSPENSO');

-- ===================== TB_REGISTRO_ACAO (6 registros) =====================
-- Usa subquery pelo SLUG da atividade (já seedada em 02_expansao_soulup.sql),
-- em vez de id numerico fixo — mais legivel e nao depende da ordem de insercao.
INSERT INTO TB_REGISTRO_ACAO (id_acao, id_usuario, status_validacao, pontos_obtidos, latitude, longitude)
VALUES ((SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'doacao-sangue'),
        (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
        'VALIDADO', 500, -23.561684, -46.625378);

INSERT INTO TB_REGISTRO_ACAO (id_acao, id_usuario, status_validacao, pontos_obtidos, latitude, longitude)
VALUES ((SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'reciclagem'),
        (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'pedro.silva@email.com'),
        'VALIDADO', 100, -23.561684, -46.625378);

INSERT INTO TB_REGISTRO_ACAO (id_acao, id_usuario, status_validacao, pontos_obtidos, latitude, longitude)
VALUES ((SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'reciclagem'),
        (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'ana.costa@email.com'),
        'VALIDADO', 100, -23.585, -46.638);

INSERT INTO TB_REGISTRO_ACAO (id_acao, id_usuario, status_validacao, pontos_obtidos, latitude, longitude)
VALUES ((SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'esporte-grupo'),
        (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'carla.souza@email.com'),
        'PENDENTE', 90, -23.567, -46.702);

INSERT INTO TB_REGISTRO_ACAO (id_acao, id_usuario, status_validacao, pontos_obtidos, latitude, longitude)
VALUES ((SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'cesta-basica'),
        (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'bruno.almeida@email.com'),
        'VALIDADO', 150, -23.454, -46.533);

INSERT INTO TB_REGISTRO_ACAO (id_acao, id_usuario, status_validacao, pontos_obtidos, latitude, longitude)
VALUES ((SELECT id_acao FROM TB_ACAO_SUSTENTAVEL WHERE slug_acao = 'ancat'),
        (SELECT id_usuario FROM TB_USUARIO WHERE email_usuario = 'diego.ferreira@email.com'),
        'REJEITADO', 0, -23.532, -46.792);

-- ===================== TB_EVIDENCIA (5 registros) =====================
-- Usa subquery pelo par (email do usuario + slug da atividade) pra
-- localizar o registro certo, sem depender de id numerico fixo.
INSERT INTO TB_EVIDENCIA (id_registro, tipo_evidencia, url_arquivo, descricao_evidencia)
VALUES (
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'pedro.silva@email.com' AND a.slug_acao = 'doacao-sangue'),
    'DOCUMENTO', 'https://storage.tmconsulting.com/evidencias/doacao1.pdf', 'Comprovante do hemocentro'
);

INSERT INTO TB_EVIDENCIA (id_registro, tipo_evidencia, url_arquivo, descricao_evidencia)
VALUES (
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'pedro.silva@email.com' AND a.slug_acao = 'reciclagem'),
    'FOTO', 'https://storage.tmconsulting.com/evidencias/reciclagem1.jpg', 'Foto dos materiais separados'
);

INSERT INTO TB_EVIDENCIA (id_registro, tipo_evidencia, url_arquivo, descricao_evidencia)
VALUES (
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'ana.costa@email.com' AND a.slug_acao = 'reciclagem'),
    'FOTO', 'https://storage.tmconsulting.com/evidencias/reciclagem2.jpg', 'Foto da entrega no ponto de coleta'
);

INSERT INTO TB_EVIDENCIA (id_registro, tipo_evidencia, url_arquivo, descricao_evidencia)
VALUES (
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'bruno.almeida@email.com' AND a.slug_acao = 'cesta-basica'),
    'FOTO', 'https://storage.tmconsulting.com/evidencias/cesta1.jpg', 'Foto da entrega das cestas'
);

INSERT INTO TB_EVIDENCIA (id_registro, tipo_evidencia, url_arquivo, descricao_evidencia)
VALUES (
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'diego.ferreira@email.com' AND a.slug_acao = 'ancat'),
    'QR_CODE', 'https://storage.tmconsulting.com/evidencias/ancat1.png', 'QR code de presenca no mutirao'
);

-- ===================== TB_VALIDACAO (4 registros) =====================
INSERT INTO TB_VALIDACAO (id_parceiro, id_registro, resultado_validacao, observacao_validacao)
VALUES (
    (SELECT id_parceiro FROM TB_PARCEIRO WHERE nome_parceiro = 'Hemocentro Municipal'),
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'pedro.silva@email.com' AND a.slug_acao = 'doacao-sangue'),
    'APROVADO', 'Doacao confirmada pelo hemocentro'
);

INSERT INTO TB_VALIDACAO (id_parceiro, id_registro, resultado_validacao, observacao_validacao)
VALUES (
    (SELECT id_parceiro FROM TB_PARCEIRO WHERE nome_parceiro = 'EcoPonto Reciclagem LTDA'),
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'pedro.silva@email.com' AND a.slug_acao = 'reciclagem'),
    'APROVADO', 'Material conferido e pesado corretamente'
);

INSERT INTO TB_VALIDACAO (id_parceiro, id_registro, resultado_validacao, observacao_validacao)
VALUES (
    (SELECT id_parceiro FROM TB_PARCEIRO WHERE nome_parceiro = 'EcoPonto Reciclagem LTDA'),
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'ana.costa@email.com' AND a.slug_acao = 'reciclagem'),
    'APROVADO', 'Material conferido pela cooperativa'
);

INSERT INTO TB_VALIDACAO (id_parceiro, id_registro, resultado_validacao, observacao_validacao)
VALUES (
    (SELECT id_parceiro FROM TB_PARCEIRO WHERE nome_parceiro = 'ONG Verde Vida'),
    (SELECT r.id_registro FROM TB_REGISTRO_ACAO r
     JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
     JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
     WHERE u.email_usuario = 'diego.ferreira@email.com' AND a.slug_acao = 'ancat'),
    'REPROVADO', 'Item entregue fora da especificacao'
);

COMMIT;
