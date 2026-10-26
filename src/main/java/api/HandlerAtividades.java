package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.AcaoSustentavelDAO;
import model.AcaoSustentavel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

// Rota: GET /api/atividades
public class HandlerAtividades implements HttpHandler {

    private final AcaoSustentavelDAO acaoDAO = new AcaoSustentavelDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();

        if ("OPTIONS".equals(metodo)) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        if (!"GET".equals(metodo)) {
            RespostaHttp.enviarErro(troca, 404, "Use GET /api/atividades");
            return;
        }

        try {
            List<AcaoSustentavel> atividades = acaoDAO.listarTodas();
            JSONArray lista = new JSONArray();

            for (AcaoSustentavel acao : atividades) {
                lista.put(new JSONObject()
                        .put("id", acao.getSlug())
                        .put("idInterno", acao.getId())
                        .put("nome", acao.getNome())
                        .put("objetivoSustentavel", acao.getObjetivoSustentavel())
                        .put("pontuacaoBase", acao.getPontuacaoBase())
                        .put("nivelDificuldade", acao.getNivelDificuldade())
                        .put("descricao", acao.getDescricao())
                        .put("pontuacaoCalculada", acao.calcularPontuacao()));
            }

            RespostaHttp.enviarJson(troca, 200, new JSONObject().put("atividades", lista));
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }
}
