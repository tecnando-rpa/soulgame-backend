package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.ConquistaDAO;
import model.Conquista;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Rotas:
// GET /api/conquistas                   -> catalogo completo (painel de conquistas)
// GET /api/conquistas?usuarioId=1       -> so as que esse usuario ja desbloqueou
public class HandlerConquistas implements HttpHandler {

    private final ConquistaDAO conquistaDAO = new ConquistaDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        if ("OPTIONS".equals(troca.getRequestMethod())) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        if (!"GET".equals(troca.getRequestMethod())) {
            RespostaHttp.enviarErro(troca, 404, "Use GET /api/conquistas");
            return;
        }

        try {
            Integer usuarioId = lerParametroUsuarioId(troca);

            List<Conquista> lista = usuarioId != null
                    ? conquistaDAO.listarDesbloqueadasPorUsuario(usuarioId)
                    : conquistaDAO.listarTodas();

            JSONArray array = new JSONArray();
            for (Conquista conquista : lista) {
                array.put(new JSONObject()
                        .put("id", conquista.getId())
                        .put("nome", conquista.getNome())
                        .put("descricao", conquista.getDescricao())
                        .put("tipoCriterio", conquista.getTipoCriterio())
                        .put("valorCriterio", conquista.getValorCriterio())
                        .put("icone", conquista.getIcone()));
            }

            RespostaHttp.enviarJson(troca, 200, new JSONObject().put("conquistas", array));
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }

    private Integer lerParametroUsuarioId(HttpExchange troca) {
        String query = troca.getRequestURI().getQuery();
        if (query == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("usuarioId=(\\d+)").matcher(query);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
    }
}
