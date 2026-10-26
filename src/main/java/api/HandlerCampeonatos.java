package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.CampeonatoDAO;
import dao.CampeonatoParticipanteDAO;
import dao.UsuarioDAO;
import model.Campeonato;
import model.CampeonatoParticipante;
import model.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

// Rotas:
// GET  /api/campeonatos                    -> lista todos
// POST /api/campeonatos                    -> cria (SOMENTE admin)
// POST /api/campeonatos/inscrever          -> inscreve um usuario num campeonato
//
// Corpo de criacao (POST /api/campeonatos):
// { "usuarioAdminId": "1", "nome": "...", "escopo": "ESTADUAL", "estado": "SP",
//   "odsRelacionadoId": 3, "dataInicio": "2026-10-01", "dataFim": "2026-10-31",
//   "premiacao": "..." }
public class HandlerCampeonatos implements HttpHandler {

    private final CampeonatoDAO campeonatoDAO = new CampeonatoDAO();
    private final CampeonatoParticipanteDAO participanteDAO = new CampeonatoParticipanteDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();
        String caminho = troca.getRequestURI().getPath();

        if ("OPTIONS".equals(metodo)) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        try {
            if ("GET".equals(metodo) && caminho.equals("/api/campeonatos")) {
                listar(troca);
            } else if ("POST".equals(metodo) && caminho.equals("/api/campeonatos")) {
                criar(troca);
            } else if ("POST".equals(metodo) && caminho.equals("/api/campeonatos/inscrever")) {
                inscrever(troca);
            } else {
                RespostaHttp.enviarErro(troca, 404, "Rota nao encontrada: " + metodo + " " + caminho);
            }
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }

    private void listar(HttpExchange troca) throws IOException {
        List<Campeonato> lista = campeonatoDAO.listarTodos();
        JSONArray array = new JSONArray();

        for (Campeonato campeonato : lista) {
            array.put(campeonatoParaJson(campeonato));
        }

        RespostaHttp.enviarJson(troca, 200, new JSONObject().put("campeonatos", array));
    }

    private void criar(HttpExchange troca) throws IOException {
        JSONObject corpo = RespostaHttp.lerCorpoRequisicao(troca);

        int usuarioAdminId = parseIntSeguro(corpo.optString("usuarioAdminId", ""));
        Usuario usuarioAdmin = usuarioAdminId > 0 ? usuarioDAO.buscarPorId(usuarioAdminId) : null;

        // Regra de negocio central deste handler: SOMENTE admin cria
        // campeonato. Verificado no back-end, nunca so pela tela.
        if (usuarioAdmin == null || !"admin".equals(usuarioAdmin.getTipo())) {
            RespostaHttp.enviarErro(troca, 403, "Somente usuarios administradores podem criar campeonatos.");
            return;
        }

        try {
            String escopo = corpo.optString("escopo", "");
            String estado = corpo.has("estado") && !corpo.isNull("estado") ? corpo.getString("estado") : null;
            Integer odsRelacionadoId = corpo.has("odsRelacionadoId") && !corpo.isNull("odsRelacionadoId")
                    ? corpo.getInt("odsRelacionadoId") : null;
            LocalDate dataInicio = LocalDate.parse(corpo.optString("dataInicio", ""));
            LocalDate dataFim = LocalDate.parse(corpo.optString("dataFim", ""));
            String premiacao = corpo.optString("premiacao", null);

            Campeonato campeonato = new Campeonato(
                    corpo.optString("nome", ""), escopo, estado, odsRelacionadoId,
                    dataInicio, dataFim, premiacao, usuarioAdminId
            );

            if (!campeonato.validarCampeonato()) {
                RespostaHttp.enviarErro(
                        troca, 400,
                        "Dados invalidos. Confira nome, escopo (REGIONAL/ESTADUAL/NACIONAL), "
                        + "estado (obrigatorio se nao for NACIONAL) e datas (fim depois do inicio)."
                );
                return;
            }

            boolean criado = campeonatoDAO.criar(campeonato);

            if (!criado) {
                RespostaHttp.enviarErro(troca, 500, "Nao foi possivel criar o campeonato.");
                return;
            }

            RespostaHttp.enviarJson(troca, 201, campeonatoParaJson(campeonato));
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 400, "Erro ao interpretar os dados do campeonato: " + e.getMessage());
        }
    }

    private void inscrever(HttpExchange troca) throws IOException {
        JSONObject corpo = RespostaHttp.lerCorpoRequisicao(troca);

        int campeonatoId = parseIntSeguro(corpo.optString("campeonatoId", ""));
        int usuarioId = parseIntSeguro(corpo.optString("usuarioId", ""));

        if (campeonatoId <= 0 || usuarioId <= 0) {
            RespostaHttp.enviarErro(troca, 400, "Informe campeonatoId e usuarioId validos.");
            return;
        }

        boolean inscrito = participanteDAO.inscrever(new CampeonatoParticipante(campeonatoId, usuarioId));

        if (!inscrito) {
            RespostaHttp.enviarErro(troca, 409, "Usuario ja inscrito nesse campeonato, ou dados invalidos.");
            return;
        }

        RespostaHttp.enviarJson(troca, 201, new JSONObject().put("inscrito", true));
    }

    private JSONObject campeonatoParaJson(Campeonato campeonato) {
        return new JSONObject()
                .put("id", campeonato.getId())
                .put("nome", campeonato.getNome())
                .put("escopo", campeonato.getEscopo())
                .put("estado", campeonato.getEstado())
                .put("odsRelacionadoId", campeonato.getOdsRelacionadoId())
                .put("dataInicio", campeonato.getDataInicio().toString())
                .put("dataFim", campeonato.getDataFim().toString())
                .put("premiacao", campeonato.getPremiacao())
                .put("status", campeonato.getStatus())
                .put("emAndamento", campeonato.estaEmAndamento());
    }

    private int parseIntSeguro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
