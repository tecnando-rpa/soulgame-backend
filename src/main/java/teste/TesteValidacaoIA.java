package teste;

import ia.ResultadoValidacaoIA;
import ia.ValidacaoEvidenciaIA;

/**
 * Teste isolado da validação por IA — separado do Main.java principal
 * de propósito, porque este aqui depende de rede e da variável de
 * ambiente GROQ_API_KEY configurada. O Main.java principal (CRUD +
 * regras de negócio) continua funcionando sem depender de nada externo.
 *
 * Como rodar:
 * 1. Configure a variável de ambiente GROQ_API_KEY (veja o guia de setup).
 * 2. Troque CAMINHO_DA_IMAGEM abaixo por uma foto real no seu computador.
 * 3. Rode esta classe.
 */
public class TesteValidacaoIA {

    private static final String CAMINHO_DA_IMAGEM = "C:/caminho/para/sua/foto-evidencia.jpg";

    public static void main(String[] args) {
        System.out.println("Testando validacao por IA (Qwen via Groq)...");

        ResultadoValidacaoIA resultado = ValidacaoEvidenciaIA.validar(
                CAMINHO_DA_IMAGEM,
                "FOTO",
                "Reciclagem de Materiais"
        );

        System.out.println("\n=== VEREDITO DA IA ===");
        System.out.println("Valido: " + resultado.isValido());
        System.out.println("Confianca: " + resultado.getConfianca());
        System.out.println("Motivo: " + resultado.getMotivo());
        System.out.println("Aprovacao automatica: " + resultado.podeAprovarAutomaticamente());
    }
}
