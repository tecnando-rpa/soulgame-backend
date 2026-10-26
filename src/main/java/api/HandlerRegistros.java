package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import dao.AcaoSustentavelDAO;
import dao.CampeonatoDAO;
import dao.CampeonatoParticipanteDAO;
import dao.EvidenciaDAO;
import dao.RegistroAcaoDAO;
import dao.UsuarioDAO;
import ia.ResultadoValidacaoIA;
import ia.ValidacaoEvidenciaIA;
import model.AcaoSustentavel;
import model.Campeonato;
import model.Conquista;
import model.Evidencia;
import model.RegistroAcao;
import model.Usuario;
import org.json.JSONArray;
import org.json.JSONObject;
import service.MotorConquistas;
import service.SistemaGamificacao;
import utils.HashImagem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;

// Rota: POST /api/registros
// Body: { "usuarioId": "1", "acaoSlug": "reciclagem", "imagemBase64": "..." }
//
// Usa o SLUG da atividade (ex: "doacao-sangue"), não o id numerico
// interno do Oracle — o slug é o mesmo "id" que o front-end React
// sempre usou (ver data/atividades.ts).
//
// A imagem de evidencia e OBRIGATORIA (sem ela: HTTP 400, nada e registrado).
// A IA analisa uma unica vez e decide: VALIDADO (confianca >= 0.7, pontos
// creditados), REJEITADO (nao corresponde a acao) ou PENDENTE (IA indisponivel
// ou duvida: revisao humana). Pontos so entram quando VALIDADO.
public class HandlerRegistros implements HttpHandler {

    private static final int TAMANHO_MAXIMO_IMAGEM_BYTES = 8 * 1024 * 1024;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final AcaoSustentavelDAO acaoDAO = new AcaoSustentavelDAO();
    private final RegistroAcaoDAO registroDAO = new RegistroAcaoDAO();
    private final EvidenciaDAO evidenciaDAO = new EvidenciaDAO();
    private final CampeonatoDAO campeonatoDAO = new CampeonatoDAO();
    private final CampeonatoParticipanteDAO participanteDAO = new CampeonatoParticipanteDAO();
    private final SistemaGamificacao sistemaGamificacao = new SistemaGamificacao();
    private final MotorConquistas motorConquistas = new MotorConquistas();

    @Override
    public void handle(HttpExchange troca) throws IOException {
        String metodo = troca.getRequestMethod();

        if ("OPTIONS".equals(metodo)) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }

        if (!"POST".equals(metodo)) {
            RespostaHttp.enviarErro(troca, 404, "Use POST /api/registros");
            return;
        }

