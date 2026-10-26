package service;

import ia.ResultadoValidacaoIA;
import ia.ValidacaoEvidenciaIA;
import model.AcaoSustentavel;
import model.RegistroAcao;
import model.Usuario;

public class SistemaGamificacao {

    public RegistroAcao registrarAcao(Usuario usuario, AcaoSustentavel acao) {
        if (usuario == null || acao == null || !usuario.validarCadastro() || !acao.validarAcao()) {
            return null;
        }

        int pontos = acao.calcularPontuacao();
        usuario.adicionarPontos(pontos);

        return new RegistroAcao(
                acao.getId(),
                usuario.getId(),
                "PENDENTE",
                pontos
        );
    }

    /**
     * Registra a ação a partir do veredito da IA sobre a evidência enviada.
     * Os pontos SÓ são creditados quando o registro nasce VALIDADO:
     *  - VALIDADO : IA aprovou com confiança >= 0.7 (pontos creditados)
     *  - REJEITADO: IA avaliou e a evidência não corresponde à ação (0 ponto)
     *  - PENDENTE : IA indisponível ou confiança baixa; fica para revisão
     *               manual de um parceiro validador (0 ponto até aprovar)
     */
    public RegistroAcao registrarAcaoComVeredito(
            Usuario usuario, AcaoSustentavel acao, ResultadoValidacaoIA resultadoIA
    ) {
        if (usuario == null || acao == null || resultadoIA == null
                || !usuario.validarCadastro() || !acao.validarAcao()) {
            return null;
        }

        String status;
        if (resultadoIA.podeAprovarAutomaticamente()) {
            status = "VALIDADO";
        } else if (!resultadoIA.isIndisponivel() && !resultadoIA.isValido()) {
            status = "REJEITADO";
        } else {
            status = "PENDENTE";
        }

        int pontos = 0;
        if ("VALIDADO".equals(status)) {
            pontos = acao.calcularPontuacao();
            usuario.adicionarPontos(pontos);
        }

        return new RegistroAcao(acao.getId(), usuario.getId(), status, pontos);
    }

    /** Valida a evidência com a IA (uma única chamada) e já registra a ação. */
    public RegistroAcao registrarAcaoComValidacaoIA(
            Usuario usuario, AcaoSustentavel acao, String caminhoImagemEvidencia, String tipoEvidencia
    ) {
        if (usuario == null || acao == null) {
            return null;
        }

        ResultadoValidacaoIA resultadoIA = ValidacaoEvidenciaIA.validar(
                caminhoImagemEvidencia, tipoEvidencia, acao.getNome()
        );

        System.out.println("Veredito da IA: " + resultadoIA);
        return registrarAcaoComVeredito(usuario, acao, resultadoIA);
    }
}
