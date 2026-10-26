package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.AcaoSustentavelDAO;
import dao.DesafioAgendadoDAO;
import model.AcaoSustentavel;
import model.DesafioAgendado;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Rotas (todas usando o SLUG da atividade, igual ao resto da API):
// POST   /api/desafios-agendados                         -> agenda um prazo
// GET    /api/desafios-agendados?usuarioId=1              -> lista os prazos de um usuario
// DELETE /api/desafios-agendados?usuarioId=1&acaoSlug=x   -> cancela um agendamento
public class HandlerDesafiosAgendados implements HttpHandler {

    private final DesafioAgendadoDAO desafioAgendadoDAO = new DesafioAgendadoDAO();
    private final AcaoSustentavelDAO acaoDAO = new AcaoSustentavelDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();

        if ("OPTIONS".equals(metodo)) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        try {
            switch (metodo) {
                case "POST" -> agendar(troca);
                case "GET" -> listar(troca);
                case "DELETE" -> cancelar(troca);
                default -> RespostaHttp.enviarErro(troca, 404, "Metodo nao suportado: " + metodo);
            }
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }

    private void agendar(HttpExchange troca) throws IOException {
        JSONObject corpo = RespostaHttp.lerCorpoRequisicao(troca);

        int usuarioId = parseIntSeguro(corpo.optString("usuarioId", ""));
        String acaoSlug = corpo.optString("acaoSlug", "");
        String prazoTexto = corpo.optString("prazo", "");

        AcaoSustentavel acao = acaoDAO.buscarPorSlug(acaoSlug);

        if (acao == null) {
            RespostaHttp.enviarErro(troca, 404, "Atividade nao encontrada: " + acaoSlug);
            return;
        }

        LocalDate prazo;
        try {
            prazo = LocalDate.parse(prazoTexto);
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 400, "Prazo invalido. Use o formato AAAA-MM-DD.");
            return;
        }

        DesafioAgendado agendamento = new DesafioAgendado(usuarioId, acao.getId(), prazo);

        if (!agendamento.validarAgendamento()) {
            RespostaHttp.enviarErro(troca, 400, "Dados invalidos: usuarioId invalido, ou o prazo nao pode ser no passado.");
            return;
        }

        boolean criado = desafioAgendadoDAO.criar(agendamento);

        if (!criado) {
            RespostaHttp.enviarErro(troca, 500, "Nao foi possivel salvar o agendamento (talvez ja exista um pra essa dupla usuario+atividade).");
            return;
        }

        RespostaHttp.enviarJson(troca, 201, agendamentoParaJson(agendamento, acaoSlug));
    }

    private void listar(HttpExchange troca) throws IOException {
        int usuarioId = lerParametroInt(troca, "usuarioId");

        if (usuarioId <= 0) {
            RespostaHttp.enviarErro(troca, 400, "Informe ?usuarioId=<numero> na URL.");
            return;
        }

        List<DesafioAgendado> agendamentos = desafioAgendadoDAO.listarPorUsuario(usuarioId);
        JSONArray lista = new JSONArray();

        for (DesafioAgendado agendamento : agendamentos) {
            AcaoSustentavel acao = acaoDAO.buscarPorId(agendamento.getAcaoId());
            lista.put(agendamentoParaJson(agendamento, acao != null ? acao.getSlug() : null));
        }

        RespostaHttp.enviarJson(troca, 200, new JSONObject().put("desafiosAgendados", lista));
    }

    private void cancelar(HttpExchange troca) throws IOException {
        int usuarioId = lerParametroInt(troca, "usuarioId");
        String acaoSlug = lerParametroTexto(troca, "acaoSlug");

        AcaoSustentavel acao = acaoSlug != null ? acaoDAO.buscarPorSlug(acaoSlug) : null;

        if (usuarioId <= 0 || acao == null) {
            RespostaHttp.enviarErro(troca, 400, "Informe ?usuarioId=<numero>&acaoSlug=<slug> na URL.");
            return;
        }

        boolean excluido = desafioAgendadoDAO.excluir(usuarioId, acao.getId());

        if (!excluido) {
            RespostaHttp.enviarErro(troca, 404, "Nenhum agendamento encontrado pra essa dupla usuario+atividade.");
            return;
        }

        RespostaHttp.enviarJson(troca, 200, new JSONObject().put("cancelado", true));
    }

    private int parseIntSeguro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private int lerParametroInt(HttpExchange troca, String nomeParametro) {
        String valor = lerParametroTexto(troca, nomeParametro);
        return valor != null ? parseIntSeguro(valor) : -1;
    }

    private String lerParametroTexto(HttpExchange troca, String nomeParametro) {
        String query = troca.getRequestURI().getQuery();

        if (query == null) {
            return null;
        }

        Matcher matcher = Pattern.compile(nomeParametro + "=([^&]+)").matcher(query);
        return matcher.find() ? matcher.group(1) : null;
    }

    private JSONObject agendamentoParaJson(DesafioAgendado agendamento, String acaoSlug) {
        return new JSONObject()
                .put("id", agendamento.getId())
                .put("usuarioId", agendamento.getUsuarioId())
                .put("acaoSlug", acaoSlug)
                .put("prazo", agendamento.getPrazo().toString())
                .put("vencido", agendamento.estaVencido());
    }
}
