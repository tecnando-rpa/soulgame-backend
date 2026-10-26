package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UsuarioDAO;
import model.Usuario;
import org.json.JSONObject;

import java.io.IOException;

// Rota: POST /api/login
public class HandlerLogin implements HttpHandler {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();

        if ("OPTIONS".equals(metodo)) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        if (!"POST".equals(metodo)) {
            RespostaHttp.enviarErro(troca, 404, "Use POST /api/login");
            return;
        }

        try {
            JSONObject corpo = RespostaHttp.lerCorpoRequisicao(troca);
            String email = corpo.optString("email", "");
            String senha = corpo.optString("senha", "");

            Usuario usuario = usuarioDAO.buscarPorEmail(email);

            // Mensagem de erro identica pra "nao existe" e "senha errada" —
            // nao da pra um invasor descobrir se o e-mail existe ou nao.
            if (usuario == null || !usuario.getSenha().equals(senha)) {
                RespostaHttp.enviarErro(troca, 401, "E-mail ou senha invalidos.");
                return;
            }

            RespostaHttp.enviarJson(troca, 200, HandlerUsuarios.usuarioParaJson(usuario));
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }
}
