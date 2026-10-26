package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UsuarioDAO;
import model.Usuario;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Rota: PUT /api/perfil/{id}
// Atualiza campos de perfil (nome, telefone, cidade, estado) e/ou avatar
// (avatarTipo + avatarPersonagemId OU avatarFotoBase64). Só atualiza os
// campos que vierem no corpo — os demais mantêm o valor atual.
public class HandlerPerfil implements HttpHandler {

    private static final Pattern PADRAO_ID_NA_URL = Pattern.compile("/api/perfil/(\\d+)");

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();

        if ("OPTIONS".equals(metodo)) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        if (!"PUT".equals(metodo)) {
            RespostaHttp.enviarErro(troca, 404, "Use PUT /api/perfil/{id}");
            return;
        }

        try {
            atualizar(troca);
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }

    private void atualizar(HttpExchange troca) throws IOException {
        Matcher matcher = PADRAO_ID_NA_URL.matcher(troca.getRequestURI().getPath());

        if (!matcher.matches()) {
            RespostaHttp.enviarErro(troca, 400, "URL invalida. Use PUT /api/perfil/{id}, com o id numerico do usuario.");
            return;
        }

        int usuarioId = Integer.parseInt(matcher.group(1));
        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);

        if (usuario == null) {
            RespostaHttp.enviarErro(troca, 404, "Usuario nao encontrado.");
            return;
        }

        JSONObject corpo = RespostaHttp.lerCorpoRequisicao(troca);

        if (corpo.has("nome")) usuario.setNome(corpo.getString("nome"));
        if (corpo.has("telefone")) usuario.setTelefone(corpo.getString("telefone"));
        if (corpo.has("cidade")) usuario.setCidade(corpo.getString("cidade"));
        if (corpo.has("estado")) usuario.setEstado(corpo.getString("estado"));

        if (corpo.has("avatarTipo")) {
            String avatarTipo = corpo.getString("avatarTipo");

            if (!avatarTipo.equals("personagem") && !avatarTipo.equals("foto")) {
                RespostaHttp.enviarErro(troca, 400, "avatarTipo deve ser 'personagem' ou 'foto'.");
                return;
            }

            usuario.setAvatarTipo(avatarTipo);

            if (avatarTipo.equals("personagem") && corpo.has("avatarPersonagemId")) {
                usuario.setAvatarPersonagemId(corpo.getString("avatarPersonagemId"));
            }

            if (avatarTipo.equals("foto") && corpo.has("avatarFotoBase64")) {
                usuario.setAvatarFotoBase64(corpo.getString("avatarFotoBase64"));
            }
        }

        if (!usuario.validarCadastro()) {
            RespostaHttp.enviarErro(troca, 400, "Dados de perfil invalidos apos a atualizacao.");
            return;
        }

        boolean atualizado = usuarioDAO.atualizar(usuario);

        if (!atualizado) {
            RespostaHttp.enviarErro(troca, 500, "Nao foi possivel salvar as alteracoes.");
            return;
        }

        RespostaHttp.enviarJson(troca, 200, HandlerUsuarios.usuarioParaJson(usuario));
    }
}
