package api;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

// Utilitário pra enviar respostas JSON de forma consistente em todos os
// endpoints, sempre com CORS liberado (o React roda numa porta diferente
// em desenvolvimento) e o Content-Type correto.
public final class RespostaHttp {

    private RespostaHttp() {
    }

    public static void enviarJson(HttpExchange troca, int codigoStatus, JSONObject corpo) throws IOException {
        byte[] bytesResposta = corpo.toString().getBytes(StandardCharsets.UTF_8);

        troca.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        troca.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        troca.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        troca.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        troca.sendResponseHeaders(codigoStatus, bytesResposta.length);

        try (OutputStream corpoResposta = troca.getResponseBody()) {
            corpoResposta.write(bytesResposta);
        }
    }

    public static void enviarErro(HttpExchange troca, int codigoStatus, String mensagem) throws IOException {
        JSONObject erro = new JSONObject().put("erro", mensagem);
        enviarJson(troca, codigoStatus, erro);
    }

    public static JSONObject lerCorpoRequisicao(HttpExchange troca) throws IOException {
        byte[] bytes = troca.getRequestBody().readAllBytes();
        String texto = new String(bytes, StandardCharsets.UTF_8);
        return texto.isBlank() ? new JSONObject() : new JSONObject(texto);
    }
}
