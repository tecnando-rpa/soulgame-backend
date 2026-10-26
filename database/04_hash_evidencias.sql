-- =====================================================================
-- ANTI-DUPLICIDADE DE EVIDÊNCIA — adiciona o hash (impressão digital)
-- de cada imagem enviada, pra detectar a mesma foto sendo reenviada
-- (pelo mesmo usuário ou por outro, tentando "farmar" pontos).
-- Rodar DEPOIS de 01/02/03.
-- =====================================================================

ALTER TABLE TB_EVIDENCIA ADD (
    hash_evidencia VARCHAR2(64)
);

-- UNIQUE permite múltiplos NULL (evidências antigas, sem hash
-- calculado) mas nunca permite dois hashes iguais.
ALTER TABLE TB_EVIDENCIA ADD CONSTRAINT UK_EVIDENCIA_HASH UNIQUE (hash_evidencia);

COMMENT ON COLUMN TB_EVIDENCIA.hash_evidencia IS
    'SHA-256 da imagem em hexadecimal (64 caracteres). Calculado no back-end antes de mandar pra IA — se já existir, bloqueia como duplicata sem nem chamar a IA de novo.';

COMMIT;