        try {
            registrar(troca);
        } catch (Exception e) {
            RespostaHttp.enviarErro(troca, 500, "Erro interno: " + e.getMessage());
        }
    }

    private void registrar(HttpExchange troca) throws IOException {
        JSONObject corpo = RespostaHttp.lerCorpoRequisicao(troca);

        int usuarioId = parseIntSeguro(corpo.optString("usuarioId", ""));
        String acaoSlug = corpo.optString("acaoSlug", "");

        Usuario usuario = usuarioId > 0 ? usuarioDAO.buscarPorId(usuarioId) : null;
        AcaoSustentavel acao = !acaoSlug.isBlank() ? acaoDAO.buscarPorSlug(acaoSlug) : null;

        if (usuario == null || acao == null) {
            RespostaHttp.enviarErro(troca, 404, "Usuario ou atividade nao encontrados.");
            return;
        }

        boolean jaConcluiu = registroDAO.listarConcluidasPorUsuario(usuarioId).stream()
                .anyMatch(registro -> registro.getAcaoId() == acao.getId());

        if (jaConcluiu) {
            RespostaHttp.enviarErro(troca, 409, "Essa atividade ja foi concluida por esse usuario.");
            return;
        }

        // ---- Evidência OBRIGATÓRIA: sem imagem, nada é registrado nem pontuado ----
        String imagemBase64 = corpo.optString("imagemBase64", "");
        if (imagemBase64.isBlank()) {
            RespostaHttp.enviarErro(
                    troca, 400,
                    "Evidencia obrigatoria: envie uma foto (JPG, PNG ou WEBP) comprovando a atividade."
            );
            return;
        }

        if (registroDAO.existePendente(usuarioId, acao.getId())) {
            RespostaHttp.enviarErro(
                    troca, 409,
                    "Voce ja enviou uma evidencia para essa atividade e ela ainda esta em analise."
            );
            return;
        }

        String tipoEvidencia = "FOTO"; // só imagens são aceitas (a IA de visão não lê PDF)

        byte[] bytesImagem;
        try {
            bytesImagem = Base64.getDecoder().decode(imagemBase64);
        } catch (IllegalArgumentException e) {
            RespostaHttp.enviarErro(troca, 400, "Imagem em base64 invalida.");
            return;
        }

        if (bytesImagem.length == 0 || bytesImagem.length > TAMANHO_MAXIMO_IMAGEM_BYTES) {
            RespostaHttp.enviarErro(troca, 400, "Imagem vazia ou maior que 8 MB.");
            return;
        }

        if (!pareceImagem(bytesImagem)) {
            RespostaHttp.enviarErro(troca, 400, "O arquivo enviado nao e uma imagem valida (use JPG, PNG ou WEBP).");
            return;
        }

        // Checagem de duplicidade ANTES de gastar uma chamada de IA:
        // se essa imagem exata já foi usada em qualquer evidência
        // anterior, bloqueia na hora — nem chama o Groq de novo.
        String hashImagem = HashImagem.calcularSha256(bytesImagem);
        if (hashImagem != null && evidenciaDAO.buscarPorHash(hashImagem) != null) {
            RespostaHttp.enviarErro(
                    troca, 409,
                    "Essa imagem ja foi usada como evidencia antes (mesmo arquivo). Envie uma foto nova."
            );
            return;
        }

        // UMA única chamada à IA (antes eram duas: gastava cota e podia dar vereditos diferentes).
        Path arquivoTemporario = salvarImagemTemporaria(bytesImagem);
        ResultadoValidacaoIA resultadoIA;
        try {
            resultadoIA = ValidacaoEvidenciaIA.validar(arquivoTemporario.toString(), tipoEvidencia, acao.getNome());
        } finally {
            Files.deleteIfExists(arquivoTemporario);
        }
        System.out.println("Veredito da IA: " + resultadoIA);

        RegistroAcao registro = sistemaGamificacao.registrarAcaoComVeredito(usuario, acao, resultadoIA);
        boolean pontuou = registro != null && "VALIDADO".equals(registro.getStatusValidacao());

        if (registro == null) {
            RespostaHttp.enviarErro(troca, 400, "Nao foi possivel registrar: usuario ou atividade invalidos.");
            return;
        }

        boolean registroCriado = registroDAO.criar(registro);
        boolean usuarioAtualizado = usuarioDAO.atualizar(usuario); // persiste os pontos ganhos

        if (!registroCriado || !usuarioAtualizado) {
            RespostaHttp.enviarErro(troca, 500, "Erro ao salvar o registro ou atualizar os pontos do usuario.");
            return;
        }

        // Guarda a evidência com o hash, pra futuras checagens de
        // duplicidade poderem encontrar essa imagem. Não guardamos o
        // arquivo em si (não há storage de blob configurado ainda) —
        // só o registro de que essa impressão digital já foi usada.
        if (hashImagem != null) {
            Evidencia evidencia = new Evidencia(
                    registro.getId(), tipoEvidencia,
                    "nao-armazenado (apenas hash verificado)",
                    resultadoIA.getMotivo(),
                    hashImagem
            );
            evidenciaDAO.criar(evidencia);
        }

        // Programa de indicação: quem indicou ganha o dobro dos pontos
        // gerados por essa ação (mesma regra que já existia no front-end).
        if (pontuou && usuario.getIndicadoPorCodigo() != null && !usuario.getIndicadoPorCodigo().isBlank()) {
            Usuario multiplicador = usuarioDAO.buscarPorCodigoIndicacao(usuario.getIndicadoPorCodigo());
            if (multiplicador != null) {
                multiplicador.adicionarPontos(registro.getPontosObtidos() * 2);
                usuarioDAO.atualizar(multiplicador);
            }
        }

        // Verifica se essa acao desbloqueou alguma conquista nova — nao
        // interrompe o fluxo se algo falhar aqui, os pontos e o registro
        // ja foram salvos antes desta etapa.
        List<Conquista> conquistasNovas = pontuou
                ? motorConquistas.verificarEDesbloquear(usuario)
                : List.of();

        // Soma pontos em qualquer campeonato EM_ANDAMENTO do mesmo ODS
        // dessa acao, se o usuario estiver inscrito nele.
        if (pontuou && acao.getIdOds() > 0) {
            for (Campeonato campeonato : campeonatoDAO.listarEmAndamentoPorOds(acao.getIdOds())) {
                if (participanteDAO.estaInscrito(campeonato.getId(), usuario.getId())) {
                    participanteDAO.somarPontos(campeonato.getId(), usuario.getId(), registro.getPontosObtidos());
                }
            }
        }

        JSONObject resposta = new JSONObject()
                .put("registro", new JSONObject()
                        .put("id", registro.getId())
                        .put("acaoSlug", acao.getSlug())
                        .put("statusValidacao", registro.getStatusValidacao())
                        .put("pontosObtidos", registro.getPontosObtidos()))
                .put("usuario", HandlerUsuarios.usuarioParaJson(usuario));

        if (!conquistasNovas.isEmpty()) {
            JSONArray listaConquistas = new JSONArray();
            for (Conquista conquista : conquistasNovas) {
                listaConquistas.put(new JSONObject()
                        .put("id", conquista.getId())
                        .put("nome", conquista.getNome())
                        .put("descricao", conquista.getDescricao())
                        .put("icone", conquista.getIcone()));
            }
            resposta.put("conquistas_desbloqueadas", listaConquistas);
        }

        resposta.put("veredito_ia", new JSONObject()
                .put("valido", resultadoIA.isValido())
                .put("confianca", resultadoIA.getConfianca())
                .put("motivo", resultadoIA.getMotivo())
                .put("indisponivel", resultadoIA.isIndisponivel()));

        RespostaHttp.enviarJson(troca, 201, resposta);
    }

    /** Confere pelos primeiros bytes se é JPEG, PNG, WEBP ou GIF (e não um PDF/texto renomeado). */
    private boolean pareceImagem(byte[] b) {
        if (b.length < 12) return false;
        boolean jpeg = (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8;
        boolean png = (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G';
        boolean gif = b[0] == 'G' && b[1] == 'I' && b[2] == 'F';
        boolean webp = b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P';
        return jpeg || png || gif || webp;
    }

    private int parseIntSeguro(String texto) {
        try {
            return Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private Path salvarImagemTemporaria(byte[] bytesImagem) throws IOException {
        Path arquivoTemporario = Files.createTempFile("evidencia-api-", ".jpg");
        Files.write(arquivoTemporario, bytesImagem);
        return arquivoTemporario;
    }
}
