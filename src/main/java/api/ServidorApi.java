package api;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Servidor HTTP embutido (com.sun.net.httpserver, nativo do JDK — nenhuma
 * dependencia nova) que expoe os DAOs como uma API REST simples, pra o
 * front-end React consumir via fetch() em vez de localStorage.
 *
 * Rotas disponiveis:
 *   GET  /api/health            -> verificação de que o servidor esta no ar
 *   POST /api/usuarios          -> cadastro
 *   POST /api/login             -> autenticacao
 *   GET  /api/usuarios/ranking  -> ranking por pontos
 *   GET  /api/atividades        -> lista de atividades sustentaveis
 *   GET  /api/desafios-agendados -> agenda/lista prazos de desafio
 *   GET  /api/historico          -> historico de atividades concluidas
 *   PUT  /api/perfil/{id}        -> atualiza perfil/avatar
 *   GET  /api/ods                -> os 17 ODS oficiais (mapa das trilhas)
 *   GET  /api/conquistas         -> catalogo de conquistas / desbloqueadas por usuario
 *   GET  /api/campeonatos        -> lista campeonatos
 *   POST /api/campeonatos        -> cria campeonato (somente admin)
 *   POST /api/campeonatos/inscrever -> inscreve usuario num campeonato
 *
 * Como rodar: execute a main() desta classe. O servidor sobe em
 * http://localhost:8080 por padrao.
 */
public class ServidorApi {

    private static final int PORTA_PADRAO = 8080;

    public static void main(String[] args) throws IOException {
        // Ordem: argumento da linha de comando > variável PORT (Railway/Render
        // definem essa variável sozinhos) > 8080.
        String portaEnv = System.getenv("PORT");
        int porta = args.length > 0 ? Integer.parseInt(args[0])
                : (portaEnv != null && !portaEnv.isBlank() ? Integer.parseInt(portaEnv) : PORTA_PADRAO);

        HttpServer servidor = HttpServer.create(new InetSocketAddress(porta), 0);

        servidor.createContext("/api/health", ServidorApi::verificarSaude);
        servidor.createContext("/api/usuarios", new HandlerUsuarios());
        servidor.createContext("/api/login", new HandlerLogin());
        servidor.createContext("/api/atividades", new HandlerAtividades());
        servidor.createContext("/api/registros", new HandlerRegistros());
        servidor.createContext("/api/desafios-agendados", new HandlerDesafiosAgendados());
        servidor.createContext("/api/historico", new HandlerHistorico());
        servidor.createContext("/api/perfil", new HandlerPerfil());
        servidor.createContext("/api/ods", new HandlerOds());
        servidor.createContext("/api/conquistas", new HandlerConquistas());
        servidor.createContext("/api/campeonatos", new HandlerCampeonatos());

        servidor.setExecutor(null); // usa o executor padrao (thread por requisicao)
        servidor.start();

        System.out.println("Servidor no ar em http://localhost:" + porta);
        System.out.println("Teste rapido: http://localhost:" + porta + "/api/health");
    }

    private static void verificarSaude(HttpExchange troca) throws IOException {
        if ("OPTIONS".equals(troca.getRequestMethod())) {
            RespostaHttp.enviarJson(troca, 204, new JSONObject());
            return;
        }
        RespostaHttp.enviarJson(troca, 200, new JSONObject().put("status", "ok"));
    }
}
