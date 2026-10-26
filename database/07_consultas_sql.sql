-- =====================================================================
-- CONSULTAS SQL — Sprint 4 (Building Relational Database), 40 pontos.
-- Requisitos mínimos da rubrica: 5 consultas, sendo pelo menos 2 com
-- JOIN, 1 com GROUP BY, 1 com função de agregação (COUNT/SUM/AVG/MIN/MAX)
-- e 1 com filtro WHERE. Entregamos 7 consultas, todas ligadas a situações
-- reais de uso da plataforma SoulGame/SoulUp.
--
-- Pré-requisitos: rodar 01, 02, 03, 04, 05 e 06 antes (schema completo +
-- carga de dados de exemplo).
-- =====================================================================


-- =====================================================================
-- CONSULTA 1 — Ranking geral de usuários por pontuação, com quantas
-- ações cada um já concluiu.
-- Usa: JOIN, GROUP BY, função de agregação (COUNT).
-- Caso de uso real: tela de "Ranking" do SoulGame.
-- =====================================================================
SELECT
    u.nome_usuario,
    u.tipo_usuario,
    u.total_pontos_usuario,
    u.nivel_usuario,
    COUNT(r.id_registro) AS total_acoes_concluidas
FROM TB_USUARIO u
JOIN TB_REGISTRO_ACAO r ON r.id_usuario = u.id_usuario
WHERE r.status_validacao = 'VALIDADO'
GROUP BY u.nome_usuario, u.tipo_usuario, u.total_pontos_usuario, u.nivel_usuario
ORDER BY u.total_pontos_usuario DESC;


-- =====================================================================
-- CONSULTA 2 — Histórico de ações concluídas por um usuário específico,
-- com o nome e os pontos de cada ação.
-- Usa: JOIN, WHERE.
-- Caso de uso real: tela "Meu Histórico" do SoulGame.
-- =====================================================================
SELECT
    u.nome_usuario,
    a.nome_acao,
    a.objetivo_sustentavel,
    r.pontos_obtidos,
    r.status_validacao,
    r.data_hora_registro
FROM TB_REGISTRO_ACAO r
JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
JOIN TB_USUARIO u ON u.id_usuario = r.id_usuario
WHERE u.email_usuario = 'pedro.silva@email.com'
ORDER BY r.data_hora_registro DESC;


-- =====================================================================
-- CONSULTA 3 — Ações sustentáveis mais realizadas, agrupadas por ODS.
-- Usa: JOIN (duplo), GROUP BY, função de agregação (COUNT e SUM).
-- Caso de uso real: tela "Mapa de ODS" e painel administrativo de
-- impacto da plataforma.
-- =====================================================================
SELECT
    o.nome_ods,
    COUNT(r.id_registro) AS total_execucoes,
    SUM(r.pontos_obtidos) AS pontos_totais_gerados
FROM TB_REGISTRO_ACAO r
JOIN TB_ACAO_SUSTENTAVEL a ON a.id_acao = r.id_acao
JOIN TB_ODS o ON o.id_ods = a.id_ods
WHERE r.status_validacao = 'VALIDADO'
GROUP BY o.nome_ods
ORDER BY total_execucoes DESC;


-- =====================================================================
-- CONSULTA 4 — Pontuação total e média concedida por tipo de conta
-- (benefícios/créditos acumulados por perfil de usuário).
-- Usa: GROUP BY, funções de agregação (SUM, AVG, COUNT).
-- Caso de uso real: painel administrativo de FinOps / custo de
-- recompensas por tipo de usuário.
-- =====================================================================
SELECT
    tipo_usuario,
    COUNT(*) AS quantidade_usuarios,
    SUM(total_pontos_usuario) AS pontos_totais_concedidos,
    ROUND(AVG(total_pontos_usuario), 2) AS media_pontos_por_usuario
FROM TB_USUARIO
GROUP BY tipo_usuario
ORDER BY pontos_totais_concedidos DESC;


-- =====================================================================
-- CONSULTA 5 — Ranking de participantes de um campeonato específico.
-- Usa: JOIN (duplo), WHERE.
-- Caso de uso real: tela de campeonatos regionais/estaduais/nacionais.
-- =====================================================================
SELECT
    c.nome_campeonato,
    c.escopo,
    u.nome_usuario,
    cp.pontos_campeonato
FROM TB_CAMPEONATO_PARTICIPANTE cp
JOIN TB_CAMPEONATO c ON c.id_campeonato = cp.id_campeonato
JOIN TB_USUARIO u ON u.id_usuario = cp.id_usuario
WHERE c.nome_campeonato = 'Campeonato Reciclagem Vila Mariana'
ORDER BY cp.pontos_campeonato DESC;


-- =====================================================================
-- CONSULTA 6 — Estatísticas gerais de utilização da plataforma: quantas
-- ações estão em cada status (validado/pendente/rejeitado) e o total de
-- créditos (pontos) já distribuídos.
-- Usa: GROUP BY, função de agregação (COUNT, SUM, MIN, MAX).
-- Caso de uso real: dashboard administrativo / apresentação de
-- resultados para a banca e para a SoulUp.
-- =====================================================================
SELECT
    status_validacao,
    COUNT(*) AS quantidade_registros,
    SUM(pontos_obtidos) AS pontos_envolvidos,
    MIN(pontos_obtidos) AS menor_pontuacao,
    MAX(pontos_obtidos) AS maior_pontuacao
FROM TB_REGISTRO_ACAO
GROUP BY status_validacao
ORDER BY quantidade_registros DESC;


-- =====================================================================
-- CONSULTA 7 — Conquistas desbloqueadas por usuário (engajamento /
-- gamificação), com data de desbloqueio.
-- Usa: JOIN (duplo), WHERE, ORDER BY.
-- Caso de uso real: tela "Painel de Conquistas" do SoulGame.
-- =====================================================================
SELECT
    u.nome_usuario,
    cq.nome_conquista,
    cq.descricao_conquista,
    uc.data_desbloqueio
FROM TB_USUARIO_CONQUISTA uc
JOIN TB_USUARIO u ON u.id_usuario = uc.id_usuario
JOIN TB_CONQUISTA cq ON cq.id_conquista = uc.id_conquista
WHERE u.cidade_usuario = 'Sao Paulo'
ORDER BY uc.data_desbloqueio DESC;
