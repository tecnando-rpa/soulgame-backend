package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// Calcula a "impressão digital" (hash SHA-256) de uma imagem —
// usada pra detectar se a mesma foto já foi enviada antes, sem
// precisar guardar nem comparar o arquivo inteiro.
public final class HashImagem {

    private HashImagem() {
    }

    public static String calcularSha256(byte[] bytesImagem) {
        try {
            MessageDigest digestor = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digestor.digest(bytesImagem);
            return converterParaHexadecimal(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 é padrão em qualquer JVM — isso nunca deveria acontecer,
            // mas se acontecer, não trava a validação: só desativa a
            // checagem de duplicidade para esse envio específico.
            System.out.println("Erro ao calcular hash da imagem: " + e.getMessage());
            return null;
        }
    }

    private static String converterParaHexadecimal(byte[] bytes) {
        StringBuilder construtor = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            construtor.append(String.format("%02x", b));
        }
        return construtor.toString();
    }
}
