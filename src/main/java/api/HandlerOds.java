package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.OdsDAO;
import model.Ods;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

// Rota: GET /api/ods
// Devolve os 17 ODS oficiais — usado pra montar o mapa visual das trilhas.
public class HandlerOds implements HttpHandler {

    private final OdsDAO odsDAO = new OdsDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        if ("OPTIONS".equals(troca.getRequestMethod())) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        if (!"GET".equals(troca.getRequestMethod())) {
            RespostaHttp.enviarErro(troca, 404, "Use GET /api/ods");
            return;
        }

        try {
            List<Ods> lista = odsDAO.listarTodos();
            JSONArray array = new JSONArray();

            for (Ods ods : lista) {
                array.put(new JSONObject()
                        .put("id", ods.getId())
                        .put("nome", ods.getNome())
                        .put("cor", ods.getCor())
                        .put("icone", ods.getIcone()));
            }

            RespostaHttp.enviarJson(troca, 200, new JSONObject().put("ods", array));
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }
}
