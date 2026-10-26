package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.AcaoSustentavelDAO;
import dao.RegistroAcaoDAO;
import model.AcaoSustentavel;
import model.RegistroAcao;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Rota: GET /api/historico?usuarioId=1
// Devolve as atividades concluídas (status VALIDADO) de um usuário,
// derivadas de TB_REGISTRO_ACAO — não é uma lista separada e sim a
// fonte real da verdade (ver RegistroAcaoDAO.listarConcluidasPorUsuario).
public class HandlerHistorico implements HttpHandler {

    private final RegistroAcaoDAO registroAcaoDAO = new RegistroAcaoDAO();
    private final AcaoSustentavelDAO acaoSustentavelDAO = new AcaoSustentavelDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();

        if ("OPTIONS".equals(metodo)) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        if (!"GET".equals(metodo)) {
            RespostaHttp.enviarErro(troca, 404, "Use GET /api/historico?usuarioId=<numero>");
            return;
        }

        try {
            int usuarioId = lerParametroInt(troca, "usuarioId");

            if (usuarioId <= 0) {
                RespostaHttp.enviarErro(troca, 400, "Informe ?usuarioId=<numero> na URL.");
                return;
            }

            List<RegistroAcao> registros = registroAcaoDAO.listarConcluidasPorUsuario(usuarioId);
            JSONArray lista = new JSONArray();

            for (RegistroAcao registro : registros) {
                AcaoSustentavel acao = acaoSustentavelDAO.buscarPorId(registro.getAcaoId());

                lista.put(new JSONObject()
                        .put("registroId", registro.getId())
                        .put("acaoSlug", acao != null ? acao.getSlug() : null)
                        .put("nomeAcao", acao != null ? acao.getNome() : "Atividade removida")
                        .put("pontosObtidos", registro.getPontosObtidos())
                        .put("dataHora", registro.getDataHoraRegistro().toString()));
            }

            RespostaHttp.enviarJson(troca, 200, new JSONObject().put("historico", lista));
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }

    private int lerParametroInt(HttpExchange troca, String nomeParametro) {
        String query = troca.getRequestURI().getQuery();

        if (query == null) {
            return -1;
        }

        Matcher matcher = Pattern.compile(nomeParametro + "=(\\d+)").matcher(query);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
    }
}
