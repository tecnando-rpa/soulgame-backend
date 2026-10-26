package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.UsuarioDAO;
import model.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

// Rotas:
// POST /api/usuarios          -> cadastro de novo usuario
// GET  /api/usuarios/ranking  -> lista ordenada por pontos (maior primeiro)
public class HandlerUsuarios implements HttpHandler {

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
            if ("POST".equals(metodo) && caminho.equals("/api/usuarios")) {
                cadastrar(troca);
            } else if ("GET".equals(metodo) && caminho.equals("/api/usuarios/ranking")) {
                ranking(troca);
            } else {
                RespostaHttp.enviarErro(troca, 404, "Rota nao encontrada: " + metodo + " " + caminho);
            }
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }

    private void cadastrar(HttpExchange troca) throws IOException {
        JSONObject corpo = RespostaHttp.lerCorpoRequisicao(troca);

        LocalDate dataNascimento = null;
        try {
            String dataTexto = corpo.optString("dataNascimento", "");
            if (!dataTexto.isBlank()) {
                dataNascimento = LocalDate.parse(dataTexto); // espera AAAA-MM-DD
            }
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 400, "Data de nascimento invalida. Use o formato AAAA-MM-DD.");
            return;
        }

        Usuario usuario = new Usuario(
                corpo.optString("nome", ""),
                corpo.optString("email", ""),
                corpo.optString("senha", ""),
                corpo.optString("telefone", ""),
                dataNascimento,
                corpo.optString("cidade", ""),
                corpo.optString("estado", ""),
                corpo.optString("tipo", "soulgamer")
        );

        // Programa de indicação: se veio um código de outra conta, confirma
        // que existe antes de aceitar o cadastro.
        String codigoUsado = corpo.optString("indicadoPorCodigo", "");
        if (!codigoUsado.isBlank()) {
            if (usuarioDAO.buscarPorCodigoIndicacao(codigoUsado) == null) {
                RespostaHttp.enviarErro(troca, 400, "Codigo de indicacao invalido: " + codigoUsado);
                return;
            }
            usuario.setIndicadoPorCodigo(codigoUsado);
        }

        // Contas multiplicadoras ganham um código próprio automaticamente.
        if (usuario.ehMultiplicador()) {
            usuario.setCodigoIndicacao(gerarCodigoIndicacao());
        }

        if (!usuario.validarCadastro()) {
            RespostaHttp.enviarErro(
                    troca, 400,
                    "Dados de cadastro invalidos. Confira nome, email, senha, cidade, estado (sigla de 2 letras) e tipo de conta."
            );
            return;
        }

        if (usuarioDAO.buscarPorEmail(usuario.getEmail()) != null) {
            RespostaHttp.enviarErro(troca, 409, "Ja existe um usuario cadastrado com este e-mail.");
            return;
        }

        boolean criado = usuarioDAO.criar(usuario);

        if (!criado) {
            RespostaHttp.enviarErro(troca, 500, "Nao foi possivel criar o usuario no banco de dados.");
            return;
        }

        RespostaHttp.enviarJson(troca, 201, usuarioParaJson(usuario));
    }

    private void ranking(HttpExchange troca) throws IOException {
        List<Usuario> usuarios = usuarioDAO.listarTodos(); // ja vem ordenado por pontos DESC (ver SQL da DAO)
        JSONArray lista = new JSONArray();

        for (Usuario usuario : usuarios) {
            lista.put(usuarioParaJson(usuario));
        }

        RespostaHttp.enviarJson(troca, 200, new JSONObject().put("ranking", lista));
    }

    // Código curto (6 caracteres, maiusculo) derivado de um UUID — simples
    // o suficiente pra esse escopo, sem precisar de uma tabela de sequencia
    // separada. Colisao é extremamente improvavel nesse volume de usuarios.
    private static String gerarCodigoIndicacao() {
        return UUID.randomUUID().toString().substring(0, 6).toUpperCase(java.util.Locale.ROOT);
    }

    // Nunca inclui a senha nem a foto do avatar (pode ser grande) na
    // listagem geral — só no perfil individual, se precisar depois.
    static JSONObject usuarioParaJson(Usuario usuario) {
        return new JSONObject()
                .put("id", String.valueOf(usuario.getId()))
                .put("nome", usuario.getNome())
                .put("email", usuario.getEmail())
                .put("telefone", usuario.getTelefone())
                .put("dataNascimento", usuario.getDataNascimento() != null ? usuario.getDataNascimento().toString() : JSONObject.NULL)
                .put("cidade", usuario.getCidade())
                .put("estado", usuario.getEstado())
                .put("tipo", usuario.getTipo())
                .put("totalPontos", usuario.getTotalPontos())
                .put("nivel", usuario.getNivel())
                .put("streakDias", usuario.getStreakDias())
                .put("codigoIndicacao", usuario.getCodigoIndicacao())
                .put("indicadoPorCodigo", usuario.getIndicadoPorCodigo())
                .put("avatarTipo", usuario.getAvatarTipo())
                .put("avatarPersonagemId", usuario.getAvatarPersonagemId())
                .put("avatarFotoBase64", usuario.getAvatarFotoBase64())
                .put("ehValidador", usuario.ehValidador())
                .put("ehMultiplicador", usuario.ehMultiplicador());
    }
}
