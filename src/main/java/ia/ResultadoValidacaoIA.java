package ia;

// Resultado estruturado da análise da IA sobre uma evidência (foto/documento).
// "confianca" vai de 0.0 a 1.0 — usado para decidir se a validação é automática
// ou se precisa de revisão humana (ex: confianca < 0.7 -> manda pra um parceiro
// revisar manualmente, como já modelado em TB_VALIDACAO).
public class ResultadoValidacaoIA {
    private final boolean valido;
    private final double confianca;
    private final String motivo;
    // true quando a IA NAO conseguiu avaliar (chave ausente, API fora do ar,
    // limite de uso, resposta ilegivel...). Nao e culpa do usuario: nesse caso
    // o registro fica PENDENTE para revisao manual, em vez de ser REJEITADO.
    private final boolean indisponivel;

    public ResultadoValidacaoIA(boolean valido, double confianca, String motivo) {
        this(valido, confianca, motivo, false);
    }

    public ResultadoValidacaoIA(boolean valido, double confianca, String motivo, boolean indisponivel) {
        this.valido = valido;
        this.confianca = confianca;
        this.motivo = motivo;
        this.indisponivel = indisponivel;
    }

    public static ResultadoValidacaoIA indisponivel(String motivo) {
        return new ResultadoValidacaoIA(false, 0.0, motivo, true);
    }

    public boolean isIndisponivel() {
        return indisponivel;
    }

    public boolean isValido() {
        return valido;
    }

    public double getConfianca() {
        return confianca;
    }

    public String getMotivo() {
        return motivo;
    }

    // Regra de negócio: só considera aprovação automática com confiança alta.
    // Abaixo disso, o registro deve ficar PENDENTE para um parceiro validar
    // manualmente (ver TB_VALIDACAO / ValidacaoDAO).
    public boolean podeAprovarAutomaticamente() {
        return valido && !indisponivel && confianca >= 0.7;
    }

    @Override
    public String toString() {
        return "ResultadoValidacaoIA{valido=" + valido + ", confianca=" + confianca + ", motivo='" + motivo + "', indisponivel=" + indisponivel + "}";
    }
}
